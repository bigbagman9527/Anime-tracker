package com.example.animetracker.data.remote

data class SearchResponse(
    val data: List<SubjectItem>?
)

data class SubjectItem(
    val id: Long,
    val name: String,
    val name_cn: String?,
    val summary: String?,
    val images: Images?,
    val air_date: String?,
    val eps: Int?
)

data class Images(
    val large: String?,
    val common: String?,
    val medium: String?,
    val small: String?,
    val grid: String?
)
