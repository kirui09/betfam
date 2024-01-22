package com.example.apptea

import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.apptea.databinding.ActivityMainBinding
import SharedPreferencesHelper
import android.view.View
import androidx.navigation.NavDestination


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var dbh: DBHelper
    private lateinit var headerView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize DBHelper
        DBHelper.init(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top-level destinations.

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_records, R.id.nav_employees_menu,R.id.nav_managers,R.id.nav_companies
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Initialize SharedPreferencesHelper and DBHelper
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        dbh = DBHelper(this)

        // Initialize headerView
        headerView = navView.getHeaderView(0)

        // Call the function to update navigation header
        updateNavigationHeader()
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
    // Add this function to update the toolbar dynamically
    private fun updateToolbar(destination: NavDestination) {
        when (destination.id) {
            R.id.nav_employees_menu -> {
                supportActionBar?.setTitle(R.string.menu_employees)
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_add_24)
            }
            else -> {
                supportActionBar?.setTitle(R.string.app_name)
            }
        }
    }

    private fun updateNavigationHeader() {
        // Retrieve the saved phone number from SharedPreferences
        val savedPhoneNumber = sharedPreferencesHelper.getPhoneNumber()

        // Use the phone number to fetch user information from the database
        val userInformation = dbh.getUserInformationByPhoneNumber(savedPhoneNumber)

        // Check if userInformation is not null before accessing user details
        if (userInformation != null) {
            val userIdTextView: TextView = headerView.findViewById(R.id.textViewUUId)
            val fullNameTextView: TextView = headerView.findViewById(R.id.textViewFirstName)
            fullNameTextView.text = "${userInformation.firstName} ${userInformation.lastName}"

            // Update UI elements with user details
            userIdTextView.text = "Farmer ID: ${userInformation.specialcode}"
            // Update other UI elements with additional user details as needed
        }
    }




}
