package com.example.apptea.ui.records

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.apptea.DBHelper
import com.example.apptea.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class EditRecordDialogFragment : DialogFragment() {

    private lateinit var editrecordEntryTime: TextInputEditText
    private lateinit var editautoCompleteCompanyname: AutoCompleteTextView
    private lateinit var editautoCompleteEmployeeName: AutoCompleteTextView
    private lateinit var editfragmentTextEmployeeKilos: EditText
    private lateinit var editbuttonSaveRecord: Button
    private lateinit var editbuttonSaveAllRecords: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_record_dialog, container, false)

        editrecordEntryTime = view.findViewById(R.id.updaterecordEntryTime)
        editautoCompleteCompanyname = view.findViewById(R.id.updateautoCompleteCompanyname)
        editautoCompleteEmployeeName = view.findViewById(R.id.updateautoCompleteEmployeeName)
        editfragmentTextEmployeeKilos = view.findViewById(R.id.updateTextEmployeeKilos)
        editbuttonSaveRecord = view.findViewById(R.id.updatebuttonSaveRecord)
        editbuttonSaveAllRecords = view.findViewById(R.id.updatebuttonSaveAllRecords)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                updateRecord()
            }
        }

        editbuttonSaveAllRecords.setOnClickListener {
            if (validateFields()) {
                // Save all records for each employee
                updateAllRecords()
            }
        }
    }

    private fun populateUI(editableTeaRecords: List<EditableTeaRecord>) {
        try {
            // Add logic to display or handle the list of records
            // For example, you might choose to display the first record:
            if (editableTeaRecords.isNotEmpty()) {
                val firstRecord = editableTeaRecords.first()
                editrecordEntryTime.setText(firstRecord.date)
                editautoCompleteCompanyname.setText(firstRecord.companies.joinToString(","))
                editautoCompleteEmployeeName.setText(firstRecord.employees.joinToString(","))
                editfragmentTextEmployeeKilos.setText(firstRecord.kilos.toString())

                Log.d("EditRecordFragment", "UI populated successfully")
            } else {
                Log.d("EditRecordFragment", "EditableTeaRecords is empty")
            }
        } catch (e: Exception) {
            Log.e("EditRecordFragment", "Error populating UI: $editableTeaRecords")
        }
    }

    private fun validateFields(): Boolean {
        // Add your validation logic here
        // Return true if all fields are valid, otherwise false
        // You can display error messages or highlight invalid fields as needed
        return true
    }

    private fun updateRecord() {
        // Implement saving record logic for the current employee
        // You can access the values using:
        // val date = editrecordEntryTime.text.toString()
        // val companies = editautoCompleteCompanyname.text.toString().split(",")
        // val employees = editautoCompleteEmployeeName.text.toString().split(",")
        // val kilos = editfragmentTextEmployeeKilos.text.toString().toDouble()
    }

    private fun updateAllRecords() {
        // Implement saving all records logic
    }
}
