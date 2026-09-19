package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY isPinned DESC, updatedAt DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE phoneNumber = :phoneNumber ORDER BY updatedAt DESC")
    fun getNotesForNumber(phoneNumber: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface CallRecordDao {
    @Query("SELECT * FROM call_records ORDER BY timestamp DESC LIMIT 100")
    fun getAllCallRecords(): Flow<List<CallRecordEntity>>

    @Query("SELECT * FROM call_records WHERE callType = :type ORDER BY timestamp DESC")
    fun getRecordsByType(type: String): Flow<List<CallRecordEntity>>

    @Query("SELECT * FROM call_records WHERE isSpam = 1 OR callType = 'BLOCKED' ORDER BY timestamp DESC")
    fun getSpamAndBlockedRecords(): Flow<List<CallRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CallRecordEntity): Long

    @Query("DELETE FROM call_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM call_records")
    suspend fun clearAllRecords()
}

@Dao
interface ContactItemDao {
    @Query("SELECT * FROM local_contacts ORDER BY displayName ASC")
    fun getAllContacts(): Flow<List<ContactItemEntity>>

    @Query("SELECT * FROM local_contacts WHERE isFavorite = 1 ORDER BY displayName ASC")
    fun getFavoriteContacts(): Flow<List<ContactItemEntity>>

    @Query("SELECT * FROM local_contacts WHERE isBlocked = 1")
    fun getBlockedContacts(): Flow<List<ContactItemEntity>>

    @Query("SELECT * FROM local_contacts WHERE displayName LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' ORDER BY displayName ASC")
    fun searchContacts(query: String): Flow<List<ContactItemEntity>>

    @Query("SELECT * FROM local_contacts WHERE phoneNumber = :number LIMIT 1")
    suspend fun getContactByNumber(number: String): ContactItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(contact: ContactItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<ContactItemEntity>)

    @Query("UPDATE local_contacts SET isFavorite = :isFav WHERE phoneNumber = :number")
    suspend fun setFavorite(number: String, isFav: Boolean)

    @Query("UPDATE local_contacts SET isBlocked = :isBlocked WHERE phoneNumber = :number")
    suspend fun setBlocked(number: String, isBlocked: Boolean)

    @Delete
    suspend fun deleteContact(contact: ContactItemEntity)
}

@Dao
interface RecordingDao {
    @Query("SELECT * FROM voice_recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<RecordingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: RecordingItemEntity): Long

    @Delete
    suspend fun deleteRecording(recording: RecordingItemEntity)
}
