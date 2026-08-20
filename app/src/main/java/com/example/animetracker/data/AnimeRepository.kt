package com.example.animetracker.data

import com.example.animetracker.data.local.AnimeDao
import com.example.animetracker.data.remote.BangumiApi
import com.example.animetracker.data.remote.SubjectDetail

class AnimeRepository(
    private val api: BangumiApi,
    private val dao: AnimeDao
) {
    suspend fun searchAnime(keyword: String) = api.searchSubjects(keyword)

    suspend fun getAnimeDetail(id: Long): SubjectDetail = api.getSubjectDetail(id)
}
