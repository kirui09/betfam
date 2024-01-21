package com.example.apptea

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ManagerActivity : AppCompatActivity() {

    private lateinit var farmCodeEditText: EditText
    private lateinit var companySpinner: Spinner
    private lateinit var signInAsSpinner: Spinner
    private lateinit var dateEditText: EditText
    private lateinit var saveDataButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)

//        // Initialize views
//        farmCodeEditText = findViewById(R.id.phone_input)
//        companySpinner = findViewById(R.id.spinnerCompany)
//        signInAsSpinner = findViewById(R.id.employeeName)
//        dateEditText = findViewById(R.id.kilo_input)
//        saveDataButton = findViewById(R.id.savedata_btn)
//
//        // Set up the spinner adapters
//        val companyAdapter = ArrayAdapter.createFromResource(
//            this,
//            R.array.kenyan_tea_companies,
//            android.R.layout.simple_spinner_item
//        )
//        companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        companySpinner.adapter = companyAdapter
//
//        val signInAsAdapter = ArrayAdapter.createFromResource(
//            this,
//            R.array.employee_names,
//            android.R.layout.simple_spinner_item
//        )
//        signInAsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        signInAsSpinner.adapter = signInAsAdapter
//
//        // Set click listener for the save data button
//        saveDataButton.setOnClickListener {
//            saveData()
//        }
//    }
//
//    private fun saveData() {
//        // Retrieve values from views
//        val farmCode = farmCodeEditText.text.toString()
//        val company = companySpinner.selectedItem.toString()
//        val signInAs = signInAsSpinner.selectedItem.toString()
//        val date = dateEditText.text.toString()
//
//        // Perform the actual saving logic here
//        // You can use a database, SharedPreferences, or any other storage method
//
//        // For now, let's just display a toast with the entered values
//        val message = "Farm Code: $farmCode\nCompany: $company\nSign In As: $signInAs\nDate: $date"
//        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
