package com.example.apptea

import SharedPreferencesHelper
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class Login : ComponentActivity() {

    private lateinit var editphone: EditText
    private lateinit var editpwd: EditText
    private lateinit var loginbtn: Button
    private lateinit var gotosignupbtn: Button
    private lateinit var dbh: DBHelper

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Assuming you have these views in your XML layout
        editphone = findViewById(R.id.phone_input)
        editpwd = findViewById(R.id.password_input)
        loginbtn = findViewById(R.id.login_btn)
        dbh = DBHelper(this)
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        gotosignupbtn = findViewById(R.id.gotosignup_btn)

        loginbtn.setOnClickListener {

            val userphone = editphone.text.toString()
            val userpsw = editpwd.text.toString()

            if (TextUtils.isEmpty(userphone) || TextUtils.isEmpty(userpsw)) {
                Toast.makeText(this, "Add Username And Password", Toast.LENGTH_SHORT).show()
            } else {
                // Save the phone number using SharedPreferencesHelper
                sharedPreferencesHelper.savePhoneNumber(userphone)

                // Check if the user is a farmer
                val checkFarmer = dbh.checkfarmerpass(userphone, userpsw)

                if (checkFarmer) {
                    Toast.makeText(this, "Farmer Login Successful", Toast.LENGTH_SHORT).show()
                    // Navigate to the appropriate activity for farmers
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // Check if the user is a farm manager
                    val checkFarmManager = dbh.checkFarmManagerPass(userphone, userpsw)
                    if (checkFarmManager) {
                        Toast.makeText(this, "Farm Manager Login Successful", Toast.LENGTH_SHORT).show()
                        // Navigate to the appropriate activity for farm managers
                        val intent = Intent(applicationContext, ManagerActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Wrong username and password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        gotosignupbtn.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }
}