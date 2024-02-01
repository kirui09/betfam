package com.example.apptea.ui.records

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apptea.DBHelper

class RecordsViewModel : ViewModel() {

    private val dbHelper = DBHelper.getInstance()

    // Use MutableLiveData with getter as LiveData
    private val _teaRecords = MutableLiveData<List<DailyTeaRecord>>()
    val teaRecords: LiveData<List<DailyTeaRecord>> get() = _teaRecords

    // Fetch tea records from the database and update the MutableLiveData
    fun fetchTeaRecords() {
        val records = dbHelper.getAllTeaRecords()
        _teaRecords.value = records
    }
}
