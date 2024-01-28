package com.example.apptea.ui.records

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.apptea.DBHelper
import com.example.apptea.ui.records.DailyTeaRecord


import androidx.lifecycle.ViewModel


class RecordsViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper: DBHelper = DBHelper.getInstance()

    fun initialize(context: Context) {
        dbHelper.initialize(context)
    }

    // Add this method to fetch tea records grouped by date
    fun getAllTeaRecords(): List<DailyTeaRecord> {
        return dbHelper.getAllTeaRecords()
    }

    // Add this method to get LiveData for tea records
    fun getAllTeaRecordsLiveData(): LiveData<List<DailyTeaRecord>> {
        val teaRecordsLiveData = MutableLiveData<List<DailyTeaRecord>>()

        // Assuming that you have a method in DBHelper to get tea records LiveData
        // If not, you may need to implement it in DBHelper
        // Example: val teaRecordsLiveData = dbHelper.getAllTeaRecordsLiveData()

        teaRecordsLiveData.postValue(getAllTeaRecords())
        return teaRecordsLiveData
    }
}