package com.example.apptea.ui.records

import android.app.DatePickerDialog
import android.content.ContentValues
import android.icu.text.AlphabeticIndex
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.apptea.DBHelper
import com.example.apptea.R
import com.example.apptea.ui.employees.EmployeeAdapter
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AddRecordDialogFragment : DialogFragment() {

    private lateinit var dbh: DBHelper
    private var tempRecords: MutableList<Record> = mutableListOf()
    var recordSavedListener: AddRecordDialogFragmentListener? = null

    internal lateinit var employeeAdapter: EmployeeAdapter

    private lateinit var savingProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_record_dialog, container, false)

        dbh = DBHelper(requireContext())

        savingProgressBar=view.findViewById(R.id.savingprogressBar)

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

            val record = Record(-1, date, company, employee, kilos)
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



    // This function simulates sending records to Google Sheets and calls the database update
// on success. You would replace this with your actual implementation for sending data.


    private fun saveAllRecords() {
        val date = getView()?.findViewById<EditText>(R.id.recordEntryTime)?.text.toString()
        val company = getView()?.findViewById<Spinner>(R.id.spinnerCompanyName)?.selectedItem.toString()
        val employee = getView()?.findViewById<Spinner>(R.id.spinnerEmployeeName)?.selectedItem.toString()
        val kilosString = getView()?.findViewById<EditText>(R.id.editTextEmployeeKilos)?.text.toString()

        // Check if all fields are filled
        if (date.isNotEmpty() && company.isNotEmpty() && employee.isNotEmpty() && kilosString.isNotEmpty()) {
            val kilos = kilosString.toDouble()
            val record = Record(id, date, company, employee, kilos)

            // Add the record to tempRecords
            tempRecords.add(record)
        }

        if (tempRecords.isNotEmpty()) {
            if (tempRecords.size == 1) {
                // If only one record is present, save it directly to the database
                val success = DBHelper.getInstance().insertTeaRecord(tempRecords.first())
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
                    sendRecordsToGoogleSheet(tempRecords) { // Mark records as synced in your SQLite database
                        val recordIds = tempRecords.map { it.id }
                        markRecordsAsSyncedInDatabase(recordIds)
                        tempRecords.clear()
                        recordSavedListener?.onAllRecordsAdded()
                    }
                } else {
                    showToast("Failed to save all records")
                }
            }
        }
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



    // This function should be called after records are successfully sent to Google Sheets
    private fun markRecordsAsSyncedInDatabase(recordIds: List<Int>) {
        val db = dbh.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("synced", 1) // Assuming '1' signifies that the record is synced

        val args = recordIds.joinToString(", ") { "?" }
        val selection = "id IN ($args)"
        val selectionArgs = recordIds.map { it.toString() }.toTypedArray()

        try {
            val rowsUpdated = db.update("TeaRecords", contentValues, selection, selectionArgs)
            if (rowsUpdated != recordIds.size) {
                Log.e("SyncStatus", "Some records might not have been marked as synced. Expected: ${recordIds.size}, Actual: $rowsUpdated")
                // Additional logging to see which IDs were not updated
                val nonUpdatedIds = recordIds.filterNot { id ->
                    // Perform a query to check if each ID was updated
                    val cursor = db.query("TeaRecords", arrayOf("id"), "id = ? AND synced = 1", arrayOf(id.toString()), null, null, null)
                    val wasUpdated = cursor.moveToFirst()
                    cursor.close()
                    wasUpdated
                }
                Log.e("SyncStatus", "Non-updated record IDs: $nonUpdatedIds")
            } else {
                Log.d("SyncStatus", "All records marked as synced successfully.")
            }
        } catch (e: Exception) {
            Log.e("DatabaseError", "Error marking records as synced: ${e.message}")
        }
    }


    private fun sendRecordsToGoogleSheet(records: List<Record>, callback: () -> Unit) {
        savingProgressBar.visibility = View.VISIBLE

        if (isAdded) { // Check if the Fragment is attached
            val activity = requireActivity() // Get the Activity instance
            val queue = Volley.newRequestQueue(activity) // Create the RequestQueue
            val url = "https://script.google.com/macros/s/AKfycbyrtbT9dFdxXhO7u9Hkc7Kr0YsHYHTmlAm8Rl9YVchcxel6Z8IuXS4CaiCv0-87oJHklg/exec"

            // Convert records list to JSONArray with the correct field names
            val jsonArray = JSONArray()
            for (record in records) {
                val jsonObject = JSONObject()
                jsonObject.put("date", record.date)
                jsonObject.put("companyName", record.company)
                jsonObject.put("employeeName", record.employee)
                jsonObject.put("employeeKilos", record.kilos)
                jsonArray.put(jsonObject)
            }

            // Create the POST request
            val stringRequest = object : StringRequest(Method.POST, url,
                Response.Listener { response ->
                    // Handle response
                    if (isAdded) {
                        savingProgressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), response, Toast.LENGTH_LONG).show()
                        callback.invoke()
                        dismiss() // Dismiss the dialog fragment
                    }
                },
                Response.ErrorListener { error ->
                    // Handle error
                    if (isAdded) {
                        savingProgressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                }
            ) {
                @Throws(AuthFailureError::class)
                override fun getBody(): ByteArray {
                    // Return the JSON as the body of the request
                    return jsonArray.toString().toByteArray(Charsets.UTF_8)
                }

                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json; charset=utf-8"
                    return headers
                }
            }

            // Add the request to the RequestQueue
            queue.add(stringRequest)
        } else {
            Log.e("AddRecordDialogFragment", "Fragment is not attached to an Activity")
        }
    }


    interface AddRecordDialogFragmentListener {
        fun onRecordAdded()
        fun onAllRecordsAdded()
    }
}
