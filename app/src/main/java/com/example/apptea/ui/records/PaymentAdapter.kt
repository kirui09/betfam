package com.example.apptea.ui.records

import android.content.ContentValues
import android.content.Context
import android.content.res.ColorStateList
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.AnimationDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.children
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
        val isVerified = sharedPreferencesHelper.getCheckBoxState(day)


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
            val headerPayAllTextView = TextView(holder.itemView.context)

            headerEmployeeNameTextView.text = "Employee Name"
            headerEmployeeNameTextView.setTypeface(null, Typeface.BOLD)

            headerKilosTextView.text = "Kilos"
            headerKilosTextView.setTypeface(null, Typeface.BOLD)

            headerPaymentTextView.text = "Payment"
            headerPaymentTextView.setTypeface(null, Typeface.BOLD)


            // Define colors for different states
            val colorStateList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_enabled)),
                intArrayOf(Color.GREEN, Color.RED) // Default color is green, color when blinking is red
            )

// Define the custom drawable for the border
            val borderDrawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.border_textview)

// Set the text and make it bold

            headerPayAllTextView.text = "Pay Now"
            headerPayAllTextView.setTypeface(null, Typeface.BOLD)
// Apply the color state list to the text view
            headerPayAllTextView.setTextColor(colorStateList)

// Set the custom drawable as the background for the TextView to create a border
            headerPayAllTextView.background = borderDrawable


// Define the blinking animation
            val animation = AlphaAnimation(0.0f, 1.0f)
            animation.duration = 500 // Blinking duration
            animation.interpolator = LinearInterpolator()
            animation.repeatCount = Animation.INFINITE // Repeat animation infinitely
            animation.repeatMode = Animation.REVERSE // Reverse animation at the end

// Start the animation
            headerPayAllTextView.startAnimation(animation)


            headerRow.addView(headerEmployeeNameTextView)
            headerRow.addView(headerKilosTextView)
            headerRow.addView(headerPaymentTextView)
            headerRow.addView(headerPayAllTextView)
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

                // Add checkbox
                val paycheckBox = CheckBox(holder.itemView.context)
                row.addView(paycheckBox)
                paycheckBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        // Checkbox is checked, save the payment to the database
//                        dbHelper.savePayment(payment)
                    } else {
                        // Checkbox is unchecked, remove the payment from the database
                    }
                }
                headerPayAllTextView.setOnClickListener {
                    val paymentsToSave = arrayListOf<Payment>()
                    tableLayout.children.forEach { view ->
                        if (view is TableRow && view.getChildAt(3) is CheckBox) {
                            val checkBox = view.getChildAt(3) as CheckBox
                            if (checkBox.isChecked) {
                                val dateTextView = view.getChildAt(0) as? TextView
                                val employeeNameTextView = view.getChildAt(1) as? TextView
                                val kilosTextView = view.getChildAt(2) as? TextView
                                val paymentAmountTextView = view.getChildAt(4) as? TextView

                                val date = dateTextView?.text.toString()
                                val employeeName = employeeNameTextView?.text.toString()
                                val kilos = kilosTextView?.text.toString().toDoubleOrNull() ?: 0.0
                                val paymentAmount = paymentAmountTextView?.text.toString().toDoubleOrNull() ?: 0.0

                                val payment = Payment(-1, date, employeeName, kilos, paymentAmount)
                                paymentsToSave.add(payment)
                            }
                        }
                    }

                    if (paymentsToSave.isNotEmpty()) {
                        dbHelper.savePayments(paymentsToSave)
                        Toast.makeText(context, "Payments saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No payments to save", Toast.LENGTH_SHORT).show()
                    }
                }




                tableLayout.addView(row)



            }

            holder.seeLessButton.setOnClickListener {
                // Handle collapsing here
                expandedPosition = RecyclerView.NO_POSITION
                notifyDataSetChanged()
            }
        }


        if (holder is GeneralPayViewHolder) {
            holder.generalPayDateTextView.text = formatDate(day) // Set the formatted date for the general pay

            // Calculate and display the sum of all payments for the day
            val totalPayment = payments?.sumByDouble { payment ->
                val employeeType = dbHelper.getEmployeeType(payment.employeeName)
                calculatePay(payment, employeeType)
            } ?: 0.0
            holder.totalPayForDayTextView.text =
                "Total Payment: Ksh ${NumberFormat.getInstance().format(totalPayment)}"

            // Set the checkbox state based on SharedPreferences
            holder.checkBox.isChecked = isVerified

            holder.verifiedButton.setBackgroundResource(android.R.color.transparent)



            // Hide or show the checkbox and verified button based on the saved state
            holder.checkBox.visibility = if (isVerified) View.GONE else View.VISIBLE
            holder.verifiedButton.visibility = if (isVerified) View.VISIBLE else View.GONE

            // Set onCheckedChangeListener for the checkbox
            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
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
                        sharedPreferencesHelper.saveCheckBoxState(day, true)
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
                    // Optionally handle unchecking if needed
                    // ...
                }
            }

            // ... (rest of your existing code)

            holder.seeMorePayButton.setOnClickListener {
                // Handle expanding here
                expandedPosition = position
                notifyDataSetChanged()
            }

            holder.verifiedButton.setOnClickListener {

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
