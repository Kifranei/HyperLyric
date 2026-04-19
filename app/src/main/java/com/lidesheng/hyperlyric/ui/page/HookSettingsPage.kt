package com.lidesheng.hyperlyric.ui.page

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.lidesheng.hyperlyric.ui.utils.Constants as UIConstants
import com.lidesheng.hyperlyric.root.utils.Constants as RootConstants
import com.lidesheng.hyperlyric.ui.navigation.LocalNavigator
import com.lidesheng.hyperlyric.ui.navigation.Route
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun HookSettingsPage() {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val prefs = remember { context.getSharedPreferences(UIConstants.PREF_NAME, Context.MODE_PRIVATE) }
    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())

    val hazeState = remember { HazeState() }
    val hazeStyle = HazeStyle(
        backgroundColor = MiuixTheme.colorScheme.surface,
        tint = HazeTint(MiuixTheme.colorScheme.surface.copy(0.8f))
    )

    var lyricMode by remember { mutableIntStateOf(prefs.getInt(RootConstants.KEY_HOOK_LYRIC_MODE, RootConstants.DEFAULT_HOOK_LYRIC_MODE)) }
    val lyricModeOptions = listOf("逐字歌词", "分离歌词")

    Scaffold(
        topBar = {
            TopAppBar(
                color = Color.Transparent,
                title = "小米超级岛歌词",
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) { Icon(imageVector = MiuixIcons.Back, contentDescription = "返回") }
                },
                modifier = Modifier.hazeEffect(hazeState) {
                    style = hazeStyle
                    blurRadius = 25.dp
                    noiseFactor = 0f
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .scrollEndHaptic()
                .hazeSource(state = hazeState)
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    OverlayDropdownPreference(
                        title = "歌词模式",
                        items = lyricModeOptions,
                        selectedIndex = lyricMode,
                        onSelectedIndexChange = { index ->
                            lyricMode = index
                            prefs.edit { putInt(RootConstants.KEY_HOOK_LYRIC_MODE, index) }
                        }
                    )
                }
            }

            item {
                SmallTitle(
                    text = "自定义配置",
                    insideMargin = PaddingValues(10.dp, 4.dp)
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ArrowPreference(title = "超级岛", onClick = {
                            navigator.navigate(Route.SuperIslandSettings)
                        })
                        ArrowPreference(title = "歌词", onClick = {
                            navigator.navigate(Route.LyricSettings)
                        })
                        ArrowPreference(title = "歌词切换动画", onClick = {
                            navigator.navigate(Route.LyricAnimation)
                        })
                        ArrowPreference(title = "歌词提供者", onClick = {
                            navigator.navigate(Route.LyricProvider)
                        })
                        ArrowPreference(title = "歌词白名单", onClick = {
                            navigator.navigate(Route.LyricWhitelist)
                        })
                    }
                }
            }
        }
    }
}