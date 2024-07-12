package com.betfam.apptea.ui.records

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
@Dao
interface PendingTeaRecordDao {
    @Query("SELECT * FROM TeaRecords where synced=0")
    fun getAllTeaRecords(): List<PendingTeaRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeaRecord(teaRecords: PendingTeaRecordEntity)

    @Update
    suspend fun updateTeaRecord(teaRecords: PendingTeaRecordEntity)

    @Delete
    suspend fun deleteTeaRecord(teaRecords: PendingTeaRecordEntity)
}