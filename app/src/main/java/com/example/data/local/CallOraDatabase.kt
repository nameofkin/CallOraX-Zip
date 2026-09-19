package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        NoteEntity::class,
        CallRecordEntity::class,
        ContactItemEntity::class,
        RecordingItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CallOraDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun callRecordDao(): CallRecordDao
    abstract fun contactItemDao(): ContactItemDao
    abstract fun recordingDao(): RecordingDao

    companion object {
        @Volatile
        private var INSTANCE: CallOraDatabase? = null

        fun getDatabase(context: Context): CallOraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CallOraDatabase::class.java,
                    "callora_database.db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
