package com.example.apptea

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.lifecycle.MutableLiveData
import com.example.apptea.ui.companies.Company
import com.example.apptea.ui.employees.Employee
import com.example.apptea.ui.records.DailyRecord
import com.example.apptea.ui.records.DailyTeaRecord
import com.example.apptea.ui.records.Record
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


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
        createCompaniesTable(db)
        createTeaRecordsTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS FarmersDatabase")
        db.execSQL("DROP TABLE IF EXISTS FarmManagers")
        db.execSQL("DROP TABLE IF EXISTS Employees")
        db.execSQL("DROP TABLE IF EXISTS Companies")
        db.execSQL("DROP TABLE IF EXISTS TeaRecords")
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

    private fun createTeaRecordsTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS TeaRecords ( " +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "date TEXT, " +
                    "employee_name TEXT, " +
                    "company TEXT, " +
                    "kilos DECIMAL)"
        )
    }

    private fun createCompaniesTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS Companies ( " +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "companyname TEXT, " +
                    "companylocation TEXT)"
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

    // Add this method to initialize DBHelper
    fun initialize(context: Context) {
        init(context)
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
        val columns = arrayOf(
            "first_name",
            "last_name",
            "phone",
            "special_code"/* Add other columns as needed */
        )
        val selection = "phone = ?"
        val selectionArgs = arrayOf(phoneNumber)

        val cursor: Cursor? =
            db.query("FarmersDatabase", columns, selection, selectionArgs, null, null, null)
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


    // Add a method in DBHelper to get a list of employees
    fun getAllEmployees(): List<Employee> {
        val employeeList = mutableListOf<Employee>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Employees", null)

        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val age = cursor.getString(cursor.getColumnIndex("age"))
            val phone = cursor.getString(cursor.getColumnIndex("phone"))
            val employeeId = cursor.getString(cursor.getColumnIndex("employee_id"))

            val employee = Employee(name, age, phone, employeeId)
            employeeList.add(employee)
        }

        cursor.close()
        return employeeList
    }

    fun getAllEmployeeNames(): List<String> {
        val employeeNames = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT name FROM Employees", null)

        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            employeeNames.add(name)
        }

        cursor.close()
        return employeeNames
    }

    fun getAllCompanyNames(): List<String> {
        val companyNames = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT companyname FROM Companies", null)

        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex("companyname"))
            companyNames.add(name)
        }

        cursor.close()
        return companyNames
    }

    fun insertCompany(name: String, location: String): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("companyname", name)
        cv.put("companylocation", location)
        val result = db.insert("Companies", null, cv)
        return result != -1L
    }

    //for recycler view
    fun getAllCompanies(): List<Company> {
        val companyList = mutableListOf<Company>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Companies", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val name = cursor.getString(cursor.getColumnIndex("companyname"))
            val location = cursor.getString(cursor.getColumnIndex("companylocation"))

            val company = Company(id, name, location)
            companyList.add(company)
        }

        cursor.close()
        return companyList
    }

    // Inside DBHelper class
    fun insertTeaRecords(records: List<Record>): Boolean {
        val db = this.writableDatabase
        val successList = mutableListOf<Boolean>()

        for (record in records) {
            val cv = ContentValues().apply {
                put("date", record.date)
                put("employee_name", record.employee)
                put("company", record.company)
                put("kilos", record.kilos)
            }

            val result = db.insert("TeaRecords", null, cv)
            successList.add(result != -1L)
        }

        return !successList.contains(false)
    }

    // Inside DBHelper class

    fun getAllTeaRecords(): List<DailyTeaRecord> {
        val teaRecordsList = mutableListOf<DailyTeaRecord>()
        val teaRecordsLiveData = MutableLiveData<List<DailyTeaRecord>>()
        val db = this.readableDatabase
        val query = "SELECT date,GROUP_CONCAT(employee_name) AS employees,GROUP_CONCAT(company) AS companies, SUM(kilos) AS total_kilos FROM TeaRecords GROUP BY date ORDER BY date DESC"

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val date = cursor.getString(cursor.getColumnIndex("date"))
            val employees = cursor.getString(cursor.getColumnIndex("employees")).split(",")
            val companies = cursor.getString(cursor.getColumnIndex("companies")).split(",")
            val totalKilos = cursor.getDouble(cursor.getColumnIndex("total_kilos"))

            val record = DailyTeaRecord( date,employees, companies, totalKilos)
            teaRecordsList.add(record)
        }

        cursor.close()
        teaRecordsLiveData.postValue(teaRecordsList)
        return teaRecordsList
    }

        // Method to fetch tea records for the past 1 week
        fun getTeaRecordsForPastWeek(): List<DailyRecord> {
            val teaRecordsList = mutableListOf<DailyRecord>()
            val currentDate = Calendar.getInstance()
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val startDate = currentDate.clone() as Calendar
            startDate.add(Calendar.DAY_OF_MONTH, -6) // Subtracts 6 days to get the past 1 week

            val db = this.readableDatabase
            val query = "SELECT date, SUM(kilos) AS total_kilos FROM TeaRecords WHERE date BETWEEN ? AND ? GROUP BY date ORDER BY date ASC"

            val cursor = db.rawQuery(query, arrayOf(formatter.format(startDate.time), formatter.format(currentDate.time)))

            while (cursor.moveToNext()) {
                val dateString = cursor.getString(cursor.getColumnIndex("date"))
                val date = formatter.parse(dateString) // Convert String to Date

                val totalKilos = cursor.getDouble(cursor.getColumnIndex("total_kilos"))

                val record = DailyRecord(date, totalKilos)
                teaRecordsList.add(record)
            }

            cursor.close()
            return teaRecordsList
        }

    fun getCompanyKilosData(): Map<String, Float> {
        val companyKilosMap = mutableMapOf<String, Float>()

        val db = this.readableDatabase
        val query = "SELECT company, SUM(kilos) AS total_kilos FROM TeaRecords GROUP BY company ORDER BY total_kilos DESC"

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val company = cursor.getString(cursor.getColumnIndex("company"))
            val totalKilos = cursor.getFloat(cursor.getColumnIndex("total_kilos"))

            companyKilosMap[company] = totalKilos
        }

        cursor.close()
        return companyKilosMap
    }






}




