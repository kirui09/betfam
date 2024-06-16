package com.betfam.apptea

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingPaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(paymentData: PendingPaymentData)

    @Query("SELECT * FROM pending_payment_data")
    suspend fun getAllPayments(): List<PendingPaymentData>
}
