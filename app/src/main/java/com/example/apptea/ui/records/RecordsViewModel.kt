package com.example.apptea.ui.records

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apptea.DBHelper

class RecordsViewModel : ViewModel() {

    private lateinit var dbHelper: DBHelper

    fun init(context: Context) {
        dbHelper = DBHelper(context)
    }

    fun getAllTeaRecords(): List<DailyTeaRecord> {
        return dbHelper.getAllRecords()
    }

}