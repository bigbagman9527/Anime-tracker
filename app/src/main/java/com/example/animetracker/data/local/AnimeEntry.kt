package com.example.animetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime_entries")
data class AnimeEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nameCn: String? = null,
    val summary: String? = null,
    val coverUrl: String? = null,
    val airDate: String? = null,
    val episodes: Int? = null,
    val status: String = "watching",
    val myRating: Float? = null,
    val createdAt: Long = System.currentTimeMillis()
)
