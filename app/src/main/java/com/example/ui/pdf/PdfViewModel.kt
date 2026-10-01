package com.example.ui.pdf

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
import java.util.UUID

enum class ActivePdfTool {
    NONE, HIGHLIGHT, NOTE, PEN, SIGNATURE
}

data class PdfUiState(
    val document: OfficeDocument? = null,
    val activePageIndex: Int = 0,
    val activeTool: ActivePdfTool = ActivePdfTool.NONE,
    val selectedColorHex: String = "#FEF08A", // yellow
    val showSignaturePad: Boolean = false,
    val showShareDialog: Boolean = false,
    val showAddNoteDialog: Boolean = false,
    val searchQuery: String = "",
    val isSaved: Boolean = true
)

class PdfViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DocumentRepository
    private val _uiState = MutableStateFlow(PdfUiState())
    val uiState: StateFlow<PdfUiState> = _uiState.asStateFlow()

    init {
        val db = StarOfficeDatabase.getDatabase(application)
        repository = DocumentRepository(db.documentDao())
    }

    fun loadDocument(documentId: String) {
        viewModelScope.launch {
            val doc = repository.getDocumentById(documentId)
            _uiState.update {
                it.copy(document = doc, activePageIndex = doc?.pdfContent?.activePageIndex ?: 0)
            }
        }
    }

    fun setDocument(document: OfficeDocument) {
        _uiState.update {
            it.copy(document = document, activePageIndex = document.pdfContent?.activePageIndex ?: 0)
        }
    }

    fun updateTitle(newTitle: String) {
        val current = _uiState.value.document ?: return
        val updated = current.copy(title = newTitle, updatedAt = System.currentTimeMillis())
        _uiState.update { it.copy(document = updated, isSaved = false) }
        saveDocument(updated)
    }

    fun setActivePage(index: Int) {
        val current = _uiState.value.document ?: return
        val pdf = current.pdfContent ?: return
        if (index in pdf.pages.indices) {
            _uiState.update { it.copy(activePageIndex = index) }
        }
    }

    fun setActiveTool(tool: ActivePdfTool) {
        _uiState.update { it.copy(activeTool = if (it.activeTool == tool) ActivePdfTool.NONE else tool) }
    }

    fun setSelectedColorHex(hex: String) {
        _uiState.update { it.copy(selectedColorHex = hex) }
    }

    fun openSignaturePad(show: Boolean) {
        _uiState.update { it.copy(showSignaturePad = show) }
    }

    fun openShareDialog(show: Boolean) {
        _uiState.update { it.copy(showShareDialog = show) }
    }

    fun openAddNoteDialog(show: Boolean) {
        _uiState.update { it.copy(showAddNoteDialog = show) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateFormField(fieldId: String, newValue: String) {
        val current = _uiState.value.document ?: return
        val pdf = current.pdfContent ?: return
        val pageIdx = _uiState.value.activePageIndex
        val page = pdf.pages.getOrNull(pageIdx) ?: return

        val updatedFields = page.formFields.map { f ->
            if (f.id == fieldId) f.copy(value = newValue) else f
        }
        val updatedPage = page.copy(formFields = updatedFields)
        val updatedPages = pdf.pages.toMutableList().apply { set(pageIdx, updatedPage) }

        val updatedDoc = current.copy(
            pdfContent = pdf.copy(pages = updatedPages),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun addAnnotation(annotation: PdfAnnotation) {
        val current = _uiState.value.document ?: return
        val pdf = current.pdfContent ?: return
        val pageIdx = _uiState.value.activePageIndex
        val page = pdf.pages.getOrNull(pageIdx) ?: return

        val updatedAnnotations = page.annotations + annotation
        val updatedPage = page.copy(annotations = updatedAnnotations)
        val updatedPages = pdf.pages.toMutableList().apply { set(pageIdx, updatedPage) }

        val updatedDoc = current.copy(
            pdfContent = pdf.copy(pages = updatedPages),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun applySignature(signerName: String, points: List<DrawPoint>) {
        val current = _uiState.value.document ?: return
        val pdf = current.pdfContent ?: return

        val updatedDoc = current.copy(
            pdfContent = pdf.copy(
                isSigned = true,
                signerName = signerName,
                signaturePath = points
            ),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, showSignaturePad = false, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun addPage() {
        val current = _uiState.value.document ?: return
        val pdf = current.pdfContent ?: return
        val newPageNum = pdf.pages.size + 1
        val newPage = PdfPageData(
            pageNumber = newPageNum,
            headerTitle = "Page $newPageNum",
            sections = listOf("Nouvelle page ajoutée au document PDF.", "Vous pouvez ajouter vos annotations et champs ici.")
        )
        val updatedPages = pdf.pages + newPage
        val updatedDoc = current.copy(
            pdfContent = pdf.copy(pages = updatedPages, activePageIndex = updatedPages.size - 1),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, activePageIndex = updatedPages.size - 1, isSaved = false) }
        saveDocument(updatedDoc)
    }

    private fun saveDocument(doc: OfficeDocument) {
        viewModelScope.launch {
            repository.saveDocument(doc)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
