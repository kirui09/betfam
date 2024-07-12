package com.betfam.apptea

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.betfam.apptea.ui.records.PendingTeaRecordDao
import com.betfam.apptea.ui.records.PendingTeaRecordEntity
import timber.log.Timber


@Database(entities = [PendingSyncData::class, PendingPaymentData::class], version =1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {


    abstract fun pendingSyncDataDao(): PendingSyncDataDao
    abstract fun pendingPaymentDao(): PendingPaymentDao

}


