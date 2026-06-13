package com.toyota.demo.data.repository

import com.toyota.demo.data.db.ToyotaDao
import com.toyota.demo.data.db.ChatMessage
import com.toyota.demo.data.db.ChatSession
import kotlinx.coroutines.flow.Flow

class ToyotaRepository(private val toyotaDao: ToyotaDao) {
    val allSessions: Flow<List<ChatSession>> = toyotaDao.getAllSessions()

    suspend fun createSession(title: String): Long {
        return toyotaDao.insertSession(ChatSession(title = title))
    }

    suspend fun updateSessionTitle(sessionId: Long, title: String) {
        toyotaDao.updateSessionTitle(sessionId, title)
    }

    suspend fun deleteSession(sessionId: Long) {
        toyotaDao.deleteMessagesForSession(sessionId)
        toyotaDao.deleteSession(sessionId)
    }

    fun getMessages(sessionId: Long): Flow<List<ChatMessage>> {
        return toyotaDao.getMessagesForSession(sessionId)
    }

    suspend fun addMessage(message: ChatMessage): Long {
        return toyotaDao.insertMessage(message)
    }
}
