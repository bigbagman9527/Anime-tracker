package com.example.animetracker.data

import com.example.animetracker.data.local.AnimeDao
import com.example.animetracker.data.local.AnimeEntry
import com.example.animetracker.data.local.AnimeProgress
import com.example.animetracker.data.remote.BangumiApi
import com.example.animetracker.data.remote.SubjectDetail

class AnimeRepository(
    private val api: BangumiApi,
    private val dao: AnimeDao
) {
    suspend fun searchAnime(keyword: String) = api.searchSubjects(keyword)

    suspend fun getAnimeDetail(id: Long): SubjectDetail = api.getSubjectDetail(id)

    // 本地保存方法
    suspend fun saveAnime(anime: AnimeEntry): Long {
        return dao.insertAnime(anime)
    }

    suspend fun addProgress(progress: AnimeProgress) {
        dao.insertProgress(progress)
    }

    fun getAllAnime() = dao.getAllAnime()

    suspend fun getLatestProgress(animeId: Long) = dao.getLatestProgress(animeId)
}
