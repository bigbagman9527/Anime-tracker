package com.example.animetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NovelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNovel(novel: NovelEntry): Long

    @Insert
    suspend fun insertProgress(progress: NovelProgress)

    @Update
    suspend fun updateNovel(novel: NovelEntry)

    @Delete
    suspend fun deleteNovel(novel: NovelEntry)

    @Query("SELECT * FROM novel_entries ORDER BY createdAt DESC")
    fun getAllNovels(): Flow<List<NovelEntry>>

    @Query("SELECT * FROM novel_entries WHERE id = :id")
    suspend fun getNovelById(id: Long): NovelEntry?

    @Query("SELECT * FROM novel_progress WHERE novelId = :novelId ORDER BY readDate DESC")
    fun getProgressForNovel(novelId: Long): Flow<List<NovelProgress>>

    @Query("SELECT * FROM novel_progress WHERE novelId = :novelId ORDER BY readDate DESC LIMIT 1")
    suspend fun getLatestProgress(novelId: Long): NovelProgress?

    @Query("DELETE FROM novel_progress WHERE novelId = :novelId")
    suspend fun deleteProgressForNovel(novelId: Long)
}
