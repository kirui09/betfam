package com.example.apptea.ui.records

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.apptea.DBHelper
import com.example.apptea.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddRecordDialogFragment : DialogFragment() {

    interface AddRecordDialogListener {
        fun onSaveRecordClicked(date: String, employeename: String, company: String, quantity: String)
    }

    private var listener: AddRecordDialogListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_record_dialog, container, false)

        val dbHelper = DBHelper(requireContext())
        val employeeNames = dbHelper.getAllEmployeeNames()

        val editTextDate = view.findViewById<EditText>(R.id.recordEntryTime)
        val autoCompleteEmployee = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteEmployeeName)
        val editTextCompany = view.findViewById<EditText>(R.id.editTextEmployeecompany)
        val editTextKilos = view.findViewById<EditText>(R.id.editTextEmployeeKilos)
        val buttonSaveRecord = view.findViewById<Button>(R.id.buttonSaveRecord)
        val buttonSaveAllRecords = view.findViewById<Button>(R.id.buttonSaveAllRecords)

        // Set up ArrayAdapter with employee names
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, employeeNames)

        // Set the adapter for the AutoCompleteTextView
        autoCompleteEmployee.setAdapter(adapter)

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
            val company = editTextCompany.text.toString()
            val quantity = editTextKilos.text.toString()

            // Notify the listener (e.g., your RecordsFragment) that Save button is clicked
            listener?.onSaveRecordClicked(date, employeename, company, quantity)

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
