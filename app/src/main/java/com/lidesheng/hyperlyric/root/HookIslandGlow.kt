package com.lidesheng.hyperlyric.root

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.content.SharedPreferences
import android.view.View
import com.lidesheng.hyperlyric.root.utils.CoverColorHelper
import com.lidesheng.hyperlyric.root.utils.Constants as RootConstants
import com.lidesheng.hyperlyric.root.utils.xLogError
import io.github.libxposed.api.XposedModule

/**
 * 超级岛胶囊边缘描边颜色注入
 *
 * 策略：Hook updateTemplate()，在系统解析完 IslandTemplate 后，
 * 将 highlightColor 设置为专辑封面主色。系统会在 updateMedianLuma /
 * updateDarkLightMode 中自动使用该颜色渲染描边。
 *
 * 此方案让系统自己处理所有渲染细节（alpha/宽度/明暗适配），
 * 我们只需提供颜色数据。
 */
@SuppressLint("DiscouragedPrivateApi", "PrivateApi")
object HookIslandGlow {
    private lateinit var module: XposedModule
    private var isHooked = false

    private val prefs: SharedPreferences?
        get() = if (::module.isInitialized) (module as? HookEntry)?.prefs else null

    fun init(xposedModule: XposedModule, cl: ClassLoader) {
        if (isHooked) return
        module = xposedModule

        try {
            val baseContentViewClass = cl.loadClass(
                "miui.systemui.dynamicisland.window.content.DynamicIslandBaseContentView"
            )

            // Hook updateTemplate — 注入 highlightColor
            // 用 baseContentViewClass 的 ClassLoader 加载 DynamicIslandData（两个类在不同 APK 中）
            val dataClass = baseContentViewClass.classLoader.loadClass(
                "com.android.systemui.plugins.miui.dynamicisland.DynamicIslandData"
            )
            val updateTemplateMethod = baseContentViewClass.getDeclaredMethod(
                "updateTemplate",
                dataClass
            )
            updateTemplateMethod.isAccessible = true
            module.deoptimize(updateTemplateMethod)
            module.hook(updateTemplateMethod).intercept { chain ->
                val result = chain.proceed()
                injectHighlightColor(chain)
                result
            }

        } catch (e: Exception) {
            xLogError("HyperLyric Glow: init failed", e)
        }

        isHooked = true
    }

    private fun injectHighlightColor(chain: io.github.libxposed.api.XposedInterface.Chain) {
        try {
            val extractEnabled = prefs?.getBoolean(
                RootConstants.KEY_HOOK_ISLAND_GLOW_EXTRACT_COLOR,
                RootConstants.DEFAULT_HOOK_ISLAND_GLOW_EXTRACT_COLOR
            ) ?: false

            if (!extractEnabled) return

            val colors = CoverColorHelper.getCachedColors()?.second
            val mainColor = colors?.firstOrNull() ?: return

            val view = chain.thisObject as? View ?: return

            // 校验：当前岛是否是正在播放的音乐 App
            val pkgName = getPkgNameFromView(view)
            if (pkgName.isEmpty() || pkgName != LyriconDataBridge.activePackageName) return
            if (!HookIslandLyric.isPackageHookEnabled(pkgName)) return

            // 获取 template 字段
            val templateField = findFieldInHierarchy(view.javaClass, "template") ?: return
            val template = templateField.get(view) ?: return

            // 设置 highlightColor
            val setHlMethod = template.javaClass.getMethod("setHighlightColor", String::class.java)
            val colorStr = String.format("#%08X", mainColor)
            setHlMethod.invoke(template, colorStr)

            // 同时更新 _highlightColor LiveData，触发系统渲染管线
            val hlField = findFieldInHierarchy(view.javaClass, "_highlightColor") ?: return
            val hlLiveData = hlField.get(view) ?: return
            val setValueMethod = hlLiveData.javaClass.getMethod("setValue", Object::class.java)
            setValueMethod.invoke(hlLiveData, colorStr)

        } catch (e: Exception) {
            xLogError("Glow: injectHighlightColor error", e)
        }
    }

    private fun getPkgNameFromView(view: View): String {
        try {
            val dataField = findFieldInHierarchy(view.javaClass, "currentIslandData") ?: return ""
            val islandData = dataField.get(view) ?: return ""
            val getExtrasMethod = islandData.javaClass.getMethod("getExtras")
            val extras = getExtrasMethod.invoke(islandData) as? android.os.Bundle ?: return ""
            return extras.getString("miui.pkg.name") ?: ""
        } catch (_: Exception) {}
        return ""
    }

    /**
     * 在 class hierarchy 中向上查找字段
     */
    private fun findFieldInHierarchy(clazz: Class<*>, fieldName: String): java.lang.reflect.Field? {
        var c: Class<*>? = clazz
        while (c != null && c != View::class.java) {
            try {
                val field = c.getDeclaredField(fieldName)
                field.isAccessible = true
                return field
            } catch (_: NoSuchFieldException) {
                c = c.superclass
            }
        }
        return null
    }

    fun injectAndTriggerGlow(_contentView: View, _islandData: Any?, _prefs: SharedPreferences) {
        // highlightColor 已通过 updateTemplate Hook 注入，系统自动处理
    }

    fun updateMusicGlow(_packageName: String, albumArt: Bitmap?, _prefs: SharedPreferences) {
        val enabled = _prefs.getBoolean(
            RootConstants.KEY_HOOK_ISLAND_GLOW_EXTRACT_COLOR,
            RootConstants.DEFAULT_HOOK_ISLAND_GLOW_EXTRACT_COLOR
        )
        if (albumArt != null && enabled && CoverColorHelper.getCachedColors() == null) {
            CoverColorHelper.extractColors(albumArt, false)
        }
    }
}
