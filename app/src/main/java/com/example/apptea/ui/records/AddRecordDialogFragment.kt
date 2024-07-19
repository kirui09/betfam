package com.betfam.apptea.ui.records

import android.app.AlertDialog
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.betfam.apptea.App
import com.betfam.apptea.DBHelper
import com.betfam.apptea.PendingSyncData
import com.betfam.apptea.PendingSyncDataDao
import com.betfam.apptea.R
import com.betfam.apptea.ui.companies.AddCompanyDialogFragment
import com.betfam.apptea.ui.employees.AddEmployeeDialogFragment
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

class AddRecordDialogFragment : DialogFragment(), AddCompanyDialogFragment.AddCompanyDialogListener, AddEmployeeDialogFragment.OnEmployeeSavedListener {

    private lateinit var dbh: DBHelper
    private var tempRecords: MutableList<Record> = mutableListOf()
    var recordSavedListener: AddRecordDialogFragmentListener? = null
    private lateinit var pendingSyncDataDao: PendingSyncDataDao
    private var addRecordsProgressLayout: RelativeLayout? = null
    private var lastGeneratedId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingSyncDataDao = App.getDatabase(requireContext().applicationContext).pendingSyncDataDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_record_dialog, container, false)

        addRecordsProgressLayout = view.findViewById(R.id.addRecordsProgress)
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

        val employeeNames = dbh.getAllEmployeeNames()
        val employeeNamesWithSelectOption = listOf("Select Employee") + employeeNames + "Add Employee"
        val employeeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, employeeNamesWithSelectOption)
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEmployeeName.adapter = employeeAdapter
        spinnerEmployeeName.setSelection(0, false)

        spinnerEmployeeName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem == "Add Employee") {
                    showAddEmployeeDialog()

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val companyNames = dbh.getAllCompanyNames()
        val companyNamesWithSelectOption = listOf("Select Company") + companyNames + "Add Company"
        val companyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, companyNamesWithSelectOption)
        companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCompanyName.adapter = companyAdapter
        spinnerCompanyName.setSelection(0, false)

        spinnerCompanyName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem == "Add Company") {
                    showAddCompanyDialog()

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return view
    }

    private fun showAddCompanyDialog() {
        val dialogFragment = AddCompanyDialogFragment()
        dialogFragment.setAddCompanyDialogListener(this)
        dialogFragment.show(parentFragmentManager, "AddCompanyDialog")

    }

    private fun showAddEmployeeDialog() {
        val dialogFragment = AddEmployeeDialogFragment()
        dialogFragment.employeeSavedListener = this
        dialogFragment.show(parentFragmentManager, "AddEmployeeDialog")

    }

    override fun onSaveCompanyClicked(name: String, location: String) {
        val companySpinner = view?.findViewById<Spinner>(R.id.spinnerCompanyName)
        val companyNames = dbh.getAllCompanyNames()
        val companyNamesWithSelectOption = listOf("Select Company") + companyNames + "Add Company"
        val companyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, companyNamesWithSelectOption)
        companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        companySpinner?.adapter = companyAdapter
        companySpinner?.setSelection(companyNames.size + 1)

        // Dismiss the dialog
        parentFragmentManager.findFragmentByTag("AddCompanyDialog")?.let {
            (it as DialogFragment).dismiss()
        }
    }

    override fun onEmployeeSaved() {
        val spinnerEmployeeName = view?.findViewById<Spinner>(R.id.spinnerEmployeeName)
        val employeeNames = dbh.getAllEmployeeNames()
        val employeeNamesWithSelectOption = listOf("Select Employee") + employeeNames + "Add Employee"
        val employeeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, employeeNamesWithSelectOption)
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEmployeeName?.adapter = employeeAdapter
        spinnerEmployeeName?.setSelection(employeeNames.size + 1)

        // Dismiss the dialog
        parentFragmentManager.findFragmentByTag("AddEmployeeDialog")?.let {
            (it as DialogFragment).dismiss()
        }
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
            val record = Record(id, date, company, employee, kilos,0.0)
            tempRecords.add(record)
            recordSavedListener?.onRecordAdded()

            employeeSpinner?.setSelection(0) // Set the employee spinner to the first item ("Select Employee")
            kilosEditText?.setText("")

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

        val tempRecordsToSave = mutableListOf<Record>()
        tempRecordsToSave.addAll(tempRecords)

        if (date.isNotEmpty() && company.isNotEmpty() && employee.isNotEmpty() && kilosString.isNotEmpty()) {
            val kilos = kilosString.toDouble()
            val id = generateUniqueId()
            val record = Record(id, date, company, employee, kilos,0.0)
            tempRecordsToSave.add(record)
        }

        if (tempRecordsToSave.isNotEmpty()) {
            val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            builder.setTitle("Confirm Records")

            val messageBuilder = StringBuilder()
            for (record in tempRecordsToSave) {
                messageBuilder.append("${record.company} - ${record.employee}: ${record.kilos} kg\n")
            }
            builder.setMessage(messageBuilder.toString())

            builder.setPositiveButton("Save") { dialog, _ ->
                val success = DBHelper.getInstance().insertTeaRecords(tempRecordsToSave.toList())
                if (success) {
                    showToast("All Records saved successfully")
                    recordSavedListener?.onAllRecordsAdded()
                } else {
                    showToast("Failed to save all records")
                }

                if (isConnectedToInternet()) {
                    GlobalScope.launch(Dispatchers.IO) {
                        tempRecordsToSave.forEach { record ->
                            //sendDataToGoogleSheet(record, requireContext())
                        }
                    }
                } else {
                   /* GlobalScope.launch(Dispatchers.IO) {
                        tempRecordsToSave.forEach { record ->
                            val pendingData = PendingSyncData(
                                id = record.id,
                                date = record.date,
                                company = record.company,
                                employeeName = record.employee,
                                kilos = record.kilos.toInt()
                            )
                            pendingSyncDataDao.insert(pendingData)
                        }
                        tempRecordsToSave.clear()
                    }*/
                }
                dialog.dismiss()
                dismiss()
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            val alertDialog = builder.create()
            alertDialog.show()
        } else {
            dismiss()
        }
    }

    private fun generateUniqueId(): Int {
        return System.currentTimeMillis().toInt()
    }

    private fun validateInput(date: String, company: String, employee: String, kilos: String): Boolean {
        return date.isNotEmpty() && company.isNotEmpty() && employee.isNotEmpty() && kilos.isNotEmpty() &&
                employee != "Select Employee" && company != "Select Company" &&
                employee != "Add Employee" && company != "Add Company"
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
                        val dbHelper = DBHelper(context)
                        val appDatabase = App.getDatabase(context)
                        dbHelper.updateTeaRecordSyncStatus(record.id)
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
            val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
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
