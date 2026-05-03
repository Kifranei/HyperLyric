package com.lidesheng.hyperlyric.ui.page

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.lidesheng.hyperlyric.ui.utils.Constants as UIConstants
import com.lidesheng.hyperlyric.service.Constants as ServiceConstants
import com.lidesheng.hyperlyric.root.utils.Constants as RootConstants
import com.lidesheng.hyperlyric.R
import com.lidesheng.hyperlyric.ui.navigation.LocalNavigator
import com.lidesheng.hyperlyric.ui.navigation.Route
import com.lidesheng.hyperlyric.ui.utils.BlurredBox
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private fun setExcludeFromRecents(context: Context, exclude: Boolean) {
    try {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        am.appTasks?.forEach {
            it.setExcludeFromRecents(exclude)
        }
    } catch (_: Exception) { }
}

private fun buildBackupJson(context: Context): String {
    val prefs = context.getSharedPreferences(UIConstants.PREF_NAME, Context.MODE_PRIVATE)
    val config = JSONObject()
    prefs.all.forEach { (key, value) ->
        if (key == RootConstants.KEY_HOOK_AI_TRANS_API_KEY) return@forEach
        when (value) {
            is Boolean -> config.put(key, value)
            is Int -> config.put(key, value)
            is Float -> config.put(key, value.toDouble())
            is Long -> config.put(key, value)
            is String -> config.put(key, value)
            is Set<*> -> {
                @Suppress("UNCHECKED_CAST")
                config.put(key, (value as Set<String>).joinToString(","))
            }
        }
    }

    val root = JSONObject().apply {
        put("version", 1)
        put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        put("config", config)
    }
    return root.toString(2)
}

private fun restoreFromJson(context: Context, json: String): Boolean {
    val prefs = context.getSharedPreferences(UIConstants.PREF_NAME, Context.MODE_PRIVATE)
    return try {
        val root = JSONObject(json)
        val version = root.optInt("version", -1)
        if (version < 1) return false

        val config = root.getJSONObject("config")

        prefs.edit {
            val keys = config.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = config.get(key)

                if (key == "key_send_normal_notification" || key == "key_send_focus_notification" || key == "key_persistent_foreground"
                    || key == RootConstants.KEY_HOOK_AI_TRANS_API_KEY) {
                    continue
                }

                if (key == ServiceConstants.KEY_NOTIFICATION_WHITELIST
                    || key == RootConstants.KEY_HOOK_WHITELIST
                    || key == RootConstants.KEY_HOOK_ADDED_LIST) {
                    val raw = value.toString()
                    val set = if (raw.isBlank()) emptySet() else raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                    putStringSet(key, set)
                    continue
                }

                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is Int -> {
                        val boundedValue = when (key) {
                            // Service
                            ServiceConstants.KEY_NOTIFICATION_TYPE -> value.coerceIn(0, 1)
                            ServiceConstants.KEY_NOTIFICATION_FOCUS_STYLE -> value.coerceIn(0, 1)
                            ServiceConstants.KEY_ISLAND_LEFT_ICON -> value.coerceIn(0, 3)
                            ServiceConstants.KEY_ISLAND_LEFT_ICON_NORMAL -> value.coerceIn(0, 3)
                            ServiceConstants.KEY_ISLAND_LEFT_ICON_FOCUS -> value.coerceIn(0, 3)
                            ServiceConstants.KEY_NOTIFICATION_CLICK_ACTION -> value.coerceIn(0, 2)
                            ServiceConstants.KEY_NOTIFICATION_ISLAND_MAX_WIDTH -> value.coerceIn(100, 720)
                            ServiceConstants.KEY_ONLINE_LYRIC_CACHE_LIMIT -> value.coerceIn(1, 1000)
                            // Root
                            RootConstants.KEY_HOOK_LYRIC_MODE -> value.coerceIn(0, 1)
                            RootConstants.KEY_HOOK_TEXT_SIZE -> value.coerceIn(8, 16)
                            RootConstants.KEY_HOOK_FONT_WEIGHT -> value.coerceIn(100, 900)
                            RootConstants.KEY_HOOK_MAX_LEFT_WIDTH -> value.coerceIn(40, 280)
                            RootConstants.KEY_HOOK_FADING_EDGE_LENGTH -> value.coerceIn(0, 100)
                            RootConstants.KEY_HOOK_ANIM_MODE -> value.coerceIn(0, 4)
                            RootConstants.KEY_HOOK_MARQUEE_SPEED -> value.coerceIn(10, 500)
                            RootConstants.KEY_HOOK_MARQUEE_DELAY -> value.coerceIn(0, 5000)
                            RootConstants.KEY_HOOK_MARQUEE_LOOP_DELAY -> value.coerceIn(0, 5000)
                            RootConstants.KEY_HOOK_ISLAND_CONTENT_LEFT -> value.coerceIn(0, 8)
                            RootConstants.KEY_HOOK_ISLAND_CONTENT_RIGHT -> value.coerceIn(0, 8)
                            RootConstants.KEY_HOOK_ISLAND_LEFT_PADDING_LEFT -> value.coerceIn(-50, 100)
                            RootConstants.KEY_HOOK_ISLAND_LEFT_PADDING_RIGHT -> value.coerceIn(-50, 100)
                            RootConstants.KEY_HOOK_ISLAND_RIGHT_PADDING_LEFT -> value.coerceIn(-50, 100)
                            RootConstants.KEY_HOOK_ISLAND_RIGHT_PADDING_RIGHT -> value.coerceIn(-50, 100)
                            RootConstants.KEY_HOOK_ISLAND_LEFT_CONTENT_MAX_WIDTH -> value.coerceIn(0, 100)
                            RootConstants.KEY_HOOK_ISLAND_RIGHT_CONTENT_MAX_WIDTH -> value.coerceIn(0, 100)
                            RootConstants.KEY_HOOK_ISLAND_BEHAVIOR_AFTER_PAUSE -> value.coerceIn(0, 1)
                            // UI
                            UIConstants.KEY_WORK_MODE -> value.coerceIn(0, 1)
                            UIConstants.KEY_THEME_MODE -> value.coerceIn(0, 5)
                            UIConstants.KEY_MONET_COLOR -> value.coerceIn(0, 7)
                            else -> value
                        }
                        putInt(key, boundedValue)
                    }
                    is Double, is Float -> {
                        val floatValue = (value as Number).toFloat()
                        val boundedFloat = when (key) {
                            RootConstants.KEY_HOOK_TEXT_SIZE_RATIO -> floatValue.coerceIn(0.1f, 1.0f)
                            RootConstants.KEY_HOOK_WORD_MOTION_CJK_LIFT,
                            RootConstants.KEY_HOOK_WORD_MOTION_LATIN_LIFT -> floatValue.coerceIn(0.0f, 0.2f)
                            RootConstants.KEY_HOOK_WORD_MOTION_CJK_WAVE,
                            RootConstants.KEY_HOOK_WORD_MOTION_LATIN_WAVE -> floatValue.coerceIn(0.0f, 10.0f)
                            else -> floatValue
                        }
                        putFloat(key, boundedFloat)
                    }
                    is Long -> putLong(key, value)
                    is String -> putString(key, value)
                }
            }
        }
        true
    } catch (_: Exception) {
        false
    }
}

