package com.betfam.apptea

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingSyncDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pendingSyncData: PendingSyncData)

    @Query("SELECT * FROM pending_sync_data ")
    suspend fun getAllPendingData(): List<PendingSyncData>

    @Delete
    suspend fun delete(pendingSyncData: PendingSyncData)
}