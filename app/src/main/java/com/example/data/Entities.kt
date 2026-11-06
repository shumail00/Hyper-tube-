package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val channelName: String,
    val thumbnailUrl: String,
    val originalUrl: String,
    val dominantColorHex: Long = 0xFF2A68F6,
    val secondaryColorHex: Long = 0xFF00B8A9,
    val lastPositionSeconds: Int = 0,
    val durationSeconds: Int = 0,
    val isPinnedToQueue: Boolean = true,
    val isFavorite: Boolean = false,
    val addedAtTimestamp: Long = System.currentTimeMillis(),
    val lastWatchedTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "study_notes",
    foreignKeys = [
        ForeignKey(
            entity = VideoEntity::class,
            parentColumns = ["videoId"],
            childColumns = ["videoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["videoId"])]
)
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId: String,
    val timestampSeconds: Int,
    val content: String,
    val categoryTag: String = "Key Idea",
    val createdAt: Long = System.currentTimeMillis()
)
