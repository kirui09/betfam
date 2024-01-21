package com.example.apptea

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.apptea.ui.employees.Employee

data class Employee(
    val id: Int,
    val name: String,
    val age: String,
    val phoneNumber: String,
    val employeeId: String = ""
)


class DBHelper(context: Context) : SQLiteOpenHelper(context, "FarmersDatabase", null, 1) {



    override fun onCreate(db: SQLiteDatabase) {
        createFarmersTable(db)
        createFarmManagersTable(db)
        createEmployeesTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS FarmersDatabase")
        db.execSQL("DROP TABLE IF EXISTS FarmManagers")
        db.execSQL("DROP TABLE IF EXISTS Employees")
        onCreate(db)
    }

    private fun createFarmersTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS FarmersDatabase ( " +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "first_name TEXT, last_name TEXT, phone TEXT, " +
                    "county TEXT, subcounty TEXT, village TEXT, " +
                    "land_acreage REAL, num_employees INTEGER, " +
                    "password TEXT, special_code TEXT)"
        )
    }

    private fun createFarmManagersTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS FarmManagers ( " +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "first_name TEXT, last_name TEXT, phone TEXT, " +
                    "county TEXT, subcounty TEXT, village TEXT, " +
                    "password TEXT)"
        )
    }

    private fun createEmployeesTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS Employees ( " +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "age INTEGER, " +
                    "phone TEXT, " +
                    "employee_id TEXT)"
        )
    }

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
        specialCode: String
    ): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("first_name", firstName)
        cv.put("last_name", lastName)
        cv.put("phone", phone)
        cv.put("county", county)
        cv.put("subcounty", subcounty)
        cv.put("village", village)
        cv.put("land_acreage", landAcreage)
        cv.put("num_employees", numEmployees)
        cv.put("password", password)
        cv.put("special_code", specialCode)
        val result = db.insert("FarmersDatabase", null, cv)
        return result != -1L
    }

    fun insertFarmManager(
        firstName: String,
        lastName: String,
        phone: String,
        county: String,
        subcounty: String,
        village: String,
        password: String,
    ): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("first_name", firstName)
        cv.put("last_name", lastName)
        cv.put("phone", phone)
        cv.put("county", county)
        cv.put("subcounty", subcounty)
        cv.put("village", village)
        cv.put("password", password)
        val result = db.insert("FarmManagers", null, cv)
        return result != -1L
    }

    // Method to insert data from Dialog Fragment to the Employee table
    fun insertEmployee(employee: Employee): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("name", employee.name)
        cv.put("age", employee.age)
        cv.put("phone", employee.phoneNumber)
        cv.put("employee_id", employee.employeeId)
        val result = db.insert("Employees", null, cv)
        return result != -1L
    }

    companion object {
        private lateinit var instance: DBHelper

        fun init(context: Context) {
            instance = DBHelper(context)
        }

        fun getInstance(): DBHelper {
            return instance
        }
    }

    fun checkfarmerpass(phone: String, password: String): Boolean {
        val db = this.writableDatabase
        val query =
            "SELECT * FROM FarmersDatabase WHERE phone = '$phone' AND password = '$password'"
        val cursor = db.rawQuery(query, null)
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun checkFarmManagerPass(phone: String, password: String): Boolean {
        val db = this.writableDatabase
        val query = "SELECT * FROM FarmManagers WHERE phone = '$phone' AND password = '$password'"
        val cursor = db.rawQuery(query, null)
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUserInformationByPhoneNumber(phoneNumber: String): FarmerInfo? {
        val db = this.readableDatabase
        val columns = arrayOf("first_name", "last_name", "phone" ,"special_code"/* Add other columns as needed */)
        val selection = "phone = ?"
        val selectionArgs = arrayOf(phoneNumber)

        val cursor: Cursor? = db.query("FarmersDatabase", columns, selection, selectionArgs, null, null, null)
        cursor?.moveToFirst()

        val user: FarmerInfo? = if (cursor != null && cursor.count > 0) {
            val firstName = cursor.getString(cursor.getColumnIndex("first_name"))
            val lastName = cursor.getString(cursor.getColumnIndex("last_name"))
            val specialcode = cursor.getString(cursor.getColumnIndex("special_code"))
            // Retrieve other user details as needed

            FarmerInfo(firstName, lastName, specialcode  /* Add other user details as needed */)
        } else {
            null
        }

        cursor?.close()
        return user
    }
}
