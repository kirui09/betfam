package com.betfam.apptea

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.betfam.apptea.ui.companies.Company
import com.betfam.apptea.ui.employees.Employee
import com.betfam.apptea.ui.payment_types.BasicPayment
import com.betfam.apptea.ui.records.DailyRecord
import com.betfam.apptea.ui.records.DailyTeaRecord
import com.betfam.apptea.ui.records.MonthlyPayment
import com.betfam.apptea.ui.records.Payment
import com.betfam.apptea.ui.records.Record
import com.betfam.apptea.ui.records.SyncedRecord
import com.betfam.apptea.ui.records.TeaPaymentRecord
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
        createPaymentTypesTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS FarmersDatabase")
        db.execSQL("DROP TABLE IF EXISTS FarmManagers")
        db.execSQL("DROP TABLE IF EXISTS Employees")
        db.execSQL("DROP TABLE IF EXISTS Companies")
        db.execSQL("DROP TABLE IF EXISTS TeaRecords")
        db.execSQL("DROP TABLE IF EXISTS PaymentTypes")
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
                    "emp_type TEXT, " + // Fixed syntax: added comma after "emp_type TEXT"
                    "age INTEGER, " +
                    "phone TEXT, " +
                    "employee_id TEXT" + // Removed trailing comma
                    ")"
        )
    }


    private fun createTeaRecordsTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS TeaRecords ( " +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "date TEXT, " +
                    "employee_name TEXT, " +
                    "company TEXT, " +
                    "kilos DECIMAL, " +
                    "pay DECIMAL, " + // Add a comma here
                    "synced INTEGER DEFAULT 0" + // Add the 'synced' column with a default value of 0
                    ")"
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

    private fun createPaymentTypesTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS PaymentTypes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "type TEXT NOT NULL, " +
                    "amount INTEGER NOT NULL)"
        )

        // Check if there are no entries in the table
        val query = "SELECT COUNT(*) FROM PaymentTypes"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        // If no entries exist, insert default values for "Basic" and "Supervisor"
        if (count == 0) {
            insertDefaultPaymentTypes(db)
        }
    }



    private fun insertDefaultPaymentTypes(db: SQLiteDatabase) {
        val defaultPaymentTypes = arrayOf("Basic", "Supervisor")
        val defaultAmount = 0 // Set the default amount here

        for (paymentType in defaultPaymentTypes) {
            val values = ContentValues().apply {
                put("type", paymentType)
                put("amount", defaultAmount)
            }
            db.insert("PaymentTypes", null, values)
        }
    }


    // Function to fetch tea records for an employee in a specific month
    // Function to fetch distinct employees for a specific month and year
    fun getEmployeesForMonth(month: Int, year: Int): List<String> {
        val db = readableDatabase
        val monthString = if (month < 10) "0$month" else month.toString()
        val yearString = year.toString()

        val query = "SELECT DISTINCT employee_name FROM TeaRecords WHERE strftime('%m', date) = ? AND strftime('%Y', date) = ?"
        val cursor = db.rawQuery(query, arrayOf(monthString, yearString))

        val employees = mutableListOf<String>()
        if (cursor.moveToFirst()) {
            do {
                val employeeName = cursor.getString(cursor.getColumnIndexOrThrow("employee_name"))
                employees.add(employeeName)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return employees
    }


    // Function to fetch tea records for an employee in a specific month
    fun getTeaRecordsForEmployeeInMonth(employeeName: String, month: Int, year: Int): List<PendingTeaRecord> {
        val db = readableDatabase
        val monthString = if (month < 10) "0$month" else month.toString()
        val yearString = year.toString()

        val query = "SELECT * FROM TeaRecords WHERE employee_name = ? AND strftime('%m', date) = ? AND strftime('%Y', date) = ?"
        val cursor = db.rawQuery(query, arrayOf(employeeName, monthString, yearString))

        val teaRecords = mutableListOf<PendingTeaRecord>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val kilos = cursor.getDouble(cursor.getColumnIndexOrThrow("kilos"))
                teaRecords.add(PendingTeaRecord(id, date, employeeName, "", kilos, 0.0, 0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return teaRecords
    }

    fun updatePaymentInTeaRecords(recordId: Int, paymentAmount: Double) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("pay", paymentAmount)
        }
        val rowsUpdated = db.update("TeaRecords", values, "id = ?", arrayOf(recordId.toString()))
        Log.d("DBHelper", "Updated $rowsUpdated rows in TeaRecords table for record ID $recordId with payment amount $paymentAmount")
    }





    fun insertFarmer(
        firstName: String,
        lastName: String,
        phone: String,
        county: String,
        subcounty: String,
        village: String,
        landAcreage: String,
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
        cv.put("emp_type", employee.empType)
        cv.put("age", employee.age)
        cv.put("phone", employee.phoneNumber)
        cv.put("employee_id", employee.employeeId)
        // Adding employee type
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

        // Use the actual column names from the SELECT query
        val cursor = db.rawQuery("SELECT id, name,emp_type , age, phone, employee_id FROM Employees", null)

        while (cursor.moveToNext()) {
            // Retrieve values using the correct column names
            val id = cursor.getLong(cursor.getColumnIndex("id"))  // Make sure to use getLong for id
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val empType = cursor.getString(cursor.getColumnIndex("emp_type")) // Retrieve employee type
            val age = cursor.getString(cursor.getColumnIndex("age"))
            val phoneNumber = cursor.getString(cursor.getColumnIndex("phone"))
            val employeeId = cursor.getString(cursor.getColumnIndex("employee_id"))
            // Create Employee object with retrieved values
            val employee = Employee(id, name, empType, age, phoneNumber, employeeId)
            employeeList.add(employee)
        }
        cursor.close()
        return employeeList
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
    fun insertTeaRecord(record: Record): Boolean {
        val db = this.writableDatabase

        val pay = record.kilos * 8.0 // Assuming pay is calculated as kilos * 8

        val cv = ContentValues().apply {
            put("date", record.date)
            put("employee_name", record.employee)
            put("company", record.company)
            put("kilos", record.kilos)
            put("pay", pay) // Add pay to ContentValues
        }

        val result = db.insert("TeaRecords", null, cv)
        return result != -1L
    }


    fun insertOrUpdateTeaRecords(records: List<TeaPaymentRecord>) {
        val existingRecords = getAllTeaRecords()
        val db = this.writableDatabase
        records.forEach { newRecord ->
            val values = ContentValues().apply {
                put("id", newRecord.id)
                put("date", newRecord.date)
                put("company", newRecord.company)
                put("employee_name", newRecord.employees)
                put("kilos", newRecord.kilos)
                put("pay", newRecord.payment)
            }
            val existingRecord = existingRecords.find { it.id.toInt() == newRecord.id }
            if (existingRecord == null) {
                // Insert a new record if it does not exist
                val result = db.insert("TeaRecords", null, values)
                Log.d("InsertionResult", "Inserted new record: $result")
            } else {
                // Update the existing record if it already exists
                val whereClause = "id = ?"
                val whereArgs = arrayOf(existingRecord.id.toString())
                val result = db.update("TeaRecords", values, whereClause, whereArgs)
                Log.d("UpdateResult", "Updated record: $result")
            }
        }
    }


    fun insertTeaRecords(records: List<Record>): Boolean {
        val db = this.writableDatabase
        val successList = mutableListOf<Boolean>()

        for (record in records) {
            val cv = ContentValues().apply {
                put("id", record.id) // Include the generated ID in the ContentValues
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
        val query = "SELECT company, SUM(kilos) AS total_kilos FROM TeaRecords GROUP BY company "

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val company = cursor.getString(cursor.getColumnIndex("company"))
            val totalKilos = cursor.getFloat(cursor.getColumnIndex("total_kilos"))

            companyKilosMap[company] = totalKilos
        }

        cursor.close()
        return companyKilosMap
    }

    // Fetch all records for a given date
    // Inside DBHelper class
//    fun getTeaRecordsByDate(date: String): List<EditableTeaRecord> {
//        val recordsList = mutableListOf<EditableTeaRecord>()
//
//        val db = this.readableDatabase
//        val query = "SELECT * FROM TeaRecords WHERE date = ?"
//        val cursor = db.rawQuery(query, arrayOf(date))
//
//        while (cursor.moveToNext()) {
//            val recordid = cursor.getInt(cursor.getColumnIndex("id"))
//            val recordDate = cursor.getString(cursor.getColumnIndex("date"))
//            val companies = cursor.getString(cursor.getColumnIndex("companies"))?.split(",")
//            val employees = cursor.getString(cursor.getColumnIndex("employee_name"))?.split(",")
//            val kilos = cursor.getDouble(cursor.getColumnIndex("kilos"))
//            val pay = cursor.getDouble(cursor.getColumnIndex("pay"))
//            val editableTeaRecord = EditableTeaRecord(recordid,recordDate, companies.orEmpty(), employees.orEmpty(), kilos,pay)
//            recordsList.add(editableTeaRecord)
//        }
//
//        cursor.close()
//        return recordsList
//    }





    // Function to update an employee
    fun updateEmployee(employee: Employee): Boolean {
        try {
            val db = this.writableDatabase
            val contentValues = ContentValues()
            contentValues.put("id", employee.id)
            contentValues.put("emp_type", employee.empType)
            contentValues.put("name", employee.name)
            contentValues.put("age", employee.age)
            contentValues.put("phone", employee.phoneNumber)
            contentValues.put("employee_id", employee.employeeId)

            // Log the update operation details
            Log.d("DBHelper", "Updating employee record:")
            Log.d("DBHelper", "Employee ID: ${employee.employeeId}")
            Log.d("DBHelper", "Name: ${employee.name}")
            Log.d("DBHelper", "Age: ${employee.age}")
            Log.d("DBHelper", "Phone: ${employee.phoneNumber}")

            // Use an array of IDs for the WHERE clause
            val rowsAffected = db.update(
                "Employees",
                contentValues,
                "id = ?",
                arrayOf(employee.id.toString())
            )

            // Log the number of rows affected
            Log.d("DBHelper", "Rows affected: $rowsAffected")

            db.close()

            return rowsAffected > 0
        } catch (e: Exception) {
            // Handle any exceptions here (e.g., log or notify)
            e.printStackTrace()
            return false
        }
    }



    fun deleteEmployee(employee: Employee): Boolean {
        try {
            val db = this.writableDatabase

            val rowsAffected = db.delete(
                "Employees",
                "id = ?",
                arrayOf(employee.id.toString())
            )

            db.close()

            return rowsAffected > 0
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }



    // Function to update a company in the database
    fun updateCompany(company: Company): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put("companyname", company.name)
        values.put("companylocation", company.location)

        val whereClause = "id = ?"
        val whereArgs = arrayOf(company.id.toString())

        // Perform the update
        val rowsUpdated = db.update("Companies", values, whereClause, whereArgs)

        db.close()

        return rowsUpdated > 0
    }


    // Delete a company by ID
    fun deleteCompany(companyId: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete("Companies", "id=?", arrayOf(companyId.toString())) > 0
        db.close()
        return result
    }



    // Method to fetch all tea records from the database
    fun getAllTeaRecords(): List<DailyTeaRecord> {
        val teaRecordsList = mutableListOf<DailyTeaRecord>()
        val teaRecordsLiveData = MutableLiveData<List<DailyTeaRecord>>()
        val db = this.readableDatabase

        try {
            val query =
                "SELECT id, date,company, employee_name, kilos, pay  FROM TeaRecords  ORDER BY date desc"

            val cursor = db.rawQuery(query, null)

            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndex("id"))
                val date = cursor.getString(cursor.getColumnIndex("date"))
                val companies = cursor.getString(cursor.getColumnIndex("company"))
                val employees = cursor.getString(cursor.getColumnIndex("employee_name"))
                val kilos = cursor.getDouble(cursor.getColumnIndex("kilos"))


                // Log the selected data
                Log.d("DB_SELECTION", "Date: $date, Employee: $employees, Company: $companies, Kilos: $kilos")

                val record = DailyTeaRecord(id , date, companies, employees, kilos)
                teaRecordsList.add(record)
            }

            cursor.close()
            teaRecordsLiveData.postValue(teaRecordsList)
        } catch (e: Exception) {
            Log.e("DB_ERROR", "Error while retrieving tea records: ${e.message}")
        } finally {
            db.close()
        }

        return teaRecordsList
    }


    fun getPaymentRecords(): List<TeaPaymentRecord> {
        val teaRecordsList = mutableListOf<TeaPaymentRecord>()
        val db = this.readableDatabase

        try {
            val query = "SELECT id, date, company, employee_name, kilos, pay FROM TeaRecords ORDER BY date DESC"
            val cursor = db.rawQuery(query, null)

            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndex("id"))
                val date = cursor.getString(cursor.getColumnIndex("date"))
                val company = cursor.getString(cursor.getColumnIndex("company"))
                val employees = cursor.getString(cursor.getColumnIndex("employee_name"))
                val kilos = cursor.getDouble(cursor.getColumnIndex("kilos"))
                val payment = cursor.getDouble(cursor.getColumnIndex("pay"))

                // Log the selected data
                Log.d("DB_SELECTION", "Date: $date, Employee: $employees, Company: $company, Kilos: $kilos, Payment: $payment")

                val record = TeaPaymentRecord(id, date, company, employees, kilos, payment)
                teaRecordsList.add(record)
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("DB_ERROR", "Error while retrieving tea records: ${e.message}")
        } finally {
            db.close()
        }

        return teaRecordsList
    }

    // Method to get all employee names
    fun getAllEmployeeNames(): List<String> {
        val employeeNames = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT name FROM Employees"
        val cursor: Cursor?

        try {
            cursor = db.rawQuery(query, null)
            cursor?.let {
                if (cursor.moveToFirst()) {
                    do {
                        val employeeName = cursor.getString(cursor.getColumnIndex("name"))
                        employeeNames.add(employeeName)
                        // Log the employee name being retrieved

                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        } catch (e: Exception) {
            // Handle exception

        } finally {
            db.close()
        }

        return employeeNames
    }

//    fun getAllEmployees(): List<Employee> {
//        val employeeList = mutableListOf<Employee>()
//        val db = this.readableDatabase
//
//        // Use the actual column names from the SELECT query
//        val cursor = db.rawQuery("SELECT id, name, age, phone, employee_id FROM Employees", null)
//
//        while (cursor.moveToNext()) {
//            // Retrieve values using the correct column names
//            val id = cursor.getLong(cursor.getColumnIndex("id"))  // Make sure to use getLong for id
//            val name = cursor.getString(cursor.getColumnIndex("name"))
//            val age = cursor.getString(cursor.getColumnIndex("age"))
//            val phoneNumber = cursor.getString(cursor.getColumnIndex("phone"))
//            val employeeId = cursor.getString(cursor.getColumnIndex("employee_id"))
//
//            // Create Employee object with retrieved values
//            val employee = Employee(id, name, age, phoneNumber, employeeId)
//            employeeList.add(employee)
//        }
//
//        cursor.close()
//        return employeeList
//    }

    fun updateTeaRecord(record: DailyTeaRecord): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put("date", record.date)
        values.put("company", record.companies)
        values.put("employee_name", record.employees)
        values.put("kilos", record.kilos)

        // Use the ID as the unique identifier for the WHERE clause
        val whereClause = "id = ?"
        val whereArgs = arrayOf(record.id.toString())

        // Perform the update
        val rowsUpdated = db.update("TeaRecords", values, whereClause, whereArgs)

        db.close()

        return rowsUpdated > 0
    }
    fun deleteRecord(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete("TeaRecords", "id=?", arrayOf(id.toString()))
        db.close()
        return result != -1
    }


    // In DBHelper.kt

    fun getAllCompanyNames(): List<String> {
        val companyNames = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT companyname FROM Companies", null)
        cursor.use {
            while (it.moveToNext()) {
                val companyName = it.getString(it.getColumnIndex("companyname"))
                companyNames.add(companyName)
            }
        }
        return companyNames
    }

    // Add this method to your DBHelper class
    fun getPaymentTypes(): Map<String, Int> {
        val paymentTypes = mutableMapOf<String, Int>()
        val db = this.readableDatabase

        try {
            val query = "SELECT type, amount FROM PaymentTypes"
            val cursor = db.rawQuery(query, null)

            while (cursor.moveToNext()) {
                val type = cursor.getString(cursor.getColumnIndex("type"))
                val amount = cursor.getInt(cursor.getColumnIndex("amount"))
                paymentTypes[type] = amount
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("DB_ERROR", "Error while retrieving payment types: ${e.message}")
        } finally {
            db.close()
        }

        return paymentTypes
    }




    fun updateSupervisorPay(newPay: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("amount", newPay)
        val whereClause = "type = ?"  // Define the WHERE clause
        val whereArgs = arrayOf("Supervisor")  // Define the arguments for the WHERE clause

        return try {
            val rowsAffected = db.update("PaymentTypes", values, whereClause, whereArgs)  // Execute the update statement with the WHERE clause
            db.close()

            // Add log messages for debugging
            Log.d("DB_UPDATE", "Rows affected: $rowsAffected")

            val success = rowsAffected > 0  // Check if at least one row was affected (updated)

            // Log success or failure
            if (success) {
                Log.d("DB_UPDATE", "Supervisor payment updated successfully")
            } else {
                Log.e("DB_UPDATE", "Failed to update supervisor payment")
            }

            success  // Return true if at least one row was affected (updated)
        } catch (e: Exception) {
            Log.e("DB_ERROR", "Error updating supervisor payment: ${e.message}")
            false  // Return false indicating that the update operation failed
        }
    }



    fun updateBasicPay(basicPayment: BasicPayment): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("amount", basicPayment.amount)
        val whereClause = "type = ?"  // Define the WHERE clause
        val whereArgs = arrayOf("Basic")  // Define the arguments for the WHERE clause

        return try {
            val rowsAffected = db.update("PaymentTypes", values, whereClause, whereArgs)  // Execute the update statement with the WHERE clause
            db.close()

            // Add log messages for debugging
            Log.d("DB_UPDATE", "Rows affected: $rowsAffected")

            val success = rowsAffected > 0  // Check if at least one row was affected (updated)

            // Log success or failure
            if (success) {
                Log.d("DB_UPDATE", "Basic payment updated successfully")
            } else {
                Log.e("DB_UPDATE", "Failed to update basic payment")
            }

            success  // Return true if at least one row was affected (updated)
        } catch (e: Exception) {
            Log.e("DB_ERROR", "Error updating basic payment: ${e.message}")
            false  // Return false indicating that the update operation failed
        }
    }


    // Method to fetch supervisor pay from the database
    fun getSupervisorPay(): Double {
        val selectQuery = "SELECT amount FROM PaymentTypes WHERE type = 'Supervisor'"
        val db = this.readableDatabase
        var supervisorPay: Double = 0.0
        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                supervisorPay = cursor.getDouble(cursor.getColumnIndex("amount"))
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("DB_ERROR", "Error fetching supervisor pay: ${e.message}")
        } finally {
            db.close()
        }

        return supervisorPay
    }

    // Method to fetch basic pay from the database
    fun getBasicPay(): Double {
        val selectQuery = "SELECT amount FROM PaymentTypes WHERE type = 'Basic'"
        val db = this.readableDatabase
        var basicPay: Double = 0.0
        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                basicPay = cursor.getDouble(cursor.getColumnIndex("amount"))
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("DB_ERROR", "Error fetching basic pay: ${e.message}")
        } finally {
            db.close()
        }

        return basicPay
    }



    fun getEmployeeType(employeeName: String): String {
        val db = this.readableDatabase
        var type = ""
        val query = "SELECT emp_type FROM Employees where name = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(employeeName))
        if (cursor.moveToFirst()) {
            type = cursor.getString(cursor.getColumnIndex("emp_type"))
        }
        cursor.close()
        db.close()
        return type
    }



    fun savePayments(payments: List<Payment>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (payment in payments) {
                val contentValues = ContentValues()
                contentValues.put("pay", payment.paymentAmount)

                val where = "date = ? AND employee_name = ?"
                val whereArgs = arrayOf(payment.date, payment.employeeName)
                db.update("TeaRecords", contentValues, where, whereArgs)
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            // Handle exceptions
        } finally {
            db.endTransaction()
            db.close()
        }
    }


//    fun deletePayment(paymentId: Long) {
//        val db = this.writableDatabase
//        // Define 'where' part of query
//        val selection = "$COLUMN_ID = ?"
//        // Specify arguments in placeholder order
//        val selectionArgs = arrayOf(paymentId.toString())
//        // Issue SQL statement
//        db.delete(TABLE_PAYMENTS, selection, selectionArgs)
//        db.close()
//    }


    fun getAllPayments(): MutableLiveData<List<Payment>> {
        val paymentsLiveData = MutableLiveData<List<Payment>>()
        val db = readableDatabase
        val payments = mutableListOf<Payment>()
        val queryBuilder = StringBuilder("SELECT id, date, employee_name, kilos ,pay FROM TeaRecords")
        queryBuilder.append(" ORDER BY date ASC")
        val query = queryBuilder.toString()

        db.rawQuery(query, null).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndex("id"))
                val date = cursor.getString(cursor.getColumnIndex("date"))
                val employeeName = cursor.getString(cursor.getColumnIndex("employee_name"))
                val kilos = cursor.getDouble(cursor.getColumnIndex("kilos"))
                val paymentAmount = cursor.getDouble(cursor.getColumnIndex("pay"))
                payments.add(Payment(id, date, employeeName, kilos,paymentAmount))
            }
        }
        paymentsLiveData.postValue(payments)
        db.close()
        return paymentsLiveData
    }

    fun insertPaymentToTeaRecords(payment: Payment) {
        val db = this.writableDatabase
        val values = ContentValues()

        try {
            if (!paymentExists(db, payment.id, payment.date, payment.employeeName, payment.kilos)) {
                values.put("id", payment.id)
                values.put("date", payment.date)
                values.put("employee_name", payment.employeeName)
                values.put("kilos", payment.kilos)
                values.put("pay", payment.paymentAmount)
                db.insert("TeaRecords", null, values)
                Log.d("DBHelper", "Inserted new payment record: Employee ${payment.employeeName}, Date ${payment.date}, Payment Ksh ${payment.paymentAmount}")
            } else {
                val whereClause = "id = ? AND date = ? AND employee_name = ? AND kilos = ?"
                val whereArgs = arrayOf(payment.id.toString(), payment.date, payment.employeeName, payment.kilos.toString())
                values.put("pay", payment.paymentAmount)
                val rowsAffected = db.update("TeaRecords", values, whereClause, whereArgs)
                if (rowsAffected > 0) {
                    Log.d("DBHelper", "Updated payment record: Employee ${payment.employeeName}, Date ${payment.date}, Payment Ksh ${payment.paymentAmount}")
                } else {
                    Log.e("DBHelper", "Failed to update payment record for ${payment.employeeName} on ${payment.date}")
                }
            }
        } catch (e: Exception) {
            Log.e("DBHelper", "Error saving payment record: ${e.localizedMessage}")
        } finally {
            db.close()
        }
    }

    private fun paymentExists(db: SQLiteDatabase, id: Int, date: String, name: String, kilos: Double): Boolean {
        val query = "SELECT * FROM TeaRecords WHERE id = ? AND date = ? AND employee_name = ? AND kilos = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString(), date, name, kilos.toString()))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }




    // Method to fetch the sum of kilos for each employee

    fun getSumOfKilosForEachEmployee(): ArrayList<MonthlyPayment> {
        val monthlyPayments = ArrayList<MonthlyPayment>()
        val db = this.readableDatabase
        val query = "SELECT date, employee_name, SUM(pay) AS totalpay FROM TeaRecords GROUP BY date, employee_name"
        val cursor: Cursor? = db.rawQuery(query, null)
        cursor?.use {
            while (it.moveToNext()) {
                val date = it.getString(0)
                val employeeName = it.getString(1)
                val paymentAmount = it.getDouble(2)
                val monthlyPayment = MonthlyPayment(date, employeeName, paymentAmount)
                monthlyPayments.add(monthlyPayment)
            }
        }
        cursor?.close()
        db.close()
        return monthlyPayments
    }

    fun getPaymentDetailsForEmployeeAndMonth(employeeName: String, month: Int, year: Int): List<Pair<String, Double>> {
        val db = readableDatabase
        val payments = mutableListOf<Pair<String, Double>>()

        val query = "SELECT date, kilos FROM TeaRecords WHERE employee_name = ? AND strftime('%m', date) = ? AND strftime('%Y', date) = ? ORDER BY date ASC"

        Log.d("DBHelper", "Executing query for employee: $employeeName, month: $month, year: $year")

        try {
            val cursor = db.rawQuery(query, arrayOf(employeeName, String.format("%02d", month), year.toString()))

            if (cursor.moveToFirst()) {
                Log.d("DBHelper", "Found payment records for $employeeName, $month/$year")

                do {
                    val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                    val kilos = cursor.getDouble(cursor.getColumnIndexOrThrow("kilos"))

                    Log.d("DBHelper", "Date: $date, Kilos: $kilos")

                    payments.add(Pair(date, kilos))
                } while (cursor.moveToNext())
            } else {
                Log.d("DBHelper", "No payment records found for $employeeName, $month/$year")
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("DBHelper", "Error executing query: ${e.message}")
        } finally {
            db.close()
        }

        return payments
    }




    fun markRecordsAsSynced(recordIds: List<Int>) {
        val ids = recordIds.joinToString(",")
        val query = "UPDATE TeaRecords SET synced = 1 WHERE id IN ($ids)"
        val db = writableDatabase
        db.execSQL(query)
        db.close()
    }

    fun getUnsyncedRecords(): List<SyncedRecord> {
        val records = mutableListOf<SyncedRecord>()
        val query = "SELECT * FROM TeaRecords WHERE synced = 0"
        val db = readableDatabase
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex("id"))
            val date = cursor.getString(cursor.getColumnIndex("date"))
            val employeeName = cursor.getString(cursor.getColumnIndex("employee_name"))
            val company = cursor.getString(cursor.getColumnIndex("company"))
            val kilos = cursor.getDouble(cursor.getColumnIndex("kilos"))
            val pay = cursor.getDouble(cursor.getColumnIndex("pay"))
            val synced = cursor.getInt(cursor.getColumnIndex("synced"))
            val record = SyncedRecord(id, date, employeeName, company, kilos, pay, synced)
            records.add(record)
        }
        cursor.close()
        db.close()
        return records
    }

    fun getPaymentAmountFromDatabase(employeeName: String, date: String): Double {
        val db = readableDatabase
        var paymentAmount = 0.0

        val query = "SELECT pay FROM TeaRecords WHERE employee_name = ? AND date = ?"
        val selectionArgs = arrayOf(employeeName, date)

        try {
            val cursor = db.rawQuery(query, selectionArgs)

            if (cursor.moveToFirst()) {
                paymentAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("pay"))
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("DBHelper", "Error executing query: ${e.message}")
        } finally {
            db.close()
        }

        return paymentAmount
    }


    fun getEmployeesAndKilosOfTheMonth(month: Int, year: Int): Map<String, Double> {
        val db = readableDatabase
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1) // month is 0-based
        val dateStart = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
        calendar.set(year, month - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val dateEnd = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)

        val query = "SELECT DISTINCT employee_name, SUM(kilos) as total_kilos FROM TeaRecords WHERE date BETWEEN '$dateStart' AND '$dateEnd' GROUP BY employee_name"
        val cursor = db.rawQuery(query, null)
        val employees = mutableMapOf<String, Double>()
        with(cursor) {
            while (moveToNext()) {
                val employeeName = cursor.getString(cursor.getColumnIndex("employee_name"))
                val totalKilos = cursor.getDouble(cursor.getColumnIndex("total_kilos"))
                employees[employeeName] = totalKilos
            }
        }
        cursor.close()
        return employees
    }

    fun clearData() {
        val db = writableDatabase
        db.execSQL("DELETE FROM FarmersDatabase")
        db.execSQL("DELETE FROM FarmManagers")
        db.execSQL("DELETE FROM Employees")
        db.execSQL("DELETE FROM Companies")
        db.execSQL("DELETE FROM TeaRecords")
        db.execSQL("DELETE FROM PaymentTypes")
        db.close()
    }


}





















