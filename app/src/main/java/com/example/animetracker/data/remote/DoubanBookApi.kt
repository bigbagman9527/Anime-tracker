package com.example.animetracker.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface DoubanBookApi {
    @GET("j/subject_suggest")
    suspend fun searchBooks(@Query("q") keyword: String): List<DoubanBookItem>
}

data class DoubanBookItem(
    val id: Long,
    val title: String,
    val author: String?,
    val cover: String?,
    val publisher: String?,
    val year: String?,
    val url: String?
)
