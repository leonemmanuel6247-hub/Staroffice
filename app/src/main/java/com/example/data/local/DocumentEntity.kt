package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val type: String, // "WRITER", "CALC", "IMPRESS", "PDF"
    val extension: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean = false,
    val isTrash: Boolean = false,
    val sizeBytes: Long = 1024,
    val contentJson: String = "",
    val thumbnailUri: String? = null,
    val tags: String = ""
)
