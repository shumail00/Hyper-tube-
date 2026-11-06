package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.HyperOSUserPreferences
import com.example.data.HyperTubeDatabase
import com.example.data.HyperTubeRepository
import com.example.data.ParsedYouTubeLink
import com.example.data.StudyNoteEntity
import com.example.data.VideoEntity
import com.example.data.YouTubeUrlParser
import com.example.ui.theme.HyperThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HyperNavDestination {
    STUDIO,
    LIBRARY,
    ALL_NOTES,
    CONTROL_CENTER
}

enum class SideDockTab {
    STUDY_NOTES,
    SAVED_QUEUE
}

data class ClipboardPromptState(
    val detectedLink: ParsedYouTubeLink,
    val rawText: String
)

data class HyperTubeUiState(
    val activeVideo: VideoEntity? = null,
    val allVideos: List<VideoEntity> = emptyList(),
    val activeVideoNotes: List<StudyNoteEntity> = emptyList(),
    val allNotes: List<StudyNoteEntity> = emptyList(),
    val currentPlaybackSeconds: Int = 0,
    val videoDurationSeconds: Int = 0,
    val isPlaying: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val isLoopEnabled: Boolean = false,
    val isZenCinemaMode: Boolean = false,
    val activeDestination: HyperNavDestination = HyperNavDestination.STUDIO,
    val sideDockTab: SideDockTab = SideDockTab.STUDY_NOTES,
    val omniBarInput: String = "",
    val omniBarError: String? = null,
    val isResolvingUrl: Boolean = false,
    val noteInputText: String = "",
    val selectedNoteTag: String = "Key Idea",
    val pinnedNoteTimestampOverride: Int? = null,
    val clipboardPrompt: ClipboardPromptState? = null,
    val searchQuery: String = "",
    val filterFavoritesOnly: Boolean = false,
    val preferences: HyperOSUserPreferences = HyperOSUserPreferences(),
    val statusToastMessage: String? = null,
    val seekCommandTrigger: Pair<Int, Long>? = null
)

