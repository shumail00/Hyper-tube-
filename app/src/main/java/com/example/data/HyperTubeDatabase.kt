package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VideoEntity::class, StudyNoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HyperTubeDatabase : RoomDatabase() {
    abstract fun hyperTubeDao(): HyperTubeDao

    companion object {
        @Volatile
        private var INSTANCE: HyperTubeDatabase? = null

        fun getInstance(context: Context): HyperTubeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HyperTubeDatabase::class.java,
                    "hypertube_pad_studio.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
