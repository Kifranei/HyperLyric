package com.lidesheng.hyperlyric.ui.page.hooksettings

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.lidesheng.hyperlyric.R
import com.lidesheng.hyperlyric.ui.navigation.LocalNavigator
import com.lidesheng.hyperlyric.ui.component.SuperSwitchPreference
import com.lidesheng.hyperlyric.ui.component.ProComponent
import com.lidesheng.hyperlyric.utils.LyricModule
import com.lidesheng.hyperlyric.utils.LyricProviderManager
import com.lidesheng.hyperlyric.utils.ModuleCategory
import com.lidesheng.hyperlyric.utils.ModuleTag
import com.lidesheng.hyperlyric.utils.ProviderUiState
import com.lidesheng.hyperlyric.ui.utils.BlurredBar
import com.lidesheng.hyperlyric.ui.utils.pageScrollModifiers
import com.lidesheng.hyperlyric.ui.utils.rememberBlurBackdrop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@Composable
fun LyricProviderPage() {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val providerReleaseHome = stringResource(R.string.provider_release_home)
    val backdrop = rememberBlurBackdrop()
    val blurActive = backdrop != null
    val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface
    val topAppBarScrollBehavior = MiuixScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val providerUiStateFlow = remember { MutableStateFlow(ProviderUiState()) }
    val providerUiState = providerUiStateFlow.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) { LyricProviderManager.loadProviders(context, providerUiStateFlow) }

    val othersCategoryName = stringResource(id = R.string.category_others)
    val groupedModules = remember(providerUiState.value.modules) {
        LyricProviderManager.categorizeModules(providerUiState.value.modules, othersCategoryName)
    }

    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
    val enabledStates = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        topBar = {
            BlurredBar(backdrop, blurActive) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(id = R.string.title_lyric_provider),
                    scrollBehavior = topAppBarScrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(imageVector = MiuixIcons.Back, contentDescription = stringResource(id = R.string.back))
                        }
                    },
                    actions = {
                        IconButton(onClick = { try { context.startActivity(Intent(Intent.ACTION_VIEW, providerReleaseHome.toUri())) } catch (_: Exception) {} }) {
                            Icon(painter = painterResource(id = R.drawable.ic_github), contentDescription = stringResource(id = R.string.github), tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.size(26.dp))
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            PullToRefresh(
                isRefreshing = providerUiState.value.isLoading,
                onRefresh = { coroutineScope.launch { LyricProviderManager.loadProviders(context, providerUiStateFlow) } },
                pullToRefreshState = pullToRefreshState,
                topAppBarScrollBehavior = topAppBarScrollBehavior,
                contentPadding = PaddingValues(top = innerPadding.calculateTopPadding()),
                refreshTexts = listOf(stringResource(id = R.string.refresh_pull_down), stringResource(id = R.string.refresh_release), stringResource(id = R.string.refreshing), stringResource(id = R.string.refresh_success)),
                modifier = Modifier.fillMaxSize()
            ) {
                val lazyListState = rememberLazyListState()
                val top = innerPadding.calculateTopPadding()
                val bottom = innerPadding.calculateBottomPadding()
                val contentPadding = remember(top, bottom) {
                    PaddingValues(top = top, start = 0.dp, end = 0.dp, bottom = bottom)
                }
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.pageScrollModifiers(true, false, topAppBarScrollBehavior),
                    contentPadding = contentPadding,
                ) {
                    providerSections(providerUiState.value, groupedModules, expandedStates, enabledStates)
                }
            }
        }
    }
}

private fun LazyListScope.providerSections(
    uiState: ProviderUiState,
    groupedModules: List<ModuleCategory>,
    expandedStates: MutableMap<String, Boolean>,
    enabledStates: MutableMap<String, Boolean>
) {
    if (!uiState.isLoading && uiState.modules.isEmpty()) {
        item(key = "no_provider") {
            Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp).fillMaxWidth()) {
                BasicComponent(title = stringResource(id = R.string.title_no_provider), summary = stringResource(id = R.string.summary_no_provider))
            }
        }
    } else {
        groupedModules.forEach { category ->
            if (category.name.isNotBlank()) {
                item(key = "header_${category.name}") {
                    SmallTitle(text = category.name, insideMargin = PaddingValues(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 4.dp))
                }
            }
            items(category.items.size, key = { "provider_${category.items[it].packageInfo.packageName}" }) { index ->
                val module = category.items[index]
                val packageName = module.packageInfo.packageName
                val isExpanded = expandedStates[packageName] ?: false
                val isEnabled = enabledStates[packageName] ?: true

                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column {
                        SuperSwitchPreference(
                            checked = isEnabled,
                            onCheckedChange = { enabledStates[packageName] = it },
                            title = module.label,
                            summary = stringResource(
                                id = R.string.format_version_author,
                                module.packageInfo.versionName ?: stringResource(id = R.string.unknown),
                                module.author ?: stringResource(id = R.string.unknown_author)
                            ),
                            onClick = { expandedStates[packageName] = !isExpanded },
                            startAction = {
                                val pm = LocalContext.current.packageManager
                                val appInfo = module.packageInfo.applicationInfo
                                val icon = remember(packageName) { appInfo?.loadIcon(pm) }
                                if (icon != null) {
                                    Box(modifier = Modifier.size(40.dp)) {
                                        androidx.compose.ui.viewinterop.AndroidView(
                                            factory = { context ->
                                                android.widget.ImageView(context).apply {
                                                    setImageDrawable(icon)
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            },
                            showIndication = false
                        )
                        AnimatedVisibility(visible = isExpanded) {
                            ProComponent(
                                summary = module.description,
                                bottomAction = {
                                    if (module.tags.isNotEmpty()) {
                                        ModuleTagsFlow(module.tags)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModuleTagsFlow(tags: List<ModuleTag>) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
    ) {
        tags.forEach { tag ->
            val title = if (tag.titleRes != -1) stringResource(tag.titleRes) else tag.title.orEmpty()
            Card(
                modifier = Modifier.padding(end = 8.dp, top = 4.dp),
                colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceVariant
                ),
                pressFeedbackType = top.yukonga.miuix.kmp.utils.PressFeedbackType.None
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (tag.iconRes != null) {
                        Icon(
                            painter = painterResource(id = tag.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MiuixTheme.colorScheme.onSurfaceVariantActions
                        )
                        Spacer(modifier = Modifier.padding(start = 4.dp))
                    }
                    Text(
                        text = title,
                        style = MiuixTheme.textStyles.body2,
                        fontSize = MiuixTheme.textStyles.body2.fontSize * 0.85f,
                        color = if (tag.isRainbow) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantActions,
                        fontWeight = if (tag.isRainbow) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
