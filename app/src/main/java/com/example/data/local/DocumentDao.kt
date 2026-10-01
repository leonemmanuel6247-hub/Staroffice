package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents WHERE isTrash = 0 ORDER BY updatedAt DESC")
    fun getAllActiveDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrash = 0 AND isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrash = 0 AND type = :docType ORDER BY updatedAt DESC")
    fun getDocumentsByType(docType: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrash = 1 ORDER BY updatedAt DESC")
    fun getTrashDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun getDocumentByIdFlow(id: String): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Query("SELECT * FROM documents WHERE isTrash = 0 AND (title LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') ORDER BY updatedAt DESC")
    fun searchDocuments(query: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documents: List<DocumentEntity>)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Query("UPDATE documents SET isTrash = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun moveToTrash(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET isTrash = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun restoreFromTrash(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: String, isFav: Boolean)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentPermanently(id: String)

    @Query("DELETE FROM documents WHERE isTrash = 1")
    suspend fun emptyTrash()

    @Query("SELECT COUNT(*) FROM documents")
    suspend fun getDocumentCount(): Int
}
