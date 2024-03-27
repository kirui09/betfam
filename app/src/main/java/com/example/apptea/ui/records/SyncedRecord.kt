package com.example.apptea.ui.records

data class SyncedRecord(
    val id: Long,
    val date: String,
    val employeeName: String,
    val company: String,
    val kilos: Double,
    val pay: Double,
    val synced: Int
)