@Composable
fun SettingsPage() {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())

    // Toast messages and format strings fetched at top level
    val msgBackupSuccess = stringResource(R.string.toast_backup_success)
    val fmtBackupFailed = stringResource(R.string.toast_backup_failed)
    val msgRestoreEmpty = stringResource(R.string.toast_restore_empty)
    val msgRestoreSuccess = stringResource(R.string.toast_restore_success)
    val msgRestoreInvalid = stringResource(R.string.toast_restore_invalid)
    val msgRestoreFailed = stringResource(R.string.toast_restore_failed)

    val surfaceColor = MiuixTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            try {
                val jsonBytes = buildBackupJson(context).toByteArray(Charsets.UTF_8)
                val output = context.contentResolver.openOutputStream(uri)
                if (output != null) {
                    output.use { it.write(jsonBytes); it.flush() }
                    Toast.makeText(context, msgBackupSuccess, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, fmtBackupFailed.format(e.message), Toast.LENGTH_SHORT).show()
            }
        }
    )

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader(Charsets.UTF_8).readText()
                } ?: ""
                if (json.isBlank()) {
                    Toast.makeText(context, msgRestoreEmpty, Toast.LENGTH_SHORT).show()
                    return@rememberLauncherForActivityResult
                }
                val success = restoreFromJson(context, json)
                Toast.makeText(
                    context,
                    if (success) msgRestoreSuccess else msgRestoreInvalid,
                    Toast.LENGTH_SHORT
                ).show()
            } catch (_: Exception) {
                Toast.makeText(context, msgRestoreFailed, Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        topBar = {
            BlurredBox(backdrop = backdrop) {
                TopAppBar(
                    color = Color.Transparent,
                    title = stringResource(R.string.title_settings_page),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() }
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .scrollEndHaptic()
                .layerBackdrop(backdrop)
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                start = 12.dp,
                end = 12.dp,
                bottom = padding.calculateBottomPadding() + 16.dp
            ),
        ) {
            item {
                SmallTitle(
                    text = stringResource(R.string.title_personalization),
                    insideMargin = PaddingValues(10.dp, 4.dp)
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    val prefs = remember { context.getSharedPreferences(UIConstants.PREF_NAME, Context.MODE_PRIVATE) }
                    var themeMode by remember { mutableIntStateOf(prefs.getInt(UIConstants.KEY_THEME_MODE, UIConstants.DEFAULT_THEME_MODE)) }
                    val themeOptions = listOf(
                        stringResource(R.string.theme_system),
                        stringResource(R.string.theme_light),
                        stringResource(R.string.theme_dark),
                        stringResource(R.string.theme_system_monet),
                        stringResource(R.string.theme_light_monet),
                        stringResource(R.string.theme_dark_monet)
                    )

                    WindowDropdownPreference(
                        title = stringResource(R.string.title_theme),
                        items = themeOptions,
                        selectedIndex = themeMode,
                        onSelectedIndexChange = {
                            themeMode = it
                            prefs.edit { putInt(UIConstants.KEY_THEME_MODE, it) }
                        }
                    )

                    if (themeMode >= 3) {
                        var monetColorIndex by remember { mutableIntStateOf(prefs.getInt(UIConstants.KEY_MONET_COLOR, UIConstants.DEFAULT_MONET_COLOR)) }
                        val monetOptions = listOf(
                            stringResource(R.string.monet_default),
                            stringResource(R.string.monet_blue),
                            stringResource(R.string.monet_green),
                            stringResource(R.string.monet_red),
                            stringResource(R.string.monet_yellow),
                            stringResource(R.string.monet_orange),
                            stringResource(R.string.monet_purple),
                            stringResource(R.string.monet_pink)
                        )

                        WindowDropdownPreference(
                            title = stringResource(R.string.title_monet),
                            items = monetOptions,
                            selectedIndex = monetColorIndex,
                            onSelectedIndexChange = {
                                monetColorIndex = it
                                prefs.edit { putInt(UIConstants.KEY_MONET_COLOR, it) }
                            }
                        )
                    }

                    var predictiveBackGestureEnabled by remember { mutableStateOf(prefs.getBoolean(UIConstants.KEY_PREDICTIVE_BACK_GESTURE, UIConstants.DEFAULT_PREDICTIVE_BACK_GESTURE)) }
                    val activity = androidx.activity.compose.LocalActivity.current
                    SwitchPreference(
                        title = stringResource(R.string.title_predictive_back),
                        checked = predictiveBackGestureEnabled,
                        onCheckedChange = {
                            predictiveBackGestureEnabled = it
                            prefs.edit { putBoolean(UIConstants.KEY_PREDICTIVE_BACK_GESTURE, it) }
                            runCatching {
                                org.lsposed.hiddenapibypass.HiddenApiBypass.addHiddenApiExemptions("Landroid/content/pm/ApplicationInfo;->setEnableOnBackInvokedCallback")
                                val applicationInfoClass = android.content.pm.ApplicationInfo::class.java
                                val method = applicationInfoClass.getDeclaredMethod("setEnableOnBackInvokedCallback", Boolean::class.javaPrimitiveType)
                                method.isAccessible = true
                                method.invoke(context.applicationInfo, it)
                            }
                            activity?.recreate()
                        }
                    )

                    var floatingNavBarEnabled by remember { mutableStateOf(prefs.getBoolean(UIConstants.KEY_FLOATING_NAV_BAR, UIConstants.DEFAULT_FLOATING_NAV_BAR)) }
                    SwitchPreference(
                        title = stringResource(R.string.title_floating_nav),
                        checked = floatingNavBarEnabled,
                        onCheckedChange = {
                            floatingNavBarEnabled = it
                            prefs.edit { putBoolean(UIConstants.KEY_FLOATING_NAV_BAR, it) }
                        }
                    )

                    var excludeFromRecents by remember { mutableStateOf(prefs.getBoolean(UIConstants.KEY_EXCLUDE_FROM_RECENTS, UIConstants.DEFAULT_EXCLUDE_FROM_RECENTS)) }
                    SwitchPreference(
                        title = stringResource(R.string.title_exclude_from_recents),
                        checked = excludeFromRecents,
                        onCheckedChange = {
                            excludeFromRecents = it
                            prefs.edit { putBoolean(UIConstants.KEY_EXCLUDE_FROM_RECENTS, it) }
                            setExcludeFromRecents(context, it)
                        }
                    )
                }
            }

            item {
                SmallTitle(
                    text = stringResource(R.string.title_config_management),
                    insideMargin = PaddingValues(10.dp, 4.dp)
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ArrowPreference(
                            title = stringResource(R.string.title_backup),
                            onClick = {
                                val dateTime = LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
                                backupLauncher.launch("hyperlyric_backup_$dateTime.json")
                            }
                        )
                        ArrowPreference(
                            title = stringResource(R.string.title_restore),
                            onClick = {
                                restoreLauncher.launch(arrayOf("application/json"))
                            }
                        )
                    }
                }

                SmallTitle(
                    text = stringResource(R.string.title_debug_info),
                    insideMargin = PaddingValues(10.dp, 4.dp)
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    ArrowPreference(
                        title = stringResource(R.string.title_view_logs),
                        onClick = {
                            navigator.navigate(Route.Log)
                        }
                    )
                }
            }
        }
    }
}
