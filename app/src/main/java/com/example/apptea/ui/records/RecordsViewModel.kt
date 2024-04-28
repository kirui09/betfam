package com.example.apptea.ui.records

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apptea.DBHelper
import com.example.apptea.R
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RecordsViewModel(private val appContext: Context) : ViewModel() {



    private val dbHelper = DBHelper.getInstance()

    private val _teaRecords = MutableLiveData<List<DailyTeaRecord>>()
    val teaRecords: LiveData<List<DailyTeaRecord>> get() = _teaRecords

    // Method to fetch tea records from the database
    fun fetchTeaRecords() {
        val records = dbHelper.getAllTeaRecords()
        _teaRecords.value = records
    }

    // Method to refresh records after adding new data
    fun refreshRecords() {
        fetchTeaRecords()
    }

    fun syncWithGoogleSheet() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val credential = getGoogleAccountCredential()
                val sheetsService = setupSheetsService(credential)
                val spreadsheetId = getSpreadsheetIdFromDrive(credential)

                if (spreadsheetId != null) {
                    val range = "Sheet1!A:E" // Adjust the range based on your needs
                    val response = sheetsService.spreadsheets().values().get(spreadsheetId, range).execute()
                    val values = response.getValues()

                    // Convert the fetched data from the sheet into a list of DailyTeaRecord objects
                    val recordsFromSheet = mutableListOf<DailyTeaRecord>()
                    if (values != null) {
                        for (rowIndex in values.indices) {
                            val row = values[rowIndex]
                            if (rowIndex == 0 || row.size < 5) {
                                // Skip the header row or rows with fewer than 5 columns
                                continue
                            }
                            val id = row[0].toString().toIntOrNull() ?: 0
                            val date = row[1].toString()
                            val company = row[2].toString()
                            val employee = row[3].toString()
                            val kilos = if (row.size >= 5) row[4].toString().toDoubleOrNull() ?: 0.0 else 0.0

                            recordsFromSheet.add(DailyTeaRecord(id, date, company, employee, kilos))
                        }
                    }

                    // Fetch existing records from the local database
                    val existingRecords = dbHelper.getAllTeaRecords()

                    // Identify records to be inserted or updated
                    val recordsToInsertOrUpdate = mutableListOf<DailyTeaRecord>()
                    recordsFromSheet.forEach { sheetRecord ->
                        val existingRecord = existingRecords.find { it.id.toInt() == sheetRecord.id }
                        if (existingRecord == null) {
                            // Insert a new record
                            recordsToInsertOrUpdate.add(sheetRecord)
                        } else {
                            // Check if the record has been updated
                            if (existingRecord != sheetRecord) {
                                recordsToInsertOrUpdate.add(sheetRecord)
                            }
                        }
                    }

                    // Insert or update records in the local database
                    val recordsToInsert = recordsToInsertOrUpdate.map { dailyTeaRecord ->
                        Record(
                            id = dailyTeaRecord.id,
                            date = dailyTeaRecord.date,
                            company = dailyTeaRecord.companies,
                            employee = dailyTeaRecord.employees,
                            kilos = dailyTeaRecord.kilos
                        )
                    }
                    dbHelper.insertOrUpdateTeaRecords(recordsToInsert)
                }
            } catch (e: Exception) {
                Log.e("SyncWithGoogleSheet", "Error syncing with Google Sheet", e)
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


    private suspend fun getGoogleAccountCredential(): GoogleAccountCredential = suspendCoroutine { cont ->
        val sharedPreferences = appContext.getSharedPreferences("user_details", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("user_id", null)
        val token = sharedPreferences.getString("id_token", null)
        if (email != null && token != null) {
            val credential = GoogleAccountCredential.usingOAuth2(
                appContext, listOf(SheetsScopes.SPREADSHEETS)
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
            .setApplicationName(appContext.getString(R.string.app_name))
            .build()
    }
}