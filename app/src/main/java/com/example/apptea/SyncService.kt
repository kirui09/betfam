package com.betfam.apptea

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.betfam.apptea.ui.companies.Company
import com.betfam.apptea.ui.employees.Employee
import com.betfam.apptea.ui.records.PendingTeaRecordDao
import com.betfam.apptea.ui.records.Record
import com.betfam.apptea.ui.records.TeaPaymentRecord
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
import com.google.api.services.sheets.v4.model.DeleteRangeRequest
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SyncService : JobService() {

    private lateinit var pendingTeaRecordDao: PendingTeaRecordDao
    private lateinit var dbHelper: DBHelper

    override fun onCreate() {
        super.onCreate()
        dbHelper = DBHelper(this)
       // pendingTeaRecordDao = App.getDatabase(this).pendingTeaRecordDao()
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        if (isConnectedToInternet(applicationContext)) {
            Log.d(TAG, "Internet is connected, starting data sync")
            GlobalScope.launch(Dispatchers.IO) {

                syncAndCompareDataWithGoogleSheet()
            }
        } else {
            Log.d(TAG, "Internet is not connected, skipping data sync")
            jobFinished(params, false)
        }
        // Return true to keep the job scheduled
        return true
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_IMMEDIATE_SYNC -> {
                GlobalScope.launch(Dispatchers.IO) {
                    syncAndCompareDataWithGoogleSheet()
                }
            }
        }
        return START_NOT_STICKY
    }
    override fun onStopJob(params: JobParameters?): Boolean {
        // Optionally handle job cancellation here
        return true
    }
