package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.VideoEntity
import com.example.data.YouTubeUrlParser
import com.example.ui.theme.LocalHyperOSGlass
import com.example.ui.viewmodel.ClipboardPromptState

@Composable
fun HyperGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    elevated: Boolean = false,
    borderColorOverride: Color? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val glass = LocalHyperOSGlass.current
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        modifier = modifier,
        shape = shape,
        color = if (elevated) glass.glassElevated else glass.glassSurface,
        border = BorderStroke(
            width = 1.dp,
            color = borderColorOverride ?: glass.glassBorder
        ),
        shadowElevation = if (glass.isDark) 0.dp else 4.dp
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
fun SuperIslandClipboardBanner(
    prompt: ClipboardPromptState?,
    onWatchNow: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalHyperOSGlass.current

    AnimatedVisibility(
        visible = prompt != null,
        enter = fadeIn() + expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        if (prompt != null) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xEE101420),
                border = BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(
                        listOf(glass.accentBlue, glass.accentCyan)
                    )
                ),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .testTag("super_island_clipboard_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(glass.accentCyan)
                    )
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = stringResource(R.string.super_island_clipboard_detected),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        Text(
                            text = "ID: ${prompt.detectedLink.videoId}" +
                                if (prompt.detectedLink.startSeconds > 0) {
                                    " • Starts at ${YouTubeUrlParser.formatTimestamp(prompt.detectedLink.startSeconds)}"
                                } else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA9B4C9)
                        )
                    }
                    Button(
                        onClick = onWatchNow,
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = glass.accentBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("super_island_watch_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.super_island_watch_now),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("super_island_dismiss_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.super_island_dismiss),
                            tint = Color(0xFFA9B4C9),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HyperOSOmniBar(
    input: String,
    errorText: String?,
    isResolving: Boolean,
    onInputChange: (String) -> Unit,
    onPasteFromClipboard: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalHyperOSGlass.current
    val keyboardController = LocalSoftwareKeyboardController.current

    HyperGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("omnibar_url_input"),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.omnibar_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = glass.accentBlue
                        )
                    },
                    trailingIcon = {
                        if (input.isNotEmpty()) {
                            IconButton(
                                onClick = { onInputChange("") },
                                modifier = Modifier.testTag("omnibar_clear_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear link",
                                    tint = glass.textSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            keyboardController?.hide()
                            onSubmit()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = glass.accentBlue,
                        unfocusedBorderColor = glass.subtleBorder,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                // Quick Clipboard Paste Pill
                FilledTonalIconButton(
                    onClick = onPasteFromClipboard,
                    shape = RoundedCornerShape(16.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = glass.accentBlue.copy(alpha = 0.14f),
                        contentColor = glass.accentBlue
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("omnibar_paste_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = stringResource(R.string.action_paste_and_watch)
                    )
                }

                // Primary Watch Button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        onSubmit()
                    },
                    enabled = !isResolving,
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = glass.accentBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("omnibar_load_button")
                ) {
                    if (isResolving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.action_load_video),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            AnimatedVisibility(visible = !errorText.isNullOrBlank()) {
                Text(
                    text = errorText.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF5C77),
                    modifier = Modifier.padding(start = 12.dp, top = 6.dp)
                )
            }
        }
    }
}

