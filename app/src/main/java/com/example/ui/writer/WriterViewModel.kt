package com.example.ui.writer

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

data class WriterUiState(
    val document: OfficeDocument? = null,
    val isReadingMode: Boolean = false,
    val paperTheme: String = "white", // "white", "sepia", "dark"
    val showFindReplace: Boolean = false,
    val searchQuery: String = "",
    val replaceQuery: String = "",
    val showInsertTableDialog: Boolean = false,
    val showShareDialog: Boolean = false,
    val isSaved: Boolean = true
)

class WriterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DocumentRepository
    private val _uiState = MutableStateFlow(WriterUiState())
    val uiState: StateFlow<WriterUiState> = _uiState.asStateFlow()

    init {
        val db = StarOfficeDatabase.getDatabase(application)
        repository = DocumentRepository(db.documentDao())
    }

    fun loadDocument(documentId: String) {
        viewModelScope.launch {
            val doc = repository.getDocumentById(documentId)
            _uiState.update { it.copy(document = doc) }
        }
    }

    fun setDocument(document: OfficeDocument) {
        _uiState.update { it.copy(document = document) }
    }

    fun updateTitle(newTitle: String) {
        val current = _uiState.value.document ?: return
        val updated = current.copy(title = newTitle, updatedAt = System.currentTimeMillis())
        _uiState.update { it.copy(document = updated, isSaved = false) }
        saveDocument(updated)
    }

    fun toggleReadingMode() {
        _uiState.update { it.copy(isReadingMode = !it.isReadingMode) }
    }

    fun setPaperTheme(theme: String) {
        _uiState.update { it.copy(paperTheme = theme) }
    }

    fun toggleFindReplace() {
        _uiState.update { it.copy(showFindReplace = !it.showFindReplace) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setReplaceQuery(query: String) {
        _uiState.update { it.copy(replaceQuery = query) }
    }

    fun openInsertTableDialog(show: Boolean) {
        _uiState.update { it.copy(showInsertTableDialog = show) }
    }

    fun openShareDialog(show: Boolean) {
        _uiState.update { it.copy(showShareDialog = show) }
    }

    fun updateBlockText(blockId: String, newText: String) {
        val current = _uiState.value.document ?: return
        val content = current.writerContent ?: WriterContent()
        val updatedBlocks = content.blocks.map { b ->
            if (b.id == blockId) b.copy(text = newText) else b
        }
        val updatedDoc = current.copy(
            writerContent = content.copy(blocks = updatedBlocks),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun updateBlockType(blockId: String, newType: BlockType) {
        val current = _uiState.value.document ?: return
        val content = current.writerContent ?: WriterContent()
        val updatedBlocks = content.blocks.map { b ->
            if (b.id == blockId) b.copy(type = newType) else b
        }
        val updatedDoc = current.copy(
            writerContent = content.copy(blocks = updatedBlocks),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun toggleBlockBold(blockId: String) {
        val current = _uiState.value.document ?: return
        val content = current.writerContent ?: WriterContent()
        val updatedBlocks = content.blocks.map { b ->
            if (b.id == blockId) b.copy(isBold = !b.isBold) else b
        }
        val updatedDoc = current.copy(
            writerContent = content.copy(blocks = updatedBlocks),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun toggleBlockItalic(blockId: String) {
        val current = _uiState.value.document ?: return
        val content = current.writerContent ?: WriterContent()
        val updatedBlocks = content.blocks.map { b ->
            if (b.id == blockId) b.copy(isItalic = !b.isItalic) else b
        }
        val updatedDoc = current.copy(
            writerContent = content.copy(blocks = updatedBlocks),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun toggleBlockUnderline(blockId: String) {
        val current = _uiState.value.document ?: return
        val content = current.writerContent ?: WriterContent()
        val updatedBlocks = content.blocks.map { b ->
            if (b.id == blockId) b.copy(isUnderline = !b.isUnderline) else b
        }
        val updatedDoc = current.copy(
            writerContent = content.copy(blocks = updatedBlocks),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun setBlockAlignment(blockId: String, alignment: BlockAlignment) {
        val current = _uiState.value.document ?: return
        val content = current.writerContent ?: WriterContent()
        val updatedBlocks = content.blocks.map { b ->
            if (b.id == blockId) b.copy(alignment = alignment) else b
        }
        val updatedDoc = current.copy(
            writerContent = content.copy(blocks = updatedBlocks),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun addBlockAfter(afterBlockId: String?, type: BlockType = BlockType.PARAGRAPH) {
        val current = _uiState.value.document ?: return
        val content = current.writerContent ?: WriterContent()
        val newBlock = WriterBlock(type = type)

        val updatedList = mutableListOf<WriterBlock>()
        if (afterBlockId == null || content.blocks.isEmpty()) {
            updatedList.addAll(content.blocks)
            updatedList.add(newBlock)
        } else {
            content.blocks.forEach { b ->
                updatedList.add(b)
                if (b.id == afterBlockId) {
                    updatedList.add(newBlock)
                }
            }
        }

        val updatedDoc = current.copy(
            writerContent = content.copy(blocks = updatedList),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun removeBlock(blockId: String) {
        val current = _uiState.value.document ?: return
        val content = current.writerContent ?: WriterContent()
        if (content.blocks.size <= 1) return // keep at least one block

        val updatedBlocks = content.blocks.filter { it.id != blockId }
        val updatedDoc = current.copy(
            writerContent = content.copy(blocks = updatedBlocks),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun insertTable(rows: Int, cols: Int) {
        val current = _uiState.value.document ?: return
        val content = current.writerContent ?: WriterContent()
        val newTable = WriterTable(
            rows = rows,
            cols = cols,
            cells = List(rows) { r -> List(cols) { c -> if (r == 0) "Entête ${c + 1}" else "" } }
        )
        val updatedDoc = current.copy(
            writerContent = content.copy(table = newTable),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, showInsertTableDialog = false, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun updateTableCell(row: Int, col: Int, text: String) {
        val current = _uiState.value.document ?: return
        val content = current.writerContent ?: return
        val table = content.table ?: return

        val newCells = table.cells.mapIndexed { r, rowList ->
            if (r == row) {
                rowList.mapIndexed { c, cellText -> if (c == col) text else cellText }
            } else rowList
        }

        val updatedTable = table.copy(cells = newCells)
        val updatedDoc = current.copy(
            writerContent = content.copy(table = updatedTable),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun performReplaceAll() {
        val current = _uiState.value.document ?: return
        val search = _uiState.value.searchQuery
        val replace = _uiState.value.replaceQuery
        if (search.isBlank()) return

        val content = current.writerContent ?: return
        val updatedBlocks = content.blocks.map { b ->
            b.copy(text = b.text.replace(search, replace, ignoreCase = true))
        }

        val updatedTable = content.table?.let { t ->
            t.copy(cells = t.cells.map { row -> row.map { it.replace(search, replace, ignoreCase = true) } })
        }

        val updatedDoc = current.copy(
            writerContent = content.copy(blocks = updatedBlocks, table = updatedTable),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    private fun saveDocument(doc: OfficeDocument) {
        viewModelScope.launch {
            repository.saveDocument(doc)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
