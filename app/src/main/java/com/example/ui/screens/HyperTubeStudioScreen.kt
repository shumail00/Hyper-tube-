package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentPasteSearch
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.R
import com.example.data.VideoEntity
import com.example.data.YouTubeUrlParser
import com.example.ui.components.HyperGlassCard
import com.example.ui.components.HyperOSOmniBar
import com.example.ui.components.HyperOSTactileControlDeck
import com.example.ui.components.SideStudioDock
import com.example.ui.components.StudyNoteCard
import com.example.ui.components.SuperIslandClipboardBanner
import com.example.ui.components.YouTubePlayerWebView
import com.example.ui.theme.HyperThemeMode
import com.example.ui.theme.LocalHyperOSGlass
import com.example.ui.viewmodel.HyperNavDestination
import com.example.ui.viewmodel.HyperTubeUiState
import com.example.ui.viewmodel.HyperTubeViewModel
import kotlinx.coroutines.delay

@Composable
fun HyperTubeStudioScreen(
    uiState: HyperTubeUiState,
    viewModel: HyperTubeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val glass = LocalHyperOSGlass.current

    // Handle Back navigation from sub-destinations or Zen Cinema Mode
    BackHandler(
        enabled = uiState.isZenCinemaMode || uiState.activeDestination != HyperNavDestination.STUDIO
    ) {
        if (uiState.isZenCinemaMode) {
            viewModel.toggleZenCinemaMode()
        } else {
            viewModel.setNavigationDestination(HyperNavDestination.STUDIO)
        }
    }

    // Auto-inspect clipboard when app resumes (Xiaomi HyperOS Super-Island detection)
    DisposableEffect(lifecycleOwner, uiState.preferences.autoClipboardDetect) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && uiState.preferences.autoClipboardDetect) {
                val clipboardText = readClipboardText(context)
                viewModel.onClipboardTextInspected(clipboardText)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Auto-clear transient status toast after 2.8 seconds
    LaunchedEffect(uiState.statusToastMessage) {
        if (uiState.statusToastMessage != null) {
            delay(2800)
            viewModel.clearStatusToast()
        }
    }

    // Smoothly animate ambient thumbnail glow colors when switching videos
    val targetPrimaryGlow = Color(
        (uiState.activeVideo?.dominantColorHex ?: 0xFF2A68F6L) or 0xFF000000L
    )
    val targetSecondaryGlow = Color(
        (uiState.activeVideo?.secondaryColorHex ?: 0xFF2CE5D3L) or 0xFF000000L
    )
    val primaryGlowColor by animateColorAsState(
        targetValue = targetPrimaryGlow,
        animationSpec = tween(durationMillis = 900),
        label = "primaryAmbientGlow"
    )
    val secondaryGlowColor by animateColorAsState(
        targetValue = targetSecondaryGlow,
        animationSpec = tween(durationMillis = 900),
        label = "secondaryAmbientGlow"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ambientBreathing")
    val pulseFactor by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseFactor"
    )

    val glowEnabled = uiState.preferences.ambientGlowEnabled
    val glowIntensity = uiState.preferences.ambientGlowIntensity

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(glass.canvasBackground)
            .drawBehind {
                if (glowEnabled) {
                    val alphaBase = (if (glass.isDark) 0.36f else 0.22f) * glowIntensity
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryGlowColor.copy(alpha = alphaBase),
                                primaryGlowColor.copy(alpha = alphaBase * 0.35f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.38f, size.height * 0.42f),
                            radius = size.minDimension * 0.68f * pulseFactor
                        ),
                        center = Offset(size.width * 0.38f, size.height * 0.42f),
                        radius = size.minDimension * 0.68f * pulseFactor
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                secondaryGlowColor.copy(alpha = alphaBase * 0.85f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.66f, size.height * 0.58f),
                            radius = size.minDimension * 0.55f
                        ),
                        center = Offset(size.width * 0.66f, size.height * 0.58f),
                        radius = size.minDimension * 0.55f
                    )
                }
            }
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val isTabletWide = maxWidth >= 700.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isTabletWide) 18.dp else 12.dp, vertical = 12.dp)
        ) {
            // Xiaomi HyperOS Super Island Clipboard Notification Pill
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                SuperIslandClipboardBanner(
                    prompt = uiState.clipboardPrompt,
                    onWatchNow = viewModel::acceptClipboardPrompt,
                    onDismiss = viewModel::dismissClipboardPrompt
                )
            }

            if (isTabletWide) {
                // 16:10 Xiaomi Pad Landscape Split-Studio Layout
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Left Floating HyperOS Navigation Pill Dock (hidden in Zen Cinema Mode)
                    AnimatedVisibility(visible = !uiState.isZenCinemaMode) {
                        HyperOSFloatingRail(
                            activeDestination = uiState.activeDestination,
                            themeMode = uiState.preferences.themeMode,
                            onSelectDestination = viewModel::setNavigationDestination,
                            onCycleThemeMode = {
                                val nextMode = when (uiState.preferences.themeMode) {
                                    HyperThemeMode.AUTO -> HyperThemeMode.DARK
                                    HyperThemeMode.DARK -> HyperThemeMode.LIGHT
                                    HyperThemeMode.LIGHT -> HyperThemeMode.AUTO
                                }
                                viewModel.setThemeMode(nextMode)
                            }
                        )
                    }

                    // Main Content Area
                    AnimatedContent(
                        targetState = uiState.activeDestination,
                        modifier = Modifier.weight(1f),
                        label = "tabletDestinationContent"
                    ) { destination ->
                        when (destination) {
                            HyperNavDestination.STUDIO -> {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Center Theatre Stage (66% width in split studio, 100% in Zen Cinema)
                                    Column(
                                        modifier = Modifier
                                            .weight(if (uiState.isZenCinemaMode) 1f else 0.65f)
                                            .fillMaxHeight()
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        AnimatedVisibility(visible = !uiState.isZenCinemaMode) {
                                            HyperOSOmniBar(
                                                input = uiState.omniBarInput,
                                                errorText = uiState.omniBarError,
                                                isResolving = uiState.isResolvingUrl,
                                                onInputChange = viewModel::onOmniBarInputChange,
                                                onPasteFromClipboard = {
                                                    val clip = readClipboardText(context).orEmpty()
                                                    viewModel.onOmniBarInputChange(clip)
                                                    if (clip.isNotBlank()) {
                                                        viewModel.submitUrlOrText(clip)
                                                    }
                                                },
                                                onSubmit = { viewModel.submitUrlOrText() }
                                            )
                                        }

                                        val activeVideo = uiState.activeVideo
                                        if (activeVideo != null) {
                                            YouTubePlayerWebView(
                                                videoId = activeVideo.videoId,
                                                videoTitle = activeVideo.title,
                                                thumbnailUrl = activeVideo.thumbnailUrl,
                                                initialStartSeconds = activeVideo.lastPositionSeconds,
                                                playbackSpeed = uiState.playbackSpeed,
                                                isLoopEnabled = uiState.isLoopEnabled,
                                                seekCommandTrigger = uiState.seekCommandTrigger,
                                                onTimeUpdated = viewModel::onPlayerTimeUpdated,
                                                onPlayingStateChanged = viewModel::onPlayerStateChanged
                                            )
                                        }

                                        HyperOSTactileControlDeck(
                                            activeVideo = uiState.activeVideo,
                                            currentSeconds = uiState.currentPlaybackSeconds,
                                            durationSeconds = uiState.videoDurationSeconds,
                                            playbackSpeed = uiState.playbackSpeed,
                                            isLoopEnabled = uiState.isLoopEnabled,
                                            isZenMode = uiState.isZenCinemaMode,
                                            onSeekTo = { viewModel.seekToSecond(it) },
                                            onSkipBy = viewModel::skipBySeconds,
                                            onSelectSpeed = viewModel::setPlaybackSpeed,
                                            onToggleLoop = viewModel::toggleLoopPlayback,
                                            onToggleFavorite = {
                                                uiState.activeVideo?.let { viewModel.toggleFavoriteVideo(it) }
                                            },
                                            onStampNoteAtCurrentTime = viewModel::stampCurrentTimeToNote,
                                            onToggleZenMode = viewModel::toggleZenCinemaMode
                                        )
                                    }

                                    // Right Side-Stage Studio Dock (35% width, collapses in Zen Cinema mode)
                                    if (!uiState.isZenCinemaMode) {
                                        SideStudioDock(
                                            selectedTab = uiState.sideDockTab,
                                            activeVideo = uiState.activeVideo,
                                            currentPlaybackSeconds = uiState.currentPlaybackSeconds,
                                            activeVideoNotes = uiState.activeVideoNotes,
                                            allVideos = uiState.allVideos,
                                            noteInputText = uiState.noteInputText,
                                            selectedNoteTag = uiState.selectedNoteTag,
                                            pinnedTimestampOverride = uiState.pinnedNoteTimestampOverride,
                                            onSelectTab = viewModel::setSideDockTab,
                                            onNoteInputChange = viewModel::onNoteInputChanged,
                                            onSelectNoteTag = viewModel::setSelectedNoteTag,
                                            onSaveNote = viewModel::saveStudyNote,
                                            onDeleteNote = viewModel::deleteStudyNote,
                                            onSeekToTimestamp = { viewModel.seekToSecond(it) },
                                            onSelectVideo = viewModel::selectVideoFromLibrary,
                                            onToggleFavoriteVideo = viewModel::toggleFavoriteVideo,
                                            onDeleteVideo = viewModel::deleteVideo,
                                            modifier = Modifier.weight(0.35f)
                                        )
                                    }
                                }
                            }

                            HyperNavDestination.LIBRARY -> {
                                HyperOSLibraryScreen(
                                    videos = uiState.allVideos,
                                    activeVideoId = uiState.activeVideo?.videoId,
                                    searchQuery = uiState.searchQuery,
                                    filterFavoritesOnly = uiState.filterFavoritesOnly,
                                    onSearchQueryChange = viewModel::setSearchQuery,
                                    onToggleFavoritesFilter = viewModel::toggleFavoritesFilter,
                                    onSelectVideo = viewModel::selectVideoFromLibrary,
                                    onToggleFavorite = viewModel::toggleFavoriteVideo,
                                    onDeleteVideo = viewModel::deleteVideo
                                )
                            }

                            HyperNavDestination.ALL_NOTES -> {
                                HyperOSAllNotesScreen(
                                    allNotes = uiState.allNotes,
                                    videos = uiState.allVideos,
                                    onJumpToVideoNote = { videoId, sec ->
                                        viewModel.seekToSecond(sec, videoIdOverride = videoId)
                                    },
                                    onDeleteNote = viewModel::deleteStudyNote
                                )
                            }

                            HyperNavDestination.CONTROL_CENTER -> {
                                HyperOSControlCenterScreen(
                                    uiState = uiState,
                                    onSetThemeMode = viewModel::setThemeMode,
                                    onSetAmbientGlowEnabled = viewModel::setAmbientGlowEnabled,
                                    onSetAmbientGlowIntensity = viewModel::setAmbientGlowIntensity,
                                    onSetAutoClipboard = viewModel::setAutoClipboardDetect,
                                    onToggleLoop = viewModel::toggleLoopPlayback,
                                    onSelectSpeed = viewModel::setPlaybackSpeed
                                )
                            }
                        }
                    }
                }
            } else {
                // Compact / Portrait Multi-Window Adaptive Layout
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (uiState.activeDestination) {
                        HyperNavDestination.STUDIO -> {
                            if (!uiState.isZenCinemaMode) {
                                HyperOSOmniBar(
                                    input = uiState.omniBarInput,
                                    errorText = uiState.omniBarError,
                                    isResolving = uiState.isResolvingUrl,
                                    onInputChange = viewModel::onOmniBarInputChange,
                                    onPasteFromClipboard = {
                                        val clip = readClipboardText(context).orEmpty()
                                        viewModel.onOmniBarInputChange(clip)
                                        if (clip.isNotBlank()) {
                                            viewModel.submitUrlOrText(clip)
                                        }
                                    },
                                    onSubmit = { viewModel.submitUrlOrText() }
                                )
                            }

                            uiState.activeVideo?.let { activeVideo ->
                                YouTubePlayerWebView(
                                    videoId = activeVideo.videoId,
                                    videoTitle = activeVideo.title,
                                    thumbnailUrl = activeVideo.thumbnailUrl,
                                    initialStartSeconds = activeVideo.lastPositionSeconds,
                                    playbackSpeed = uiState.playbackSpeed,
                                    isLoopEnabled = uiState.isLoopEnabled,
                                    seekCommandTrigger = uiState.seekCommandTrigger,
                                    onTimeUpdated = viewModel::onPlayerTimeUpdated,
                                    onPlayingStateChanged = viewModel::onPlayerStateChanged
                                )
                            }

                            HyperOSTactileControlDeck(
                                activeVideo = uiState.activeVideo,
                                currentSeconds = uiState.currentPlaybackSeconds,
                                durationSeconds = uiState.videoDurationSeconds,
                                playbackSpeed = uiState.playbackSpeed,
                                isLoopEnabled = uiState.isLoopEnabled,
                                isZenMode = uiState.isZenCinemaMode,
                                onSeekTo = { viewModel.seekToSecond(it) },
                                onSkipBy = viewModel::skipBySeconds,
                                onSelectSpeed = viewModel::setPlaybackSpeed,
                                onToggleLoop = viewModel::toggleLoopPlayback,
                                onToggleFavorite = {
                                    uiState.activeVideo?.let { viewModel.toggleFavoriteVideo(it) }
                                },
                                onStampNoteAtCurrentTime = viewModel::stampCurrentTimeToNote,
                                onToggleZenMode = viewModel::toggleZenCinemaMode
                            )

                            if (!uiState.isZenCinemaMode) {
                                SideStudioDock(
                                    selectedTab = uiState.sideDockTab,
                                    activeVideo = uiState.activeVideo,
                                    currentPlaybackSeconds = uiState.currentPlaybackSeconds,
                                    activeVideoNotes = uiState.activeVideoNotes,
                                    allVideos = uiState.allVideos,
                                    noteInputText = uiState.noteInputText,
                                    selectedNoteTag = uiState.selectedNoteTag,
                                    pinnedTimestampOverride = uiState.pinnedNoteTimestampOverride,
                                    onSelectTab = viewModel::setSideDockTab,
                                    onNoteInputChange = viewModel::onNoteInputChanged,
                                    onSelectNoteTag = viewModel::setSelectedNoteTag,
                                    onSaveNote = viewModel::saveStudyNote,
                                    onDeleteNote = viewModel::deleteStudyNote,
                                    onSeekToTimestamp = { viewModel.seekToSecond(it) },
                                    onSelectVideo = viewModel::selectVideoFromLibrary,
                                    onToggleFavoriteVideo = viewModel::toggleFavoriteVideo,
                                    onDeleteVideo = viewModel::deleteVideo,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HyperNavDestination.LIBRARY -> {
                            HyperOSLibraryScreen(
                                videos = uiState.allVideos,
                                activeVideoId = uiState.activeVideo?.videoId,
                                searchQuery = uiState.searchQuery,
                                filterFavoritesOnly = uiState.filterFavoritesOnly,
                                onSearchQueryChange = viewModel::setSearchQuery,
                                onToggleFavoritesFilter = viewModel::toggleFavoritesFilter,
                                onSelectVideo = viewModel::selectVideoFromLibrary,
                                onToggleFavorite = viewModel::toggleFavoriteVideo,
                                onDeleteVideo = viewModel::deleteVideo,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HyperNavDestination.ALL_NOTES -> {
                            HyperOSAllNotesScreen(
                                allNotes = uiState.allNotes,
                                videos = uiState.allVideos,
                                onJumpToVideoNote = { videoId, sec ->
                                    viewModel.seekToSecond(sec, videoIdOverride = videoId)
                                },
                                onDeleteNote = viewModel::deleteStudyNote,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HyperNavDestination.CONTROL_CENTER -> {
                            HyperOSControlCenterScreen(
                                uiState = uiState,
                                onSetThemeMode = viewModel::setThemeMode,
                                onSetAmbientGlowEnabled = viewModel::setAmbientGlowEnabled,
                                onSetAmbientGlowIntensity = viewModel::setAmbientGlowIntensity,
                                onSetAutoClipboard = viewModel::setAutoClipboardDetect,
                                onToggleLoop = viewModel::toggleLoopPlayback,
                                onSelectSpeed = viewModel::setPlaybackSpeed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (!uiState.isZenCinemaMode) {
                        HyperOSBottomFloatingDock(
                            activeDestination = uiState.activeDestination,
                            onSelectDestination = viewModel::setNavigationDestination
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HyperOSFloatingRail(
    activeDestination: HyperNavDestination,
    themeMode: HyperThemeMode,
    onSelectDestination: (HyperNavDestination) -> Unit,
    onCycleThemeMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalHyperOSGlass.current

    HyperGlassCard(
        modifier = modifier
            .width(84.dp)
            .fillMaxHeight(),
        cornerRadius = 30.dp,
        contentPadding = PaddingValues(vertical = 18.dp, horizontal = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand emblem
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = glass.accentBlue,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = "HyperTube Pad",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "HyperOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = glass.accentBlue,
                    fontWeight = FontWeight.Bold
                )
            }

            // Navigation Destinations
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RailNavItem(
                    icon = Icons.Default.SmartDisplay,
                    label = stringResource(R.string.nav_studio),
                    selected = activeDestination == HyperNavDestination.STUDIO,
                    onClick = { onSelectDestination(HyperNavDestination.STUDIO) },
                    testTag = "nav_studio"
                )
                RailNavItem(
                    icon = Icons.Default.VideoLibrary,
                    label = stringResource(R.string.nav_library),
                    selected = activeDestination == HyperNavDestination.LIBRARY,
                    onClick = { onSelectDestination(HyperNavDestination.LIBRARY) },
                    testTag = "nav_library"
                )
                RailNavItem(
                    icon = Icons.Default.EditNote,
                    label = stringResource(R.string.nav_notes),
                    selected = activeDestination == HyperNavDestination.ALL_NOTES,
                    onClick = { onSelectDestination(HyperNavDestination.ALL_NOTES) },
                    testTag = "nav_notes"
                )
                RailNavItem(
                    icon = Icons.Default.Tune,
                    label = stringResource(R.string.nav_control_center),
                    selected = activeDestination == HyperNavDestination.CONTROL_CENTER,
                    onClick = { onSelectDestination(HyperNavDestination.CONTROL_CENTER) },
                    testTag = "nav_control_center"
                )
            }

            // Quick Theme Mode Switcher
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = glass.subtleBorder,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onCycleThemeMode)
                    .testTag("rail_theme_toggle")
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = when (themeMode) {
                            HyperThemeMode.AUTO -> Icons.Default.SettingsSuggest
                            HyperThemeMode.DARK -> Icons.Default.DarkMode
                            HyperThemeMode.LIGHT -> Icons.Default.LightMode
                        },
                        contentDescription = "Cycle HyperOS theme",
                        tint = glass.accentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = themeMode.name.take(4),
                        style = MaterialTheme.typography.labelSmall,
                        color = glass.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun RailNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val glass = LocalHyperOSGlass.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) glass.accentBlue else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else glass.textSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color.White else glass.textSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HyperOSBottomFloatingDock(
    activeDestination: HyperNavDestination,
    onSelectDestination: (HyperNavDestination) -> Unit
) {
    val glass = LocalHyperOSGlass.current
    HyperGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        cornerRadius = 26.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Triple(HyperNavDestination.STUDIO, Icons.Default.SmartDisplay, R.string.nav_studio),
                Triple(HyperNavDestination.LIBRARY, Icons.Default.VideoLibrary, R.string.nav_library),
                Triple(HyperNavDestination.ALL_NOTES, Icons.Default.EditNote, R.string.nav_notes),
                Triple(HyperNavDestination.CONTROL_CENTER, Icons.Default.Tune, R.string.nav_control_center)
            )
            items.forEach { (dest, icon, labelRes) ->
                val selected = activeDestination == dest
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) glass.accentBlue else Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectDestination(dest) }
                        .testTag("bottom_${dest.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = stringResource(labelRes),
                            tint = if (selected) Color.White else glass.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        if (selected) {
                            Text(
                                text = stringResource(labelRes),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HyperOSLibraryScreen(
    videos: List<VideoEntity>,
    activeVideoId: String?,
    searchQuery: String,
    filterFavoritesOnly: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onToggleFavoritesFilter: () -> Unit,
    onSelectVideo: (VideoEntity) -> Unit,
    onToggleFavorite: (VideoEntity) -> Unit,
    onDeleteVideo: (VideoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalHyperOSGlass.current
    val filtered = videos.filter { video ->
        val matchesQuery = searchQuery.isBlank() ||
            video.title.contains(searchQuery, ignoreCase = true) ||
            video.channelName.contains(searchQuery, ignoreCase = true) ||
            video.videoId.contains(searchQuery, ignoreCase = true)
        val matchesFav = !filterFavoritesOnly || video.isFavorite
        matchesQuery && matchesFav
    }

    HyperGlassCard(
        modifier = modifier.fillMaxSize(),
        cornerRadius = 28.dp,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Banner with Generated HyperOS 3 Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hyperos_hero_1793960350661),
                    contentDescription = "HyperOS 3 Focus Library",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xD90A0D16), Color(0x660A0D16))
                            )
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = "Distraction-Free Focus Library",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${videos.size} saved videos • Local on-device Room storage • Zero algorithmic feed",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD0DCF5)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search & Filter Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search saved videos or channels…") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = glass.accentBlue
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = glass.accentBlue,
                        unfocusedBorderColor = glass.subtleBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("library_search_input")
                )

                FilterChip(
                    selected = filterFavoritesOnly,
                    onClick = onToggleFavoritesFilter,
                    label = { Text("Favorites Only") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (filterFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("library_favorites_filter")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 250.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.videoId }) { video ->
                    val isCurrent = video.videoId == activeVideoId
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = glass.subtleBorder,
                        border = BorderStroke(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isCurrent) glass.accentBlue else glass.glassBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .clickable { onSelectVideo(video) }
                            .testTag("library_grid_card_${video.videoId}")
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(135.dp)
                            ) {
                                AsyncImage(
                                    model = video.thumbnailUrl,
                                    contentDescription = video.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xCC0A0D14),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(video.dominantColorHex or 0xFF000000L))
                                        )
                                        Text(
                                            text = if (video.lastPositionSeconds > 0) {
                                                "Resume ${YouTubeUrlParser.formatTimestamp(video.lastPositionSeconds)}"
                                            } else "Watch",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = video.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = glass.textPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = video.channelName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = glass.accentBlue,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row {
                                        IconButton(
                                            onClick = { onToggleFavorite(video) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (video.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (video.isFavorite) Color(0xFFFF4D6D) else glass.textSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteVideo(video) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Remove video",
                                                tint = glass.textSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HyperOSAllNotesScreen(
    allNotes: List<com.example.data.StudyNoteEntity>,
    videos: List<VideoEntity>,
    onJumpToVideoNote: (String, Int) -> Unit,
    onDeleteNote: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalHyperOSGlass.current
    val videoMap = videos.associateBy { it.videoId }

    HyperGlassCard(
        modifier = modifier.fillMaxSize(),
        cornerRadius = 28.dp,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "All Timestamped Study Notes",
                style = MaterialTheme.typography.headlineSmall,
                color = glass.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap any note card or timecode pill to open that video and jump straight to the exact second.",
                style = MaterialTheme.typography.bodySmall,
                color = glass.textSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (allNotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.empty_notes_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = glass.textSecondary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(allNotes, key = { it.id }) { note ->
                        val parentTitle = videoMap[note.videoId]?.title ?: "Video • ${note.videoId}"
                        StudyNoteCard(
                            note = note,
                            isHighlighted = false,
                            videoTitleBadge = parentTitle,
                            onSeek = { onJumpToVideoNote(note.videoId, note.timestampSeconds) },
                            onDelete = { onDeleteNote(note.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HyperOSControlCenterScreen(
    uiState: HyperTubeUiState,
    onSetThemeMode: (HyperThemeMode) -> Unit,
    onSetAmbientGlowEnabled: (Boolean) -> Unit,
    onSetAmbientGlowIntensity: (Float) -> Unit,
    onSetAutoClipboard: (Boolean) -> Unit,
    onToggleLoop: () -> Unit,
    onSelectSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalHyperOSGlass.current
    val prefs = uiState.preferences

    HyperGlassCard(
        modifier = modifier.fillMaxSize(),
        cornerRadius = 28.dp,
        contentPadding = PaddingValues(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = stringResource(R.string.control_center_title),
                style = MaterialTheme.typography.headlineSmall,
                color = glass.textPrimary
            )
            Text(
                text = "Xiaomi Pad 16:10 Alive Design • Customize glassmorphic surfaces, ambient thumbnail lighting, and Super-Island clipboard behavior.",
                style = MaterialTheme.typography.bodyMedium,
                color = glass.textSecondary
            )

            // 1. HyperOS Theme Mode Selector Cards
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = glass.subtleBorder,
                border = BorderStroke(1.dp, glass.glassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "HyperOS 3 Visual Theme",
                        style = MaterialTheme.typography.titleMedium,
                        color = glass.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val modes = listOf(
                            Triple(HyperThemeMode.AUTO, "Auto System", Icons.Default.SettingsSuggest),
                            Triple(HyperThemeMode.LIGHT, "Porcelain Light", Icons.Default.LightMode),
                            Triple(HyperThemeMode.DARK, "Obsidian Dark", Icons.Default.DarkMode)
                        )
                        modes.forEach { (mode, label, icon) ->
                            val selected = prefs.themeMode == mode
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (selected) glass.accentBlue else Color.Transparent,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) glass.accentBlue else glass.glassBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { onSetThemeMode(mode) }
                                    .testTag("theme_mode_${mode.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (selected) Color.White else glass.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) Color.White else glass.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Ambient Thumbnail Glow Pill Card
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = glass.subtleBorder,
                border = BorderStroke(1.dp, glass.glassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = glass.accentCyan
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.ambient_glow_label),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = glass.textPrimary
                                )
                                Text(
                                    text = "Samples vibrant colors from the active video thumbnail for a breathing radial theatre glow",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glass.textSecondary
                                )
                            }
                        }
                        Switch(
                            checked = prefs.ambientGlowEnabled,
                            onCheckedChange = onSetAmbientGlowEnabled,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = glass.accentBlue),
                            modifier = Modifier.testTag("control_ambient_glow_switch")
                        )
                    }

                    if (prefs.ambientGlowEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Glow Intensity",
                                style = MaterialTheme.typography.labelLarge,
                                color = glass.textSecondary
                            )
                            Slider(
                                value = prefs.ambientGlowIntensity,
                                onValueChange = onSetAmbientGlowIntensity,
                                valueRange = 0.15f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = glass.accentCyan,
                                    activeTrackColor = glass.accentCyan
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("control_ambient_glow_slider")
                            )
                            Text(
                                text = "${(prefs.ambientGlowIntensity * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = glass.accentCyan
                            )
                        }
                    }
                }
            }

            // 3. Super-Island Clipboard Auto-Detection Card
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = glass.subtleBorder,
                border = BorderStroke(1.dp, glass.glassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPasteSearch,
                            contentDescription = null,
                            tint = glass.accentBlue
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.clipboard_detect_label),
                                style = MaterialTheme.typography.titleMedium,
                                color = glass.textPrimary
                            )
                            Text(
                                text = "Automatically surfaces a top Super-Island prompt when opening the app with a copied YouTube link",
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary
                            )
                        }
                    }
                    Switch(
                        checked = prefs.autoClipboardDetect,
                        onCheckedChange = onSetAutoClipboard,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = glass.accentBlue),
                        modifier = Modifier.testTag("control_clipboard_switch")
                    )
                }
            }
        }
    }
}

private fun readClipboardText(context: Context): String? {
    return runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = clipboard?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0).coerceToText(context)?.toString()
        } else null
    }.getOrNull()
}
