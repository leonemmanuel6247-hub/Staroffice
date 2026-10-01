package com.example.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StarOfficeDatabase
import com.example.data.model.DocumentType
import com.example.data.model.OfficeDocument
import com.example.data.repository.DefaultTemplates
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class HomeFilter {
    ALL, WRITER, CALC, IMPRESS, PDF, FAVORITES, TRASH
}

enum class SortOrder {
    DATE_DESC, NAME_ASC, SIZE_DESC
}

data class HomeUiState(
    val documents: List<OfficeDocument> = emptyList(),
    val filter: HomeFilter = HomeFilter.ALL,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    val isLoading: Boolean = true,
    val isPinLocked: Boolean = false,
    val isPinProtectionEnabled: Boolean = false,
    val userPin: String = "1234",
    val selectedDocForActions: OfficeDocument? = null,
    val showNewDocDialog: Boolean = false,
    val showShareDialogForDoc: OfficeDocument? = null,
    val selectedDocForRename: OfficeDocument? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DocumentRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val db = StarOfficeDatabase.getDatabase(application)
        repository = DocumentRepository(db.documentDao())

        viewModelScope.launch {
            repository.checkAndSeedDefaults()
            loadDocuments()
        }
    }

    private fun loadDocuments() {
        viewModelScope.launch {
            combine(
                repository.getAllActiveDocuments(),
                repository.getTrashDocuments(),
                _uiState.map { Triple(it.filter, it.searchQuery, it.sortOrder) }.distinctUntilChanged()
            ) { activeDocs, trashDocs, (filter, query, sort) ->
                val sourceList = if (filter == HomeFilter.TRASH) trashDocs else activeDocs

                val filtered = sourceList.filter { doc ->
                    val matchesType = when (filter) {
                        HomeFilter.ALL -> true
                        HomeFilter.WRITER -> doc.type == DocumentType.WRITER
                        HomeFilter.CALC -> doc.type == DocumentType.CALC
                        HomeFilter.IMPRESS -> doc.type == DocumentType.IMPRESS
                        HomeFilter.PDF -> doc.type == DocumentType.PDF
                        HomeFilter.FAVORITES -> doc.isFavorite
                        HomeFilter.TRASH -> true
                    }
                    val matchesQuery = query.isBlank() ||
                            doc.title.contains(query, ignoreCase = true) ||
                            doc.tags.any { it.contains(query, ignoreCase = true) }

                    matchesType && matchesQuery
                }

                when (sort) {
                    SortOrder.DATE_DESC -> filtered.sortedByDescending { it.updatedAt }
                    SortOrder.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
                    SortOrder.SIZE_DESC -> filtered.sortedByDescending { it.sizeBytes }
                }
            }.collect { sortedDocs ->
                _uiState.update { it.copy(documents = sortedDocs, isLoading = false) }
            }
        }
    }

    fun setFilter(filter: HomeFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
    }

    fun openNewDocDialog(show: Boolean) {
        _uiState.update { it.copy(showNewDocDialog = show) }
    }

    fun openActionsForDoc(doc: OfficeDocument?) {
        _uiState.update { it.copy(selectedDocForActions = doc) }
    }

    fun openShareDialogForDoc(doc: OfficeDocument?) {
        _uiState.update { it.copy(showShareDialogForDoc = doc) }
    }

    fun openRenameDialog(doc: OfficeDocument?) {
        _uiState.update { it.copy(selectedDocForRename = doc) }
    }

    fun createDocument(
        title: String,
        type: DocumentType,
        extension: String,
        onCreated: (OfficeDocument) -> Unit
    ) {
        viewModelScope.launch {
            val doc = repository.createNewDocument(title, type, extension)
            _uiState.update { it.copy(showNewDocDialog = false) }
            onCreated(doc)
        }
    }

    fun createFromTemplate(templateName: String, onCreated: (OfficeDocument) -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val templateDoc = when (templateName) {
                "report" -> DefaultTemplates.createStrategicReport(now).copy(id = java.util.UUID.randomUUID().toString(), title = "Nouveau Rapport Stratégique")
                "cv" -> DefaultTemplates.createModernResume(now).copy(id = java.util.UUID.randomUUID().toString(), title = "Mon Nouveau CV")
                "budget" -> DefaultTemplates.createPersonalBudget(now).copy(id = java.util.UUID.randomUUID().toString(), title = "Mon Budget Personnel")
                "invoice" -> DefaultTemplates.createCommercialInvoice(now).copy(id = java.util.UUID.randomUUID().toString(), title = "Nouvelle Facture")
                "pitch" -> DefaultTemplates.createStartupPitch(now).copy(id = java.util.UUID.randomUUID().toString(), title = "Nouvelle Présentation Pitch")
                "contract" -> DefaultTemplates.createServiceContractPdf(now).copy(id = java.util.UUID.randomUUID().toString(), title = "Nouveau Contrat PDF")
                else -> DefaultTemplates.createStrategicReport(now).copy(id = java.util.UUID.randomUUID().toString())
            }
            repository.saveDocument(templateDoc)
            onCreated(templateDoc)
        }
    }

    fun renameDocument(doc: OfficeDocument, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.saveDocument(doc.copy(title = newTitle.trim()))
            _uiState.update { it.copy(selectedDocForRename = null) }
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }

    fun moveToTrash(id: String) {
        viewModelScope.launch {
            repository.moveToTrash(id)
            _uiState.update { it.copy(selectedDocForActions = null) }
        }
    }

    fun restoreFromTrash(id: String) {
        viewModelScope.launch {
            repository.restoreFromTrash(id)
        }
    }

    fun deletePermanently(id: String) {
        viewModelScope.launch {
            repository.deletePermanently(id)
            _uiState.update { it.copy(selectedDocForActions = null) }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }

    fun duplicateDocument(id: String, onDuplicated: (OfficeDocument) -> Unit) {
        viewModelScope.launch {
            val copy = repository.duplicateDocument(id)
            _uiState.update { it.copy(selectedDocForActions = null) }
            if (copy != null) onDuplicated(copy)
        }
    }

    fun unlockApp() {
        _uiState.update { it.copy(isPinLocked = false) }
    }

    fun setPinProtection(enabled: Boolean, pin: String) {
        _uiState.update { it.copy(isPinProtectionEnabled = enabled, userPin = pin) }
    }
}
