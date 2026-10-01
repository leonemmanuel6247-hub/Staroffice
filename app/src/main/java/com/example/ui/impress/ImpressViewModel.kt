package com.example.ui.impress

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

data class ImpressUiState(
    val document: OfficeDocument? = null,
    val activeSlideIndex: Int = 0,
    val isSlideshowActive: Boolean = false,
    val showAddSlideDialog: Boolean = false,
    val showThemeDialog: Boolean = false,
    val showShareDialog: Boolean = false,
    val isSaved: Boolean = true
)

class ImpressViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DocumentRepository
    private val _uiState = MutableStateFlow(ImpressUiState())
    val uiState: StateFlow<ImpressUiState> = _uiState.asStateFlow()

    init {
        val db = StarOfficeDatabase.getDatabase(application)
        repository = DocumentRepository(db.documentDao())
    }

    fun loadDocument(documentId: String) {
        viewModelScope.launch {
            val doc = repository.getDocumentById(documentId)
            _uiState.update {
                it.copy(document = doc, activeSlideIndex = doc?.impressContent?.activeSlideIndex ?: 0)
            }
        }
    }

    fun setDocument(document: OfficeDocument) {
        _uiState.update {
            it.copy(document = document, activeSlideIndex = document.impressContent?.activeSlideIndex ?: 0)
        }
    }

    fun updateTitle(newTitle: String) {
        val current = _uiState.value.document ?: return
        val updated = current.copy(title = newTitle, updatedAt = System.currentTimeMillis())
        _uiState.update { it.copy(document = updated, isSaved = false) }
        saveDocument(updated)
    }

    fun setActiveSlide(index: Int) {
        val current = _uiState.value.document ?: return
        val impress = current.impressContent ?: return
        if (index in impress.slides.indices) {
            _uiState.update { it.copy(activeSlideIndex = index) }
        }
    }

    fun updateActiveSlide(transform: (ImpressSlide) -> ImpressSlide) {
        val current = _uiState.value.document ?: return
        val impress = current.impressContent ?: return
        val index = _uiState.value.activeSlideIndex
        if (index !in impress.slides.indices) return

        val currentSlide = impress.slides[index]
        val updatedSlide = transform(currentSlide)
        val updatedSlides = impress.slides.toMutableList().apply { set(index, updatedSlide) }

        val updatedDoc = current.copy(
            impressContent = impress.copy(slides = updatedSlides),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun addSlide(layout: SlideLayout) {
        val current = _uiState.value.document ?: return
        val impress = current.impressContent ?: return
        val newSlide = ImpressSlide(
            id = UUID.randomUUID().toString(),
            layout = layout,
            title = when (layout) {
                SlideLayout.TITLE -> "Titre de la présentation"
                SlideLayout.TITLE_CONTENT -> "Nouveau Sujet"
                SlideLayout.TWO_COLUMNS -> "Comparatif & Analyse"
                SlideLayout.BIG_STAT -> "Chiffre Clé"
                SlideLayout.BLANK -> ""
            },
            subtitle = if (layout == SlideLayout.TITLE) "Sous-titre" else "",
            bullets = if (layout == SlideLayout.TITLE_CONTENT) listOf("Premier point", "Deuxième point") else emptyList(),
            statValue = if (layout == SlideLayout.BIG_STAT) "+42%" else "",
            statLabel = if (layout == SlideLayout.BIG_STAT) "Croissance annuelle" else "",
            leftColumnTitle = if (layout == SlideLayout.TWO_COLUMNS) "Option A" else "",
            rightColumnTitle = if (layout == SlideLayout.TWO_COLUMNS) "Option B" else ""
        )

        val updatedSlides = impress.slides + newSlide
        val newIndex = updatedSlides.size - 1
        val updatedDoc = current.copy(
            impressContent = impress.copy(slides = updatedSlides, activeSlideIndex = newIndex),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update {
            it.copy(document = updatedDoc, activeSlideIndex = newIndex, showAddSlideDialog = false, isSaved = false)
        }
        saveDocument(updatedDoc)
    }

    fun duplicateActiveSlide() {
        val current = _uiState.value.document ?: return
        val impress = current.impressContent ?: return
        val index = _uiState.value.activeSlideIndex
        if (index !in impress.slides.indices) return

        val original = impress.slides[index]
        val copy = original.copy(
            id = UUID.randomUUID().toString(),
            title = "${original.title} (Copie)"
        )
        val updatedSlides = impress.slides.toMutableList().apply { add(index + 1, copy) }
        val newIndex = index + 1

        val updatedDoc = current.copy(
            impressContent = impress.copy(slides = updatedSlides, activeSlideIndex = newIndex),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, activeSlideIndex = newIndex, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun deleteActiveSlide() {
        val current = _uiState.value.document ?: return
        val impress = current.impressContent ?: return
        if (impress.slides.size <= 1) return // Keep at least one slide

        val index = _uiState.value.activeSlideIndex
        val updatedSlides = impress.slides.toMutableList().apply { removeAt(index) }
        val newIndex = index.coerceAtMost(updatedSlides.size - 1)

        val updatedDoc = current.copy(
            impressContent = impress.copy(slides = updatedSlides, activeSlideIndex = newIndex),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, activeSlideIndex = newIndex, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun moveSlide(direction: Int) { // -1 up, +1 down
        val current = _uiState.value.document ?: return
        val impress = current.impressContent ?: return
        val index = _uiState.value.activeSlideIndex
        val targetIndex = index + direction
        if (targetIndex !in impress.slides.indices) return

        val updatedSlides = impress.slides.toMutableList()
        val item = updatedSlides.removeAt(index)
        updatedSlides.add(targetIndex, item)

        val updatedDoc = current.copy(
            impressContent = impress.copy(slides = updatedSlides, activeSlideIndex = targetIndex),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, activeSlideIndex = targetIndex, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun setTheme(theme: PresentationTheme) {
        val current = _uiState.value.document ?: return
        val impress = current.impressContent ?: return
        val updatedDoc = current.copy(
            impressContent = impress.copy(theme = theme),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(document = updatedDoc, showThemeDialog = false, isSaved = false) }
        saveDocument(updatedDoc)
    }

    fun setSlideshowActive(active: Boolean) {
        _uiState.update { it.copy(isSlideshowActive = active) }
    }

    fun openAddSlideDialog(show: Boolean) {
        _uiState.update { it.copy(showAddSlideDialog = show) }
    }

    fun openThemeDialog(show: Boolean) {
        _uiState.update { it.copy(showThemeDialog = show) }
    }

    fun openShareDialog(show: Boolean) {
        _uiState.update { it.copy(showShareDialog = show) }
    }

    private fun saveDocument(doc: OfficeDocument) {
        viewModelScope.launch {
            repository.saveDocument(doc)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
