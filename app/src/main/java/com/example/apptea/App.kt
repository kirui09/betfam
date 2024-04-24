package com.example.apptea

import android.app.Application
import androidx.room.Room
import timber.log.Timber

class App : Application() {
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Application): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context, AppDatabase::class.java, "app-db")
                    .fallbackToDestructiveMigration() // Handle migrations properly in a real app
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        val database: AppDatabase
            get() = INSTANCE ?: throw IllegalStateException("Database has not been created. Did you forget to call getDatabase?")
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree()) // Plant a debug tree for logging
        // Initialize the database
        INSTANCE = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app-db").build()
    }
}