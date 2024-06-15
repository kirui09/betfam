package com.example.apptea

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_payment_data")
data class PendingPaymentData(
    @PrimaryKey val id: Int,
    val date: String,
    val employeeName: String,
    val paymentAmount: Double
)

@Entity(tableName = "tea_records")
data class PendingTeaRecord(
    @PrimaryKey val id: Int,
    val date: String,
    val employee_name: String,
    val company: String,
    val kilos: Double,
    val pay: Double,
    val synced: Int
)

