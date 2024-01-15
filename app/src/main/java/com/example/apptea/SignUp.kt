package com.example.apptea

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import com.example.apptea.DBHelper


class SignUp : AppCompatActivity() {

    // Declare UI elements
    private lateinit var fname: EditText
    private lateinit var lname: EditText
    private lateinit var phn: EditText
    private lateinit var cntSpinner: Spinner
    private lateinit var sbcnt: EditText
    private lateinit var vlg: EditText
    private lateinit var lacrage: EditText
    private lateinit var numEmp: EditText
    private lateinit var psw: EditText
    private lateinit var cpsw: EditText
    private lateinit var signUpBtn: Button
    private lateinit var db: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        // Initialize UI elements
        fname = findViewById(R.id.first_name)
        lname = findViewById(R.id.last_name)
        phn = findViewById(R.id.phone_input)
        cntSpinner = findViewById(R.id.county_input)
        sbcnt = findViewById(R.id.subcounty_input)
        vlg = findViewById(R.id.village_input)
        lacrage = findViewById(R.id.acrage_input)
        numEmp = findViewById(R.id.employees_input)
        psw = findViewById(R.id.password_input)
        cpsw = findViewById(R.id.confirmpassword_input)
        signUpBtn = findViewById(R.id.signup_btn)
        db = DBHelper(this)

        // setup spinner with county data

        val county = arrayOf(
            "Baringo", "Bomet", "Bungoma", "Busia", "Elgeyo-Marakwet", "Embu", "Garissa", "Homa Bay", "Isiolo", "Kajiado",
            "Kakamega", "Kericho", "Kiambu", "Kilifi", "Kirinyaga", "Kisii", "Kisumu", "Kitui", "Kwale", "Laikipia", "Lamu",
            "Machakos", "Makueni", "Mandera", "Marsabit", "Meru", "Migori",
            "Mombasa", "Murang'a", "Nairobi", "Nakuru", "Nandi", "Narok", "Nyamira",
            "Nyandarua", "Nyeri", "Samburu", "Siaya", "Taita-Taveta",
            "Tana River", "Tharaka-Nithi", "Trans-Nzoia", "Turkana", "Uasin Gishu", "Vihiga", "Wajir", "West Pokot"
        )
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, county)
        cntSpinner.adapter = spinnerAdapter


        // Set click listener for the sign-up button
        // Retrieve data from the form
        signUpBtn.setOnClickListener {
            // Get user input
            val firstName = fname.text.toString()
            val lastName = lname.text.toString()
            val phone = phn.text.toString()
            val county = cntSpinner.selectedItem.toString()
            val subcounty = sbcnt.text.toString()
            val village = vlg.text.toString()
            val landAcreage = lacrage.text.toString()
            val numEmployees = numEmp.text.toString()
            val password = psw.text.toString()
            val confirmPassword = cpsw.text.toString()
            val savedata = db.insertFarmer(
                firstName,
                lastName,
                phone,
                county,
                subcounty,
                village,
                landAcreage,
                numEmployees,
                password
            )


            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                TextUtils.isEmpty(county) || TextUtils.isEmpty(subcounty) ||
                TextUtils.isEmpty(village) || TextUtils.isEmpty(landAcreage) ||
                TextUtils.isEmpty(numEmployees) || TextUtils.isEmpty(password)
            ) {
                // Display a toast message indicating that all fields are required
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                if (password.equals(confirmPassword) ){
                    if (savedata==true) {
                        Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(applicationContext, Login::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}




