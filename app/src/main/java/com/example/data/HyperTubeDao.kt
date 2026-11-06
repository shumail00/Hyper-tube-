package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HyperTubeDao {
    @Query("SELECT * FROM videos ORDER BY lastWatchedTimestamp DESC")
    fun observeAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE videoId = :videoId LIMIT 1")
    suspend fun getVideoById(videoId: String): VideoEntity?

    @Query("SELECT * FROM videos ORDER BY lastWatchedTimestamp DESC LIMIT 1")
    suspend fun getMostRecentVideo(): VideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVideo(video: VideoEntity)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("UPDATE videos SET lastPositionSeconds = :positionSec, durationSeconds = CASE WHEN :durationSec > 0 THEN :durationSec ELSE durationSeconds END, lastWatchedTimestamp = :timestamp WHERE videoId = :videoId")
    suspend fun updatePlaybackProgress(
        videoId: String,
        positionSec: Int,
        durationSec: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE videos SET isFavorite = :isFavorite WHERE videoId = :videoId")
    suspend fun setVideoFavorite(videoId: String, isFavorite: Boolean)

    @Query("UPDATE videos SET isPinnedToQueue = :isPinned WHERE videoId = :videoId")
    suspend fun setVideoPinned(videoId: String, isPinned: Boolean)

    @Query("DELETE FROM videos WHERE videoId = :videoId")
    suspend fun deleteVideoById(videoId: String)

    @Query("SELECT * FROM study_notes WHERE videoId = :videoId ORDER BY timestampSeconds ASC, createdAt ASC")
    fun observeNotesForVideo(videoId: String): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes ORDER BY createdAt DESC")
    fun observeAllNotes(): Flow<List<StudyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyNote(note: StudyNoteEntity): Long

    @Query("DELETE FROM study_notes WHERE id = :noteId")
    suspend fun deleteStudyNoteById(noteId: Long)
}
