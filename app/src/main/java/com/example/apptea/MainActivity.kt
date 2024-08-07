package com.betfam.apptea

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.betfam.apptea.databinding.ActivityMainBinding
import com.betfam.apptea.ui.home.HomeFragment
import com.betfam.apptea.ui.records.RecordsViewModel
import com.betfam.apptea.ui.records.RecordsViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.material.navigation.NavigationView
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.CellData
import com.google.api.services.sheets.v4.model.CellFormat
import com.google.api.services.sheets.v4.model.Color
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.RepeatCellRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 123


    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var dbh: DBHelper
    private lateinit var headerView: View
    private var blinkingAnimation: Animation? = null
    private var isUserSignedIn: Boolean = false

    private lateinit var googleCloudSignUp: ImageButton
    private lateinit var btnSyncRecord: com.google.android.material.button.MaterialButton
    private lateinit var viewModel: RecordsViewModel


    val CITY: String = "Litein, KE"
    val API: String = "1a105b90f41489e05ece19d6f6c326b9" // Use API key


    private lateinit var googleSignInClient: GoogleSignInClient


    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            Log.e(TAG, "successful")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)

                if (account != null) {
                    val userId = account.email


                    val idToken = account.idToken
                    if (userId != null) {
                        if (idToken != null) {
                            saveUserDetailsToSharedPreferences(userId, idToken)
                            createSheetAndAddHeader(userId)
                        }
                    }
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign-in failed", e)
            }

        }
        Log.w(TAG, "Google Sign-In failed with resultCode: ${result.resultCode}")



    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if notification permission is granted
