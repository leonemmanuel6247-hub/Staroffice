package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CalcGreen
import com.example.ui.theme.CalcGreenContainer
import com.example.ui.theme.ImpressOrange
import com.example.ui.theme.ImpressOrangeContainer
import com.example.ui.theme.PdfRed
import com.example.ui.theme.PdfRedContainer
import com.example.ui.theme.WriterBlue
import com.example.ui.theme.WriterBlueContainer

enum class DocumentType(
    val displayNameFr: String,
    val displayNameEn: String,
    val defaultExtension: String,
    val supportedExtensions: List<String>,
    val color: Color,
    val containerColor: Color,
    val iconEmoji: String
) {
    WRITER(
        displayNameFr = "Document Writer",
        displayNameEn = "Writer Document",
        defaultExtension = ".docx",
        supportedExtensions = listOf(".docx", ".odt", ".rtf", ".txt"),
        color = WriterBlue,
        containerColor = WriterBlueContainer,
        iconEmoji = "📄"
    ),
    CALC(
        displayNameFr = "Tableur Calc",
        displayNameEn = "Calc Spreadsheet",
        defaultExtension = ".xlsx",
        supportedExtensions = listOf(".xlsx", ".ods", ".csv"),
        color = CalcGreen,
        containerColor = CalcGreenContainer,
        iconEmoji = "📊"
    ),
    IMPRESS(
        displayNameFr = "Présentation Impress",
        displayNameEn = "Impress Presentation",
        defaultExtension = ".pptx",
        supportedExtensions = listOf(".pptx", ".odp"),
        color = ImpressOrange,
        containerColor = ImpressOrangeContainer,
        iconEmoji = "📽️"
    ),
    PDF(
        displayNameFr = "Document PDF",
        displayNameEn = "PDF Document",
        defaultExtension = ".pdf",
        supportedExtensions = listOf(".pdf"),
        color = PdfRed,
        containerColor = PdfRedContainer,
        iconEmoji = "📑"
    )
}
