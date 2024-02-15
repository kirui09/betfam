package com.example.apptea.ui.records

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.apptea.DBHelper
import com.example.apptea.R
import com.example.apptea.ui.employees.EmployeeAdapter
import java.text.SimpleDateFormat
import java.util.*

class AddRecordDialogFragment : DialogFragment() {

    private lateinit var dbh: DBHelper
    private var tempRecords: MutableList<Record> = mutableListOf()
    var recordSavedListener: AddRecordDialogFragmentListener? = null

    internal lateinit var employeeAdapter: EmployeeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_record_dialog, container, false)

        dbh = DBHelper(requireContext())

        val editTextDate = view.findViewById<EditText>(R.id.recordEntryTime)
        val spinnerCompanyName = view.findViewById<Spinner>(R.id.spinnerCompanyName)
        val spinnerEmployeeName = view.findViewById<Spinner>(R.id.spinnerEmployeeName)

        // Find the EditText for kilos
        val editTextKilos = view.findViewById<EditText>(R.id.editTextEmployeeKilos)

        // Set input filter to restrict the number of digits
        editTextKilos.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(4))


        editTextKilos.inputType =
            InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        val buttonSaveRecord = view.findViewById<Button>(R.id.buttonSaveRecord)
        val buttonSaveAllRecords = view.findViewById<Button>(R.id.buttonSaveAllRecords)

        val currentDate = Calendar.getInstance()
        val formattedDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(currentDate.time)
        editTextDate.setText(formattedDate)

        editTextDate.setOnClickListener {
            showDatePickerDialog(currentDate, editTextDate)
        }

        buttonSaveRecord.setOnClickListener {
            saveTempRecord()
        }

        buttonSaveAllRecords.setOnClickListener {
            saveAllRecords()
        }

        // Step 1: Initialize MyDBHelper and retrieve employee names
        val dbHelper = DBHelper(requireContext())
        var employeeNames = dbHelper.getAllEmployeeNames()
      // Prepend "Select Employee" to the list of employee names
        val employeeNamesWithSelectOption = listOf("Select Employee") + employeeNames
        // Step 2: Create an adapter for the employee spinner and set it
        val employeeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, employeeNamesWithSelectOption)
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEmployeeName.adapter = employeeAdapter
        // Step 3: Set default position for the spinner (Select Employee cannot be selected)
        spinnerEmployeeName.setSelection(0, false)

        // Retrieve company names from the database
        val companyNames = dbh.getAllCompanyNames()

        // Prepend "Select Company" to the list of employee names
        val companyNamesWithSelectOption = listOf("Select Company") + companyNames
        // Step 2: Create an adapter for the company spinner and set it
        val companyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, companyNamesWithSelectOption)
        companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCompanyName.adapter = companyAdapter
        // Step 3: Set default position for the spinner (Select Employee cannot be selected)
        spinnerCompanyName.setSelection(0, false)

        return view
    }

    private fun saveTempRecord() {
        // Handle the "Save Record" button click
        val dateEditText = getView()?.findViewById<EditText>(R.id.recordEntryTime)
        val companySpinner = getView()?.findViewById<Spinner>(R.id.spinnerCompanyName)
        val employeeSpinner = getView()?.findViewById<Spinner>(R.id.spinnerEmployeeName)
        val kilosEditText = getView()?.findViewById<EditText>(R.id.editTextEmployeeKilos)

        val date = dateEditText?.text.toString()
        val company = companySpinner?.selectedItem.toString()
        val employee = employeeSpinner?.selectedItem.toString()
        val kilosString = kilosEditText?.text.toString()

        if (validateInput(date, company, employee, kilosString)) {
            val kilos = kilosString.toDouble()

            val record = Record(date, company, employee, kilos)
            tempRecords.add(record)
            recordSavedListener?.onRecordAdded()

            // Clear all fields
            employeeSpinner?.setSelection(0, false)
            kilosEditText?.setText("")


            // Remove the employee from the spinner adapter if the company is not changed
            if (employee != "Select Employee") {
                val employeeAdapter = employeeSpinner?.adapter as? ArrayAdapter<String>
                employeeAdapter?.remove(employee)
                employeeAdapter?.notifyDataSetChanged()
            }


            // Show toast indicating the record has been saved temporarily
            Toast.makeText(requireContext(), " $employee has been saved temporarily. Enter Records For Next Employee..", Toast.LENGTH_LONG).show()
        } else {
            showToast("Please enter all fields")
        }
    }



    private fun saveAllRecords() {
        val date = getView()?.findViewById<EditText>(R.id.recordEntryTime)?.text.toString()
        val company =
            getView()?.findViewById<Spinner>(R.id.spinnerCompanyName)?.selectedItem.toString()
        val employee =
            getView()?.findViewById<Spinner>(R.id.spinnerEmployeeName)?.selectedItem.toString()
        val kilosString =
            getView()?.findViewById<EditText>(R.id.editTextEmployeeKilos)?.text.toString()

        if (validateInput(date, company, employee, kilosString)) {
            val kilos = kilosString.toDouble()
            val record = Record(date, company, employee, kilos)

            // Add the last record to tempRecords
            tempRecords.add(record)

            if (tempRecords.size == 1) {
                // If only one record is present, save it directly to the database
                val success = DBHelper.getInstance().insertTeaRecord(record)

                if (success) {
                    showToast("Record saved successfully")
                    recordSavedListener?.onRecordAdded()
                    recordSavedListener?.onAllRecordsAdded()
                } else {
                    showToast("Failed to save record")
                }
            } else {
                // If there are multiple records, save them all to the database
                val success = DBHelper.getInstance().insertTeaRecords(tempRecords)

                if (success) {
                    showToast("All Records saved successfully")
                    tempRecords.clear()
                    recordSavedListener?.onAllRecordsAdded()
                } else {
                    showToast("Failed to save all records")
                }
            }
        } else {
            showToast("Please enter all fields")
        }

        dismiss()
    }



    private fun validateInput(date: String, company: String, employee: String, kilos: String): Boolean {
        return date.isNotEmpty() && company.isNotEmpty() && employee.isNotEmpty() && kilos.isNotEmpty() &&
                employee != "Select Employee" && company != "Select Company"
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showDatePickerDialog(calendar: Calendar, editTextDate: EditText) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
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

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }


    // Define a custom InputFilter class
    private class DecimalDigitsInputFilter(private val maxDigitsBeforeDecimalPoint: Int) :
        InputFilter {
        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val builder = StringBuilder(dest)
            builder.replace(dstart, dend, source?.subSequence(start, end).toString())
            return if (!builder.toString().matches(Regex("^\\d{0,$maxDigitsBeforeDecimalPoint}+(\\.\\d{0,4})?$"))) {
                if (source!!.length > 0) "" else dest?.subSequence(dstart, dend)
            } else null
        }
    }

    interface AddRecordDialogFragmentListener {
        fun onRecordAdded()
        fun onAllRecordsAdded()
    }
}
