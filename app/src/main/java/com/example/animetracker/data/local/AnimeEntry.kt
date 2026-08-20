package com.example.animetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime_entries")
data class AnimeEntry(
    @PrimaryKey val id: Long,
    val name: String,
    val nameCn: String?,
    val summary: String?,
    val coverUrl: String?,
    val airDate: String?,
    val episodes: Int?,
    val status: String,
    val myRating: Float? = null,
    val createdAt: Long = System.currentTimeMillis()
)
