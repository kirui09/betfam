package com.example.apptea.ui.records

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.DBHelper
import com.example.apptea.R
import com.example.apptea.SharedPreferencesHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class PaymentAdapter(
    private val context: Context,
    private val groupedData: LinkedHashMap<String, ArrayList<Payment>>,
    private val dbHelper: DBHelper, // Database helper to fetch employee type
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM_PAYMENT = 1
    private val VIEW_TYPE_GENERAL_PAY = 2
    private var expandedPosition = RecyclerView.NO_POSITION

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paymentDateTextView: TextView = itemView.findViewById(R.id.paymentdateTextView)
        val paymentEmployeeNameTextView: TextView = itemView.findViewById(R.id.paymentemployeeNameTextView)
        val paymentKilosTextView: TextView = itemView.findViewById(R.id.paymentkilosTextView)
        val paymentTextView: TextView = itemView.findViewById(R.id.paymentTextView)
        val seeLessButton: Button = itemView.findViewById(R.id.seelessPayButton)
    }

    inner class GeneralPayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val generalPayDateTextView: TextView = itemView.findViewById(R.id.generalpaydateTextView)
        val totalPayForDayTextView: TextView = itemView.findViewById(R.id.totalpayForDay)
        val seeMorePayButton: Button = itemView.findViewById(R.id.seeMorePayButton)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkedpayCheckBox)
        val verifiedButton: ImageButton = itemView.findViewById(R.id.verifiedButton)

        init {
            verifiedButton.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM_PAYMENT -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_payment, parent, false)
                PaymentViewHolder(view)
            }
            VIEW_TYPE_GENERAL_PAY -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_general_pay, parent, false)
                GeneralPayViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val day = groupedData.keys.elementAt(position)
        val payments = groupedData[day]

        if (holder is PaymentViewHolder) {
            val formattedDate = formatDate(day)
            val tableLayout = holder.itemView.findViewById<TableLayout>(R.id.paymentTableLayout)
            tableLayout.removeAllViews()


            // Create a date row
            val dateRow = TableRow(holder.itemView.context)
            val dateTextView = TextView(holder.itemView.context)
            dateTextView.text = "$formattedDate"
            dateTextView.setTypeface(null, Typeface.BOLD)
            dateRow.addView(dateTextView)
            tableLayout.addView(dateRow)

            // Create a header row
            val headerRow = TableRow(holder.itemView.context)
            val headerEmployeeNameTextView = TextView(holder.itemView.context)
            val headerKilosTextView = TextView(holder.itemView.context)
            val headerPaymentTextView = TextView(holder.itemView.context)
//            headerEmployeeNameTextView.text = "Employee Name"
//            headerKilosTextView.text = "Kilos"
//            headerPaymentTextView.text = "Payment"
            headerRow.addView(headerEmployeeNameTextView)
            headerRow.addView(headerKilosTextView)
            headerRow.addView(headerPaymentTextView)
            tableLayout.addView(headerRow)

            // Add rows for each payment
            payments?.forEach { payment ->
                val row = TableRow(holder.itemView.context)
                val employeeNameTextView = TextView(holder.itemView.context)
                employeeNameTextView.setTypeface(null, Typeface.BOLD)

                val kilosTextView = TextView(holder.itemView.context)
                kilosTextView.setTypeface(null, Typeface.BOLD)

                val paymentTextView = TextView(holder.itemView.context)
                paymentTextView.setTypeface(null, Typeface.BOLD)

                employeeNameTextView.text = payment.employeeName
                kilosTextView.text = "${NumberFormat.getInstance().format(payment.kilos)} Kilos"
                val employeeType = dbHelper.getEmployeeType(payment.employeeName)

                val paymentAmount = calculatePay(payment, employeeType)
                paymentTextView.text = "Ksh ${NumberFormat.getInstance().format(paymentAmount)}"

                row.addView(employeeNameTextView)
                row.addView(kilosTextView)
                row.addView(paymentTextView)
                tableLayout.addView(row)
            }

            holder.seeLessButton.setOnClickListener {
                // Handle collapsing here
                expandedPosition = RecyclerView.NO_POSITION
                notifyDataSetChanged()
            }
        }
        else if (holder is GeneralPayViewHolder) {
            val formattedDate = formatDate(day)
            holder.generalPayDateTextView.text = formattedDate // Set the formatted date for the general pay

            // Calculate and display the sum of all payments for the day
            val totalPayment = payments?.sumByDouble { payment ->
                val employeeType = dbHelper.getEmployeeType(payment.employeeName)
                calculatePay(payment, employeeType)
            } ?: 0.0
            holder.totalPayForDayTextView.text =
                "Total Payment: Ksh ${NumberFormat.getInstance().format(totalPayment)}"


            val isChecked = sharedPreferencesHelper.getCheckBoxState()
            holder.checkBox.isChecked = isChecked

            holder.checkBox.setOnCheckedChangeListener(null) // Remove previous listener

            holder.checkBox.setOnClickListener {
                if (holder.checkBox.isChecked) {
                    val alertDialogBuilder = AlertDialog.Builder(context)
                    alertDialogBuilder.setTitle("Confirm")
                    alertDialogBuilder.setMessage("Are you sure you want to save these payments?")
                    alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                        // Iterate through payments and save each payment to TeaRecords table
                        payments?.forEach { payment ->
                            val employeeType = dbHelper.getEmployeeType(payment.employeeName)
                            val paymentAmount = calculatePay(payment, employeeType)

                            // Create a new instance of Payment with the updated paymentAmount
                            val updatedPayment = payment.copy(paymentAmount = paymentAmount)
                            savePaymentToTeaRecords(updatedPayment)
                        }
                        // Update the shared preferences to reflect the checkbox state
                        sharedPreferencesHelper.saveCheckBoxState(true)
                        // Show a toast message to indicate that data is saved
                        Toast.makeText(context, "Payments saved to database", Toast.LENGTH_SHORT).show()

                        // Change the checkbox to the verified button
                        holder.checkBox.visibility = View.GONE
                        holder.verifiedButton.visibility = View.VISIBLE
                        holder.verifiedButton.setBackgroundResource(android.R.color.transparent)

                    }
                    alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                        holder.checkBox.isChecked = false
                    }
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                } else {
                    // Handle unchecking the checkbox if needed
                }
            }


            holder.seeMorePayButton.setOnClickListener {
                // Handle expanding here
                expandedPosition = position
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return groupedData.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == expandedPosition) VIEW_TYPE_ITEM_PAYMENT else VIEW_TYPE_GENERAL_PAY
    }

    fun updateData(newGroupedData: LinkedHashMap<String, ArrayList<Payment>>) {
        groupedData.clear()
        groupedData.putAll(newGroupedData)
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = dateFormat.parse(dateString)
        val formattedDateFormat = SimpleDateFormat("EEE, d MMMM, yyyy", Locale.ENGLISH)
        return formattedDateFormat.format(date)
    }

    private fun calculatePay(payment: Payment, employeeType: String): Double {
        val kilos = payment.kilos
        val payRate = dbHelper.getPaymentTypes()[employeeType] ?: return 0.0 // Use 0.0 if pay rate not found
        Log.d("PaymentAdapter", "Calculating pay for $employeeType: $kilos * $payRate")
        return kilos * payRate
    }


    private fun savePaymentToTeaRecords(payment: Payment) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", payment.id)
            put("date", payment.date)
            put("employee_name", payment.employeeName)
            put("kilos", payment.kilos)
            put("pay", payment.paymentAmount)
        }

        try {
            // Check if the payment already exists in the database
            if (!paymentExists(db, payment.id, payment.date, payment.employeeName, payment.kilos)) {
                // Payment does not exist, so insert it
                db.insert("TeaRecords", null, values)
                Log.d("DBHelper", "Inserted payment record for ${payment.employeeName} on ${payment.date}")
            } else {
                // Payment already exists, so update it
                val whereClause = "id = ? AND date = ? AND employee_name = ? AND kilos = ?"
                val whereArgs = arrayOf(
                    payment.id.toString(),
                    payment.date,
                    payment.employeeName,
                    payment.kilos.toString()
                )
                db.update("TeaRecords", values, whereClause, whereArgs)
                Log.d("DBHelper", "Updated payment record for ${payment.employeeName} on ${payment.date} with payment ${payment.paymentAmount}")
            }
        } catch (e: Exception) {
            Log.e("DBHelper", "Error saving payment record: ${e.localizedMessage}")
        } finally {
            db.close()
        }
    }


    private fun paymentExists(db: SQLiteDatabase, id: Int, date: String, name: String, kilos: Double): Boolean {
        val query = "SELECT * FROM TeaRecords WHERE id = ? AND date = ? AND employee_name = ? AND kilos = ?"
        // Explicitly create an array of strings for the query parameters
        val cursor = db.rawQuery(query, arrayOf(id.toString(), date, name, kilos.toString()))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

}
