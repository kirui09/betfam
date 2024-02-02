// File: EditEmployeeDialogFragment.kt
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

class EditEmployeeDialogFragment : DialogFragment() {

    // Listener for notifying the calling fragment or activity
    private var onEmployeeUpdatedListener: OnEmployeeUpdatedListener? = null

    // Setter for the listener
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

        // Inside onCreateView or onViewCreated method
        val updateEmployeeName = view.findViewById<EditText>(R.id.updateEmployeeName)
        val updateEmployeeAge = view.findViewById<EditText>(R.id.updateEmployeeAge)
        val updateEmployeePhoneNumber = view.findViewById<EditText>(R.id.updateEmployeePhoneNumber)
        val updateEmployeeID = view.findViewById<EditText>(R.id.updateEmployeeID)
        val updateButtonEmployee = view.findViewById<Button>(R.id.updatebuttonEmployee)

        val employee = arguments?.getParcelable<Employee>("employee")

        updateEmployeeName.setText(employee?.name)
        updateEmployeeAge.setText(employee?.age)
        updateEmployeePhoneNumber.setText(employee?.phoneNumber)
        updateEmployeeID.setText(employee?.employeeId)

        updateButtonEmployee.setOnClickListener {
            val updatedName = updateEmployeeName.text.toString()
            val updatedAge = updateEmployeeAge.text.toString()
            val updatedPhoneNumber = updateEmployeePhoneNumber.text.toString()
            val updatedID = updateEmployeeID.text.toString()

            if (updatedName.isNotEmpty() && updatedAge.isNotEmpty() && updatedPhoneNumber.isNotEmpty() && updatedID.isNotEmpty()) {
                // Create an Employee object with the updated information
                val updatedEmployee =
                    Employee(updatedName, updatedAge, updatedPhoneNumber, updatedID)

                // Update record in the database
                val dbHelper = DBHelper(requireContext())
                val success = dbHelper.updateEmployee(updatedEmployee)

                if (success) {
                    Toast.makeText(
                        requireContext(),
                        "Record updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Notify the listener that the employee has been updated
                    onEmployeeUpdatedListener?.onEmployeeUpdated()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error updating record",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                dbHelper.close()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
                ).show()
            }

            dismiss()
        }

        return view
    }

    interface OnEmployeeUpdatedListener {
        fun onEmployeeUpdated()
    }
}
