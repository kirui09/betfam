package com.example.apptea.ui.records

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.apptea.DBHelper
import com.example.apptea.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.fragment.app.DialogFragment

class EditRecordDialogFragment : DialogFragment() {

    private lateinit var textInputLayoutDate: TextInputLayout
    private lateinit var editrecordEntryTime: TextInputEditText
    private lateinit var textInputLayoutCompanyName: TextInputLayout
    private lateinit var editautoCompleteCompanyname: TextInputEditText
    private lateinit var textInputLayoutEmployeeName: TextInputLayout
    private lateinit var editautoCompleteEmployeeName: TextInputEditText
    private lateinit var textInputLayoutEmployeeKilos: TextInputLayout
    private lateinit var editfragmentTextEmployeeKilos: TextInputEditText
    private lateinit var editbuttonSaveRecord: MaterialButton
    private lateinit var editbuttonSaveAllRecords: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_record_dialog, container, false)

        textInputLayoutDate = view.findViewById(R.id.textInputLayoutDate)
        editrecordEntryTime = view.findViewById(R.id.editrecordEntryTime)
        textInputLayoutCompanyName = view.findViewById(R.id.textInputLayoutCompanyName)
        editautoCompleteCompanyname = view.findViewById(R.id.editautoCompleteCompanyname)
        textInputLayoutEmployeeName = view.findViewById(R.id.textInputLayoutEmployeeName)
        editautoCompleteEmployeeName = view.findViewById(R.id.editautoCompleteEmployeeName)
        textInputLayoutEmployeeKilos = view.findViewById(R.id.textInputLayoutEmployeeKilos)
        editfragmentTextEmployeeKilos = view.findViewById(R.id.editfragmentTextEmployeeKilos)
        editbuttonSaveRecord = view.findViewById(R.id.editbuttonSaveRecord)
        editbuttonSaveAllRecords = view.findViewById(R.id.editbuttonSaveAllRecords)

        // Get the selected date from arguments
        val selectedDate = arguments?.getString("selectedDate")

        // Check if selectedDate is not null
        if (!selectedDate.isNullOrEmpty()) {
            // Fetch records for the selected date
            val dbHelper = DBHelper(requireContext())
            val editableTeaRecords = dbHelper.getTeaRecordsByDate(selectedDate)

            // Log the selected date and retrieved data
            Log.d("EditRecordFragment", "Selected Date: $selectedDate")
            Log.d("EditRecordFragment", "EditableTeaRecords: $editableTeaRecords")

            // Populate the UI with the retrieved data
            populateUI(editableTeaRecords)

            // Handle your UI elements or actions here
        }

        editbuttonSaveRecord.setOnClickListener {
            if (validateFields()) {
                // Save the record for the current employee
                saveRecord()
            }
        }

        editbuttonSaveAllRecords.setOnClickListener {
            if (validateFields()) {
                // Save all records for each employee
                saveAllRecords()
            }
        }

        return view
    }

    private fun populateUI(editableTeaRecords: List<EditableTeaRecord>) {
        // Add logic to display or handle the list of records
        // For example, you might choose to display the first record:
        if (editableTeaRecords.isNotEmpty()) {
            val firstRecord = editableTeaRecords.first()
            textInputLayoutDate.editText?.setText(firstRecord.date)
            textInputLayoutCompanyName.editText?.setText(firstRecord.companies.joinToString(","))
            textInputLayoutEmployeeName.editText?.setText(firstRecord.employees.joinToString(","))
            textInputLayoutEmployeeKilos.editText?.setText(firstRecord.kilos.toString())
        }
    }

    private fun validateFields(): Boolean {
        // Add your validation logic here
        // Return true if all fields are valid, otherwise false
        // You can display error messages or highlight invalid fields as needed
        return true
    }

    private fun saveRecord() {
        // Implement saving record logic for the current employee
        // You can access the values using:
        // val date = textInputLayoutDate.editText?.text.toString()
        // val companies = editautoCompleteCompanyname.text.toString().split(",")
        // val employees = editautoCompleteEmployeeName.text.toString().split(",")
        // val kilos = editfragmentTextEmployeeKilos.text.toString().toDouble()
    }

    private fun saveAllRecords() {
        // Implement saving all records logic
    }
}