//        if (!isNotificationPermissionGranted()) {
//            showNotificationPermissionDialog()
//        }
        Log.d("Navigation", "Testing if it gets here")

        DBHelper.init(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS), Scope(DriveScopes.DRIVE_FILE))
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
       // val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        Log.d("Navigation", "Testing if it gets here")
        appBarConfiguration = AppBarConfiguration(
            setOf(

                R.id.nav_home, R.id.nav_records, R.id.nav_employees_menu, R.id.nav_managers, R.id.nav_companies,
                R.id.nav_payment_types
            ), drawerLayout

        )
        navView.setCheckedItem(R.id.nav_home)


        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    if (navController.currentDestination?.id == R.id.nav_records) {
                        navController.navigate(R.id.action_records_to_home)
                    } else {
                        navController.navigate(R.id.nav_home)
                    }
                    drawerLayout.closeDrawers()
                     true
                }
                R.id.nav_records -> {
                    Log.d("Navigation", "Tea Records clicked")
                    navController.navigate(R.id.nav_records)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_employees_menu -> {
                    Log.d("Navigation", "Employees clicked")
                    navController.navigate(R.id.nav_employees_menu)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_managers -> {
                    navController.navigate(R.id.nav_managers)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_companies -> {
                    navController.navigate(R.id.nav_companies)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_payment_types -> {
                    navController.navigate(R.id.nav_payment_types)
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }


        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        sharedPreferencesHelper = SharedPreferencesHelper(this)
        dbh = DBHelper(this)

        headerView = navView.getHeaderView(0)
        updateNavigationHeader()

        fetchWeatherData()

        googleCloudSignUp = findViewById(R.id.googleSignUpButton)
        btnSyncRecord = findViewById(R.id.btnSyncRecord)

        blinkingAnimation = AnimationUtils.loadAnimation(this, R.anim.blink_animation)

        isUserSignedIn = isUserSignedIn()

        if (!isUserSignedIn) {
            googleCloudSignUp.startAnimation(blinkingAnimation)
            AlertDialog.Builder(this)
                .setTitle("Login Required")
                .setMessage("You need to log in to access all features of the application.")
                .setPositiveButton("Log In") { _, _ -> signIn() }
                .setNegativeButton("Cancel") { dialog, _ ->
                    showRiskWarningDialog()
                    dialog.dismiss() // Dismiss the initial dialog
                }
                .show()
        }

        googleCloudSignUp.setOnClickListener {

            val sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE)
            val userEmail = sharedPreferences.getString("user_id", null)
            if (userEmail != null) {
                showUserEmailDialog(userEmail)
            } else {
                signIn()
                googleCloudSignUp.clearAnimation()
            }
        }
        btnSyncRecord.setOnClickListener{

            SyncService.startImmediateSync(this)
            Toast.makeText(this, "Sync started", Toast.LENGTH_SHORT).show()

        }




        val sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        val idToken = sharedPreferences.getString("id_token", null)

        // Trigger synchronization process
        SyncService.scheduleSync(this)



    }
    private fun showRiskWarningDialog() {
        AlertDialog.Builder(this)
            .setTitle("Warning")
            .setMessage("If you do not log in, you may not be able to back up your data. Do you understand the risk?")
            .setPositiveButton("Yes, I understand") { dialog, _ ->
                // User acknowledges the risk
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                // Optionally: Do something else or just dismiss
                AlertDialog.Builder(this)
                    .setTitle("Login Required")
                    .setMessage("You need to log in to access all features of the application.")
                    .setPositiveButton("Log In") { _, _ -> signIn() }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        showRiskWarningDialog()
                        dialog.dismiss() // Dismiss the initial dialog
                    }
                    .show()
            }
            .show()
    }
    fun openTeaRecordsDrawer() {
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Check if we're not already on the Tea Records fragment
        if (navController.currentDestination?.id != R.id.nav_records) {
            // Navigate to the Tea Records fragment
            navController.navigate(R.id.nav_records)
        }

        // Update the checked item in the navigation view
        binding.navView.setCheckedItem(R.id.nav_records)

        // Close the drawer
        binding.drawerLayout.closeDrawers()
    }
   


    private fun isUserSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        return account != null

    }

    private fun stopBlinkingAnimation() {
        googleCloudSignUp.clearAnimation()
    }

    private fun showUserEmailDialog(email: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("User Email")
        builder.setMessage("Logged in as: $email")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setNegativeButton("Logout") { dialog, _ ->
            // Show a confirmation dialog before logging out
            val logoutConfirmationDialog = AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    // Handle logout logic here
                    googleSignInClient.signOut().addOnCompleteListener(this) { signOutTask ->
                        if (signOutTask.isSuccessful) {
                            // Clear user details from SharedPreferences
                            val sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.clear()
                            editor.apply()

                            // Clear the data from the SQLite database
                            try {
                                dbh.clearData()
                            } catch (e: Exception) {
                                Log.e("LogoutError", "Failed to clear database: ${e.message}")
                                Toast.makeText(this, "Error clearing data. Please try again.", Toast.LENGTH_SHORT).show()
                                return@addOnCompleteListener
                            }

                            // Set the signed-in status flag to false
                            isUserSignedIn = false

                            // Show a success message
                            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

                            // Redirect the user to the sign-in screen or main screen
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            // Handle sign-out failure
                            Toast.makeText(this, "Sign out failed. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("No", null)
                .create()

            logoutConfirmationDialog.show()
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }



    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }


    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            // User signed in successfully
            // Update UI as needed
            // Save user details if required
            account.email?.let { account.idToken?.let { it1 ->
                saveUserDetailsToSharedPreferences(it,
                    it1
                )
            } }
            // Update the signed-in status flag
            isUserSignedIn = true

            // Display a toast message with the user ID
            Toast.makeText(this, "User ID: ${account.id}", Toast.LENGTH_SHORT).show()
        } else {
            // Handle sign-in failure
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun updateNavigationHeader() {
        // Retrieve the saved phone number from SharedPreferences
        val savedPhoneNumber = sharedPreferencesHelper.getPhoneNumber()

        // Use the phone number to fetch user information from the database
        val userInformation = dbh.getUserInformationByPhoneNumber(savedPhoneNumber)

        // Check if userInformation is not null before accessing user details
        if (userInformation != null) {
            val fullNameTextView: TextView = headerView.findViewById(R.id.textViewFirstName)
            fullNameTextView.text = "${userInformation.firstName} ${userInformation.lastName}"

            // Update dashboard text view in HomeFragment
            val homeFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)?.childFragmentManager?.fragments?.find { it is HomeFragment } as? HomeFragment
            homeFragment?.updateDashboardText(userInformation.firstName)
        }
    }

    private fun fetchWeatherData() {
        WeatherTask().execute()
    }

    inner class WeatherTask : AsyncTask<String, Void, WeatherInfo>() {
        override fun doInBackground(vararg params: String?): WeatherInfo? {
            var weatherInfo: WeatherInfo? = null
            try {
                val response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API").readText(
                    Charsets.UTF_8
                )

                val jsonObj = JSONObject(response)
                val main = jsonObj.getJSONObject("main")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val temperature = main.getString("temp") + "°C"
                val description = weather.getString("description")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
                val city = jsonObj.getString("name") + ", " + jsonObj.getJSONObject("sys").getString("country")

                weatherInfo = WeatherInfo(temperature, description, city)
            } catch (e: Exception) {
                // Handle exceptions if necessary
            }
            return weatherInfo
        }

        override fun onPostExecute(result: WeatherInfo?) {
            super.onPostExecute(result)
            // Update UI elements in your HomeFragment using the result
            result?.let {
                val homeFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as? HomeFragment
                homeFragment?.handleWeatherResult(it)
            }
        }
    }

//    fun syncUnsyncedRecords(context: Context) {
//        val dbHelper = DBHelper(context)
//        val unsyncedRecords = dbHelper.getUnsyncedRecords()
//        if (unsyncedRecords.isNotEmpty()) {
//            // Use the existing sendRecordsToGoogleSheet method to sync
//            sendRecordsToGoogleSheet(unsyncedRecords)
//            // After successful sync, mark these records as 'synced' in your local database
//        }
//    }


    private fun saveUserDetailsToSharedPreferences(userId: String, idToken: String) {
        val sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_id", userId)
        editor.putString("id_token", idToken)
        editor.apply()
    }

    private fun saveSpreadsheetIdToDrive(credentials: GoogleAccountCredential, sheetId: String) {
        val drive = Drive.Builder(
            NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            credentials
        ).setApplicationName("BetFam(DoNotDelete)").build()

        val folderName = "BetFam(DoNotDelete)"
        val spreadsheetFileName = "BetFamSpreadSheet(DoNotDelete)"

        // Function to check if the folder with the specified name already exists
        fun getFolderId(drive: Drive, folderName: String): String? {
            val query = "name = '$folderName' and mimeType = 'application/vnd.google-apps.folder'"
            val result = drive.files().list().setQ(query).execute()
            return if (result.files.isNotEmpty()) {
                result.files[0].id
            } else {
                null
            }
        }

        // Function to create a new folder if it does not exist
        fun createFolder(drive: Drive, folderName: String): String {
            val folderMetadata = File()
            folderMetadata.name = folderName
            folderMetadata.mimeType = "application/vnd.google-apps.folder"
            return drive.files().create(folderMetadata).execute().id
        }

        // Get the folder ID or create a new folder
        val folderId = getFolderId(drive, folderName) ?: createFolder(drive, folderName)

        // Function to check if the file (Google Sheet) exists inside the folder
        fun getFileId(drive: Drive, folderId: String, fileName: String): String? {
            val query = "name = '$fileName' and '$folderId' in parents"
            val result = drive.files().list().setQ(query).execute()
            return if (result.files.isNotEmpty()) {
                result.files[0].id
            } else {
                null
            }
        }

        // Check if the file exists inside the folder
        val fileId = getFileId(drive, folderId, spreadsheetFileName)

        if (fileId != null) {
            // Update the properties of the existing file (Google Sheet) instead of creating a new one
            val fileMetadata = File()
            fileMetadata.properties = mapOf("spreadsheet_id" to sheetId)
            drive.files().update(fileId, fileMetadata).execute()
        } else {
            // Create a new file inside the folder
            val fileMetadata = File()
            fileMetadata.name = spreadsheetFileName
            fileMetadata.parents = listOf(folderId)
            fileMetadata.properties = mapOf("spreadsheet_id" to sheetId)

            try {
                val file = drive.files().create(fileMetadata).execute()
                Log.d(TAG, "Spreadsheet saved to Drive in folder: $folderId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save spreadsheet to Drive", e)
                // Handle the error here (e.g., inform user or retry)
            }
        }
    }

    private fun createSheetAndAddHeader(userId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val credential = GoogleAccountCredential.usingOAuth2(
                    applicationContext, listOf(SheetsScopes.SPREADSHEETS)
                )
                credential.setSelectedAccountName(userId)
                val sheetsService = Sheets.Builder(
                    NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
                )
                    .setApplicationName(getString(R.string.app_name))
                    .build()

                val drive = Drive.Builder(
                    NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
                ).setApplicationName("BetFam").build()

                val folderName = "BetFam(DoNotDelete)"
                val spreadsheetFileName = "BetFamSpreadSheet(DoNotDelete)"

                // Function to check if the folder with the specified name already exists
                fun getFolderId(drive: Drive, folderName: String): String? {
                    val query = "name = '$folderName' and mimeType = 'application/vnd.google-apps.folder'"
                    val result = drive.files().list().setQ(query).execute()
                    return if (result.files.isNotEmpty()) {
                        result.files[0].id
                    } else {
                        null
                    }
                }

                // Function to create a new folder if it does not exist
                fun createFolder(drive: Drive, folderName: String): String {
                    val folderMetadata = File()
                    folderMetadata.name = folderName
                    folderMetadata.mimeType = "application/vnd.google-apps.folder"
                    return drive.files().create(folderMetadata).execute().id
                }

                // Function to check if the file (Google Sheet) exists inside the folder
                fun getFileId(drive: Drive, folderId: String, fileName: String): String? {
                    val query = "name = '$fileName' and '$folderId' in parents"
                    val result = drive.files().list().setQ(query).execute()
                    return if (result.files.isNotEmpty()) {
                        result.files[0].id
                    } else {
                        null
                    }
                }

                // Get the folder ID or create a new folder
                val folderId = getFolderId(drive, folderName) ?: createFolder(drive, folderName)

                // Check if the file exists inside the folder
                val fileId = getFileId(drive, folderId, spreadsheetFileName)

                if (fileId == null) {
                    val spreadsheet = sheetsService.spreadsheets().create(
                        Spreadsheet().setProperties(
                            SpreadsheetProperties().setTitle("Tea Records")
                        )
                    ).execute()
                    val addSheetRequest = Request().setAddSheet(
                        AddSheetRequest().setProperties(
                            SheetProperties().setTitle("Companies")
                        )
                    )
                    val addSheetRequest2 = Request().setAddSheet(
                        AddSheetRequest().setProperties(
                            SheetProperties().setTitle("Employees")
                        )
                    )
                    val addSheetRequest3 = Request().setAddSheet(
                        AddSheetRequest().setProperties(
                            SheetProperties().setTitle("Farms")
                        )
                    )
                    val addSheetRequest4 = Request().setAddSheet(
                        AddSheetRequest().setProperties(
                            SheetProperties().setTitle("placeholder")
                        )
                    )
                    val addSheetRequest5 = Request().setAddSheet(
                        AddSheetRequest().setProperties(
                            SheetProperties().setTitle("placeholder2")
                        )
                    )
                    val addSheetRequest6 = Request().setAddSheet(
                        AddSheetRequest().setProperties(
                            SheetProperties().setTitle("placeholder3")
                        )
                    )
                    val batchUpdateRequest = BatchUpdateSpreadsheetRequest().setRequests(listOf(addSheetRequest,addSheetRequest2,addSheetRequest3,addSheetRequest4,addSheetRequest5,addSheetRequest6))
                    sheetsService.spreadsheets().batchUpdate(spreadsheet.spreadsheetId, batchUpdateRequest).execute()


                    // Add header row and set green background in a single BatchUpdate
                    val headerValues = listOf("ID", "Date", "Company", "EmployeeName", "Kilos" ,"Pay")
                    val valueRange = ValueRange() // Create a ValueRange object
                        .setValues(listOf(headerValues)) // Set the values

                    val updateValuesRequest = sheetsService.spreadsheets().values()
                        .update(spreadsheet.spreadsheetId, "A1:F1", valueRange)
                    updateValuesRequest.setValueInputOption("RAW") // Set the value input option here
                    updateValuesRequest.execute()

                    // Add headers to the second sheet (Companies)
                    val headerValues2 = listOf("id", "name", "location","synced")
                    val valueRange2 = ValueRange().setValues(listOf(headerValues2))

                    val updateValuesRequest2 = sheetsService.spreadsheets().values()
                        .update(spreadsheet.spreadsheetId, "Companies!A1:D1", valueRange2)
                    updateValuesRequest2.setValueInputOption("RAW")
                    updateValuesRequest2.execute()

                    // Add headers to the second sheet (Employees)
                    val headerValues3 = listOf("id", "name", "emp_type","age","phone","employee_id","synced")
                    val valueRange3 = ValueRange().setValues(listOf(headerValues3))

                    val updateValuesRequest3 = sheetsService.spreadsheets().values()
                        .update(spreadsheet.spreadsheetId, "Employees!A1:G1", valueRange3)
                    updateValuesRequest3.setValueInputOption("RAW")
                    updateValuesRequest3.execute()
                    //colors
                    val headerRequest = Request()
                        .setRepeatCell( // Using Request again
                            RepeatCellRequest().setRange(
                                GridRange().setSheetId(0).setStartRowIndex(0).setEndRowIndex(1)
                                    .setStartColumnIndex(0).setEndColumnIndex(6)
                            ).setCell(
                                CellData().setUserEnteredFormat(
                                    CellFormat().setBackgroundColor(
                                        Color().setRed(0.0f).setGreen(1.0f).setBlue(0.0f)
                                    )
                                )
                            ).setFields("userEnteredFormat.backgroundColor")
                        )


                    val headerRangeRequest = BatchUpdateSpreadsheetRequest()
                        .setRequests(listOf(headerRequest))

                    sheetsService.spreadsheets().batchUpdate(
                        spreadsheet.spreadsheetId,
                        headerRangeRequest
                    ).execute()

                    saveSpreadsheetIdToDrive(credential, spreadsheet.spreadsheetId)
                } else {
                    Log.d(TAG, "Sheet already exists for user: $fileId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create sheet for user", e)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Notification permission granted, proceed with your app logic
            } else {
                // Notification permission denied, handle accordingly
                // You can show a dialog or a message to the user explaining the importance of notification permission
            }
        }
    }


//    private fun showNotificationPermissionDialog() {
//        val alertDialog = AlertDialog.Builder(this)
//            .setTitle("Notification Permission Request")
//            .setMessage("Please grant the notification permission to receive important updates")
//            .setPositiveButton("Grant") { dialog, which ->
//                // Request the notification permission
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    val intent = Intent().apply {
//                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                        data = Uri.fromParts("package", packageName, null)
//                    }
//                    startActivityForResult(intent, NOTIFICATION_PERMISSION_REQUEST_CODE)
//                } else {
//                    // Handle older versions where direct permission request is needed
//                    // Request for notification permission directly
//                }
//            }
//            .setNegativeButton("Cancel") { dialog, which ->
//                // Handle the case where the user denies the notification permission
//                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
//            }
//            .create()
//
//        alertDialog.show()
//    }


    //    private fun isNotificationPermissionGranted(): Boolean {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notificationManager = getSystemService(NotificationManager::class.java)
//            return notificationManager?.areNotificationsEnabled() == true
//        } else {
//            // Handle notification permission for older versions if needed
//            return true
//        }
//    }
    data class WeatherInfo(
        val temperature: String,
        val description: String,
        val city: String
    )
} 