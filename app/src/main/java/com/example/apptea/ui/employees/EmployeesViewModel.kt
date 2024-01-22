package com.example.apptea.ui.employees

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apptea.DBHelper

class EmployeesViewModel : ViewModel() {

    private val dbHelper = DBHelper.getInstance()

    // LiveData to observe changes in the employee list
    private val _employeeList = MutableLiveData<List<Employee>>()
    val employeeList: LiveData<List<Employee>> get() = _employeeList

    // Function to fetch employees and update LiveData
    fun fetchEmployees() {
        val employees = dbHelper.getAllEmployees()
        _employeeList.postValue(employees)
    }

}