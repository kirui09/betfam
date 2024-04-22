package com.example.apptea.ui.records

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TeaRecordDao {
    @Query("SELECT * FROM tea_records")
    fun getAllTeaRecords(): LiveData<List<TeaRecordEntity>>

    @Insert
    suspend fun insertTeaRecord(teaRecord: DailyTeaRecord)

    @Update
    suspend fun updateTeaRecord(teaRecord: DailyTeaRecord)

    @Delete
    suspend fun deleteTeaRecord(teaRecord: DailyTeaRecord)
}