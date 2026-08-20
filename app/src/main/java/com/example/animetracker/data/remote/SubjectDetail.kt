package com.example.animetracker.data.remote

data class SubjectDetail(
    val id: Long,
    val name: String,
    val name_cn: String?,
    val summary: String?,
    val images: Images?,
    val air_date: String?,
    val eps: Int?,
    val rating: Rating?,
    val tags: List<Tag>?
)

data class Rating(
    val score: Double?,
    val total: Int?
)

data class Tag(
    val name: String,
    val count: Int
)
