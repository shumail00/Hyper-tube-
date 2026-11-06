package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.HyperThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.hyperDataStore by preferencesDataStore(name = "hyperos_studio_prefs")

data class HyperOSUserPreferences(
    val themeMode: HyperThemeMode = HyperThemeMode.AUTO,
    val ambientGlowEnabled: Boolean = true,
    val ambientGlowIntensity: Float = 0.78f,
    val autoClipboardDetect: Boolean = true,
    val lastDismissedClipboardVideoId: String = ""
)

class HyperTubeRepository(
    private val context: Context,
    private val dao: HyperTubeDao,
    private val metadataService: YouTubeMetadataService = YouTubeMetadataService()
) {
    private object PrefKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AMBIENT_GLOW_ENABLED = booleanPreferencesKey("ambient_glow_enabled")
        val AMBIENT_GLOW_INTENSITY = floatPreferencesKey("ambient_glow_intensity")
        val AUTO_CLIPBOARD_DETECT = booleanPreferencesKey("auto_clipboard_detect")
        val LAST_DISMISSED_CLIPBOARD_ID = stringPreferencesKey("last_dismissed_clipboard_id")
        val SEEDED_INITIAL_LIBRARY = booleanPreferencesKey("seeded_initial_library")
    }

    val allVideos: Flow<List<VideoEntity>> = dao.observeAllVideos()
    val allNotes: Flow<List<StudyNoteEntity>> = dao.observeAllNotes()

    val userPreferences: Flow<HyperOSUserPreferences> = context.hyperDataStore.data.map { prefs ->
        val modeStr = prefs[PrefKeys.THEME_MODE] ?: HyperThemeMode.AUTO.name
        val themeMode = runCatching { HyperThemeMode.valueOf(modeStr) }.getOrDefault(HyperThemeMode.AUTO)
        HyperOSUserPreferences(
            themeMode = themeMode,
            ambientGlowEnabled = prefs[PrefKeys.AMBIENT_GLOW_ENABLED] ?: true,
            ambientGlowIntensity = prefs[PrefKeys.AMBIENT_GLOW_INTENSITY] ?: 0.78f,
            autoClipboardDetect = prefs[PrefKeys.AUTO_CLIPBOARD_DETECT] ?: true,
            lastDismissedClipboardVideoId = prefs[PrefKeys.LAST_DISMISSED_CLIPBOARD_ID] ?: ""
        )
    }

    fun observeNotesForVideo(videoId: String): Flow<List<StudyNoteEntity>> {
        return dao.observeNotesForVideo(videoId)
    }

    suspend fun ensureInitialCuratedLibrary(): VideoEntity {
        val prefs = context.hyperDataStore.data.first()
        val alreadySeeded = prefs[PrefKeys.SEEDED_INITIAL_LIBRARY] ?: false
        val existingRecent = dao.getMostRecentVideo()

        if (!alreadySeeded && existingRecent == null) {
            val now = System.currentTimeMillis()
            val curatedVideos = listOf(
                VideoEntity(
                    videoId = "aircAruvnKk",
                    title = "But what is a neural network? | Deep learning, chapter 1",
                    channelName = "3Blue1Brown",
                    thumbnailUrl = "https://i.ytimg.com/vi/aircAruvnKk/hqdefault.jpg",
                    originalUrl = "https://www.youtube.com/watch?v=aircAruvnKk",
                    dominantColorHex = 0xFF2A68F6,
                    secondaryColorHex = 0xFF2CE5D3,
                    lastPositionSeconds = 0,
                    durationSeconds = 1153,
                    isPinnedToQueue = true,
                    isFavorite = true,
                    addedAtTimestamp = now - 300_000,
                    lastWatchedTimestamp = now
                ),
                VideoEntity(
                    videoId = "jfKfPfyJRdk",
                    title = "lofi hip hop radio - beats to relax/study to",
                    channelName = "Lofi Girl",
                    thumbnailUrl = "https://i.ytimg.com/vi/jfKfPfyJRdk/hqdefault.jpg",
                    originalUrl = "https://www.youtube.com/watch?v=jfKfPfyJRdk",
                    dominantColorHex = 0xFFFF7A59,
                    secondaryColorHex = 0xFF635BFF,
                    lastPositionSeconds = 0,
                    durationSeconds = 0,
                    isPinnedToQueue = true,
                    isFavorite = false,
                    addedAtTimestamp = now - 600_000,
                    lastWatchedTimestamp = now - 120_000
                ),
                VideoEntity(
                    videoId = "8jPQjjsBbIc",
                    title = "How to speak so that people want to listen | Julian Treasure",
                    channelName = "TED",
                    thumbnailUrl = "https://i.ytimg.com/vi/8jPQjjsBbIc/hqdefault.jpg",
                    originalUrl = "https://www.youtube.com/watch?v=8jPQjjsBbIc",
                    dominantColorHex = 0xFFFF4D6D,
                    secondaryColorHex = 0xFF2A68F6,
                    lastPositionSeconds = 0,
                    durationSeconds = 598,
                    isPinnedToQueue = true,
                    isFavorite = false,
                    addedAtTimestamp = now - 900_000,
                    lastWatchedTimestamp = now - 240_000
                )
            )

            curatedVideos.forEach { dao.upsertVideo(it) }

            // Seed starter timestamped study notes on the first lecture so the user immediately sees how timestamp seeking works
            dao.insertStudyNote(
                StudyNoteEntity(
                    videoId = "aircAruvnKk",
                    timestampSeconds = 68,
                    content = "Each neuron holds an activation number between 0.0 and 1.0 representing pixel grayscale intensity.",
                    categoryTag = "Key Idea",
                    createdAt = now - 200_000
                )
            )
            dao.insertStudyNote(
                StudyNoteEntity(
                    videoId = "aircAruvnKk",
                    timestampSeconds = 212,
                    content = "Hidden layers decompose digit recognition into edges and loops before combining them into the final digit.",
                    categoryTag = "Summary",
                    createdAt = now - 150_000
                )
            )
            dao.insertStudyNote(
                StudyNoteEntity(
                    videoId = "aircAruvnKk",
                    timestampSeconds = 515,
                    content = "a^(1) = σ(W · a^(0) + b) — matrix vector product plus bias squished by sigmoid / ReLU.",
                    categoryTag = "Formula",
                    createdAt = now - 100_000
                )
            )

            context.hyperDataStore.edit { it[PrefKeys.SEEDED_INITIAL_LIBRARY] = true }
            return curatedVideos.first()
        }

        return existingRecent ?: VideoEntity(
            videoId = "aircAruvnKk",
            title = "But what is a neural network? | Deep learning, chapter 1",
            channelName = "3Blue1Brown",
            thumbnailUrl = "https://i.ytimg.com/vi/aircAruvnKk/hqdefault.jpg",
            originalUrl = "https://www.youtube.com/watch?v=aircAruvnKk"
        )
    }

    suspend fun loadAndSaveYouTubeLink(parsed: ParsedYouTubeLink): VideoEntity {
        val existing = dao.getVideoById(parsed.videoId)
        val now = System.currentTimeMillis()

        // Resolve real metadata and ambient colors from YouTube oEmbed + thumbnail
        val metadata = metadataService.resolveVideoMetadata(parsed.videoId)

        val entity = if (existing != null) {
            existing.copy(
                title = if (metadata.title.startsWith("YouTube Video •") && !existing.title.startsWith("YouTube Video •")) existing.title else metadata.title,
                channelName = if (metadata.channelName == "YouTube Creator" && existing.channelName != "YouTube Creator") existing.channelName else metadata.channelName,
                thumbnailUrl = metadata.thumbnailUrl,
                dominantColorHex = metadata.dominantColorHex,
                secondaryColorHex = metadata.secondaryColorHex,
                lastPositionSeconds = if (parsed.startSeconds > 0) parsed.startSeconds else existing.lastPositionSeconds,
                lastWatchedTimestamp = now
            )
        } else {
            VideoEntity(
                videoId = parsed.videoId,
                title = metadata.title,
                channelName = metadata.channelName,
                thumbnailUrl = metadata.thumbnailUrl,
                originalUrl = parsed.canonicalUrl,
                dominantColorHex = metadata.dominantColorHex,
                secondaryColorHex = metadata.secondaryColorHex,
                lastPositionSeconds = parsed.startSeconds,
                isPinnedToQueue = true,
                isFavorite = false,
                addedAtTimestamp = now,
                lastWatchedTimestamp = now
            )
        }

        dao.upsertVideo(entity)
        return entity
    }

    suspend fun updatePlaybackProgress(videoId: String, positionSeconds: Int, durationSeconds: Int) {
        dao.updatePlaybackProgress(videoId, positionSeconds, durationSeconds)
    }

    suspend fun toggleFavorite(video: VideoEntity) {
        dao.setVideoFavorite(video.videoId, !video.isFavorite)
    }

    suspend fun deleteVideo(videoId: String) {
        dao.deleteVideoById(videoId)
    }

    suspend fun addStudyNote(
        videoId: String,
        timestampSeconds: Int,
        content: String,
        categoryTag: String
    ) {
        if (content.isBlank()) return
        dao.insertStudyNote(
            StudyNoteEntity(
                videoId = videoId,
                timestampSeconds = timestampSeconds.coerceAtLeast(0),
                content = content.trim(),
                categoryTag = categoryTag
            )
        )
    }

    suspend fun deleteStudyNote(noteId: Long) {
        dao.deleteStudyNoteById(noteId)
    }

    suspend fun setThemeMode(mode: HyperThemeMode) {
        context.hyperDataStore.edit { it[PrefKeys.THEME_MODE] = mode.name }
    }

    suspend fun setAmbientGlowEnabled(enabled: Boolean) {
        context.hyperDataStore.edit { it[PrefKeys.AMBIENT_GLOW_ENABLED] = enabled }
    }

    suspend fun setAmbientGlowIntensity(intensity: Float) {
        context.hyperDataStore.edit {
            it[PrefKeys.AMBIENT_GLOW_INTENSITY] = intensity.coerceIn(0.15f, 1.0f)
        }
    }

    suspend fun setAutoClipboardDetect(enabled: Boolean) {
        context.hyperDataStore.edit { it[PrefKeys.AUTO_CLIPBOARD_DETECT] = enabled }
    }

    suspend fun dismissClipboardVideoId(videoId: String) {
        context.hyperDataStore.edit { it[PrefKeys.LAST_DISMISSED_CLIPBOARD_ID] = videoId }
    }
}
