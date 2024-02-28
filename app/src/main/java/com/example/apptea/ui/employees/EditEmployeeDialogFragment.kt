package com.example.apptea.ui.employees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.apptea.DBHelper
import com.example.apptea.R

class EditEmployeeDialogFragment : DialogFragment() {

    private var onEmployeeUpdatedListener: OnEmployeeUpdatedListener? = null

    fun setOnEmployeeUpdatedListener(listener: OnEmployeeUpdatedListener) {
        this.onEmployeeUpdatedListener = listener
    }

    companion object {
        fun newInstance(employee: Employee): EditEmployeeDialogFragment {
            val fragment = EditEmployeeDialogFragment()
            val args = Bundle()
            args.putParcelable("employee", employee)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_employee_dialog, container, false)

        val updateEmployeeName = view.findViewById<EditText>(R.id.updateEmployeeName)
        val spinnerEmpType = view.findViewById<Spinner>(R.id.spinnerEmpType)
        val updateEmployeeAge = view.findViewById<EditText>(R.id.updateEmployeeAge)
        val updateEmployeePhoneNumber = view.findViewById<EditText>(R.id.updateEmployeePhoneNumber)
        val updateEmployeeID = view.findViewById<EditText>(R.id.updateEmployeeID)
        val updateButtonEmployee = view.findViewById<Button>(R.id.updatebuttonEmployee)

        val employee = arguments?.getParcelable<Employee>("employee")

        // Check if employee is null before accessing its properties
        if (employee != null) {
            updateEmployeeName.setText(employee.name)
            updateEmployeeAge.setText(employee.age)
            updateEmployeePhoneNumber.setText(employee.phoneNumber)
            updateEmployeeID.setText(employee.employeeId)
            // Set up the spinner with options
            val employeeTypes = arrayOf("Basic Employee", "Supervisor")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, employeeTypes)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerEmpType.adapter = adapter
            // Set the selected item in the spinner
            if (!employee.empType.isNullOrEmpty()) {
                val empTypeIndex = employeeTypes.indexOf(employee.empType)
                if (empTypeIndex != -1) {
                    spinnerEmpType.setSelection(empTypeIndex)
                }
            }
        }

        updateButtonEmployee.setOnClickListener {
            val updatedName = updateEmployeeName.text.toString()
            val updatedAge = updateEmployeeAge.text.toString()
            val updatedPhoneNumber = updateEmployeePhoneNumber.text.toString()
            val updatedID = updateEmployeeID.text.toString()
            val updatedEmpType = spinnerEmpType.selectedItem.toString()

            if (updatedName.isNotEmpty()) {
                // Create an Employee object with the updated information
                val updatedEmployee = Employee(
                    id = employee?.id,  // Pass the existing ID
                    name = updatedName,
                    empType = updatedEmpType,
                    age = updatedAge,
                    phoneNumber = updatedPhoneNumber,
                    employeeId = updatedID
                )
                // Update record in the database
                val dbHelper = DBHelper(requireContext())
                val success = dbHelper.updateEmployee(updatedEmployee)

                if (success) {
                    Toast.makeText(
                        requireContext(),
                        "Record updated successfully",
                        Toast.LENGTH_LONG
                    ).show()

                    // Notify the listener that the employee has been updated
                    onEmployeeUpdatedListener?.onEmployeeUpdated(updatedEmployee)

                    // Dismiss the dialog
                    dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error updating record",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                dbHelper.close()
            }
        }

        return view
    }

    interface OnEmployeeUpdatedListener {
        fun onEmployeeUpdated(employee: Employee)
    }
}

