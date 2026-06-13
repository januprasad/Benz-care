package com.toyota.demo.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChatSession::class, ChatMessage::class, ScanHistory::class], version = 2, exportSchema = false)
abstract class ToyotaDatabase : RoomDatabase() {
    abstract fun toyotaDao(): ToyotaDao
    abstract fun scanDao(): ScanDao

    companion object {
        @Volatile
        private var INSTANCE: ToyotaDatabase? = null

        fun getDatabase(context: Context): ToyotaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToyotaDatabase::class.java,
                    "toyota_care_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
