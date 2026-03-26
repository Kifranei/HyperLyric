package com.lidesheng.hyperlyric.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.lidesheng.hyperlyric.Constants
import com.lidesheng.hyperlyric.root.ConfigSync
import com.lidesheng.hyperlyric.utils.LyricModule
import com.lidesheng.hyperlyric.utils.LyricProviderManager
import com.lidesheng.hyperlyric.utils.ProviderUiState
import com.lidesheng.hyperlyric.utils.ThemeUtils
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SliderDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.SuperSwitch
import top.yukonga.miuix.kmp.extra.WindowDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import androidx.core.net.toUri

class HookSettingsActivity : ComponentActivity() {

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        setContent {
            ThemeUtils.MiuixThemeWrapper {
                HookSettingsScreen()
            }
        }
    }

    @Composable
    fun HookSettingsScreen() {
        var textSize by remember {
            mutableIntStateOf(
                prefs.getInt(
                    Constants.KEY_TEXT_SIZE,
                    Constants.DEFAULT_TEXT_SIZE
                )
            )
        }
        var fontWeight by remember {
            mutableIntStateOf(
                prefs.getInt(
                    Constants.KEY_FONT_WEIGHT,
                    Constants.DEFAULT_FONT_WEIGHT
                )
            )
        }
        var fontBold by remember {
            mutableStateOf(
                prefs.getBoolean(
                    Constants.KEY_FONT_BOLD,
                    Constants.DEFAULT_FONT_BOLD
                )
            )
        }
        var fontItalic by remember {
            mutableStateOf(
                prefs.getBoolean(
                    Constants.KEY_FONT_ITALIC,
                    Constants.DEFAULT_FONT_ITALIC
                )
            )
        }
        var fadingEdge by remember {
            mutableIntStateOf(
                prefs.getInt(
                    Constants.KEY_FADING_EDGE_LENGTH,
                    Constants.DEFAULT_FADING_EDGE_LENGTH
                )
            )
        }
        var gradientStyle by remember {
            mutableStateOf(
                prefs.getBoolean(
                    Constants.KEY_GRADIENT_PROGRESS,
                    Constants.DEFAULT_GRADIENT_PROGRESS
                )
            )
        }

        var marqueeMode by remember {
            mutableStateOf(
                prefs.getBoolean(
                    Constants.KEY_MARQUEE_MODE,
                    Constants.DEFAULT_MARQUEE_MODE
                )
            )
        }
        var marqueeSpeed by remember {
            mutableIntStateOf(
                prefs.getInt(
                    Constants.KEY_MARQUEE_SPEED,
                    Constants.DEFAULT_MARQUEE_SPEED
                )
            )
        }
        var marqueeDelay by remember {
            mutableIntStateOf(
                prefs.getInt(
                    Constants.KEY_MARQUEE_DELAY,
                    Constants.DEFAULT_MARQUEE_DELAY
                )
            )
        }
        var marqueeLoop by remember {
            mutableIntStateOf(
                prefs.getInt(
                    Constants.KEY_MARQUEE_LOOP_DELAY,
                    Constants.DEFAULT_MARQUEE_LOOP_DELAY
                )
            )
        }
        var marqueeInfinite by remember {
            mutableStateOf(
                prefs.getBoolean(
                    Constants.KEY_MARQUEE_INFINITE,
                    Constants.DEFAULT_MARQUEE_INFINITE
                )
            )
        }
        var marqueeStopEnd by remember {
            mutableStateOf(
                prefs.getBoolean(
                    Constants.KEY_MARQUEE_STOP_END,
                    Constants.DEFAULT_MARQUEE_STOP_END
                )
            )
        }

        var syllableRelative by remember {
            mutableStateOf(
                prefs.getBoolean(
                    Constants.KEY_SYLLABLE_RELATIVE,
                    Constants.DEFAULT_SYLLABLE_RELATIVE
                )
            )
        }
        var syllableHighlight by remember {
            mutableStateOf(
                prefs.getBoolean(
                    Constants.KEY_SYLLABLE_HIGHLIGHT,
                    Constants.DEFAULT_SYLLABLE_HIGHLIGHT
                )
            )
        }
        var textSizeRatio by remember {
            mutableFloatStateOf(
                prefs.getFloat(
                    Constants.KEY_TEXT_SIZE_RATIO,
                    Constants.DEFAULT_TEXT_SIZE_RATIO
                )
            )
        }

        var islandLength by remember {
            mutableIntStateOf(
                prefs.getInt(
                    Constants.KEY_MAX_LEFT_WIDTH,
                    Constants.DEFAULT_MAX_LEFT_WIDTH
                )
            )
        }

        // State variables for dialogs
        var showTextSizeDialog by remember { mutableStateOf(false) }
        var showFontWeightDialog by remember { mutableStateOf(false) }
        var showFadingEdgeDialog by remember { mutableStateOf(false) }
        var showMarqueeSpeedDialog by remember { mutableStateOf(false) }
        var showMarqueeDelayDialog by remember { mutableStateOf(false) }
        var showMarqueeLoopDialog by remember { mutableStateOf(false) }
        var showTextSizeRatioDialog by remember { mutableStateOf(false) }
        var showIslandLengthDialog by remember { mutableStateOf(false) }

        fun saveConfig(key: String, value: Any) {
            prefs.edit {
                when (value) {
                    is Int -> putInt(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Float -> putFloat(key, value)
                }
            }
            ConfigSync.syncPreference(Constants.PREF_NAME, key, value)
        }

        val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())
        val hazeState = remember { HazeState() }
        val hazeStyle = HazeStyle(
            backgroundColor = MiuixTheme.colorScheme.surface,
            tint = HazeTint(MiuixTheme.colorScheme.surface.copy(0.8f))
        )

        // Tab state
        val tabs = listOf("歌词", "动画", "歌词提供者")
        val pagerState = rememberPagerState { tabs.size }
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        val providerUiStateFlow = remember { MutableStateFlow(ProviderUiState()) }
        val providerUiState = providerUiStateFlow.collectAsState()

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                selectedTabIndex = page
            }
        }

        LaunchedEffect(Unit) {
            LyricProviderManager.loadProviders(context, providerUiStateFlow)
        }

        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier.hazeEffect(hazeState) {
                        style = hazeStyle
                        blurRadius = 25.dp
                        noiseFactor = 0f
                    }
                ) {
                    TopAppBar(
                        color = Color.Transparent,
                        title = "自定义配置",
                        largeTitle = "自定义配置",
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            IconButton(
                                onClick = { finish() },
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                Icon(imageVector = MiuixIcons.Back, contentDescription = "返回")
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    try {
                                        val url = getString(com.lidesheng.hyperlyric.R.string.provider_release_home)
                                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                        startActivity(intent)
                                    } catch (_: Exception) { }
                                },
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = com.lidesheng.hyperlyric.R.drawable.ic_github),
                                    contentDescription = "GitHub",
                                    tint = MiuixTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        },
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 8.dp),
                        pressFeedbackType = PressFeedbackType.Sink,
                        onClick = { }
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color(0xFF3582FF).copy(alpha = 0.4f))
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Powered by lyricon词幕",
                                    style = MiuixTheme.textStyles.title4,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    TabRow(
                        tabs = tabs,
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = {
                            selectedTabIndex = it
                            coroutineScope.launch {
                                pagerState.scrollToPage(it)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 8.dp),
                        colors = TabRowDefaults.tabRowColors(
                            backgroundColor = Color.Transparent
                        )
                    )
                }
            }
        ) { padding ->

            NumberInputDialog(
                show = showTextSizeDialog,
                title = "文字大小",
                label = "范围：8 ~ 16",
                initialValue = textSize,
                min = 8, max = 16,
                onDismiss = { showTextSizeDialog = false },
                onConfirm = { value ->
                    textSize = value
                    saveConfig(Constants.KEY_TEXT_SIZE, value)
                }
            )
            NumberInputDialog(
                show = showFontWeightDialog,
                title = "字重",
                label = "范围：100 ~ 900",
                initialValue = fontWeight,
                min = 100, max = 900,
                onDismiss = { showFontWeightDialog = false },
                onConfirm = { value ->
                    fontWeight = value
                    saveConfig(Constants.KEY_FONT_WEIGHT, value)
                }
            )
            NumberInputDialog(
                show = showFadingEdgeDialog,
                title = "羽化边缘长度",
                label = "范围：0 ~ 100",
                initialValue = fadingEdge,
                min = 0, max = 100,
                onDismiss = { showFadingEdgeDialog = false },
                onConfirm = { value ->
                    fadingEdge = value
                    saveConfig(Constants.KEY_FADING_EDGE_LENGTH, value)
                }
            )
            NumberInputDialog(
                show = showMarqueeSpeedDialog,
                title = "滚动速度",
                label = "范围：10 ~ 500",
                initialValue = marqueeSpeed,
                min = 10, max = 500,
                onDismiss = { showMarqueeSpeedDialog = false },
                onConfirm = { value ->
                    marqueeSpeed = value
                    saveConfig(Constants.KEY_MARQUEE_SPEED, value)
                }
            )
            NumberInputDialog(
                show = showMarqueeDelayDialog,
                title = "初始滚动延迟",
                label = "ms 范围：0 ~ 5000",
                initialValue = marqueeDelay,
                min = 0, max = 5000,
                onDismiss = { showMarqueeDelayDialog = false },
                onConfirm = { value ->
                    marqueeDelay = value
                    saveConfig(Constants.KEY_MARQUEE_DELAY, value)
                }
            )
            NumberInputDialog(
                show = showMarqueeLoopDialog,
                title = "循环间隔",
                label = "ms 范围：0 ~ 5000",
                initialValue = marqueeLoop,
                min = 0, max = 5000,
                onDismiss = { showMarqueeLoopDialog = false },
                onConfirm = { value ->
                    marqueeLoop = value
                    saveConfig(Constants.KEY_MARQUEE_LOOP_DELAY, value)
                }
            )
            NumberInputDialog(
                show = showIslandLengthDialog,
                title = "超级岛长度",
                label = "范围：50 ~ 100",
                initialValue = islandLength,
                min = 50, max = 100,
                onDismiss = { showIslandLengthDialog = false },
                onConfirm = { value ->
                    islandLength = value
                    saveConfig(Constants.KEY_MAX_LEFT_WIDTH, value)
                }
            )
            NumberInputDialog(
                show = showTextSizeRatioDialog,
                title = "多行模式下文字大小比例",
                label = "范围：10 ~ 100",
                initialValue = (textSizeRatio * 100).toInt(),
                min = 10, max = 100,
                onDismiss = { showTextSizeRatioDialog = false },
                onConfirm = { value ->
                    textSizeRatio = value.toFloat() / 100f
                    saveConfig(Constants.KEY_TEXT_SIZE_RATIO, textSizeRatio)
                }
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true,
            ) { page ->
                when (page) {
                    0 -> {
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
                                bottom = padding.calculateBottomPadding()
                            )
                        ) {
                            item {
                                Column {
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column {
                                            SuperArrow(
                                                title = "超级岛长度",
                                                endActions = {
                                                    Text(
                                                        "$islandLength",
                                                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                        color = MiuixTheme.colorScheme.onSurfaceVariantActions
                                                    )
                                                },
                                                onClick = { showIslandLengthDialog = true }
                                            )
                                            Slider(
                                                value = islandLength.toFloat(),
                                                onValueChange = {
                                                    islandLength = it.toInt()
                                                    saveConfig(Constants.KEY_MAX_LEFT_WIDTH, islandLength)
                                                },
                                                valueRange = 50f..100f,
                                                keyPoints = listOf(50f, 60f, 70f, 80f, 90f, 100f),
                                                hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Column {
                                    SmallTitle(
                                        text = "文字",
                                        insideMargin = PaddingValues(10.dp, 4.dp)
                                    )
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            SuperArrow(
                                                title = "大小",
                                                endActions = {
                                                    Text(
                                                        "$textSize",
                                                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                        color = MiuixTheme.colorScheme.onSurfaceVariantActions
                                                    )
                                                },
                                                onClick = { showTextSizeDialog = true }
                                            )
                                            SuperArrow(
                                                title = "字重",
                                                endActions = {
                                                    Text(
                                                        fontWeight.toString(),
                                                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                        color = MiuixTheme.colorScheme.onSurfaceVariantActions
                                                    )
                                                },
                                                onClick = { showFontWeightDialog = true }
                                            )
                                            SuperSwitch(
                                                title = "粗体",
                                                checked = fontBold,
                                                onCheckedChange = {
                                                    fontBold = it
                                                    saveConfig(Constants.KEY_FONT_BOLD, it)
                                                }
                                            )
                                            SuperSwitch(
                                                title = "斜体",
                                                checked = fontItalic,
                                                onCheckedChange = {
                                                    fontItalic = it
                                                    saveConfig(Constants.KEY_FONT_ITALIC, it)
                                                }
                                            )
                                            SuperArrow(
                                                title = "多行模式下文字大小比例",
                                                endActions = {
                                                    Text(
                                                        "${(textSizeRatio * 100).toInt()}%",
                                                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                        color = MiuixTheme.colorScheme.onSurfaceVariantActions
                                                    )
                                                },
                                                onClick = { showTextSizeRatioDialog = true }
                                            )
                                            SuperArrow(
                                                title = "羽化边缘长度",
                                                endActions = {
                                                    Text(
                                                        "$fadingEdge",
                                                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                        color = MiuixTheme.colorScheme.onSurfaceVariantActions
                                                    )
                                                },
                                                onClick = { showFadingEdgeDialog = true }
                                            )
                                            SuperSwitch(
                                                title = "羽化进度样式",
                                                checked = gradientStyle,
                                                onCheckedChange = {
                                                    gradientStyle = it
                                                    saveConfig(Constants.KEY_GRADIENT_PROGRESS, it)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // 2. 歌词滚动
                            item {
                                Column {
                                    SmallTitle(
                                        text = "歌词滚动",
                                        insideMargin = PaddingValues(10.dp, 4.dp)
                                    )
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column {
                                            // 跑马灯部分
                                            SuperSwitch(
                                                title = "歌词滚动",
                                                checked = marqueeMode,
                                                onCheckedChange = {
                                                    marqueeMode = it
                                                    saveConfig(Constants.KEY_MARQUEE_MODE, it)
                                                }
                                            )
                                            AnimatedVisibility(visible = marqueeMode) {
                                                Column {
                                                    SuperArrow(
                                                        title = "滚动速度",
                                                        endActions = {
                                                            Text(
                                                                "$marqueeSpeed",
                                                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                                color = MiuixTheme.colorScheme.onSurfaceVariantActions
                                                            )
                                                        },
                                                        onClick = { showMarqueeSpeedDialog = true }
                                                    )
                                                    SuperArrow(
                                                        title = "初始滚动延迟",
                                                        endActions = {
                                                            Text(
                                                                "${marqueeDelay}ms",
                                                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                                color = MiuixTheme.colorScheme.onSurfaceVariantActions
                                                            )
                                                        },
                                                        onClick = { showMarqueeDelayDialog = true }
                                                    )
                                                    SuperSwitch(
                                                        title = "无限循环",
                                                        checked = marqueeInfinite,
                                                        onCheckedChange = {
                                                            marqueeInfinite = it
                                                            saveConfig(Constants.KEY_MARQUEE_INFINITE, it)
                                                        }
                                                    )
                                                    SuperArrow(
                                                        title = "循环间隔",
                                                        endActions = {
                                                            Text(
                                                                "${marqueeLoop}ms",
                                                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                                color = MiuixTheme.colorScheme.onSurfaceVariantActions
                                                            )
                                                        },
                                                        onClick = { showMarqueeLoopDialog = true }
                                                    )
                                                    SuperSwitch(
                                                        title = "结束时在末尾停止",
                                                        checked = marqueeStopEnd,
                                                        onCheckedChange = {
                                                            marqueeStopEnd = it
                                                            saveConfig(Constants.KEY_MARQUEE_STOP_END, it)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. 逐字歌词
                            item {
                                Column {
                                    SmallTitle(
                                        text = "逐字歌词",
                                        insideMargin = PaddingValues(10.dp, 4.dp)
                                    )
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column {
                                            SuperSwitch(
                                                title = "启用相对进度歌词",
                                                checked = syllableRelative,
                                                onCheckedChange = {
                                                    syllableRelative = it
                                                    saveConfig(Constants.KEY_SYLLABLE_RELATIVE, it)
                                                }
                                            )
                                            SuperSwitch(
                                                title = "显示相对进度歌词高亮进度",
                                                checked = syllableHighlight,
                                                onCheckedChange = {
                                                    syllableHighlight = it
                                                    saveConfig(Constants.KEY_SYLLABLE_HIGHLIGHT, it)
                                                }
                                            )
                                            // 注意：逐字歌词功能的开启取决于数据源是否提供逐字数据，不再提供全局开关
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
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
                                bottom = padding.calculateBottomPadding()
                            )
                        ) {
                            item {
                                BasicComponent(title = "占位")
                            }
                        }
                    }
                    2 -> {
                        LyricProviderTab(
                            uiState = providerUiState.value,
                            padding = padding,
                            hazeState = hazeState,
                            scrollBehavior = scrollBehavior
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun NumberInputDialog(
        show: Boolean,
        title: String,
        label: String,
        initialValue: Int,
        min: Int,
        max: Int,
        onDismiss: () -> Unit,
        onConfirm: (Int) -> Unit
    ) {
        if (!show) return
        var inputValue by remember { mutableStateOf(initialValue.toString()) }

        WindowDialog(
            title = title,
            show = true,
            onDismissRequest = onDismiss
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = inputValue,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) inputValue = newValue
                    },
                    label = label,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    maxLines = 1
                )
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(
                        text = "取消",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(20.dp))
                    TextButton(
                        text = "确认",
                        onClick = {
                            inputValue.toIntOrNull()?.let {
                                onConfirm(it.coerceIn(min, max))
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }
        }
    }

    @Composable
    private fun LyricProviderTab(
        uiState: ProviderUiState,
        padding: PaddingValues,
        hazeState: HazeState,
        scrollBehavior: ScrollBehavior
    ) {
        val groupedModules = remember(uiState.modules) {
            LyricProviderManager.categorizeModules(uiState.modules, "其他")
        }

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
                bottom = padding.calculateBottomPadding()
            )
        ) {
            if (uiState.isLoading && uiState.modules.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "正在扫描已安装的歌词提供者...",
                            color = MiuixTheme.colorScheme.onSurfaceVariantActions
                        )
                    }
                }
            } else if (!uiState.isLoading && uiState.modules.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "暂未发现歌词提供者，请点击右上角图标前往LyricProvider仓库下载",
                            color = MiuixTheme.colorScheme.onSurfaceVariantActions
                        )
                    }
                }
            } else {
                groupedModules.forEach { category ->
                    if (category.name.isNotBlank()) {
                        item(key = "header_${category.name}") {
                            SmallTitle(
                                text = category.name,
                                insideMargin = PaddingValues(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 4.dp)
                            )
                        }
                    }

                    items(category.items.size) { index ->
                        val module = category.items[index]
                        ModuleCard(module)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun ModuleCard(module: LyricModule) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = module.label,
                    style = MiuixTheme.textStyles.title4,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${module.packageInfo.versionName ?: "未知"} | ${module.author ?: "未知作者"}",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantActions
                )

                if (!module.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 0.5.dp,
                        color = MiuixTheme.colorScheme.outline
                    )
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = module.description,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantActions
                    )
                }
            }
        }
    }

}
