package com.example.apptea

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PendingSyncData::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pendingSyncDataDao(): PendingSyncDataDao
}