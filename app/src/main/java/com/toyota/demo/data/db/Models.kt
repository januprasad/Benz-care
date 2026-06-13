package com.toyota.demo.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val isUser: Boolean,
    val messageText: String,
    val imageUri: String? = null, // Stores raw base64 data or standard relative file storage
    val estimateJson: String? = null, // Stored serialized RepairEstimate JSON response metadata
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val errorCode: String,
    val explanation: String,
    val language: String,
    val timestamp: Long = System.currentTimeMillis()
)
