package com.example.apptea

import SharedPreferencesHelper
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.apptea.databinding.ActivityMainBinding
import com.example.apptea.ui.home.HomeFragment
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var dbh: DBHelper
    private lateinit var headerView: View

    val CITY: String = "Litein, KE"
    val API: String = "1a105b90f41489e05ece19d6f6c326b9" // Use API key

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

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_records, R.id.nav_employees_menu, R.id.nav_managers, R.id.nav_companies
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

        // Fetch weather data
        fetchWeatherData()
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

    data class WeatherInfo(
        val temperature: String,
        val description: String,
        val city: String
    )
}
