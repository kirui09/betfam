package com.example.apptea.ui.records

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.apptea.DBHelper
import com.example.apptea.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddRecordDialogFragment : DialogFragment() {


    interface AddRecordDialogListener {
        fun onSaveRecordClicked(date: String, employeename: String, company: String, kilos: String)
        fun onSaveAllRecordsClicked(recordsList: List<Record>)
    }

    private var listener: AddRecordDialogListener? = null
    private val recordsList = mutableListOf<Record>() // List to store records

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_record_dialog, container, false)

        val dbHelper = DBHelper(requireContext())
        val employeeNames = dbHelper.getAllEmployeeNames()
        val companyNames = dbHelper.getAllCompanyNames()

        val editTextDate = view.findViewById<EditText>(R.id.recordEntryTime)
        val autoCompleteEmployee = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteEmployeeName)
        val autoCompleteCompany = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteCompanyname)
        val editTextKilos = view.findViewById<EditText>(R.id.editTextEmployeeKilos)
        // Set the input type to numberDecimal
        editTextKilos.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        val buttonSaveRecord = view.findViewById<Button>(R.id.buttonSaveRecord)
        val buttonSaveAllRecords = view.findViewById<Button>(R.id.buttonSaveAllRecords)

        // Set up ArrayAdapter with employee names
        val employeeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, employeeNames)
        autoCompleteEmployee.setAdapter(employeeAdapter)

        // Set up ArrayAdapter with company names
        val companyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, companyNames)
        autoCompleteCompany.setAdapter(companyAdapter)


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
            val date = editTextDate.text.toString()
            val employeename = autoCompleteEmployee.text.toString()
            val company = autoCompleteCompany.text.toString()
            val kilos = editTextKilos.text.toString().toDoubleOrNull() ?: 0.0


            // Add the record to the list
            val record = Record(date, employeename, company, kilos)
            recordsList.add(record)

            // Clear the input fields or perform any other necessary actions
            autoCompleteEmployee.setText("")
            autoCompleteCompany.setText("")
            editTextKilos.setText("")
        }

        buttonSaveAllRecords.setOnClickListener {
            // Save the last record to the list if there is any
            val date = editTextDate.text.toString()
            val employeename = autoCompleteEmployee.text.toString()
            val company = autoCompleteCompany.text.toString()
            val kilos = editTextKilos.text.toString()

            if (date.isNotEmpty() && employeename.isNotEmpty() && company.isNotEmpty() && kilos.isNotEmpty()) {
                val lastRecord = Record(date, employeename, company, kilos.toDouble())
                recordsList.add(lastRecord)
            }


            // Notify the listener that Save All Records button is clicked
            listener?.onSaveAllRecordsClicked(recordsList)

            // Save all records to the database using DBHelper
            val dbHelper = DBHelper(requireContext())
            val success = dbHelper.insertTeaRecords(recordsList)

            if (success) {

                Toast.makeText(requireContext(), "Records saved successfully", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(requireContext(), "Records Not Saved", Toast.LENGTH_SHORT).show()

            }
            recordsList.clear()

            dismiss() // Close the dialog
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle("Add Tea Record")
        return dialog
    }

    fun setAddRecordDialogListener(listener: AddRecordDialogListener) {
        this.listener = listener
    }

    private fun showDatePickerDialog(calendar: Calendar, editTextDate: EditText) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                // Update the EditText with the selected date
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val formattedDate =
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDate.time)
                editTextDate.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // Prevent future dates
        datePickerDialog.show()
    }


}
