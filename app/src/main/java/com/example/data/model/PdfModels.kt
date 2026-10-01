package com.example.data.model

enum class AnnotationType {
    HIGHLIGHT,
    PEN,
    NOTE,
    SIGNATURE
}

data class DrawPoint(val x: Float, val y: Float)

data class DrawStroke(
    val points: List<DrawPoint> = emptyList(),
    val colorHex: String = "#DC2626",
    val strokeWidth: Float = 4f
)

data class PdfAnnotation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: AnnotationType = AnnotationType.HIGHLIGHT,
    val text: String = "",
    val colorHex: String = "#FEF08A",
    val author: String = "StarOffice",
    val date: Long = System.currentTimeMillis(),
    val stroke: DrawStroke? = null,
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f
)

data class PdfFormField(
    val id: String = java.util.UUID.randomUUID().toString(),
    val label: String,
    val value: String = "",
    val placeholder: String = "",
    val isRequired: Boolean = false
)

data class PdfPageData(
    val pageNumber: Int,
    val headerTitle: String = "StarOffice Document",
    val sections: List<String> = emptyList(),
    val formFields: List<PdfFormField> = emptyList(),
    val annotations: List<PdfAnnotation> = emptyList()
)

data class PdfContent(
    val pages: List<PdfPageData> = listOf(PdfPageData(pageNumber = 1)),
    val activePageIndex: Int = 0,
    val isSigned: Boolean = false,
    val signaturePath: List<DrawPoint>? = null,
    val signerName: String? = null
)
