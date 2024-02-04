package com.example.apptea.ui.employees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.apptea.DBHelper
import com.example.apptea.R

class AddEmployeeDialogFragment : DialogFragment() {

    private lateinit var dbh: DBHelper
    var employeeSavedListener: OnEmployeeSavedListener? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_employee_form_dialog, container, false)

        dbh = DBHelper(requireContext())

        val editTextEmployeeName = view.findViewById<EditText>(R.id.editTextEmployeeName)
        val editTextEmployeeAge = view.findViewById<EditText>(R.id.editTextEmployeeAge)
        val editTextEmployeePhoneNumber = view.findViewById<EditText>(R.id.editTextEmployeePhoneNumber)
        val editTextEmployeeID = view.findViewById<EditText>(R.id.editTextEmployeeID)
        val buttonSaveEmployee = view.findViewById<Button>(R.id.buttonSaveEmployee)

        buttonSaveEmployee.setOnClickListener {
            // Handle the "Save" button click
            val name = editTextEmployeeName.text.toString().trim()
            val age = editTextEmployeeAge.text.toString().trim()
            val phoneNumber = editTextEmployeePhoneNumber.text.toString().trim()
            val id = editTextEmployeeID.text.toString().trim()

            // Check for empty values
            if (name.isEmpty()) {
                showToast("Add Employee Name")
                return@setOnClickListener
            }

            // Create an Employee object
            // Create an Employee object
            val employee = Employee(id = null, name = name, age = age, phoneNumber = phoneNumber, employeeId = id)

            // Save employee to the database
            saveEmployeeToDatabase(employee)

            // Notify the listener about the saved employee
            employeeSavedListener?.onEmployeeSaved()

            // Close the dialog
            dismiss()
        }

        return view
    }

    private fun saveEmployeeToDatabase(employee: Employee) {
        // Insert the employee into the database
        val success = DBHelper.getInstance().insertEmployee(employee)

        if (success) {
            showToast("Employee saved successfully")
        } else {
            showToast("Failed to save employee")
        }
    }

    private fun showToast(message: String) {
        // Display a toast message
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    interface OnEmployeeSavedListener {
        fun onEmployeeSaved()
    }
}
