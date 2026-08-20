package com.example.animetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnime(anime: AnimeEntry)

    @Insert
    suspend fun insertProgress(progress: AnimeProgress)

    @Query("SELECT * FROM anime_entries ORDER BY createdAt DESC")
    fun getAllAnime(): Flow<List<AnimeEntry>>

    @Query("SELECT * FROM anime_entries WHERE id = :id")
    suspend fun getAnimeById(id: Long): AnimeEntry?

    @Query("SELECT * FROM anime_progress WHERE animeId = :animeId ORDER BY watchedDate DESC")
    fun getProgressForAnime(animeId: Long): Flow<List<AnimeProgress>>

    @Query("SELECT * FROM anime_progress WHERE animeId = :animeId ORDER BY watchedDate DESC LIMIT 1")
    suspend fun getLatestProgress(animeId: Long): AnimeProgress?

    @Delete
    suspend fun deleteAnime(anime: AnimeEntry)
}
