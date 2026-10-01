package com.example.data.repository

import com.example.data.local.DocumentDao
import com.example.data.local.DocumentEntity
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class DocumentRepository(private val documentDao: DocumentDao) {

    suspend fun checkAndSeedDefaults() {
        if (documentDao.getDocumentCount() == 0) {
            val defaults = DefaultTemplates.getAllDefaultDocuments()
            val entities = defaults.map { it.toEntity() }
            documentDao.insertAll(entities)
        }
    }

    fun getAllActiveDocuments(): Flow<List<OfficeDocument>> {
        return documentDao.getAllActiveDocuments().map { list -> list.map { it.toDomain() } }
    }

    fun getFavoriteDocuments(): Flow<List<OfficeDocument>> {
        return documentDao.getFavoriteDocuments().map { list -> list.map { it.toDomain() } }
    }

    fun getDocumentsByType(type: DocumentType): Flow<List<OfficeDocument>> {
        return documentDao.getDocumentsByType(type.name).map { list -> list.map { it.toDomain() } }
    }

    fun getTrashDocuments(): Flow<List<OfficeDocument>> {
        return documentDao.getTrashDocuments().map { list -> list.map { it.toDomain() } }
    }

    fun getDocumentByIdFlow(id: String): Flow<OfficeDocument?> {
        return documentDao.getDocumentByIdFlow(id).map { it?.toDomain() }
    }

    suspend fun getDocumentById(id: String): OfficeDocument? {
        return documentDao.getDocumentById(id)?.toDomain()
    }

    fun searchDocuments(query: String): Flow<List<OfficeDocument>> {
        return documentDao.searchDocuments(query).map { list -> list.map { it.toDomain() } }
    }

    suspend fun saveDocument(document: OfficeDocument) {
        val updated = document.copy(updatedAt = System.currentTimeMillis())
        documentDao.insertDocument(updated.toEntity())
    }

    suspend fun createNewDocument(
        title: String,
        type: DocumentType,
        extension: String? = null
    ): OfficeDocument {
        val now = System.currentTimeMillis()
        val docId = UUID.randomUUID().toString()
        val ext = extension ?: type.defaultExtension

        val initialDoc = when (type) {
            DocumentType.WRITER -> OfficeDocument(
                id = docId,
                title = title,
                type = type,
                extension = ext,
                createdAt = now,
                updatedAt = now,
                sizeBytes = 4096,
                writerContent = WriterContent(
                    blocks = listOf(
                        WriterBlock(
                            type = BlockType.HEADING_1,
                            text = title,
                            isBold = true,
                            textColorHex = "#1E3A8A"
                        ),
                        WriterBlock(
                            type = BlockType.PARAGRAPH,
                            text = "Commencez à rédiger votre document ici..."
                        )
                    )
                )
            )
            DocumentType.CALC -> {
                val initialCells = mutableMapOf<String, CalcCell>()
                initialCells["A1"] = CalcCell(rawValue = "Article", isBold = true, bgColorHex = "#D1FAE5")
                initialCells["B1"] = CalcCell(rawValue = "Quantité", isBold = true, bgColorHex = "#D1FAE5")
                initialCells["C1"] = CalcCell(rawValue = "Prix", isBold = true, bgColorHex = "#D1FAE5")
                initialCells["D1"] = CalcCell(rawValue = "Total", isBold = true, bgColorHex = "#D1FAE5")
                OfficeDocument(
                    id = docId,
                    title = title,
                    type = type,
                    extension = ext,
                    createdAt = now,
                    updatedAt = now,
                    sizeBytes = 6144,
                    calcContent = CalcContent(
                        sheets = listOf(
                            CalcSheet(name = "Feuille 1", cells = initialCells)
                        )
                    )
                )
            }
            DocumentType.IMPRESS -> OfficeDocument(
                id = docId,
                title = title,
                type = type,
                extension = ext,
                createdAt = now,
                updatedAt = now,
                sizeBytes = 8192,
                impressContent = ImpressContent(
                    slides = listOf(
                        ImpressSlide(
                            layout = SlideLayout.TITLE,
                            title = title,
                            subtitle = "Sous-titre de la présentation"
                        ),
                        ImpressSlide(
                            layout = SlideLayout.TITLE_CONTENT,
                            title = "Premier Sujet",
                            bullets = listOf("Point clé numéro 1", "Point clé numéro 2", "Point clé numéro 3")
                        )
                    )
                )
            )
            DocumentType.PDF -> OfficeDocument(
                id = docId,
                title = title,
                type = type,
                extension = ext,
                createdAt = now,
                updatedAt = now,
                sizeBytes = 12288,
                pdfContent = PdfContent(
                    pages = listOf(
                        PdfPageData(
                            pageNumber = 1,
                            headerTitle = title,
                            sections = listOf(
                                "Document officiel généré par StarOffice PDF.",
                                "Ce document est interactif : vous pouvez surligner du texte, ajouter des annotations manuscrites, remplir les champs de formulaires et apposer votre signature numérique en bas de page."
                            ),
                            formFields = listOf(
                                PdfFormField(id = "field_name", label = "Nom complet", placeholder = "Votre nom"),
                                PdfFormField(id = "field_comment", label = "Commentaires", placeholder = "Observations éventuelles")
                            )
                        )
                    )
                )
            )
        }

        documentDao.insertDocument(initialDoc.toEntity())
        return initialDoc
    }

    suspend fun duplicateDocument(id: String): OfficeDocument? {
        val original = getDocumentById(id) ?: return null
        val now = System.currentTimeMillis()
        val copy = original.copy(
            id = UUID.randomUUID().toString(),
            title = "${original.title} (Copie)",
            createdAt = now,
            updatedAt = now,
            isFavorite = false
        )
        documentDao.insertDocument(copy.toEntity())
        return copy
    }

    suspend fun toggleFavorite(id: String) {
        val doc = getDocumentById(id) ?: return
        documentDao.setFavorite(id, !doc.isFavorite)
    }

    suspend fun moveToTrash(id: String) {
        documentDao.moveToTrash(id)
    }

    suspend fun restoreFromTrash(id: String) {
        documentDao.restoreFromTrash(id)
    }

    suspend fun deletePermanently(id: String) {
        documentDao.deleteDocumentPermanently(id)
    }

    suspend fun emptyTrash() {
        documentDao.emptyTrash()
    }

    // --- Entity <-> Domain Mappers ---
    private fun DocumentEntity.toDomain(): OfficeDocument {
        val docType = try {
            DocumentType.valueOf(type)
        } catch (e: Exception) {
            DocumentType.WRITER
        }

        val writerContent = if (docType == DocumentType.WRITER) DocumentSerializer.deserializeWriter(contentJson) else null
        val calcContent = if (docType == DocumentType.CALC) DocumentSerializer.deserializeCalc(contentJson) else null
        val impressContent = if (docType == DocumentType.IMPRESS) DocumentSerializer.deserializeImpress(contentJson) else null
        val pdfContent = if (docType == DocumentType.PDF) DocumentSerializer.deserializePdf(contentJson) else null

        val tagList = if (tags.isNotBlank()) tags.split(",").map { it.trim() } else emptyList()

        return OfficeDocument(
            id = id,
            title = title,
            type = docType,
            extension = extension,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isFavorite = isFavorite,
            isTrash = isTrash,
            sizeBytes = sizeBytes,
            tags = tagList,
            writerContent = writerContent,
            calcContent = calcContent,
            impressContent = impressContent,
            pdfContent = pdfContent
        )
    }

    private fun OfficeDocument.toEntity(): DocumentEntity {
        val json = when (type) {
            DocumentType.WRITER -> DocumentSerializer.serializeWriter(writerContent ?: WriterContent())
            DocumentType.CALC -> DocumentSerializer.serializeCalc(calcContent ?: CalcContent())
            DocumentType.IMPRESS -> DocumentSerializer.serializeImpress(impressContent ?: ImpressContent())
            DocumentType.PDF -> DocumentSerializer.serializePdf(pdfContent ?: PdfContent())
        }

        return DocumentEntity(
            id = id,
            title = title,
            type = type.name,
            extension = extension,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isFavorite = isFavorite,
            isTrash = isTrash,
            sizeBytes = sizeBytes,
            contentJson = json,
            tags = tags.joinToString(",")
        )
    }
}
