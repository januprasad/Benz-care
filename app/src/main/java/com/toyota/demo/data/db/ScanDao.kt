package com.toyota.demo.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanHistory)

    @Delete
    suspend fun deleteScan(scan: ScanHistory)

    @Query("DELETE FROM scan_history")
    suspend fun deleteAllScans()
}
