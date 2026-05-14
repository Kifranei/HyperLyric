package com.lidesheng.hyperlyric.ui.page

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import com.lidesheng.hyperlyric.BuildConfig
import androidx.compose.animation.AnimatedVisibility
import com.lidesheng.hyperlyric.ui.component.NumberInputDialog
import com.lidesheng.hyperlyric.ui.component.SimpleDialog
import com.lidesheng.hyperlyric.ui.component.TextInputDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import com.lidesheng.hyperlyric.ui.utils.Constants as UIConstants
import com.lidesheng.hyperlyric.service.Constants as ServiceConstants
import com.lidesheng.hyperlyric.lyric.DynamicLyricData
import com.lidesheng.hyperlyric.R
import com.lidesheng.hyperlyric.ui.navigation.LocalNavigator
import com.lidesheng.hyperlyric.ui.utils.BlurredBar
import com.lidesheng.hyperlyric.ui.utils.pageScrollModifiers
import com.lidesheng.hyperlyric.ui.utils.rememberBlurBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.basic.SnackbarDuration
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.lidesheng.hyperlyric.lyric.commonMusicApps

@SuppressLint("BatteryLife")
@Composable
fun DynamicIslandNotificationPage() {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val prefs =
        remember { context.getSharedPreferences(UIConstants.PREF_NAME, Context.MODE_PRIVATE) }
    val scrollBehavior = MiuixScrollBehavior()
    val configLazyListState = rememberLazyListState()
    val whitelistLazyListState = rememberLazyListState()
    val backdrop = rememberBlurBackdrop()
    val blurActive = backdrop != null
    val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface
    val snackbarHostState = remember { SnackbarHostState() }

    val onlineLyricCacheLimit = prefs.getInt(
        ServiceConstants.KEY_ONLINE_LYRIC_CACHE_LIMIT,
        ServiceConstants.DEFAULT_ONLINE_LYRIC_CACHE_LIMIT
    )
    var onlineLyricEnabled by remember {
        mutableStateOf(
            prefs.getBoolean(
                ServiceConstants.KEY_ONLINE_LYRIC_ENABLED,
                ServiceConstants.DEFAULT_ONLINE_LYRIC_ENABLED
            )
        )
    }
    var onlineLyricCacheLimitState by remember { mutableIntStateOf(onlineLyricCacheLimit) }
    var limitWidthEnabled by remember {
        mutableStateOf(
            prefs.getBoolean(
                ServiceConstants.KEY_NOTIFICATION_ISLAND_LIMIT_WIDTH,
                ServiceConstants.DEFAULT_NOTIFICATION_ISLAND_LIMIT_WIDTH
            )
        )
    }
    var maxWidth by remember {
        mutableIntStateOf(
            prefs.getInt(
                ServiceConstants.KEY_NOTIFICATION_ISLAND_MAX_WIDTH,
                ServiceConstants.DEFAULT_NOTIFICATION_ISLAND_MAX_WIDTH
            )
        )
    }
    var showCacheLimitDialog by remember { mutableStateOf(false) }

    var notificationType by remember {
        mutableIntStateOf(
            prefs.getInt(
                ServiceConstants.KEY_NOTIFICATION_TYPE,
                ServiceConstants.DEFAULT_NOTIFICATION_TYPE
            )
        )
    }
    val initialIconStyleKey =
        if (notificationType == 1) ServiceConstants.KEY_ISLAND_LEFT_ICON_FOCUS else ServiceConstants.KEY_ISLAND_LEFT_ICON_NORMAL
    var islandLeftIconStyle by remember {
        mutableIntStateOf(
            prefs.getInt(
                initialIconStyleKey,
                ServiceConstants.DEFAULT_ISLAND_LEFT_ICON
            )
        )
    }

    val tabs = listOf(
        stringResource(R.string.title_custom_config),
        stringResource(R.string.title_lyric_whitelist)
    )
    val pagerState = rememberPagerState { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    val msgAppExists = stringResource(R.string.toast_app_exists)
    val msgPkgEmpty = stringResource(R.string.toast_pkg_empty)
    val msgAutostartFailed = stringResource(R.string.toast_autostart_failed)
    val msgBatteryIgnored = stringResource(R.string.toast_battery_ignored)
    val msgBatteryFailed = stringResource(R.string.toast_battery_failed)
    val fmtSongsCount = stringResource(R.string.format_songs_count)

    LaunchedEffect(Unit) { DynamicLyricData.initWhitelist(context) }

    val whitelistSet by DynamicLyricData.whitelistState.collectAsState()
    val whitelist = remember(whitelistSet) { whitelistSet.toList() }

    var showAddWhitelistDialog by remember { mutableStateOf(false) }
    var showDeleteWhitelistDialog by remember { mutableStateOf(false) }
    var tempWhitelistInput by remember { mutableStateOf("") }
    var packageToDelete by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(state = snackbarHostState) },
        topBar = {
            BlurredBar(backdrop, blurActive) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(R.string.title_dynamic_island_lyrics),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    bottomContent = {
                        TabRow(
                            tabs = tabs,
                            selectedTabIndex = pagerState.currentPage,
                            onTabSelected = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        it
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .padding(bottom = 8.dp),
                            colors = TabRowDefaults.tabRowColors(backgroundColor = Color.Transparent)
                        )
                    }
                )
            }
        }
    ) { padding ->
        val topPadding = padding.calculateTopPadding()
        val bottomPadding = padding.calculateBottomPadding()
        val contentPadding = remember(topPadding, bottomPadding) {
            PaddingValues(
                top = topPadding,
                start = 0.dp,
                end = 0.dp,
                bottom = bottomPadding + 16.dp
            )
        }


        NumberInputDialog(
            show = showCacheLimitDialog,
            title = stringResource(R.string.dialog_cache_limit_title),
            label = stringResource(R.string.label_cache_limit_range),
            initialValue = onlineLyricCacheLimitState,
            min = 0,
            max = 10000,
            onDismiss = { showCacheLimitDialog = false },
            onConfirm = {
                onlineLyricCacheLimitState = it
                prefs.edit { putInt(ServiceConstants.KEY_ONLINE_LYRIC_CACHE_LIMIT, it) }
                showCacheLimitDialog = false
            }
        )

        TextInputDialog(
            show = showAddWhitelistDialog,
            title = stringResource(R.string.dialog_add_whitelist_title),
            initialValue = tempWhitelistInput,
            label = stringResource(R.string.dialog_add_whitelist_hint),
            confirmText = stringResource(R.string.save),
            onDismiss = { showAddWhitelistDialog = false },
            onConfirm = { input ->
                if (input.isNotBlank()) {
                    val success = DynamicLyricData.addPackageToWhitelist(context, input)
                    if (success) {
                        showAddWhitelistDialog = false
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = msgAppExists,
                                duration = SnackbarDuration.Custom(2000L)
                            )
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = msgPkgEmpty,
                            duration = SnackbarDuration.Custom(2000L)
                        )
                    }
                }
            }
        )

        SimpleDialog(
            show = showDeleteWhitelistDialog,
            title = stringResource(R.string.dialog_delete_whitelist_title),
            onDismiss = { showDeleteWhitelistDialog = false },
            onConfirm = {
                DynamicLyricData.removePackageFromWhitelist(context, packageToDelete)
                showDeleteWhitelistDialog = false
            }
        )


        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true,
                beyondViewportPageCount = 1
            ) { page ->
                when (page) {
                    0 -> {
                        LazyColumn(
                            state = configLazyListState,
                            modifier = Modifier.pageScrollModifiers(
                                enableScrollEndHaptic = true,
                                showTopAppBar = true,
                                topAppBarScrollBehavior = scrollBehavior
                            ),
                            contentPadding = contentPadding
                        ) {
                            // Notification Type
                            item(key = "notification_type") {
                                val notificationTypeOptions = remember {
                                    listOf(R.string.option_notification_live, R.string.option_notification_focus)
                                }.map { stringResource(id = it) }
                                Card(
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                        .padding(bottom = 12.dp).fillMaxWidth()
                                ) {
                                    OverlayDropdownPreference(
                                        title = stringResource(R.string.title_notification_type),
                                        items = notificationTypeOptions,
                                        selectedIndex = notificationType,
                                        onSelectedIndexChange = { index ->
                                            val oldTypeKey =
                                                if (notificationType == 1) ServiceConstants.KEY_ISLAND_LEFT_ICON_FOCUS else ServiceConstants.KEY_ISLAND_LEFT_ICON_NORMAL
                                            prefs.edit {
                                                putInt(
                                                    oldTypeKey,
                                                    islandLeftIconStyle
                                                )
                                            }
                                            notificationType = index
                                            prefs.edit {
                                                putInt(
                                                    ServiceConstants.KEY_NOTIFICATION_TYPE,
                                                    index
                                                )
                                            }
                                            val newTypeKey =
                                                if (index == 1) ServiceConstants.KEY_ISLAND_LEFT_ICON_FOCUS else ServiceConstants.KEY_ISLAND_LEFT_ICON_NORMAL
                                            islandLeftIconStyle = prefs.getInt(
                                                newTypeKey,
                                                ServiceConstants.DEFAULT_ISLAND_LEFT_ICON
                                            )
                                            prefs.edit {
                                                putInt(
                                                    ServiceConstants.KEY_ISLAND_LEFT_ICON,
                                                    islandLeftIconStyle
                                                )
                                            }
                                        })
                                }
                            }

                            // Island Settings
                            item(key = "island_settings_title") {
                                SmallTitle(text = stringResource(R.string.title_island_settings))
                            }

                            item(key = "island_settings_content") {
                                var disableLyricSplitEnabled by remember {
                                    mutableStateOf(
                                        prefs.getBoolean(
                                            ServiceConstants.KEY_NOTIFICATION_ISLAND_DISABLE_LYRIC_SPLIT,
                                            ServiceConstants.DEFAULT_NOTIFICATION_ISLAND_DISABLE_LYRIC_SPLIT
                                        )
                                    )
                                }
                                Card(
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                        .padding(bottom = 12.dp).fillMaxWidth()
                                ) {
                                    Column {
                                        val iconStyleOptions = remember(notificationType) {
                                            if (notificationType == 1) {
                                                listOf(R.string.option_icon_style_note, R.string.option_icon_style_rounded, R.string.option_icon_style_circular, R.string.option_icon_style_none)
                                            } else {
                                                listOf(R.string.option_icon_style_note, R.string.option_icon_style_rounded, R.string.option_icon_style_circular)
                                            }
                                        }.map { stringResource(id = it) }
                                        val iconStyleKey =
                                            if (notificationType == 1) ServiceConstants.KEY_ISLAND_LEFT_ICON_FOCUS else ServiceConstants.KEY_ISLAND_LEFT_ICON_NORMAL
                                        WindowDropdownPreference(
                                            title = stringResource(R.string.title_island_left_icon),
                                            items = iconStyleOptions,
                                            selectedIndex = islandLeftIconStyle,
                                            onSelectedIndexChange = { index ->
                                                islandLeftIconStyle = index
                                                prefs.edit {
                                                    putInt(iconStyleKey, index)
                                                    putInt(
                                                        ServiceConstants.KEY_ISLAND_LEFT_ICON,
                                                        index
                                                    )
                                                }
                                                if (index !in 0..2) {
                                                    disableLyricSplitEnabled = false
                                                    prefs.edit {
                                                        putBoolean(
                                                            ServiceConstants.KEY_NOTIFICATION_ISLAND_DISABLE_LYRIC_SPLIT,
                                                            false
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                        AnimatedVisibility(visible = notificationType == 1 && islandLeftIconStyle in 0..2) {
                                            SwitchPreference(
                                                title = stringResource(R.string.title_disable_lyric_split),
                                                checked = disableLyricSplitEnabled,
                                                onCheckedChange = { checked ->
                                                    disableLyricSplitEnabled =
                                                        checked; prefs.edit {
                                                    putBoolean(
                                                        ServiceConstants.KEY_NOTIFICATION_ISLAND_DISABLE_LYRIC_SPLIT,
                                                        checked
                                                    )
                                                }
                                                })
                                        }
                                        SwitchPreference(
                                            title = stringResource(R.string.title_limit_width),
                                            summary = stringResource(R.string.summary_experimental),
                                            checked = limitWidthEnabled,
                                            onCheckedChange = { checked ->
                                                limitWidthEnabled = checked; prefs.edit {
                                                putBoolean(
                                                    ServiceConstants.KEY_NOTIFICATION_ISLAND_LIMIT_WIDTH,
                                                    checked
                                                )
                                            }
                                            }
                                        )
                                        AnimatedVisibility(visible = limitWidthEnabled) {
                                            var sliderDragValue by remember { mutableIntStateOf(maxWidth) }
                                            BasicComponent(
                                                title = stringResource(R.string.title_limit_width_desc),
                                                summary = stringResource(R.string.summary_limit_width),
                                                endActions = {
                                                    top.yukonga.miuix.kmp.basic.Text(
                                                        "$sliderDragValue",
                                                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                                                        color = MiuixTheme.colorScheme.onSurfaceVariantActions
                                                    )
                                                },
                                                bottomAction = {
                                                    Slider(
                                                        value = sliderDragValue.toFloat(),
                                                        onValueChange = {
                                                            sliderDragValue = it.toInt()
                                                        },
                                                        onValueChangeFinished = {
                                                            maxWidth = sliderDragValue
                                                            prefs.edit {
                                                                putInt(
                                                                    ServiceConstants.KEY_NOTIFICATION_ISLAND_MAX_WIDTH,
                                                                    sliderDragValue
                                                                )
                                                            }
                                                        },
                                                        valueRange = 100f..720f
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Notification Settings
                            item(key = "notification_settings_title") {
                                SmallTitle(text = stringResource(R.string.title_notification_settings))
                            }

                            item(key = "notification_settings_content") {
                                Card(
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                        .padding(bottom = 12.dp).fillMaxWidth()
                                ) {
                                    var notificationClickAction by remember {
                                        mutableIntStateOf(
                                            prefs.getInt(
                                                ServiceConstants.KEY_NOTIFICATION_CLICK_ACTION,
                                                ServiceConstants.DEFAULT_NOTIFICATION_CLICK_ACTION
                                            )
                                        )
                                    }
                                    val clickOptions = remember {
                                        listOf(R.string.option_click_pause, R.string.option_click_open_app, R.string.option_click_open_media)
                                    }.map { stringResource(id = it) }
                                    WindowDropdownPreference(
                                        title = stringResource(R.string.title_notification_click),
                                        items = clickOptions,
                                        selectedIndex = notificationClickAction,
                                        onSelectedIndexChange = {
                                            notificationClickAction = it; prefs.edit {
                                            putInt(
                                                ServiceConstants.KEY_NOTIFICATION_CLICK_ACTION,
                                                it
                                            )
                                        }
                                        })

                                    var showProgressEnabled by remember {
                                        mutableStateOf(
                                            prefs.getBoolean(
                                                ServiceConstants.KEY_NOTIFICATION_SHOW_PROGRESS,
                                                ServiceConstants.DEFAULT_NOTIFICATION_SHOW_PROGRESS
                                            )
                                        )
                                    }
                                    SwitchPreference(
                                        title = stringResource(R.string.title_show_progress),
                                        summary = stringResource(R.string.summary_show_progress),
                                        checked = showProgressEnabled,
                                        onCheckedChange = { checked ->
                                            showProgressEnabled = checked; prefs.edit {
                                            putBoolean(
                                                ServiceConstants.KEY_NOTIFICATION_SHOW_PROGRESS,
                                                checked
                                            )
                                        }
                                        })

                                    AnimatedVisibility(visible = showProgressEnabled) {
                                        var progressColorEnabled by remember {
                                            mutableStateOf(
                                                prefs.getBoolean(
                                                    ServiceConstants.KEY_NOTIFICATION_PROGRESS_COLOR,
                                                    ServiceConstants.DEFAULT_NOTIFICATION_PROGRESS_COLOR
                                                )
                                            )
                                        }
                                        SwitchPreference(
                                            title = stringResource(R.string.title_progress_color),
                                            summary = stringResource(R.string.summary_progress_color),
                                            checked = progressColorEnabled,
                                            onCheckedChange = { checked ->
                                                progressColorEnabled = checked; prefs.edit {
                                                putBoolean(
                                                    ServiceConstants.KEY_NOTIFICATION_PROGRESS_COLOR,
                                                    checked
                                                )
                                            }
                                            })
                                    }

                                    var showAlbumArtEnabled by remember {
                                        mutableStateOf(
                                            prefs.getBoolean(
                                                ServiceConstants.KEY_NOTIFICATION_ALBUM,
                                                ServiceConstants.DEFAULT_NOTIFICATION_ALBUM
                                            )
                                        )
                                    }
                                    SwitchPreference(
                                        title = stringResource(R.string.title_show_album_art),
                                        checked = showAlbumArtEnabled,
                                        onCheckedChange = { checked ->
                                            showAlbumArtEnabled = checked; prefs.edit {
                                            putBoolean(
                                                ServiceConstants.KEY_NOTIFICATION_ALBUM,
                                                checked
                                            )
                                        }
                                        })

                                    val focusStyleOptions = remember { listOf("OS2", "OS3") }
                                    var focusNotificationType by remember {
                                        mutableIntStateOf(
                                            prefs.getInt(
                                                ServiceConstants.KEY_NOTIFICATION_FOCUS_STYLE,
                                                ServiceConstants.DEFAULT_NOTIFICATION_FOCUS_STYLE
                                            )
                                        )
                                    }
                                    AnimatedVisibility(visible = notificationType == 1) {
                                        WindowDropdownPreference(
                                            title = stringResource(R.string.title_focus_style),
                                            items = focusStyleOptions,
                                            selectedIndex = 1 - focusNotificationType,
                                            onSelectedIndexChange = { index ->
                                                val storedValue =
                                                    1 - index; focusNotificationType =
                                                storedValue; prefs.edit {
                                                putInt(
                                                    ServiceConstants.KEY_NOTIFICATION_FOCUS_STYLE,
                                                    storedValue
                                                )
                                            }
                                            })
                                    }

                                    val normalTitleOptions = remember {
                                        listOf(R.string.option_info_none, R.string.option_info_title, R.string.option_info_artist, R.string.option_info_album, R.string.option_info_title_artist, R.string.option_info_artist_title, R.string.option_info_artist_album)
                                    }.map { stringResource(id = it) }
                                    var normalNotificationTitleStyle by remember {
                                        mutableIntStateOf(
                                            prefs.getInt(
                                                ServiceConstants.KEY_NOTIFICATION_TITLE_STYLE,
                                                ServiceConstants.DEFAULT_NOTIFICATION_TITLE_STYLE
                                            )
                                        )
                                    }
                                    WindowDropdownPreference(
                                        title = stringResource(R.string.title_song_info),
                                        items = normalTitleOptions,
                                        selectedIndex = normalNotificationTitleStyle,
                                        onSelectedIndexChange = {
                                            normalNotificationTitleStyle = it; prefs.edit {
                                            putInt(
                                                ServiceConstants.KEY_NOTIFICATION_TITLE_STYLE,
                                                it
                                            )
                                        }
                                        })
                                }
                            }

                            // Advanced Features
                            item(key = "advanced_features_title") {
                                SmallTitle(text = stringResource(R.string.title_advanced_features))
                            }

                            item(key = "advanced_features_content") {
                                Card(
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                        .padding(bottom = 12.dp).fillMaxWidth()
                                ) {
                                    Column {
                                        ArrowPreference(
                                            title = stringResource(R.string.title_autostart),
                                            onClick = {
                                                try {
                                                    val intent = Intent().apply {
                                                        component =
                                                            android.content.ComponentName(
                                                                "com.miui.securitycenter",
                                                                "com.miui.permcenter.autostart.AutoStartManagementActivity"
                                                            )
                                                    }
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {
                                                    try {
                                                        val intent =
                                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                                data =
                                                                    "package:${context.packageName}".toUri()
                                                            }
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                message = msgAutostartFailed,
                                                                duration = SnackbarDuration.Custom(2000L)
                                                            )
                                                        }
                                                    }
                                                }
                                            })
                                        ArrowPreference(
                                            title = stringResource(R.string.title_battery_optimization),
                                            onClick = {
                                                try {
                                                    val pm =
                                                        context.getSystemService(Context.POWER_SERVICE) as PowerManager
                                                    if (!pm.isIgnoringBatteryOptimizations(
                                                            context.packageName
                                                        )
                                                    ) {
                                                        val intent =
                                                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                                                data =
                                                                    "package:${context.packageName}".toUri()
                                                            }
                                                        context.startActivity(intent)
                                                    } else {
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                message = msgBatteryIgnored,
                                                                duration = SnackbarDuration.Custom(2000L)
                                                            )
                                                        }
                                                    }
                                                } catch (_: Exception) {
                                                    try {
                                                        val intent =
                                                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                message = msgBatteryFailed,
                                                                duration = SnackbarDuration.Custom(2000L)
                                                            )
                                                        }
                                                    }
                                                }
                                            })
                                        if (BuildConfig.ONLINE_FEATURES_ENABLED) {
                                            SwitchPreference(
                                                title = stringResource(R.string.title_online_lyric),
                                                summary = stringResource(R.string.summary_online_lyric),
                                                checked = onlineLyricEnabled,
                                                onCheckedChange = { checked ->
                                                    onlineLyricEnabled = checked; prefs.edit {
                                                    putBoolean(
                                                        ServiceConstants.KEY_ONLINE_LYRIC_ENABLED,
                                                        checked
                                                    )
                                                }
                                                })
                                            if (onlineLyricEnabled) {
                                                ArrowPreference(
                                                    title = stringResource(R.string.dialog_cache_limit_title),
                                                    summary = fmtSongsCount.format(
                                                        onlineLyricCacheLimitState
                                                    ),
                                                    onClick = { showCacheLimitDialog = true })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        LazyColumn(
                            state = whitelistLazyListState,
                            modifier = Modifier.pageScrollModifiers(
                                enableScrollEndHaptic = true,
                                showTopAppBar = true,
                                topAppBarScrollBehavior = scrollBehavior
                            ),
                            contentPadding = contentPadding
                        ) {
                            item(key = "add_whitelist_button") {
                                Card(
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                        .padding(bottom = 12.dp).fillMaxWidth()
                                ) {
                                    ArrowPreference(
                                        title = stringResource(R.string.title_add_whitelist),
                                        onClick = {
                                            tempWhitelistInput = ""; showAddWhitelistDialog = true
                                        },
                                        holdDownState = showAddWhitelistDialog
                                    )
                                }
                            }
                            item(key = "added_apps_title") { SmallTitle(text = stringResource(R.string.title_added_apps)) }
                            if (whitelist.isNotEmpty()) {
                                whitelist.forEach { packageName ->
                                    item(key = packageName) {
                                        val appName = commonMusicApps[packageName]
                                        Card(
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                                .padding(bottom = 12.dp).fillMaxWidth()
                                        ) {
                                            BasicComponent(
                                                title = appName ?: packageName,
                                                summary = if (appName != null) packageName else null,
                                                endActions = {
                                                    IconButton(onClick = {
                                                        packageToDelete =
                                                            packageName; showDeleteWhitelistDialog =
                                                        true
                                                    }) {
                                                        Icon(
                                                            imageVector = MiuixIcons.Delete,
                                                            contentDescription = stringResource(
                                                                R.string.delete
                                                            ),
                                                            tint = MiuixTheme.colorScheme.onSurfaceVariantActions
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    packageToDelete =
                                                        packageName; showDeleteWhitelistDialog =
                                                    true
                                                },
                                                holdDownState = showDeleteWhitelistDialog && packageToDelete == packageName
                                            )
                                        }
                                    }
                                }
                            } else {
                                item(key = "no_whitelist") {
                                    Card(
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                            .padding(bottom = 12.dp).fillMaxWidth()
                                    ) {
                                        BasicComponent(
                                            title = stringResource(R.string.title_no_whitelist),
                                        )
                                    }
                                }
                            }
                            item(key = "whitelist_bottom_spacer") { Spacer(modifier = Modifier.height(20.dp)) }
                        }
                    }
                }
            }
        }
    }
}