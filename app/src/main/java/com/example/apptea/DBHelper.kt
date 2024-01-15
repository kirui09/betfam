package com.example.apptea


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.ContactsContract.CommonDataKinds.Phone

// Define the database name, table name, and version
class DBHelper(context: Context) : SQLiteOpenHelper(context, "FarmersDatabase",null, 1, ) {


    // Called when the database is created for the first time
    override fun onCreate(p0: SQLiteDatabase) {
        p0?.execSQL("CREATE TABLE FarmersDatabase ( id INTEGER PRIMARY KEY AUTOINCREMENT, first_name TEXT, last_name TEXT,phone TEXT ,county TEXT, subcounty TEXT, village TEXT, land_acreage REAL, num_employees INTEGER, password TEXT)")
    }

    // Called when the database needs to be upgraded
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        // Handle database upgrades if needed
        // For simplicity, we'll drop the existing table and recreate it
        p0?.execSQL("DROP TABLE IF EXISTS FarmersDatabase")

    }

    // Function to insert data into the Farmers table
    fun insertFarmer(
        firstName: String,
        lastName: String,
        phone: String,
        county: String,
        subcounty: String,
        village: String,
        landAcreage: String,
        numEmployees: String,
        password: String,
    ) : Boolean {
        // Get a writable database
        val p0 = this.writableDatabase
        // Create a ContentValues object to store the data
        val cv = ContentValues()
        cv.put("first_name", firstName)
        cv.put("last_name", lastName)
        cv.put("phone",phone)
        cv.put("county", county)
        cv.put("subcounty", subcounty)
        cv.put("village", village)
        cv.put("land_acreage", landAcreage)
        cv.put("num_employees", numEmployees)
        cv.put("password", password)
        val result = p0.insert("FarmersDatabase", null, cv)
        if (result == -1.toLong()) {
            return false
        }
        return true
    }
    fun checkfarmerpass(phone: String,  password: String): Boolean{
        val p0 = this.writableDatabase
        val query = "select  * from FarmersDatabase where phone ='$phone' and password ='$password'"
        val cursor = p0.rawQuery(query,null)
        if (cursor.count<=0){
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }
}

