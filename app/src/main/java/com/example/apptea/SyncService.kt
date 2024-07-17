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
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.betfam.apptea.ui.records.PendingTeaRecordDao
import com.betfam.apptea.ui.records.Record
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

                syncPendingData(params)
            }
        } else {
            Log.d(TAG, "Internet is not connected, skipping data sync")
            jobFinished(params, false)
        }
        // Return true to keep the job scheduled
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        // Optionally handle job cancellation here
        return true
    }

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

                sendDataToGoogleSheet(record, applicationContext)
               // pendingSyncDataDao.delete(data)
            }
            Log.d(TAG, "Pending data sync completed successfully")
            jobFinished(params, false)
            showSyncSuccessNotification() // Call the notification function here
        } else {
            Log.d(TAG, "No pending data to sync")
            jobFinished(params, false)
        }
    }

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

    private fun sendDataToGoogleSheet(record: Record, context: Context) {
        GlobalScope.launch(Dispatchers.Main) {
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
    }
}

