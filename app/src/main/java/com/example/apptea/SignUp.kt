package com.example.apptea

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.math.BigInteger
import java.security.SecureRandom


class SignUp : AppCompatActivity() {

    private lateinit var userTypeSpn: Spinner
    private lateinit var fname: EditText
    private lateinit var lname: EditText
    private lateinit var phone: EditText
    private lateinit var cntSpinner: Spinner
    private lateinit var sbcnt: EditText
    private lateinit var vlg: EditText
    private lateinit var lacrage: EditText
    private lateinit var numEmp: EditText
    private lateinit var psw: EditText
    private lateinit var cpsw: EditText
    private lateinit var signUpBtn: Button

    private lateinit var db: DBHelper

    private lateinit var farmManagerFields: Array<View>
    private lateinit var farmerFields: Array<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        userTypeSpn = findViewById(R.id.spinnerUserType)
        fname = findViewById(R.id.first_name)
        lname = findViewById(R.id.last_name)
        phone = findViewById(R.id.phone_input)
        cntSpinner = findViewById(R.id.county_input)
        sbcnt = findViewById(R.id.subcounty_input)
        vlg = findViewById(R.id.village_input)
        lacrage = findViewById(R.id.acrage_input)
        numEmp = findViewById(R.id.employees_input)
        psw = findViewById(R.id.password_input)
        cpsw = findViewById(R.id.confirmpassword_input)
        signUpBtn = findViewById(R.id.signup_btn)
        db = DBHelper(this)

        // Add the views related to Farm Manager
        farmManagerFields = arrayOf(fname, lname, phone, cntSpinner, sbcnt, vlg, psw, cpsw)
        // Add the views related to Farmer
        farmerFields = arrayOf(fname, lname, phone, cntSpinner, sbcnt, vlg, lacrage, numEmp, psw, cpsw)

        val userTypes = arrayOf("Select User Type", "Farmer", "Farm Manager")
        val userTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypes)
        userTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userTypeSpn.adapter = userTypeAdapter

        val county = resources.getStringArray(R.array.kenyan_counties)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, county)
        cntSpinner.adapter = spinnerAdapter

        userTypeSpn.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                val selectedUserType = parentView?.getItemAtPosition(position).toString()
                updateUIBasedOnUserType(selectedUserType)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing here
            }
        })

        signUpBtn.setOnClickListener {
            val firstName = fname.text.toString()
            val lastName = lname.text.toString()
            val phoneNumber = phone.text.toString()
            val county = cntSpinner.selectedItem.toString()
            val subcounty = sbcnt.text.toString()
            val village = vlg.text.toString()
            val landAcreage = lacrage.text.toString()
            val numEmployees = numEmp.text.toString()
            val password = psw.text.toString()
            val confirmPassword = cpsw.text.toString()

            val selectedUserType = userTypeSpn.selectedItem.toString()

            if (selectedUserType == "Select User Type") {
                Toast.makeText(this, "Please select a user type", Toast.LENGTH_SHORT).show()
            } else if (firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty() || subcounty.isEmpty() || village.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            } else {
                if (password != confirmPassword) {
                    // Passwords do not match
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                } else {
                    // Passwords match, proceed with registration
                    // Generate special code
                    val specialCode = generateSpecialCode()

                    // Your existing logic for Farmer and Farm Manager registration
                    // Insert Farm Manager data into the database
                    if (selectedUserType == "Farm Manager") {
                        // Insert Farm Manager data into the database
                        val savedata = db.insertFarmManager(
                            firstName, lastName, phoneNumber, county, subcounty, village, password
                        )

                        if (savedata) {
                            Toast.makeText(this, "Farm Manager Registered", Toast.LENGTH_SHORT).show()
                            val intent = Intent(applicationContext, Login::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Failed to register Farm Manager", Toast.LENGTH_SHORT).show()
                        }
                    } else if (selectedUserType == "Farmer") {
                        // Insert Farmer data into the database
                        val savedata = db.insertFarmer(
                            firstName, lastName, phoneNumber, county, subcounty, village, landAcreage, numEmployees, password, specialCode
                        )

                        if (savedata) {
                            Toast.makeText(this, "Farmer Registered", Toast.LENGTH_SHORT).show()
                            val intent = Intent(applicationContext, Login::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Failed to Register farmer", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateUIBasedOnUserType(userType: String) {
        // Hide all fields by default
        for (field in farmManagerFields + farmerFields) {
            field.visibility = View.GONE
        }

        // Show relevant fields based on the selected user type
        when (userType) {
            "Farm Manager" -> {
                for (field in farmManagerFields) {
                    field.visibility = View.VISIBLE
                }
            }
            "Farmer" -> {
                for (field in farmerFields) {
                    field.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun generateSpecialCode(): String {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = SecureRandom()

        return (1..6)
            .map { characters[random.nextInt(characters.length)] }
            .joinToString("")
    }

}
