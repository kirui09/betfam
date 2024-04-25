package com.example.apptea.ui.records

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.apptea.App
import com.example.apptea.DBHelper
import com.example.apptea.PendingSyncData
import com.example.apptea.PendingSyncDataDao
import com.example.apptea.R
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AddRecordDialogFragment : DialogFragment() {

    private lateinit var dbh: DBHelper
    private var tempRecords: MutableList<Record> = mutableListOf()
    var recordSavedListener: AddRecordDialogFragmentListener? = null
    private lateinit var pendingSyncDataDao: PendingSyncDataDao

    private var addRecordsProgressLayout: RelativeLayout? = null

    private var lastGeneratedId = 0 // Declare and initialize lastGeneratedId at the class level


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingSyncDataDao = App.database.pendingSyncDataDao() // Initialize pendingSyncDataDao

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_record_dialog, container, false)


        val addRecordsProgressLayout = view.findViewById<RelativeLayout>(R.id.addRecordsProgress)
        dbh = DBHelper(requireContext())

        val editTextDate = view.findViewById<EditText>(R.id.recordEntryTime)
        val spinnerCompanyName = view.findViewById<Spinner>(R.id.spinnerCompanyName)
        val spinnerEmployeeName = view.findViewById<Spinner>(R.id.spinnerEmployeeName)
        val editTextKilos = view.findViewById<EditText>(R.id.editTextEmployeeKilos)

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

        val dbHelper = DBHelper(requireContext())
        val employeeNames = dbHelper.getAllEmployeeNames()
        val employeeNamesWithSelectOption = listOf("Select Employee") + employeeNames
        val employeeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, employeeNamesWithSelectOption)
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEmployeeName.adapter = employeeAdapter
        spinnerEmployeeName.setSelection(0, false)

        val companyNames = dbh.getAllCompanyNames()
        val companyNamesWithSelectOption = listOf("Select Company") + companyNames
        val companyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, companyNamesWithSelectOption)
        companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCompanyName.adapter = companyAdapter
        spinnerCompanyName.setSelection(0, false)

        return view
    }


    private fun saveTempRecord() {
        val dateEditText = view?.findViewById<EditText>(R.id.recordEntryTime)
        val companySpinner = view?.findViewById<Spinner>(R.id.spinnerCompanyName)
        val employeeSpinner = view?.findViewById<Spinner>(R.id.spinnerEmployeeName)
        val kilosEditText = view?.findViewById<EditText>(R.id.editTextEmployeeKilos)

        val date = dateEditText?.text.toString()
        val company = companySpinner?.selectedItem.toString()
        val employee = employeeSpinner?.selectedItem.toString()
        val kilosString = kilosEditText?.text.toString()

        if (validateInput(date, company, employee, kilosString)) {
            val kilos = kilosString.toDouble()

            // Assuming that 'id' is initialized with a default value where 'Record' class is defined
            val id = generateUniqueId() // Generate a unique ID
            val record = Record(id, date, company, employee, kilos)
            tempRecords.add(record)
            recordSavedListener?.onRecordAdded()

            employeeSpinner?.setSelection(0, false)
            kilosEditText?.setText("")

            if (employee != "Select Employee") {
                val employeeAdapter = employeeSpinner?.adapter as? ArrayAdapter<String>
                employeeAdapter?.remove(employee)
                employeeAdapter?.notifyDataSetChanged()
            }

            Toast.makeText(requireContext(), "$employee has been saved temporarily. Enter Records For Next Employee..", Toast.LENGTH_LONG).show()
        } else {
            showToast("Please enter all fields")
        }
    }

    private fun saveAllRecords() {
        val date = view?.findViewById<EditText>(R.id.recordEntryTime)?.text.toString()
        val company = view?.findViewById<Spinner>(R.id.spinnerCompanyName)?.selectedItem.toString()
        val employee = view?.findViewById<Spinner>(R.id.spinnerEmployeeName)?.selectedItem.toString()
        val kilosString = view?.findViewById<EditText>(R.id.editTextEmployeeKilos)?.text.toString()

        if (date.isNotEmpty() && company.isNotEmpty() && employee.isNotEmpty() && kilosString.isNotEmpty()) {
            val kilos = kilosString.toDouble()
            val id = generateUniqueId() // Generate a unique ID
            val record = Record(id, date, company, employee, kilos)
            tempRecords.add(record)
        }

        if (tempRecords.isNotEmpty()) {
            val success = DBHelper.getInstance().insertTeaRecords(tempRecords)
            if (success) {
                showToast("All Records saved successfully")
                recordSavedListener?.onAllRecordsAdded()
            } else {
                showToast("Failed to save all records")
            }
        }

        if (isConnectedToInternet()) {
            GlobalScope.launch(Dispatchers.IO) {
                tempRecords.forEach { record ->
                    sendDataToGoogleSheet(record, requireContext())
                }
            }
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                tempRecords.forEach { record ->
                    val pendingData = PendingSyncData(
                        id = record.id,
                        date = record.date,
                        company = record.company,
                        employeeName = record.employee,
                        kilos = record.kilos.toInt()
                    )
                    pendingSyncDataDao.insert(pendingData) // Save data with generated ID when no internet
                }
                tempRecords.clear()
            }
        }
    }

    private fun generateUniqueId(): Int {
        return System.currentTimeMillis().toInt()
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

    private fun sendDataToGoogleSheet(record: Record, context: Context) {
        GlobalScope.launch(Dispatchers.Main) {
            addRecordsProgressLayout?.visibility = View.VISIBLE
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val credential = getGoogleAccountCredential()
                val sheetsService = setupSheetsService(credential)

                // Get the spreadsheet ID from Google Drive
                val spreadsheetId = getSpreadsheetIdFromDrive(credential)
                if (spreadsheetId != null) {
                    val range = "Sheet1!A:E" // Adjust the range based on your needs.
                    val valueRange = ValueRange().setValues(
                        listOf(listOf(record.id, record.date, record.company, record.employee, record.kilos))
                    )
                    val append = sheetsService.spreadsheets().values().append(spreadsheetId, range, valueRange)
                        .setValueInputOption("USER_ENTERED")
                    val response = append.execute()
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Data sent to Google Sheets")
                        Toast.makeText(context, "Data sent to Google Sheets", Toast.LENGTH_SHORT).show()
                        addRecordsProgressLayout?.visibility = View.GONE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Google Sheet file not found in Google Drive")
                        Toast.makeText(context, "Google Sheet file not found in Google Drive", Toast.LENGTH_SHORT).show()
                        addRecordsProgressLayout?.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to send data to Google Sheet", e)
                    Toast.makeText(context, "Failed to send data to Google Sheet: ${e.message}", Toast.LENGTH_SHORT).show()
                    addRecordsProgressLayout?.visibility = View.GONE
                }
            }
        }
    }



    private suspend fun getSpreadsheetIdFromDrive(credential: GoogleAccountCredential): String? = suspendCoroutine { cont ->
        GlobalScope.launch {
            try {
                val drive = Drive.Builder(
                    NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
                ).setApplicationName("AppChai(DoNotDelete)").build()

                // Query the file to fetch the Google Sheet ID
                val query = "mimeType='application/vnd.google-apps.spreadsheet'"
                val result = drive.files().list().setQ(query).execute()

                if (result.files.isNotEmpty()) {
                    val file = result.files[0] // Get the first file (assuming there is only one)
                    cont.resume(file.id)
                } else {
                    cont.resumeWithException(Exception("Google Sheet file not found in Google Drive"))
                }
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }
    }
    // This function retrieves a GoogleAccountCredential using the OAuth token
    private suspend fun getGoogleAccountCredential(): GoogleAccountCredential = suspendCoroutine { cont ->
        val sharedPreferences = requireContext().getSharedPreferences("user_details", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("user_id", null)
        val token = sharedPreferences.getString("id_token", null)

        if (email != null && token != null) {
            val credential = GoogleAccountCredential.usingOAuth2(
                requireContext(), listOf(SheetsScopes.SPREADSHEETS)
            )
            credential.setSelectedAccountName(email)
            cont.resume(credential)
        } else {
            cont.resumeWithException(Exception("User details not found"))
        }
    }

    private fun setupSheetsService(credential: GoogleAccountCredential): Sheets {
        val transport = NetHttpTransport()
        val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
        return Sheets.Builder(transport, jsonFactory, credential)
            .setApplicationName(getString(R.string.app_name))
            .build()
    }

    fun isConnectedToInternet(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
            val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            Log.d("InternetConnection", "Connected: $isConnected")
            return isConnected
        } else {
            val networkInfo = connectivityManager?.activeNetworkInfo
            val isConnected = networkInfo != null && networkInfo.isConnected
            Log.d("InternetConnection", "Connected: $isConnected")
            return isConnected
        }
    }

    interface AddRecordDialogFragmentListener {
        fun onRecordAdded()
        fun onAllRecordsAdded()
    }
}
