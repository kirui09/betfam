package com.betfam.apptea.ui.records

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "TeaRecords")
data class PendingTeaRecordEntity(
    @PrimaryKey val id: Int = 0,
    val date: String,
    val employee_name: String,
    val company: String,        // Ensure that these properties exist
    val kilos: Double,
    val pay: Double,
    val synced: Int
)