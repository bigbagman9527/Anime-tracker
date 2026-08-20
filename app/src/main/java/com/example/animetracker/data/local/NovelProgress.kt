package com.example.animetracker.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "novel_progress",
    foreignKeys = [
        ForeignKey(
            entity = NovelEntry::class,
            parentColumns = ["id"],
            childColumns = ["novelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("novelId")]
)
data class NovelProgress(
    @PrimaryKey(autoGenerate = true) val progressId: Long = 0,
    val novelId: Long,
    val chapter: Int?,          // 读到第几章
    val page: Int?,             // 或第几页
    val readDate: Long,
    val note: String? = null
)
