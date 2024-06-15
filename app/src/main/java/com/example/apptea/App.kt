package com.example.apptea

import android.app.Application
import android.content.Context
import androidx.room.Room
import timber.log.Timber

class App : Application() {
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app-db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree()) // Plant a debug tree for logging
        // Initialize the database
        INSTANCE = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app-db").build()
    }
}
