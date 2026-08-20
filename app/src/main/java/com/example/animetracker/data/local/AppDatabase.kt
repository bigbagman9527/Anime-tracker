package com.example.animetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AnimeEntry::class, AnimeProgress::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
}
