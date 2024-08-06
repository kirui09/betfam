package com.betfam.apptea.ui.records

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.betfam.apptea.R
import com.betfam.apptea.DBHelper
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RecordsViewModel(private val appContext: Context) : ViewModel() {



    private val dbHelper = DBHelper.getInstance()

    private val _teaRecords = MutableLiveData<List<TeaPaymentRecord>>()
    val teaRecords: LiveData<List<TeaPaymentRecord>> get() = _teaRecords

    // Method to fetch tea records from the database
    fun fetchTeaRecords() {
        val records = dbHelper.getAllTeaRecords()
        _teaRecords.value = records
    }

    // Method to refresh records after adding new data
    fun refreshRecords() {
        fetchTeaRecords()
    }



    private val syncMutex = Mutex()
    private val isSyncing = AtomicBoolean(false)

    fun syncAndCompareDataWithGoogleSheet() {
        GlobalScope.launch(Dispatchers.IO) {
            if (isSyncing.getAndSet(true)) {
                Log.d("SyncWithGoogleSheet", "Sync already in progress, skipping this call")
                return@launch
            }

            syncMutex.withLock {
                try {
                    val credential = getGoogleAccountCredential()
                    val sheetsService = setupSheetsService(credential)
                    val spreadsheetId = getSpreadsheetIdFromDrive(credential)

                    if (spreadsheetId != null) {
                        val localRecordsToDelete = dbHelper.getPaymentRecordstodelete()
                        val sheetRecordsbeforedelete = fetchTeaRecords(sheetsService, spreadsheetId)

                        deleteRecordsFromSheet(sheetsService, spreadsheetId, localRecordsToDelete, sheetRecordsbeforedelete)
                        val idsToDelete = localRecordsToDelete.map { it.id }
                        idsToDelete.forEach { id ->
                            dbHelper.deleteRecord(id)
                        }

                        val localRecords = dbHelper.getPaymentRecords()
                        val sheetRecords = fetchTeaRecords(sheetsService, spreadsheetId)
                        val newTeaRecords = localRecords.filter { localRecord ->
                            sheetRecords.none { it.id == localRecord.id }
                        }

                        val newValues = newTeaRecords.map { record ->
                            listOf(
                                record.id,
                                record.date,
                                record.company,
                                record.employees,
                                record.kilos,
                                record.payment
                            )
                        }
                        if (newValues.isNotEmpty()) {
                            val appendRange = "Sheet1!A:F"
                            val appendBody = ValueRange().setValues(newValues)

                            try {
                                Log.d(
                                    "SheetUpdater",
                                    "Appending new records with values: ${newValues.joinToString(", ")}"
                                )

                                sheetsService.spreadsheets().values()
                                    .append(spreadsheetId, appendRange, appendBody)
                                    .setValueInputOption("RAW")
                                    .setInsertDataOption("INSERT_ROWS")
                                    .execute()

                                Log.d("SheetUpdater", "Successfully appended new records")
                            } catch (e: Exception) {
                                Log.e("SheetUpdater", "Failed to append new records", e)
                            }
                        }

                        val recordsToUpdate = localRecords.filter { localRecord ->
                            val sheetRecord = sheetRecords.find { it.id == localRecord.id }
                            sheetRecord != null && (
                                    sheetRecord.date != localRecord.date ||
                                            sheetRecord.company != localRecord.company ||
                                            sheetRecord.employees != localRecord.employees ||
                                            sheetRecord.kilos != localRecord.kilos ||
                                            sheetRecord.payment != localRecord.payment
                                    )
                        }

                        val updateValues = recordsToUpdate.map { record ->
                            listOf(record.id, record.date, record.company, record.employees, record.kilos, record.payment)
                        }
                        for ((index, updateValue) in updateValues.withIndex()) {
                            val rowIndex = sheetRecords.indexOfFirst { it.id == updateValue[0] } + 2
                            val updateRange = "Sheet1!A$rowIndex:F$rowIndex"
                            val updateBody = ValueRange().setValues(listOf(updateValue))

                            try {
                                Log.d("SheetUpdater", "Updating row $rowIndex with values: ${updateValue.joinToString(", ")}")

                                sheetsService.spreadsheets().values().update(spreadsheetId, updateRange, updateBody)
                                    .setValueInputOption("RAW")
                                    .execute()

                                Log.d("SheetUpdater", "Successfully updated row $rowIndex")
                            } catch (e: Exception) {
                                Log.e("SheetUpdater", "Failed to update row $rowIndex", e)
                            }
                        }

                        val updatedSheetRecords = fetchTeaRecords(sheetsService, spreadsheetId)

                        dbHelper.insertOrUpdateTeaRecords(updatedSheetRecords)

                        val newRecords = updatedSheetRecords.filter { sheetRecord ->
                            localRecords.none { it.id == sheetRecord.id }
                        }
                        if (newRecords.isNotEmpty()) {
                            dbHelper.insertOrUpdateTeaRecords(newRecords)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SyncWithGoogleSheet", "Error syncing with Google Sheet", e)
                } finally {
                    isSyncing.set(false)
                }
            }
        }
    }

    private fun deleteRecordsFromSheet(
        sheetsService: Sheets,
        spreadsheetId: String,
        recordsToDelete: List<TeaPaymentRecord>,
        sheetRecords: List<TeaPaymentRecord>
    ) {
        val idToRowMap = sheetRecords.mapIndexed { index, record -> record.id to index }.toMap()
        val rowsToDelete = recordsToDelete.mapNotNull { record ->
            idToRowMap[record.id]?.let { it + 2 }  // +2 because sheet rows are 1-indexed and we have a header row
        }.sortedDescending()  // Sort in descending order to delete from bottom to top

        val batchRequests = rowsToDelete.map { rowIndex ->
            Request().setDeleteDimension(
                DeleteDimensionRequest().setRange(
                    DimensionRange()
                        .setSheetId(0)
                        .setDimension("ROWS")
                        .setStartIndex(rowIndex - 1)
                        .setEndIndex(rowIndex)
                )
            )
        }

        if (batchRequests.isNotEmpty()) {
            try {
                val requestBody = BatchUpdateSpreadsheetRequest().setRequests(batchRequests)
                sheetsService.spreadsheets().batchUpdate(spreadsheetId, requestBody).execute()
                Log.d("SheetDeleter", "Successfully deleted ${batchRequests.size} rows")
            } catch (e: Exception) {
                Log.e("SheetDeleter", "Failed to delete rows", e)
                throw e  // Re-throw the exception to be caught by the outer try-catch block
            }
        } else {
            Log.d("SheetDeleter", "No matching records found to delete")
        }
    }


    private fun fetchTeaRecords(sheetsService: Sheets, spreadsheetId: String): List<TeaPaymentRecord> {
        val range = "Sheet1!A:F"
        val response = sheetsService.spreadsheets().values().get(spreadsheetId, range).execute()
        val values = response.getValues()

        val recordsFromSheet = mutableListOf<TeaPaymentRecord>()
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
                val payment = if (row.size >= 6) row[5].toString().toDoubleOrNull() ?: 0.0 else 0.0
                recordsFromSheet.add(TeaPaymentRecord(id, date, company, employee, kilos, payment))
            }
        }
        return recordsFromSheet
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


    companion object {
        fun create(appContext: Context) = RecordsViewModel(appContext)
    }
}