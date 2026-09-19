package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val phoneNumber: String? = null,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_records")
data class CallRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val displayName: String?,
    val callType: String, // INCOMING, OUTGOING, MISSED, BLOCKED
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val simSlot: Int = 0, // 0 for SIM 1, 1 for SIM 2
    val isSpam: Boolean = false,
    val spamCategory: String? = null
)

@Entity(tableName = "local_contacts")
data class ContactItemEntity(
    @PrimaryKey val phoneNumber: String,
    val displayName: String,
    val avatarUri: String? = null,
    val isFavorite: Boolean = false,
    val isBlocked: Boolean = false,
    val category: String? = "Personal"
)

@Entity(tableName = "voice_recordings")
data class RecordingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String,
    val durationSeconds: Int = 0,
    val phoneNumber: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
