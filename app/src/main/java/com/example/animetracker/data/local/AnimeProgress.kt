package com.example.animetracker.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "anime_progress",
    foreignKeys = [
        ForeignKey(
            entity = AnimeEntry::class,
            parentColumns = ["id"],
            childColumns = ["animeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("animeId")]
)
data class AnimeProgress(
    @PrimaryKey(autoGenerate = true) val progressId: Long = 0,
    val animeId: Long,
    val episode: Int,
    val watchedDate: Long,
    val note: String? = null
)
