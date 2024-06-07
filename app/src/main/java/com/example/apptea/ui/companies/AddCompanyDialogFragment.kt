package com.example.apptea.ui.companies

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

class AddCompanyDialogFragment : DialogFragment() {

    interface AddCompanyDialogListener {
        fun onSaveCompanyClicked(name: String, location: String)
    }

    private var listener: AddCompanyDialogListener? = null
    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_company_dialog, container, false)

        dbHelper = DBHelper(requireContext()) // Initialize DBHelper

        val editTextCompanyName = view.findViewById<EditText>(R.id.editTextCompanyName)
        val editTextCompanyLocation = view.findViewById<EditText>(R.id.editTextCompanyLocation)
        val buttonSaveCompany = view.findViewById<Button>(R.id.buttonAddCompany)

        buttonSaveCompany.setOnClickListener {
            val name = editTextCompanyName.text.toString().trim()
            val location = editTextCompanyLocation.text.toString().trim()

            // Check for empty values
            if (name.isEmpty() || location.isEmpty()) {
                showToast("Please fill in all fields")
                return@setOnClickListener
            }

            // Insert the company into the database
            saveCompany(name, location)

            // Notify the listener if needed
            listener?.onSaveCompanyClicked(name, location)

            // Close the dialog
            dismiss()
        }

        return view
    }

    private fun saveCompany(name: String, location: String) {
        // Call the DBHelper method to insert the company into the database
        val isSuccess = dbHelper.insertCompany(name, location)

        if (isSuccess) {
            // Show a success message using Toast
            showToast("Company saved successfully")

            // Notify the listener if needed
            listener?.onSaveCompanyClicked(name, location)
        } else {
            // Show an error message using Toast
            showToast("Failed to save company")
        }
    }

    fun setAddCompanyDialogListener(listener: AddCompanyDialogListener) {
        this.listener = listener
    }

    private fun showToast(message: String) {
        // Display a toast message
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


}
