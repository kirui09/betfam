package com.example.apptea.ui.employees

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apptea.DBHelper

class EmployeesViewModel : ViewModel() {

    private val dbHelper: DBHelper by lazy { DBHelper.getInstance() }

    // MutableLiveData to observe changes in the employee list
    private val _employeeList: MutableLiveData<List<Employee>> = MutableLiveData()
    val employeeList: LiveData<List<Employee>> get() = _employeeList

    // Function to fetch employees and update MutableLiveData
    fun fetchEmployees() {
        val employees = dbHelper.getAllEmployees()
        _employeeList.value = employees
    }

    // Clear the DBHelper instance when the ViewModel is no longer used
    override fun onCleared() {
        super.onCleared()
        dbHelper.close()
    }
}
