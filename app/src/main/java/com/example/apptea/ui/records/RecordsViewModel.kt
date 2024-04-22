package com.example.apptea.ui.records

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apptea.DBHelper

class RecordsViewModel : ViewModel() {
    private val dbHelper = DBHelper.getInstance()

    private val _teaRecords = MutableLiveData<List<DailyTeaRecord>>()
    val teaRecords: LiveData<List<DailyTeaRecord>> get() = _teaRecords

    // Method to fetch tea records from the database
    fun fetchTeaRecords() {
        val records = dbHelper.getAllTeaRecords()
        _teaRecords.value = records
    }

    // Method to refresh records after adding new data
    fun refreshRecords() {
        fetchTeaRecords()
    }
}