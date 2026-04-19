package com.lidesheng.hyperlyric.ui.page.hooksettings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lidesheng.hyperlyric.online.model.DynamicLyricData
import com.lidesheng.hyperlyric.online.model.commonMusicApps
import com.lidesheng.hyperlyric.ui.navigation.LocalNavigator
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FabPosition
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun LyricWhitelistPage() {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())

    val hazeState = remember { HazeState() }
    val hazeStyle = HazeStyle(
        backgroundColor = MiuixTheme.colorScheme.surface,
        tint = HazeTint(MiuixTheme.colorScheme.surface.copy(0.8f))
    )

    LaunchedEffect(Unit) { DynamicLyricData.initWhitelist(context) }

    val addedSet by DynamicLyricData.hookAddedState.collectAsState()
    val activeSet by DynamicLyricData.hookWhitelistState.collectAsState()
    val whitelist = remember(addedSet) { addedSet.toList() }

    var showAddWhitelistDialog by remember { mutableStateOf(false) }
    var showDeleteWhitelistDialog by remember { mutableStateOf(false) }
    var tempWhitelistInput by remember { mutableStateOf("") }
    var packageToDelete by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                color = Color.Transparent,
                title = "歌词白名单",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { tempWhitelistInput = ""; showAddWhitelistDialog = true }
            ) {
                Icon(
                    imageVector = MiuixIcons.Add,
                    contentDescription = "添加",
                    tint = Color.White
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->

        if (showAddWhitelistDialog) {
            WindowDialog(title = "输入应用包名", show = true, onDismissRequest = { showAddWhitelistDialog = false }) {
                Column {
                    TextField(
                        value = tempWhitelistInput,
                        onValueChange = { tempWhitelistInput = it },
                        label = "例如 com.netease.cloudmusic",
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(text = "取消", onClick = { showAddWhitelistDialog = false }, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(20.dp))
                        TextButton(
                            text = "保存",
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (tempWhitelistInput.isNotBlank()) {
                                    val success = DynamicLyricData.addPackageToHookList(context, tempWhitelistInput)
                                    if (success) {
                                        showAddWhitelistDialog = false
                                    } else {
                                        Toast.makeText(context, "该应用已存在", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "包名不能为空", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showDeleteWhitelistDialog) {
            WindowDialog(title = "确认要从列表中移除该应用吗？", show = true, onDismissRequest = { showDeleteWhitelistDialog = false }) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(text = "取消", onClick = { showDeleteWhitelistDialog = false }, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(20.dp))
                    TextButton(
                        text = "确认",
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            DynamicLyricData.removePackageFromHookPage(context, packageToDelete)
                            showDeleteWhitelistDialog = false
                        }
                    )
                }
            }
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
                bottom = padding.calculateBottomPadding() + 80.dp
            )
        ) {
            item {
                if (whitelist.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            whitelist.forEachIndexed { _, packageName ->
                                val appName = commonMusicApps[packageName]
                                SwitchPreference(
                                    title = appName ?: packageName,
                                    summary = if (appName != null) packageName else null,
                                    checked = activeSet.contains(packageName),
                                    onCheckedChange = { isChecked ->
                                        DynamicLyricData.toggleHookStatus(context, packageName, isChecked)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        BasicComponent(
                            title = "暂无白名单应用",
                            summary = "点击右下角按钮添加并启用"
                        )
                    }
                }
            }
        }
    }
}
