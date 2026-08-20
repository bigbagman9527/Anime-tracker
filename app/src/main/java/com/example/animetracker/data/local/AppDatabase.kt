package com.example.animetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AnimeEntry::class,
        AnimeProgress::class,
        NovelEntry::class,
        NovelProgress::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
    abstract fun novelDao(): NovelDao
}
