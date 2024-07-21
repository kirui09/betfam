package com.betfam.apptea.ui.employees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.betfam.apptea.DBHelper
import com.betfam.apptea.R

class AddEmployeeDialogFragment : DialogFragment() {

    interface OnEmployeeSavedListener {
        fun onEmployeeSaved()
    }

    var employeeSavedListener: OnEmployeeSavedListener? = null
    private lateinit var dbh: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_employee_form_dialog, container, false)

        dbh = DBHelper(requireContext())

        val editTextEmployeeName = view.findViewById<EditText>(R.id.editTextEmployeeName)
        val editTextEmployeeAge = view.findViewById<EditText>(R.id.editTextEmployeeAge)
        val editTextEmployeePhoneNumber = view.findViewById<EditText>(R.id.editTextEmployeePhoneNumber)
        val editTextEmployeeID = view.findViewById<EditText>(R.id.editTextEmployeeID)
        val buttonSaveEmployee = view.findViewById<Button>(R.id.buttonSaveEmployee)
        val spinnerEmpType = view.findViewById<Spinner>(R.id.spinnerEmpType)

        val empTypes = arrayOf("Select Employee Type", "Basic", "Supervisor")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, empTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEmpType.adapter = adapter

        buttonSaveEmployee.setOnClickListener {
            val name = editTextEmployeeName.text.toString().trim()
            val age = editTextEmployeeAge.text.toString().trim()
            val phoneNumber = editTextEmployeePhoneNumber.text.toString().trim()
            val id = editTextEmployeeID.text.toString().trim()
            val empType = spinnerEmpType.selectedItem.toString()

            if (empType == "Select Employee Type") {
                showToast("Please select an employee type")
                return@setOnClickListener
            }

            if (name.isEmpty()) {
                showToast("Add Employee Name")
                return@setOnClickListener
            }

            val employee = Employee(id = null, empType = empType, name = name, age = age, phoneNumber = phoneNumber, employeeId = id)
            saveEmployeeToDatabase(employee)
        }

        return view
    }

    private fun saveEmployeeToDatabase(employee: Employee) {
        val success = dbh.insertEmployee(employee)
        if (success) {
            showToast("Employee saved successfully")
            employeeSavedListener?.onEmployeeSaved()
            dismiss()
        } else {
            showToast("Failed to save employee")
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
