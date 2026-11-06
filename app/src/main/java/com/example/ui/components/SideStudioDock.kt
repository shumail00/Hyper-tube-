package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.StudyNoteEntity
import com.example.data.VideoEntity
import com.example.data.YouTubeUrlParser
import com.example.ui.theme.LocalHyperOSGlass
import com.example.ui.viewmodel.SideDockTab
import kotlin.math.abs

private val NOTE_TAGS = listOf("Key Idea", "Formula", "Summary", "Action Item", "Question")

@Composable
fun SideStudioDock(
    selectedTab: SideDockTab,
    activeVideo: VideoEntity?,
    currentPlaybackSeconds: Int,
    activeVideoNotes: List<StudyNoteEntity>,
    allVideos: List<VideoEntity>,
    noteInputText: String,
    selectedNoteTag: String,
    pinnedTimestampOverride: Int?,
    onSelectTab: (SideDockTab) -> Unit,
    onNoteInputChange: (String) -> Unit,
    onSelectNoteTag: (String) -> Unit,
    onSaveNote: () -> Unit,
    onDeleteNote: (Long) -> Unit,
    onSeekToTimestamp: (Int) -> Unit,
    onSelectVideo: (VideoEntity) -> Unit,
    onToggleFavoriteVideo: (VideoEntity) -> Unit,
    onDeleteVideo: (VideoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalHyperOSGlass.current

    HyperGlassCard(
        modifier = modifier.fillMaxHeight(),
        cornerRadius = 28.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Segmented HyperOS Pill Switcher
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = glass.subtleBorder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SideDockSegmentedTab(
                        title = "${stringResource(R.string.tab_study_notes)} (${activeVideoNotes.size})",
                        icon = Icons.Default.EditNote,
                        selected = selectedTab == SideDockTab.STUDY_NOTES,
                        onClick = { onSelectTab(SideDockTab.STUDY_NOTES) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dock_tab_notes")
                    )
                    SideDockSegmentedTab(
                        title = "${stringResource(R.string.tab_saved_queue)} (${allVideos.size})",
                        icon = Icons.Default.QueueMusic,
                        selected = selectedTab == SideDockTab.SAVED_QUEUE,
                        onClick = { onSelectTab(SideDockTab.SAVED_QUEUE) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dock_tab_queue")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                SideDockTab.STUDY_NOTES -> {
                    StudyNotesPane(
                        currentPlaybackSeconds = currentPlaybackSeconds,
                        pinnedTimestampOverride = pinnedTimestampOverride,
                        notes = activeVideoNotes,
                        noteInputText = noteInputText,
                        selectedNoteTag = selectedNoteTag,
                        onNoteInputChange = onNoteInputChange,
                        onSelectNoteTag = onSelectNoteTag,
                        onSaveNote = onSaveNote,
                        onDeleteNote = onDeleteNote,
                        onSeekToTimestamp = onSeekToTimestamp,
                        modifier = Modifier.weight(1f)
                    )
                }

                SideDockTab.SAVED_QUEUE -> {
                    SavedQueuePane(
                        activeVideoId = activeVideo?.videoId,
                        videos = allVideos,
                        onSelectVideo = onSelectVideo,
                        onToggleFavorite = onToggleFavoriteVideo,
                        onDeleteVideo = onDeleteVideo,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SideDockSegmentedTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalHyperOSGlass.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) glass.accentBlue else Color.Transparent,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else glass.textSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) Color.White else glass.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StudyNotesPane(
    currentPlaybackSeconds: Int,
    pinnedTimestampOverride: Int?,
    notes: List<StudyNoteEntity>,
    noteInputText: String,
    selectedNoteTag: String,
    onNoteInputChange: (String) -> Unit,
    onSelectNoteTag: (String) -> Unit,
    onSaveNote: () -> Unit,
    onDeleteNote: (Long) -> Unit,
    onSeekToTimestamp: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalHyperOSGlass.current
    val effectiveSec = pinnedTimestampOverride ?: currentPlaybackSeconds

    Column(modifier = modifier.fillMaxSize()) {
        // Composer Box
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = glass.subtleBorder,
            border = BorderStroke(1.dp, glass.glassBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tag selector & live timestamp pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = glass.accentCyan.copy(alpha = 0.18f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = glass.accentCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Pin @ ${YouTubeUrlParser.formatTimestamp(effectiveSec)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = glass.accentCyan
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NOTE_TAGS.forEach { tag ->
                            val isSelected = selectedNoteTag == tag
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) glass.accentBlue else Color.Transparent,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) glass.accentBlue else glass.glassBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectNoteTag(tag) }
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else glass.textSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = noteInputText,
                    onValueChange = onNoteInputChange,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.note_input_placeholder),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = glass.accentBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = glass.glassSurface,
                        unfocusedContainerColor = glass.glassSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("study_note_input")
                )

                Button(
                    onClick = onSaveNote,
                    enabled = noteInputText.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = glass.accentBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("save_study_note_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${stringResource(R.string.action_add_note)} (${YouTubeUrlParser.formatTimestamp(effectiveSec)})",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = glass.accentBlue.copy(alpha = 0.6f),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.empty_notes_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = glass.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.empty_notes_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = glass.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("study_notes_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    val isNearCurrentTime = abs(currentPlaybackSeconds - note.timestampSeconds) <= 8
                    StudyNoteCard(
                        note = note,
                        isHighlighted = isNearCurrentTime,
                        onSeek = { onSeekToTimestamp(note.timestampSeconds) },
                        onDelete = { onDeleteNote(note.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StudyNoteCard(
    note: StudyNoteEntity,
    isHighlighted: Boolean,
    onSeek: () -> Unit,
    onDelete: () -> Unit,
    videoTitleBadge: String? = null,
    modifier: Modifier = Modifier
) {
    val glass = LocalHyperOSGlass.current
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isHighlighted) glass.accentBlue.copy(alpha = 0.14f) else glass.subtleBorder,
        border = BorderStroke(
            width = if (isHighlighted) 1.5.dp else 1.dp,
            color = if (isHighlighted) glass.accentCyan else glass.glassBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onSeek)
            .testTag("study_note_card_${note.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Interactive Timestamp Jump Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = glass.accentBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onSeek)
                            .testTag("note_timestamp_pill_${note.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Jump to timestamp",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = YouTubeUrlParser.formatTimestamp(note.timestampSeconds),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = glass.accentCyan.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = note.categoryTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = glass.accentCyan,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_note_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete note",
                        tint = glass.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (!videoTitleBadge.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = videoTitleBadge,
                    style = MaterialTheme.typography.labelSmall,
                    color = glass.accentBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = glass.textPrimary
            )
        }
    }
}

@Composable
private fun SavedQueuePane(
    activeVideoId: String?,
    videos: List<VideoEntity>,
    onSelectVideo: (VideoEntity) -> Unit,
    onToggleFavorite: (VideoEntity) -> Unit,
    onDeleteVideo: (VideoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalHyperOSGlass.current

    if (videos.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.empty_library_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = glass.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("saved_queue_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(videos, key = { it.videoId }) { video ->
                val isPlayingNow = video.videoId == activeVideoId
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isPlayingNow) glass.accentBlue.copy(alpha = 0.16f) else glass.subtleBorder,
                    border = BorderStroke(
                        width = if (isPlayingNow) 1.5.dp else 1.dp,
                        color = if (isPlayingNow) glass.accentBlue else glass.glassBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSelectVideo(video) }
                        .testTag("queue_video_card_${video.videoId}")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(88.dp)
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0E121B))
                        ) {
                            AsyncImage(
                                model = video.thumbnailUrl,
                                contentDescription = video.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Ambient Color Dot
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(video.dominantColorHex or 0xFF000000L))
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = glass.textPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = video.channelName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glass.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (video.lastPositionSeconds > 5) {
                                    Text(
                                        text = "• ${YouTubeUrlParser.formatTimestamp(video.lastPositionSeconds)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = glass.accentCyan
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { onToggleFavorite(video) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = if (video.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (video.isFavorite) Color(0xFFFF4D6D) else glass.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDeleteVideo(video) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete video",
                                    tint = glass.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
