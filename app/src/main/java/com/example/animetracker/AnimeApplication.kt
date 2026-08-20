package com.example.animetracker

import android.app.Application
import androidx.room.Room
import com.example.animetracker.data.AnimeRepository
import com.example.animetracker.data.NovelRepository
import com.example.animetracker.data.local.AppDatabase
import com.example.animetracker.data.remote.NetworkModule

class AnimeApplication : Application() {
    lateinit var database: AppDatabase
    lateinit var animeRepository: AnimeRepository
    lateinit var novelRepository: NovelRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "anime_tracker_db"
        ).fallbackToDestructiveMigration()
         .build()

        animeRepository = AnimeRepository(
            NetworkModule.bangumiApi,
            NetworkModule.biliApi,
            database.animeDao()
        )
        novelRepository = NovelRepository(database.novelDao(), NetworkModule.doubanBookApi)
    }
}
