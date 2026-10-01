package com.example.data.model

enum class CellFormat {
    GENERAL,
    NUMBER,
    CURRENCY_EUR,
    CURRENCY_USD,
    PERCENT,
    DATE
}

data class CalcCell(
    val rawValue: String = "",
    val format: CellFormat = CellFormat.GENERAL,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val alignment: BlockAlignment = BlockAlignment.LEFT,
    val bgColorHex: String? = null,
    val textColorHex: String? = null
)

enum class ChartType {
    BAR,
    LINE,
    PIE
}

data class CalcChart(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "Graphique",
    val type: ChartType = ChartType.BAR,
    val labelColumn: String = "A",
    val valueColumn: String = "B",
    val startRow: Int = 2,
    val endRow: Int = 6
)

data class CalcSheet(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "Feuille 1",
    val cells: Map<String, CalcCell> = emptyMap(),
    val maxRows: Int = 30,
    val maxCols: Int = 10,
    val charts: List<CalcChart> = emptyList()
)

data class CalcContent(
    val sheets: List<CalcSheet> = listOf(CalcSheet()),
    val activeSheetIndex: Int = 0
) {
    fun activeSheet(): CalcSheet {
        return sheets.getOrElse(activeSheetIndex) { sheets.firstOrNull() ?: CalcSheet() }
    }
}
