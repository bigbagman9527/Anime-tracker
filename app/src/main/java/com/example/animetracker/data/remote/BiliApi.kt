package com.example.animetracker.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface BiliApi {

    @GET("x/web-interface/search/type")
    suspend fun searchBangumi(
        @Query("search_type") searchType: String = "media_bangumi",
        @Query("keyword") keyword: String
    ): BiliSearchResponse
}

data class BiliSearchResponse(
    val code: Int,
    val message: String,
    val data: BiliSearchData?
)

data class BiliSearchData(
    val result: List<BiliResultItem>?
)

data class BiliResultItem(
    val media_id: Long?,
    val season_id: Long?,
    val title: String,
    val cover: String?,
    val desc: String?,
    val episodes: Int?,
    val org_title: String?,
    val areas: String?,
    val styles: String?
)
