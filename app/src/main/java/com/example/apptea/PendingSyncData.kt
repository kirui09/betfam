package com.example.apptea

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_sync_data")
data class PendingSyncData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val company: String,        // Ensure that these properties exist
    val employeeName: String,   // in your TeaRecordEntity class
    val kilos: Int
)