/*
    private suspend fun syncPendingData(params: JobParameters?) {
        Log.d(TAG, "failing ")

        val pendingData = dbHelper.getAllPendingTeaRecords()

        Log.d(TAG, "failing $pendingData")
        if (pendingData.isNotEmpty()) {
            Log.d(TAG, "Syncing ${pendingData.size} pending data records")
            for (data in pendingData) {
                val record = Record(
                    id = data.id,
                    date = data.date,
                    company = data.company,
                    employee = data.employees,
                    kilos = data.kilos.toDouble(),
                    pay=data.payment
                )

                syncAndCompareDataWithGoogleSheet(record, applicationContext)
               // pendingSyncDataDao.delete(data)
            }
            Log.d(TAG, "Pending data sync completed successfully")
            jobFinished(params, false)
            showSyncSuccessNotification() // Call the notification function here
        } else {
            Log.d(TAG, "No pending data to sync")
            jobFinished(params, false)
        }
    }*/

    private fun showSyncSuccessNotification() {
        Log.d(ContentValues.TAG, "Creating sync success notification")

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "sync_channel"
        val channelName = "Sync Channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle("Tea Records Synced Successful")
            .setContentText("All unsynchronized data has been sent to the Google Sheet.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(0, builder.build())
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
                        // New code for Company Records (Sheet2)

                        val sheetCompanies = fetchCompanyRecords(sheetsService, spreadsheetId)

                        // Delete companies from Sheet2
                         val localCompaniesToDelete = dbHelper.getCompanyRecordsToDelete()
                        Log.d("SyncCompanies", "Local companies to delete: $localCompaniesToDelete")
                          deleteCompaniesFromSheet(sheetsService, spreadsheetId, localCompaniesToDelete, sheetCompanies,"Companies")
                          val companyIdsToDelete = localCompaniesToDelete.map { it.name }
                        Log.d("SyncCompanies", "Company IDs to delete: $companyIdsToDelete")
                        companyIdsToDelete.forEach { name ->
                            val isDeleted = dbHelper.deleteCompany(name)
                            if (isDeleted) {
                                Log.d("CompanyDeletion", "Successfully deleted company: $name")
                            } else {
                                Log.w("CompanyDeletion", "Failed to delete company: $name")
                            }
                        }

                        // Append new companies to Sheet2
                        val companies = dbHelper.getAllCompanies()
                        val newCompanies = companies.filter { localCompany ->
                            sheetCompanies.none { it.name == localCompany.name }
                        }
                        if (newCompanies.isNotEmpty()) {
                            appendCompaniesToSheet(sheetsService, spreadsheetId, newCompanies)
                        }

                        // Update existing companies in Sheet2
                        val companiesToUpdate = companies.filter { localCompany ->
                            val sheetCompany = sheetCompanies.find { it.id == localCompany.id }
                            sheetCompany != null && (
                                    sheetCompany.name != localCompany.name ||
                                            sheetCompany.location != localCompany.location ||
                                            sheetCompany.synced != localCompany.synced
                                    )
                        }
                        updateCompaniesInSheet(sheetsService, spreadsheetId, companiesToUpdate, sheetCompanies)

                        // Fetch updated company records from Sheet2
                        val updatedSheetCompanies = fetchCompanyRecords(sheetsService, spreadsheetId)

                        // Update local database with new or updated company records from Sheet2
                        dbHelper.insertOrUpdateCompanyRecords(updatedSheetCompanies)

                        val newCompanyRecords = updatedSheetCompanies.filter { sheetCompany ->
                            companies.none { it.name == sheetCompany.name }
                        }
                        if (newCompanyRecords.isNotEmpty()) {
                            dbHelper.insertOrUpdateCompanyRecords(newCompanyRecords)
                        }
                        // New code for Employees Records (Sheet3)

                        val sheetEmployees = fetchEmployeeRecords(sheetsService, spreadsheetId)

                         // Delete employees from Sheet
                        val localEmployeesToDelete = dbHelper.getEmployeesRecordsToDelete()
                        Log.d("SyncEmployees", "Local employees to delete: $localEmployeesToDelete")

                        deleteEmployeesFromSheet(sheetsService, spreadsheetId, localEmployeesToDelete, sheetEmployees, "Employees")

                        localEmployeesToDelete.forEach { employee ->
                            val isDeleted = dbHelper.deleteEmployee(employee)
                            if (isDeleted) {
                                Log.d("EmployeeDeletion", "Successfully deleted employee: ${employee.name}")
                            } else {
                                Log.w("EmployeeDeletion", "Failed to delete employee: ${employee.name}")
                            }
                        }
                        // Append new employees to Sheet3
                        val employees = dbHelper.getAllEmployees()
                        val newemployees = employees.filter { localEmployee ->
                            sheetEmployees.none { it.name == localEmployee.name }
                        }
                        if (newemployees.isNotEmpty()) {
                            appendEmployeesToSheet(sheetsService, spreadsheetId, newemployees)
                        }
                        // Update existing companies in Sheet2
                        val employeesToUpdate = employees.filter { localEmployee ->
                            val sheetEmployee = sheetEmployees.find { it.name == localEmployee.name }
                            sheetEmployee != null && (

                                            sheetEmployee.empType != localEmployee.empType ||
                                            sheetEmployee.age != localEmployee.age ||
                                                    sheetEmployee.phoneNumber != localEmployee.phoneNumber ||
                                                    sheetEmployee.employeeId != localEmployee.employeeId

                                    )
                        }
                        updateEmployeesInSheet(sheetsService, spreadsheetId, employeesToUpdate, sheetEmployees)
                        // Fetch updated company records from Sheet2
                        val updatedSheetEmployee = fetchEmployeeRecords(sheetsService, spreadsheetId)

                        // Update local database with new or updated company records from Sheet2
                        dbHelper.insertOrUpdateEmployeeRecords(updatedSheetEmployee)

                        val newEmployeesRecords = updatedSheetEmployee.filter { sheetEmployee ->
                            companies.none { it.name == sheetEmployee.name }
                        }
                        if (newEmployeesRecords.isNotEmpty()) {
                            dbHelper.insertOrUpdateEmployeeRecords(newEmployeesRecords)
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
    // New helper functions for Company operations

    private fun fetchCompanyRecords(sheetsService: Sheets, spreadsheetId: String): List<Company> {
        val range = "Companies!A:D"
        val response = sheetsService.spreadsheets().values().get(spreadsheetId, range).execute()
        val values = response.getValues()

        return values?.drop(1)?.mapNotNull { row ->
            try {
                Company(
                    id = row.getOrNull(0)?.toString()?.toIntOrNull() ?: return@mapNotNull null,
                    name = row.getOrNull(1)?.toString() ?: "",
                    location = row.getOrNull(2)?.toString() ?: "",
                    synced = row.getOrNull(3)?.toString()?.toIntOrNull() ?: 0
                )
            } catch (e: Exception) {
                Log.e("FetchCompanyRecords", "Error parsing row: $row", e)
                null
            }
        } ?: emptyList()
    }
    // New helper functions for Company operations

    private fun fetchEmployeeRecords(sheetsService: Sheets, spreadsheetId: String): List<com.betfam.apptea.ui.employees.Employee> {
        val range = "Employees!A:F"
        val response = sheetsService.spreadsheets().values().get(spreadsheetId, range).execute()
        val values = response.getValues()

        return values?.drop(1)?.mapNotNull { row ->
            try {
                Employee(
                    id = row.getOrNull(0)?.toString()?.toLongOrNull(),
                    name = row.getOrNull(1)?.toString(),
                    empType = row.getOrNull(2)?.toString(),
                    age = row.getOrNull(3)?.toString(),
                    phoneNumber = row.getOrNull(4)?.toString(),
                    employeeId = row.getOrNull(5)?.toString()
                )
            } catch (e: Exception) {
                Log.e("FetchEmployeeRecords", "Error parsing row: $row", e)
                null
            }
        } ?: emptyList()
    }

    private fun deleteCompaniesFromSheet(
        sheetsService: Sheets,
        spreadsheetId: String,
        companiesToDelete: List<Company>,
        sheetCompanies: List<Company>,
        sheetName: String) {
// Get the sheet ID
        // Get the sheet ID
        val spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute()
        val sheet = spreadsheet.sheets.firstOrNull { it.properties.title == sheetName }
        val sheetId = sheet?.properties?.sheetId
        if (sheetId == null) {
            Log.e("SheetDeletercompanies", "Sheet ID not found for sheet: $sheetName")
            return
        }

            for (companyToDelete in companiesToDelete) {
            val rowIndex = sheetCompanies.indexOfFirst { it.name.equals(companyToDelete.name, ignoreCase = true) }
            if (rowIndex != -1) {

                try {
                    val requestBody = BatchUpdateSpreadsheetRequest().setRequests(
                        listOf(
                            Request().setDeleteRange(
                                DeleteRangeRequest()
                                    .setRange(
                                        GridRange()
                                            .setSheetId(sheetId) // You might need to set the sheetId explicitly
                                            .setStartRowIndex(rowIndex+1)
                                            .setEndRowIndex(rowIndex + 2)
                                    )
                                    .setShiftDimension("ROWS")
                            )
                        )
                    )

                    Log.d("SheetDeletercompanies", "Attempting to delete row ${rowIndex + 1} with company name: ${companyToDelete.name}")

                    val response = sheetsService.spreadsheets().batchUpdate(spreadsheetId, requestBody).execute()

                    Log.d("SheetDeletercompanies", "API Response: ${response.toPrettyString()}")

                    if (response.replies != null && response.replies.isNotEmpty()) {
                        Log.d("SheetDeletercompanies", "Successfully deleted row ${rowIndex + 1} for company: ${companyToDelete.name}")
                    } else {
                        Log.w("SheetDeletercompanies", "No changes made for row ${rowIndex + 1}, company: ${companyToDelete.name}")
                    }

                    // Verify deletion
                    val range = "$sheetName!A${rowIndex + 1}:F${rowIndex + 1}"
                    val readResult = sheetsService.spreadsheets().values().get(spreadsheetId, range).execute()
                    if (readResult.values == null || readResult.values.isEmpty()) {
                        Log.d("SheetDeletercompanies", "Verified: Row ${rowIndex + 1} is now empty or deleted")
                    } else {
                        Log.w("SheetDeletercompanies", "Verification failed: Row ${rowIndex + 1} still contains data")
                    }

                } catch (e: Exception) {
                    Log.e("SheetDeletercompanies", "Failed to delete row ${rowIndex + 1} for company: ${companyToDelete.name}", e)
                }
            } else {
                Log.w("SheetDeletercompanies", "Company not found in sheet: ${companyToDelete.name}")
            }
        }
    }

    private fun deleteEmployeesFromSheet(
        sheetsService: Sheets,
        spreadsheetId: String,
        employeesToDelete: List<Employee>,
        sheetemployees: List<Employee>,
        sheetName: String) {
// Get the sheet ID
        // Get the sheet ID
        val spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute()
        val sheet = spreadsheet.sheets.firstOrNull { it.properties.title == sheetName }
        val sheetId = sheet?.properties?.sheetId
        if (sheetId == null) {
            Log.e("SheetDeleteremployees", "Sheet ID not found for sheet: $sheetName")
            return
        }

        for (employeesToDelete in employeesToDelete) {
            val rowIndex = sheetemployees.indexOfFirst { it.name.equals(employeesToDelete.name, ignoreCase = true) }
            if (rowIndex != -1) {

                try {
                    val requestBody = BatchUpdateSpreadsheetRequest().setRequests(
                        listOf(
                            Request().setDeleteRange(
                                DeleteRangeRequest()
                                    .setRange(
                                        GridRange()
                                            .setSheetId(sheetId) // You might need to set the sheetId explicitly
                                            .setStartRowIndex(rowIndex+1)
                                            .setEndRowIndex(rowIndex + 2)
                                    )
                                    .setShiftDimension("ROWS")
                            )
                        )
                    )

                    Log.d("SheetDeleteremployees", "Attempting to delete row ${rowIndex + 1} with employee name: ${employeesToDelete.name}")

                    val response = sheetsService.spreadsheets().batchUpdate(spreadsheetId, requestBody).execute()

                    Log.d("SheetDeleteremployees", "API Response: ${response.toPrettyString()}")

                    if (response.replies != null && response.replies.isNotEmpty()) {
                        Log.d("SheetDeleteremployees", "Successfully deleted row ${rowIndex + 1} for employee: ${employeesToDelete.name}")
                    } else {
                        Log.w("SheetDeleteremployees", "No changes made for row ${rowIndex + 1}, employee: ${employeesToDelete.name}")
                    }

                    // Verify deletion
                    val range = "$sheetName!A${rowIndex + 1}:F${rowIndex + 1}"
                    val readResult = sheetsService.spreadsheets().values().get(spreadsheetId, range).execute()
                    if (readResult.values == null || readResult.values.isEmpty()) {
                        Log.d("SheetDeleteremployees", "Verified: Row ${rowIndex + 1} is now empty or deleted")
                    } else {
                        Log.w("SheetDeleteremployees", "Verification failed: Row ${rowIndex + 1} still contains data")
                    }

                } catch (e: Exception) {
                    Log.e("SheetDeleteremployees", "Failed to delete row ${rowIndex + 1} for company: ${employeesToDelete.name}", e)
                }
            } else {
                Log.w("SheetDeletercompanies", "Company not found in sheet: ${employeesToDelete.name}")
            }
        }
    }


    private fun appendCompaniesToSheet(sheetsService: Sheets, spreadsheetId: String, newCompanies: List<com.betfam.apptea.ui.companies.Company>) {
        val appendRange = "Companies!A:D"
        val appendBody = ValueRange().setValues(newCompanies.map { company ->
            listOf(company.id, company.name, company.location, company.synced)
        })

        sheetsService.spreadsheets().values()
            .append(spreadsheetId, appendRange, appendBody)
            .setValueInputOption("RAW")
            .setInsertDataOption("INSERT_ROWS")
            .execute()
    }
    private fun appendEmployeesToSheet(sheetsService: Sheets, spreadsheetId: String, newEmployees: List<Employee>) {
        val appendRange = "Employees!A:H"
        val appendBody = ValueRange().setValues(newEmployees.map { employees ->
            listOf(employees.id, employees.name, employees.empType, employees.age,employees.phoneNumber,employees.employeeId)
        })

        sheetsService.spreadsheets().values()
            .append(spreadsheetId, appendRange, appendBody)
            .setValueInputOption("RAW")
            .setInsertDataOption("INSERT_ROWS")
            .execute()
    }

    private fun updateCompaniesInSheet(sheetsService: Sheets, spreadsheetId: String, companiesToUpdate: List<Company>, sheetCompanies: List<Company>) {
        companiesToUpdate.forEach { company ->
            val rowIndex = sheetCompanies.indexOfFirst { it.id == company.id } + 2
            val updateRange = "Companies!A$rowIndex:D$rowIndex"
            val updateBody = ValueRange().setValues(listOf(
                listOf(company.id, company.name, company.location, company.synced)
            ))

            sheetsService.spreadsheets().values().update(spreadsheetId, updateRange, updateBody)
                .setValueInputOption("RAW")
                .execute()
        }
    }
    private fun updateEmployeesInSheet(sheetsService: Sheets, spreadsheetId: String,employeesToupdate : List<Employee>, sheetEmployee : List<Employee>) {
        employeesToupdate.forEach { employees ->
            val rowIndex = sheetEmployee.indexOfFirst { it.name == employees.name} + 2
            val updateRange = "Employees!A$rowIndex:H$rowIndex"
            val updateBody = ValueRange().setValues(listOf(
                listOf( employees.id,employees.name,employees.empType, employees.age,employees.phoneNumber,employees.employeeId)
            ))

            sheetsService.spreadsheets().values().update(spreadsheetId, updateRange, updateBody)
                .setValueInputOption("RAW")
                .execute()
        }
    }


    private fun deleteRecordsFromSheet(
        sheetsService: Sheets,
        spreadsheetId: String,
        recordsToDelete: List<TeaPaymentRecord>,
        sheetRecords: List<TeaPaymentRecord>
    ) {
        for (recordToDelete in recordsToDelete) {
            val rowIndex = sheetRecords.indexOfFirst { it.id == recordToDelete.id }
            if (rowIndex != -1) {
                val deleteRange = "Sheet1!A${rowIndex + 2}:F${rowIndex + 2}"
                try {
                    val requestBody = BatchUpdateSpreadsheetRequest().setRequests(
                        listOf(
                            Request().setDeleteDimension(
                                DeleteDimensionRequest().setRange(
                                    DimensionRange()
                                        .setSheetId(0)
                                        .setDimension("ROWS")
                                        .setStartIndex(rowIndex + 1)
                                        .setEndIndex(rowIndex + 2)
                                )
                            )
                        )
                    )

                    Log.d("SheetDeleter", "Deleting row ${rowIndex + 2} with ID: ${recordToDelete.id}")

                    sheetsService.spreadsheets().batchUpdate(spreadsheetId, requestBody).execute()

                    Log.d("SheetDeleter", "Successfully deleted row ${rowIndex + 2}")
                } catch (e: Exception) {
                    Log.e("SheetDeleter", "Failed to delete row ${rowIndex + 2}", e)
                }
            }
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

   /* private fun sendDataToGoogleSheet(record: Record, context: Context) {
        GlobalScope.launch(Dispatchers.Main) {
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val credential = getGoogleAccountCredential()
                val sheetsService = setupSheetsService(credential)
                // Get the spreadsheet ID from Google Drive
                val spreadsheetId = getSpreadsheetIdFromDrive(credential)
                if (spreadsheetId != null) {
                    val range = "Sheet1!A:F" // Adjust the range based on your needs.
                    val valueRange = ValueRange().setValues(
                        listOf(listOf(record.id, record.date, record.company, record.employee, record.kilos))
                    )
                    val append = sheetsService.spreadsheets().values().append(spreadsheetId, range, valueRange)
                        .setValueInputOption("USER_ENTERED")
                    val response = append.execute()
                    withContext(Dispatchers.Main) {
                        Log.d(ContentValues.TAG, "Data sent to Google Sheets")
                        Toast.makeText(context, "Data sent to Google Sheets", Toast.LENGTH_SHORT).show()
                        val dbHelper = DBHelper(context)
                        val appDatabase = App.getDatabase(context)
                        dbHelper.updateTeaRecordSyncStatus(record.id)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e(ContentValues.TAG, "Google Sheet file not found in Google Drive")
                        Toast.makeText(context, "Google Sheet file not found in Google Drive", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(ContentValues.TAG, "Failed to send data to Google Sheet", e)
                    Toast.makeText(context, "Failed to send data to Google Sheet: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }*/



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
        val sharedPreferences = applicationContext.getSharedPreferences("user_details", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("user_id", null)
        val token = sharedPreferences.getString("id_token", null)
        if (email != null && token != null) {
            val credential = GoogleAccountCredential.usingOAuth2(
                applicationContext, listOf(SheetsScopes.SPREADSHEETS)
            )
            credential.setSelectedAccountName(email)
            cont.resume(credential)
        } else {
            cont.resumeWithException(Exception("User details not found"))
        }
    }

    // This function sets up the Sheets service
    private fun setupSheetsService(credential: GoogleAccountCredential): Sheets {
        val transport = NetHttpTransport()
        val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
        return Sheets.Builder(transport, jsonFactory, credential)
            .setApplicationName(getString(R.string.app_name))
            .build()
    }

    fun isConnectedToInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
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


    companion object {
        private const val TAG = "SyncService"
        fun scheduleSync(context: Context) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val jobInfo = JobInfo.Builder(1, ComponentName(context, SyncService::class.java))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000) // Run every 15 minutes
                .build()
            jobScheduler.schedule(jobInfo)
        }
        fun startImmediateSync(context: Context) {
            val intent = Intent(context, SyncService::class.java)
            intent.action = ACTION_IMMEDIATE_SYNC
            context.startService(intent)
        }

        private const val ACTION_IMMEDIATE_SYNC = "ACTION_IMMEDIATE_SYNC"
    }
}