@Composable
fun HyperOSTactileControlDeck(
    activeVideo: VideoEntity?,
    currentSeconds: Int,
    durationSeconds: Int,
    playbackSpeed: Float,
    isLoopEnabled: Boolean,
    isZenMode: Boolean,
    onSeekTo: (Int) -> Unit,
    onSkipBy: (Int) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onToggleLoop: () -> Unit,
    onToggleFavorite: () -> Unit,
    onStampNoteAtCurrentTime: () -> Unit,
    onToggleZenMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalHyperOSGlass.current
    val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    HyperGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 26.dp,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Top Row: Video Title, Channel & Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeVideo?.title ?: "Ready to Watch",
                        style = MaterialTheme.typography.titleMedium,
                        color = glass.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = glass.accentBlue.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = activeVideo?.channelName ?: "Paste any YouTube link",
                                style = MaterialTheme.typography.labelSmall,
                                color = glass.accentBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "${YouTubeUrlParser.formatTimestamp(currentSeconds)} / ${
                                if (durationSeconds > 0) YouTubeUrlParser.formatTimestamp(durationSeconds) else "--:--"
                            }",
                            style = MaterialTheme.typography.labelMedium,
                            color = glass.accentCyan
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (activeVideo != null) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.testTag("deck_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (activeVideo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite video",
                                tint = if (activeVideo.isFavorite) Color(0xFFFF4D6D) else glass.textSecondary
                            )
                        }
                    }

                    // Pin Timestamp Note Button
                    Button(
                        onClick = onStampNoteAtCurrentTime,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = glass.accentCyan.copy(alpha = 0.18f),
                            contentColor = glass.accentCyan
                        ),
                        modifier = Modifier.testTag("deck_stamp_note_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Note @ ${YouTubeUrlParser.formatTimestamp(currentSeconds)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Zen Cinema Mode Button
                    FilledTonalIconButton(
                        onClick = onToggleZenMode,
                        shape = RoundedCornerShape(14.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (isZenMode) glass.accentBlue else glass.subtleBorder,
                            contentColor = if (isZenMode) Color.White else glass.textPrimary
                        ),
                        modifier = Modifier.testTag("deck_zen_mode_button")
                    ) {
                        Icon(
                            imageVector = if (isZenMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = stringResource(
                                if (isZenMode) R.string.action_exit_zen_mode else R.string.action_zen_mode
                            )
                        )
                    }
                }
            }

            // Interactive Progress Slider if duration is known
            if (durationSeconds > 0) {
                Slider(
                    value = currentSeconds.toFloat().coerceIn(0f, durationSeconds.toFloat()),
                    onValueChange = { onSeekTo(it.toInt()) },
                    valueRange = 0f..durationSeconds.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = glass.accentBlue,
                        activeTrackColor = glass.accentBlue,
                        inactiveTrackColor = glass.subtleBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .testTag("deck_progress_slider")
                )
            }

            // Bottom Tactile Row: -10s / +10s Seek, Loop Pill, and Speed Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HyperPillActionChip(
                    icon = Icons.Default.Replay10,
                    label = "-10s",
                    isActive = false,
                    onClick = { onSkipBy(-10) },
                    testTag = "deck_skip_back_10"
                )
                HyperPillActionChip(
                    icon = Icons.Default.Forward10,
                    label = "+10s",
                    isActive = false,
                    onClick = { onSkipBy(10) },
                    testTag = "deck_skip_forward_10"
                )
                HyperPillActionChip(
                    icon = Icons.Default.Repeat,
                    label = if (isLoopEnabled) "Loop On" else "Loop",
                    isActive = isLoopEnabled,
                    onClick = onToggleLoop,
                    testTag = "deck_loop_toggle"
                )

                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = glass.textSecondary,
                    modifier = Modifier.size(16.dp)
                )

                speeds.forEach { spd ->
                    val isSelected = kotlin.math.abs(playbackSpeed - spd) < 0.01f
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) glass.accentBlue else Color.Transparent,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) glass.accentBlue else glass.subtleBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectSpeed(spd) }
                            .testTag("speed_chip_${spd}")
                    ) {
                        Text(
                            text = "${spd}x",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color.White else glass.textSecondary,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HyperPillActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val glass = LocalHyperOSGlass.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) glass.accentBlue.copy(alpha = 0.2f) else glass.subtleBorder,
        border = BorderStroke(
            1.dp,
            if (isActive) glass.accentBlue else Color.Transparent
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) glass.accentBlue else glass.textPrimary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isActive) glass.accentBlue else glass.textPrimary
            )
        }
    }
}
