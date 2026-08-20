package com.example.animetracker.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BangumiApi {

    // 搜索番剧，type=2 表示动画
    @GET("v0/search/subjects")
    suspend fun searchSubjects(
        @Query("keywords") keywords: String,
        @Query("type") type: Int = 2
    ): SearchResponse

    // 获取番剧详情
    @GET("v0/subjects/{id}")
    suspend fun getSubjectDetail(
        @Path("id") id: Long
    ): SubjectDetail
}