private data class LocalViewState(
    val activeVideoId: String? = null,
    val currentPlaybackSeconds: Int = 0,
    val videoDurationSeconds: Int = 0,
    val isPlaying: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val isLoopEnabled: Boolean = false,
    val isZenCinemaMode: Boolean = false,
    val activeDestination: HyperNavDestination = HyperNavDestination.STUDIO,
    val sideDockTab: SideDockTab = SideDockTab.STUDY_NOTES,
    val omniBarInput: String = "",
    val omniBarError: String? = null,
    val isResolvingUrl: Boolean = false,
    val noteInputText: String = "",
    val selectedNoteTag: String = "Key Idea",
    val pinnedNoteTimestampOverride: Int? = null,
    val clipboardCandidate: ClipboardPromptState? = null,
    val searchQuery: String = "",
    val filterFavoritesOnly: Boolean = false,
    val statusToastMessage: String? = null,
    val seekCommandTrigger: Pair<Int, Long>? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class HyperTubeViewModel(
    private val repository: HyperTubeRepository
) : ViewModel() {

    private val localState = MutableStateFlow(LocalViewState())
    private var lastSavedProgressSec = -1

    private val activeVideoNotesFlow = localState
        .flatMapLatest { state ->
            val id = state.activeVideoId
            if (id.isNullOrBlank()) flowOf(emptyList())
            else repository.observeNotesForVideo(id)
        }

    val uiState: StateFlow<HyperTubeUiState> = combine(
        localState,
        repository.allVideos,
        activeVideoNotesFlow,
        repository.allNotes,
        repository.userPreferences
    ) { local, videos, activeNotes, allNotes, prefs ->
        val activeVideo = videos.find { it.videoId == local.activeVideoId } ?: videos.firstOrNull()
        val effectiveClipboardPrompt = local.clipboardCandidate?.takeIf { candidate ->
            prefs.autoClipboardDetect &&
                candidate.detectedLink.videoId != activeVideo?.videoId &&
                candidate.detectedLink.videoId != prefs.lastDismissedClipboardVideoId
        }

        HyperTubeUiState(
            activeVideo = activeVideo,
            allVideos = videos,
            activeVideoNotes = activeNotes,
            allNotes = allNotes,
            currentPlaybackSeconds = local.currentPlaybackSeconds,
            videoDurationSeconds = local.videoDurationSeconds.coerceAtLeast(activeVideo?.durationSeconds ?: 0),
            isPlaying = local.isPlaying,
            playbackSpeed = local.playbackSpeed,
            isLoopEnabled = local.isLoopEnabled,
            isZenCinemaMode = local.isZenCinemaMode,
            activeDestination = local.activeDestination,
            sideDockTab = local.sideDockTab,
            omniBarInput = local.omniBarInput,
            omniBarError = local.omniBarError,
            isResolvingUrl = local.isResolvingUrl,
            noteInputText = local.noteInputText,
            selectedNoteTag = local.selectedNoteTag,
            pinnedNoteTimestampOverride = local.pinnedNoteTimestampOverride,
            clipboardPrompt = effectiveClipboardPrompt,
            searchQuery = local.searchQuery,
            filterFavoritesOnly = local.filterFavoritesOnly,
            preferences = prefs,
            statusToastMessage = local.statusToastMessage,
            seekCommandTrigger = local.seekCommandTrigger
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HyperTubeUiState()
    )

    init {
        viewModelScope.launch {
            val initialVideo = repository.ensureInitialCuratedLibrary()
            localState.update {
                it.copy(
                    activeVideoId = initialVideo.videoId,
                    currentPlaybackSeconds = initialVideo.lastPositionSeconds,
                    videoDurationSeconds = initialVideo.durationSeconds
                )
            }
        }
    }

    fun onOmniBarInputChange(input: String) {
        localState.update {
            it.copy(omniBarInput = input, omniBarError = null)
        }
    }

    fun submitUrlOrText(rawInput: String = localState.value.omniBarInput) {
        val parsed = YouTubeUrlParser.findFirstYouTubeLinkInText(rawInput)
        if (parsed == null) {
            localState.update {
                it.copy(
                    omniBarError = "Paste a valid YouTube link (youtube.com/watch?v=..., youtu.be/..., live/, or 11-char ID)"
                )
            }
            return
        }

        viewModelScope.launch {
            localState.update {
                it.copy(
                    isResolvingUrl = true,
                    omniBarError = null
                )
            }
            val savedEntity = repository.loadAndSaveYouTubeLink(parsed)
            val startSec = if (parsed.startSeconds > 0) parsed.startSeconds else savedEntity.lastPositionSeconds
            localState.update {
                it.copy(
                    activeVideoId = savedEntity.videoId,
                    currentPlaybackSeconds = startSec,
                    videoDurationSeconds = savedEntity.durationSeconds,
                    omniBarInput = "",
                    omniBarError = null,
                    isResolvingUrl = false,
                    activeDestination = HyperNavDestination.STUDIO,
                    clipboardCandidate = null,
                    statusToastMessage = "Loaded: ${savedEntity.title}",
                    seekCommandTrigger = if (startSec > 0) startSec to System.currentTimeMillis() else null
                )
            }
        }
    }

    fun onClipboardTextInspected(clipboardText: String?) {
        val parsed = YouTubeUrlParser.findFirstYouTubeLinkInText(clipboardText) ?: return
        val currentId = localState.value.activeVideoId
        if (parsed.videoId == currentId) return

        localState.update {
            it.copy(
                clipboardCandidate = ClipboardPromptState(
                    detectedLink = parsed,
                    rawText = clipboardText ?: parsed.canonicalUrl
                )
            )
        }
    }

    fun acceptClipboardPrompt() {
        val candidate = localState.value.clipboardCandidate ?: return
        submitUrlOrText(candidate.rawText)
    }

    fun dismissClipboardPrompt() {
        val candidate = localState.value.clipboardCandidate ?: return
        localState.update { it.copy(clipboardCandidate = null) }
        viewModelScope.launch {
            repository.dismissClipboardVideoId(candidate.detectedLink.videoId)
        }
    }

    fun selectVideoFromLibrary(video: VideoEntity) {
        localState.update {
            it.copy(
                activeVideoId = video.videoId,
                currentPlaybackSeconds = video.lastPositionSeconds,
                videoDurationSeconds = video.durationSeconds,
                activeDestination = HyperNavDestination.STUDIO,
                seekCommandTrigger = if (video.lastPositionSeconds > 0) {
                    video.lastPositionSeconds to System.currentTimeMillis()
                } else null
            )
        }
        viewModelScope.launch {
            repository.updatePlaybackProgress(
                videoId = video.videoId,
                positionSeconds = video.lastPositionSeconds,
                durationSeconds = video.durationSeconds
            )
        }
    }

    fun onPlayerTimeUpdated(currentSeconds: Int, durationSeconds: Int) {
        val safeSec = currentSeconds.coerceAtLeast(0)
        val safeDur = durationSeconds.coerceAtLeast(0)
        localState.update {
            it.copy(
                currentPlaybackSeconds = safeSec,
                videoDurationSeconds = if (safeDur > 0) safeDur else it.videoDurationSeconds
            )
        }

        val activeId = localState.value.activeVideoId ?: return
        if (kotlin.math.abs(safeSec - lastSavedProgressSec) >= 5) {
            lastSavedProgressSec = safeSec
            viewModelScope.launch {
                repository.updatePlaybackProgress(activeId, safeSec, safeDur)
            }
        }
    }

    fun onPlayerStateChanged(isPlaying: Boolean) {
        localState.update { it.copy(isPlaying = isPlaying) }
    }

    fun seekToSecond(targetSeconds: Int, videoIdOverride: String? = null) {
        val bounded = targetSeconds.coerceAtLeast(0)
        localState.update { state ->
            val targetVideoId = videoIdOverride ?: state.activeVideoId
            state.copy(
                activeVideoId = targetVideoId,
                activeDestination = HyperNavDestination.STUDIO,
                currentPlaybackSeconds = bounded,
                seekCommandTrigger = bounded to System.currentTimeMillis()
            )
        }
    }

    fun skipBySeconds(deltaSeconds: Int) {
        val nextSec = (localState.value.currentPlaybackSeconds + deltaSeconds).coerceAtLeast(0)
        seekToSecond(nextSec)
    }

    fun setPlaybackSpeed(speed: Float) {
        localState.update {
            it.copy(
                playbackSpeed = speed,
                statusToastMessage = "Playback speed ${speed}x"
            )
        }
    }

    fun toggleLoopPlayback() {
        localState.update {
            val next = !it.isLoopEnabled
            it.copy(
                isLoopEnabled = next,
                statusToastMessage = if (next) "Loop enabled" else "Loop disabled"
            )
        }
    }

    fun toggleZenCinemaMode() {
        localState.update {
            it.copy(isZenCinemaMode = !it.isZenCinemaMode)
        }
    }

    fun setNavigationDestination(destination: HyperNavDestination) {
        localState.update {
            it.copy(
                activeDestination = destination,
                isZenCinemaMode = if (destination != HyperNavDestination.STUDIO) false else it.isZenCinemaMode
            )
        }
    }

    fun setSideDockTab(tab: SideDockTab) {
        localState.update { it.copy(sideDockTab = tab) }
    }

    fun onNoteInputChanged(text: String) {
        localState.update { state ->
            // Lock in the timestamp when the user starts typing a new note
            val lockedTimestamp = if (state.noteInputText.isEmpty() && text.isNotEmpty() && state.pinnedNoteTimestampOverride == null) {
                state.currentPlaybackSeconds
            } else if (text.isEmpty()) {
                null
            } else {
                state.pinnedNoteTimestampOverride
            }
            state.copy(
                noteInputText = text,
                pinnedNoteTimestampOverride = lockedTimestamp
            )
        }
    }

    fun stampCurrentTimeToNote() {
        val nowSec = localState.value.currentPlaybackSeconds
        localState.update {
            it.copy(
                sideDockTab = SideDockTab.STUDY_NOTES,
                pinnedNoteTimestampOverride = nowSec,
                statusToastMessage = "Stamped note at ${YouTubeUrlParser.formatTimestamp(nowSec)}"
            )
        }
    }

    fun setSelectedNoteTag(tag: String) {
        localState.update { it.copy(selectedNoteTag = tag) }
    }

    fun saveStudyNote() {
        val currentState = localState.value
        val activeVideoId = currentState.activeVideoId ?: uiState.value.activeVideo?.videoId ?: return
        val content = currentState.noteInputText.trim()
        if (content.isEmpty()) return

        val timestampSec = currentState.pinnedNoteTimestampOverride ?: currentState.currentPlaybackSeconds
        val tag = currentState.selectedNoteTag

        viewModelScope.launch {
            repository.addStudyNote(
                videoId = activeVideoId,
                timestampSeconds = timestampSec,
                content = content,
                categoryTag = tag
            )
            localState.update {
                it.copy(
                    noteInputText = "",
                    pinnedNoteTimestampOverride = null,
                    statusToastMessage = "Saved note at ${YouTubeUrlParser.formatTimestamp(timestampSec)}"
                )
            }
        }
    }

    fun deleteStudyNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteStudyNote(noteId)
        }
    }

    fun toggleFavoriteVideo(video: VideoEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(video)
        }
    }

    fun deleteVideo(video: VideoEntity) {
        viewModelScope.launch {
            repository.deleteVideo(video.videoId)
            if (localState.value.activeVideoId == video.videoId) {
                val remaining = uiState.value.allVideos.filterNot { it.videoId == video.videoId }
                localState.update {
                    it.copy(activeVideoId = remaining.firstOrNull()?.videoId)
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        localState.update { it.copy(searchQuery = query) }
    }

    fun toggleFavoritesFilter() {
        localState.update { it.copy(filterFavoritesOnly = !it.filterFavoritesOnly) }
    }

    fun setThemeMode(mode: HyperThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setAmbientGlowEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAmbientGlowEnabled(enabled)
        }
    }

    fun setAmbientGlowIntensity(intensity: Float) {
        viewModelScope.launch {
            repository.setAmbientGlowIntensity(intensity)
        }
    }

    fun setAutoClipboardDetect(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAutoClipboardDetect(enabled)
        }
    }

    fun clearStatusToast() {
        localState.update { it.copy(statusToastMessage = null) }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = HyperTubeDatabase.getInstance(context.applicationContext)
                    val repository = HyperTubeRepository(
                        context = context.applicationContext,
                        dao = database.hyperTubeDao()
                    )
                    return HyperTubeViewModel(repository) as T
                }
            }
        }
    }
}
