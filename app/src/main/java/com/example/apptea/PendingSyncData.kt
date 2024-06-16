package com.betfam.apptea

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_sync_data")
data class PendingSyncData(
    @PrimaryKey val id: Int,
    val date: String,
    val company: String,
    val employeeName: String,
    val kilos: Int
)
