    package com.example.apptea.ui.employees

    import android.util.Log
    import androidx.lifecycle.LiveData
    import androidx.lifecycle.MutableLiveData
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.apptea.DBHelper
    import kotlinx.coroutines.launch

    class EmployeesViewModel : ViewModel() {

        private val dbHelper: DBHelper by lazy { DBHelper.getInstance() }

        // MutableLiveData to observe changes in the employee list
        private val _employeeList: MutableLiveData<List<Employee>> = MutableLiveData()
        val employeeList: LiveData<List<Employee>> get() = _employeeList

        // Function to fetch employees and update MutableLiveData
        fun fetchEmployees() {
            viewModelScope.launch {
                try {
                    val employees = dbHelper.getAllEmployees()
                    _employeeList.postValue(employees)

                    // Add log statement
                    Log.d("EmployeesViewModel", "Fetched ${employees.size} employees")
                } catch (e: Exception) {
                    // Handle exceptions
                    Log.e("EmployeesViewModel", "Error fetching employees", e)
                }
            }
        }


        // Clear the DBHelper instance when the ViewModel is no longer used
        override fun onCleared() {
            super.onCleared()
            dbHelper.close()
        }
    }
