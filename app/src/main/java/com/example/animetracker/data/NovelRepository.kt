package com.example.animetracker.data

import com.example.animetracker.data.local.NovelDao
import com.example.animetracker.data.local.NovelEntry
import com.example.animetracker.data.local.NovelProgress
import com.example.animetracker.data.remote.DoubanBookApi
import kotlinx.coroutines.flow.Flow

class NovelRepository(
    private val dao: NovelDao,
    private val doubanBookApi: DoubanBookApi
) {
    suspend fun searchDoubanBooks(keyword: String) = doubanBookApi.searchBooks(keyword)

    suspend fun saveNovel(novel: NovelEntry): Long = dao.insertNovel(novel)

    suspend fun addProgress(progress: NovelProgress) = dao.insertProgress(progress)

    fun getAllNovels(): Flow<List<NovelEntry>> = dao.getAllNovels()

    suspend fun getNovelById(id: Long): NovelEntry? = dao.getNovelById(id)

    fun getProgressForNovel(novelId: Long): Flow<List<NovelProgress>> = dao.getProgressForNovel(novelId)

    suspend fun getLatestProgress(novelId: Long) = dao.getLatestProgress(novelId)

    suspend fun updateNovel(novel: NovelEntry) = dao.updateNovel(novel)

    suspend fun deleteNovel(novel: NovelEntry) {
        dao.deleteProgressForNovel(novel.id)
        dao.deleteNovel(novel)
    }
}
