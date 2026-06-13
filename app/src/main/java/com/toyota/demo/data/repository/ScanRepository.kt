package com.toyota.demo.data.repository

import com.toyota.demo.data.db.ScanDao
import com.toyota.demo.data.db.ScanHistory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepository @Inject constructor(
    private val scanDao: ScanDao
) {
    val allScans: Flow<List<ScanHistory>> = scanDao.getAllScans()

    suspend fun saveScan(scan: ScanHistory) {
        scanDao.insertScan(scan)
    }

    suspend fun deleteScan(scan: ScanHistory) {
        scanDao.deleteScan(scan)
    }

    suspend fun clearHistory() {
        scanDao.deleteAllScans()
    }
}
