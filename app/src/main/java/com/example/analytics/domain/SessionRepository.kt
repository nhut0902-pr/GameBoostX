package com.example.analytics.domain

import com.example.analytics.data.SessionDao
import com.example.analytics.data.SessionRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SessionRepository(private val sessionDao: SessionDao) {
    val allSessions: Flow<List<SessionRecord>> = sessionDao.getAllSessions()

    suspend fun insertSession(session: SessionRecord): Long = withContext(Dispatchers.IO) {
        sessionDao.insertSession(session)
    }

    suspend fun deleteSession(id: Int) = withContext(Dispatchers.IO) {
        sessionDao.deleteSessionById(id)
    }

    suspend fun clearSessions() = withContext(Dispatchers.IO) {
        sessionDao.clearAll()
    }
}
