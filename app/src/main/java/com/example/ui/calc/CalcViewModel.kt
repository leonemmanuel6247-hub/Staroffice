package com.example.ui.calc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StarOfficeDatabase
import com.example.data.model.*
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalcUiState(
    val document: OfficeDocument? = null,
    val selectedCellCoord: String = "A1",
    val formulaBarInput: String = "",
    val showChartDialog: Boolean = false,
    val activeChart: CalcChart? = null,
    val showShareDialog: Boolean = false,
    val isSaved: Boolean = true
)

class CalcViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DocumentRepository
    private val _uiState = MutableStateFlow(CalcUiState())
    val uiState: StateFlow<CalcUiState> = _uiState.asStateFlow()

    init {
        val db = StarOfficeDatabase.getDatabase(application)
        repository = DocumentRepository(db.documentDao())
    }

    fun loadDocument(documentId: String) {
        viewModelScope.launch {
            val doc = repository.getDocumentById(documentId)
            _uiState.update {
                val initialInput = doc?.calcContent?.activeSheet()?.cells?.get("A1")?.rawValue ?: ""
                it.copy(document = doc, selectedCellCoord = "A1", formulaBarInput = initialInput)
            }
        }
    }

    fun setDocument(document: OfficeDocument) {
        val initialInput = document.calcContent?.activeSheet()?.cells?.get("A1")?.rawValue ?: ""
        _uiState.update { it.copy(document = document, selectedCellCoord = "A1", formulaBarInput = initialInput) }
    }

    fun updateTitle(newTitle: String) {
        val current = _uiState.value.document ?: return
        val updated = current.copy(title = newTitle, updatedAt = System.currentTimeMillis())
        _uiState.update { it.copy(document = updated, isSaved = false) }
        saveDocument(updated)
    }

    fun selectCell(coord: String) {
        val sheet = _uiState.value.document?.calcContent?.activeSheet()
        val currentRaw = sheet?.cells?.get(coord)?.rawValue ?: ""
        _uiState.update {
            it.copy(selectedCellCoord = coord, formulaBarInput = currentRaw)
        }
    }

    fun updateFormulaBarInput(input: String) {
        _uiState.update { it.copy(formulaBarInput = input) }
        val coord = _uiState.value.selectedCellCoord
        updateCellRawValue(coord, input)
    }

    fun insertFormulaSnippet(snippet: String) {
        val current = _uiState.value.formulaBarInput
        val updated = if (current.isEmpty() && !snippet.startsWith("=")) "=$snippet" else current + snippet
        updateFormulaBarInput(updated)
    }

    fun updateCellRawValue(coord: String, rawValue: String) {
        val current = _uiState.value.document ?: return
        val calc = current.calcContent ?: CalcContent()
        val sheetIdx = calc.activeSheetIndex
        val sheet = calc.activeSheet()

        val existingCell = sheet.cells[coord] ?: CalcCell()
        val updatedCell = existingCell.copy(rawValue = rawValue)
        val updatedCells = sheet.cells.toMutableMap().apply { put(coord, updatedCell) }
        val updatedSheet = sheet.copy(cells = updatedCells)

        val updatedSheets = calc.sheets.toMutableList().apply { set(sheetIdx, updatedSheet) }
        val updatedDoc = current.copy(
            calcContent = calc.copy(sheets = updatedSheets),
            updatedAt = System.currentTimeMillis()
        )

        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun toggleCellBold() {
        val coord = _uiState.value.selectedCellCoord
        val current = _uiState.value.document ?: return
        val calc = current.calcContent ?: return
        val sheet = calc.activeSheet()
        val cell = sheet.cells[coord] ?: CalcCell()

        val updatedCell = cell.copy(isBold = !cell.isBold)
        updateCell(coord, updatedCell)
    }

    fun toggleCellItalic() {
        val coord = _uiState.value.selectedCellCoord
        val current = _uiState.value.document ?: return
        val calc = current.calcContent ?: return
        val sheet = calc.activeSheet()
        val cell = sheet.cells[coord] ?: CalcCell()

        val updatedCell = cell.copy(isItalic = !cell.isItalic)
        updateCell(coord, updatedCell)
    }

    fun setCellFormat(format: CellFormat) {
        val coord = _uiState.value.selectedCellCoord
        val current = _uiState.value.document ?: return
        val calc = current.calcContent ?: return
        val sheet = calc.activeSheet()
        val cell = sheet.cells[coord] ?: CalcCell()

        val updatedCell = cell.copy(format = format)
        updateCell(coord, updatedCell)
    }

    fun setCellAlignment(alignment: BlockAlignment) {
        val coord = _uiState.value.selectedCellCoord
        val current = _uiState.value.document ?: return
        val calc = current.calcContent ?: return
        val sheet = calc.activeSheet()
        val cell = sheet.cells[coord] ?: CalcCell()

        val updatedCell = cell.copy(alignment = alignment)
        updateCell(coord, updatedCell)
    }

    fun setCellBgColor(hex: String?) {
        val coord = _uiState.value.selectedCellCoord
        val current = _uiState.value.document ?: return
        val calc = current.calcContent ?: return
        val sheet = calc.activeSheet()
        val cell = sheet.cells[coord] ?: CalcCell()

        val updatedCell = cell.copy(bgColorHex = hex)
        updateCell(coord, updatedCell)
    }

    private fun updateCell(coord: String, newCell: CalcCell) {
        val current = _uiState.value.document ?: return
        val calc = current.calcContent ?: return
        val sheetIdx = calc.activeSheetIndex
        val sheet = calc.activeSheet()

        val updatedCells = sheet.cells.toMutableMap().apply { put(coord, newCell) }
        val updatedSheet = sheet.copy(cells = updatedCells)
        val updatedSheets = calc.sheets.toMutableList().apply { set(sheetIdx, updatedSheet) }

        val updatedDoc = current.copy(
            calcContent = calc.copy(sheets = updatedSheets),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun addSheet() {
        val current = _uiState.value.document ?: return
        val calc = current.calcContent ?: return
        val newSheetNum = calc.sheets.size + 1
        val newSheet = CalcSheet(name = "Feuille $newSheetNum")

        val updatedSheets = calc.sheets + newSheet
        val updatedDoc = current.copy(
            calcContent = calc.copy(sheets = updatedSheets, activeSheetIndex = updatedSheets.size - 1),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, selectedCellCoord = "A1", formulaBarInput = "") }
        saveDocument(updatedDoc)
    }

    fun switchSheet(index: Int) {
        val current = _uiState.value.document ?: return
        val calc = current.calcContent ?: return
        if (index !in calc.sheets.indices) return

        val targetSheet = calc.sheets[index]
        val raw = targetSheet.cells["A1"]?.rawValue ?: ""
        val updatedDoc = current.copy(
            calcContent = calc.copy(activeSheetIndex = index)
        )
        _uiState.update {
            it.copy(document = updatedDoc, selectedCellCoord = "A1", formulaBarInput = raw)
        }
        saveDocument(updatedDoc)
    }

    fun openChartDialog(show: Boolean, chart: CalcChart? = null) {
        val sheet = _uiState.value.document?.calcContent?.activeSheet()
        val c = chart ?: sheet?.charts?.firstOrNull() ?: CalcChart(
            title = "Aperçu Graphique",
            labelColumn = "A",
            valueColumn = "C",
            startRow = 2,
            endRow = 6
        )
        _uiState.update { it.copy(showChartDialog = show, activeChart = c) }
    }

    fun openShareDialog(show: Boolean) {
        _uiState.update { it.copy(showShareDialog = show) }
    }

    fun getChartDataPoints(chart: CalcChart): List<ChartDataPoint> {
        val sheet = _uiState.value.document?.calcContent?.activeSheet() ?: return emptyList()
        val points = mutableListOf<ChartDataPoint>()
        for (r in chart.startRow..chart.endRow) {
            val labelCell = sheet.cells["${chart.labelColumn}$r"]
            val valueCell = sheet.cells["${chart.valueColumn}$r"]

            val label = labelCell?.rawValue ?: "Ligne $r"
            val evalVal = if (valueCell != null) {
                CalcFormulaEvaluator.evaluate("${chart.valueColumn}$r", valueCell, sheet.cells)
            } else "0"
            val numeric = CalcFormulaEvaluator.parseNumber(evalVal) ?: 0.0

            points.add(ChartDataPoint(label = label, value = numeric))
        }
        return points
    }

    private fun saveDocument(doc: OfficeDocument) {
        viewModelScope.launch {
            repository.saveDocument(doc)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
