package com.example.animetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "novel_entries")
data class NovelEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String? = null,
    val totalChapters: Int? = null,
    val status: String = "reading",
    val createdAt: Long = System.currentTimeMillis()
)
