// Copyright 2026, HyperLyric contributors
// SPDX-License-Identifier: Apache-2.0

package com.lidesheng.hyperlyric.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BasicComponentColors
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.SwitchColors
import top.yukonga.miuix.kmp.basic.SwitchDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * A "Super" Switch Preference that separates the click event of the card from the switch.
 *
 * @param checked The checked state of the switch.
 * @param onCheckedChange The callback when the switch state is changed.
 * @param title The title of the preference.
 * @param modifier The modifier to be applied to the preference.
 * @param onClick The callback when the card itself is clicked.
 * @param titleColor The color of the title.
 * @param summary The summary text.
 * @param summaryColor The color of the summary.
 * @param startAction Optional composable on the start side.
 * @param endActions Optional additional composables before the divider.
 * @param bottomAction Optional composable at the bottom.
 * @param switchColors The colors for the switch.
 * @param insideMargin The margin inside the preference.
 * @param holdDownState Used to determine whether it is in the pressed state.
 * @param enabled Whether the preference is enabled.
 */
@Composable
@NonRestartableComposable
fun SuperSwitchPreference(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summary: String? = null,
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    startAction: @Composable (() -> Unit)? = null,
    endActions: @Composable RowScope.() -> Unit = {},
    bottomAction: (@Composable () -> Unit)? = null,
    switchColors: SwitchColors = SwitchDefaults.switchColors(),
    insideMargin: PaddingValues = BasicComponentDefaults.InsideMargin,
    holdDownState: Boolean = false,
    enabled: Boolean = true,
) {
    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)
    val currentOnClick by rememberUpdatedState(onClick)

    BasicComponent(
        modifier = modifier,
        insideMargin = insideMargin,
        title = title,
        titleColor = titleColor,
        summary = summary,
        summaryColor = summaryColor,
        startAction = startAction,
        endActions = {
            // User provided end actions (e.g. secondary icons or status text)
            Row(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically)
                    .weight(1f, fill = false),
            ) {
                endActions()
            }

            // The Switch area - maintained with padding to keep functional separation
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Switch(
                    checked = checked,
                    onCheckedChange = currentOnCheckedChange,
                    enabled = enabled,
                    colors = switchColors,
                )
            }
        },
        bottomAction = bottomAction,
        onClick = {
            // Card click event is now independent of the switch
            if (enabled) {
                currentOnClick?.invoke()
            }
        },
        role = Role.Button, // Changed from Role.Switch to Button because the card is a button now
        holdDownState = holdDownState,
        enabled = enabled,
    )
}
