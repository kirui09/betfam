package com.example.apptea.ui.records

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.apptea.DBHelper
import com.example.apptea.R
import java.text.SimpleDateFormat
import java.util.*

class AddRecordDialogFragment : DialogFragment() {

    private lateinit var dbh: DBHelper
    private var tempRecords: MutableList<Record> = mutableListOf()
    var recordSavedListener: AddRecordDialogFragmentListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_record_dialog, container, false)

        dbh = DBHelper(requireContext())

        val editTextDate = view.findViewById<EditText>(R.id.recordEntryTime)
        val autoCompleteEmployee = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteEmployeeName)
        val autoCompleteCompany = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteCompanyname)
        val editTextKilos = view.findViewById<EditText>(R.id.editTextEmployeeKilos)
        // Set the input type to numberDecimal
        editTextKilos.inputType =
            InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        val buttonSaveRecord = view.findViewById<Button>(R.id.buttonSaveRecord)
        val buttonSaveAllRecords = view.findViewById<Button>(R.id.buttonSaveAllRecords)

        // Set the default date to today
        val currentDate = Calendar.getInstance()
        val formattedDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(currentDate.time)
        editTextDate.setText(formattedDate)

        // Show DatePickerDialog when the date EditText is clicked
        editTextDate.setOnClickListener {
            showDatePickerDialog(currentDate, editTextDate)
        }

        buttonSaveRecord.setOnClickListener {
            // Handle the "Save Record" button click
            saveTempRecord()
        }

        buttonSaveAllRecords.setOnClickListener {
            // Handle the "Save All Records" button click
            saveAllRecords()
        }

        return view
    }

    private fun saveTempRecord() {
        // Handle the "Save Record" button click
        val date = getEditTextText(R.id.recordEntryTime)
        val company = getAutoCompleteText(R.id.autoCompleteCompanyname)
        val employee = getAutoCompleteText(R.id.autoCompleteEmployeeName)
        val kilosString = getEditTextText(R.id.editTextEmployeeKilos)

        if (validateInput(date, company, employee, kilosString)) {
            val kilos = kilosString.toDouble()

            // Create a Record object
            val record = Record(date, company, employee, kilos)

            // Add the record to the temporary list
            tempRecords.add(record)

            // Notify the listener about the saved record
            recordSavedListener?.onRecordAdded()

            // Refresh records in RecordsFragment


            // Clear the form
            clearForm()


        } else {
            showToast("Please enter all fields")
        }
    }

    private fun saveAllRecords() {
        // Handle the "Save All Records" button click
        if (tempRecords.isNotEmpty()) {
            // Include the last record from the form
            val date = getEditTextText(R.id.recordEntryTime)
            val employeename = getAutoCompleteText(R.id.autoCompleteEmployeeName)
            val company = getAutoCompleteText(R.id.autoCompleteCompanyname)
            val kilosString = getEditTextText(R.id.editTextEmployeeKilos)

            if (validateInput(date, company,employeename, kilosString)) {
                val kilos = kilosString.toDouble()

                // Create a Record object for the last record from the form
                val lastRecord = Record(date,  company,employeename, kilos)

                // Add the last record from the form to the temporary list
                tempRecords.add(lastRecord)
            }

            // Save all records to the database using DBHelper
            val success = DBHelper.getInstance().insertTeaRecords(tempRecords)

            if (success) {
                showToast("All Records saved successfully")
                // Clear the temporary list
                tempRecords.clear()

                // Notify the listener about the saved records
                recordSavedListener?.onAllRecordsAdded()

                // Refresh records in RecordsFragmet
            } else {
                showToast("Failed to save all records")
            }
        } else {
            showToast("No records to save")
        }
        // Close the dialog
        dismiss()
    }



    private fun validateInput(date: String, company: String, employee: String, kilos: String): Boolean {
        return date.isNotEmpty() && company.isNotEmpty() && employee.isNotEmpty() && kilos.isNotEmpty()
    }

    private fun getEditTextText(viewId: Int): String {
        val editText = view?.findViewById<EditText>(viewId)
        return editText?.text?.toString()?.trim() ?: ""
    }

    private fun getAutoCompleteText(viewId: Int): String {
        val autoCompleteTextView = view?.findViewById<AutoCompleteTextView>(viewId)
        return autoCompleteTextView?.text?.toString()?.trim() ?: ""
    }

    private fun showToast(message: String) {
        // Display a toast message
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showDatePickerDialog(calendar: Calendar, editTextDate: EditText) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                // Update the EditText with the selected date
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val formattedDate =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)
                editTextDate.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // Prevent future dates
        datePickerDialog.show()
    }

    private fun clearForm() {
        // Clear the form fields

        view?.findViewById<AutoCompleteTextView>(R.id.autoCompleteEmployeeName)?.text?.clear()
        view?.findViewById<EditText>(R.id.editTextEmployeeKilos)?.text?.clear()
    }



    interface AddRecordDialogFragmentListener {
        fun onRecordAdded()
        fun onAllRecordsAdded()
    }
}
