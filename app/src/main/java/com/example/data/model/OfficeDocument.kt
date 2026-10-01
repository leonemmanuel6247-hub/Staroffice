package com.example.data.model

data class OfficeDocument(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val type: DocumentType,
    val extension: String = type.defaultExtension,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isTrash: Boolean = false,
    val sizeBytes: Long = 1024,
    val tags: List<String> = emptyList(),
    val writerContent: WriterContent? = null,
    val calcContent: CalcContent? = null,
    val impressContent: ImpressContent? = null,
    val pdfContent: PdfContent? = null
)
