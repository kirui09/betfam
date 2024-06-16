package com.betfam.apptea.ui.records

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tea_records")
data class TeaRecordEntity(
    @PrimaryKey val id: Int = 0,
    val date: String,
    val company: String,        // Ensure that these properties exist
    val employeeName: String,   // in your TeaRecordEntity class
    val kilos: Int
)