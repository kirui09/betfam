package com.betfam.apptea.ui.records

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Typeface
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.betfam.apptea.App
import com.betfam.apptea.DBHelper
import com.betfam.apptea.PendingPaymentData
import com.betfam.apptea.R
import com.betfam.apptea.SharedPreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
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
        val paybutton: AppCompatButton = itemView.findViewById(R.id.payButton)
        val verifiedButton: ImageButton = itemView.findViewById(R.id.verifiedButton)

        init {
            verifiedButton.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM_PAYMENT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment, parent, false)
                PaymentViewHolder(view)
            }
            VIEW_TYPE_GENERAL_PAY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_general_pay, parent, false)
                GeneralPayViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val day = groupedData.keys.elementAt(position)
        Log.d("DayValue", "Day at position $position: $day")
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

            headerPaymentTextView.text = "Est. Pay"
            headerPaymentTextView.setTypeface(null, Typeface.BOLD)

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
                val paymentAmount = calculatePay(payment)
                paymentTextView.text = "Ksh ${NumberFormat.getInstance().format(paymentAmount)}"

                row.addView(employeeNameTextView)
                row.addView(kilosTextView)
                row.addView(paymentTextView)

                tableLayout.addView(row)
            }

            holder.seeLessButton.setOnClickListener {
                expandedPosition = RecyclerView.NO_POSITION
                notifyDataSetChanged()
            }
        }

        if (holder is GeneralPayViewHolder) {
            holder.generalPayDateTextView.text = formatDate(day)
            val totalPayment = getTotalPaymentForDay(day)
            holder.totalPayForDayTextView.text = "Total Payment: Ksh ${NumberFormat.getInstance().format(totalPayment)}"

          //  holder.checkBox.isChecked = isVerified
          //  holder.checkBox.visibility = if (isVerified) View.GONE else View.VISIBLE
       //    holder.verifiedButton.visibility = if (isVerified) View.VISIBLE else View.GONE

            holder.paybutton.setOnClickListener { view ->
               // val isChecked = (view as CheckBox).isChecked
                if (holder.paybutton.text == "Pay") {

                    val sharedPreferences = context.getSharedPreferences("com.betfam.apptea.preferences", Context.MODE_PRIVATE)
                    val payRate = 8
                    val payRateFromPreferences = sharedPreferences.getFloat("pay_rate", payRate.toFloat()).toDouble()
                    val formattedPayRate = String.format("%.2f", payRateFromPreferences).toDouble()

                   // val employeesForDay = payments?.map { it.employeeName to it.kilos }?.toMap() ?: emptyMap()
                    val employeesForDay = dbHelper.getEmployeesAndKilosOfTheDay(day)


                    val (totalPayMessage, totalAmount) = generateTotalPayMessage(employeesForDay, formattedPayRate)

                    val confirmationDialogBuilder = AlertDialog.Builder(context)
                    confirmationDialogBuilder.setTitle("Confirm Pay Rate")

                    val layout = LinearLayout(context)
                    layout.orientation = LinearLayout.VERTICAL

                    val messageTextView = TextView(context)
                    messageTextView.text = "The pay rate is Ksh $formattedPayRate.\nHere is the payment breakdown:\n$totalPayMessage\nTotal Amount: Ksh $totalAmount"
                    layout.addView(messageTextView)

                    val editButton = Button(context)
                    editButton.text = "Edit Pay Rate"
                    layout.addView(editButton)

                    confirmationDialogBuilder.setView(layout)

                    editButton.setOnClickListener {
                        showEditPayRateDialog(context, employeesForDay, messageTextView)
                    }

                    confirmationDialogBuilder.setPositiveButton("Confirm") { dialog, _ ->
                        payments?.forEach { payment ->
                            handlePayment(context,formattedPayRate,day)
                        }
                        sharedPreferencesHelper.saveCheckBoxState(day, true)
                        Toast.makeText(context, "Payments saved to database", Toast.LENGTH_SHORT).show()
                       // holder.checkBox.visibility = View.GONE
                        holder.paybutton.text = "Paid"
                        holder.paybutton.isEnabled = false
                       // holder.paybutton.setBackgroundColor(ContextCompat.getColor(this, R.color.paid_button_color))
                       // holder.verifiedButton.visibility = View.VISIBLE
                        dialog.dismiss()
                    }

                    confirmationDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                     //   holder.checkBox.isChecked = false
                    }

                    confirmationDialogBuilder.show()
                } else {
                    // Handle unchecking if needed
                    Log.d("HOlder is GeneralViewHolder", "CheckBox unchecked")
                    // You may want to add logic here if unchecking should do something specific
                }
            }


            holder.seeMorePayButton.setOnClickListener {
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
    fun getTotalPaymentForDay(date: String): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(pay) FROM TeaRecords WHERE date = ? and (status<>'Del' OR status IS NULL)", arrayOf(date))
        cursor.moveToFirst()
        val totalPayment = cursor.getDouble(0)
        cursor.close()
        db.close()
        return totalPayment
    }
    private fun calculatePay(payment: Payment): Double {
        val kilos = payment.kilos
        val defaultPayRate = 8.0
        val sharedPreferences = context.getSharedPreferences("com.betfam.apptea.preferences", Context.MODE_PRIVATE)
        val payRateFromPreferences = sharedPreferences.getFloat("pay_rate", defaultPayRate.toFloat()).toDouble() // Assuming payRateKey is the key for stored pay rate
        val formattedPayRate = String.format("%.2f", payRateFromPreferences).toDouble() // Format the pay rate to two decimal places and convert to double
        Log.d("PaymentAdapter", "Calculating pay: $kilos * $formattedPayRate")
        return kilos * formattedPayRate
    }

    private fun savePaymentToTeaRecords(payment: Payment) {
        val recordsViewModel = RecordsViewModel.create(context)
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
            CoroutineScope(Dispatchers.IO).launch {
                try {
                   // recordsViewModel.syncAndCompareDataWithGoogleSheet()
                    Log.d("HandlePayment", "Successfully synced with Google Sheets")
                } catch (e: Exception) {
                    Log.e("HandlePayment", "Error syncing with Google Sheets", e)
                }

                // Refresh local records
                withContext(Dispatchers.Main) {
                    recordsViewModel.refreshRecords()
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
        // Explicitly create an array of strings for the query parameters
        val cursor = db.rawQuery(query, arrayOf(id.toString(), date, name, kilos.toString()))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }


    private fun showEditPayRateDialog(context: Context, payments: Map<String, Double>, messageTextView: TextView) {
        val editDialogBuilder = AlertDialog.Builder(context)
        editDialogBuilder.setTitle("Edit Pay Rate")

        val editText = EditText(context)
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editText.hint = "Enter new pay rate"
        editDialogBuilder.setView(editText)

        editDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            val inputText = editText.text.toString()
            val newPayRate = inputText.toDoubleOrNull()
            if (newPayRate != null) {
                val formattedPayRate = String.format("%.2f", newPayRate).toFloat()
                val sharedPreferences = context.getSharedPreferences("com.betfam.apptea.preferences", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putFloat("pay_rate", formattedPayRate)
                    apply()
                }

                messageTextView.text = """
                The pay rate is Ksh $formattedPayRate.
                
      
            """.trimIndent()
            } else {
                Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        editDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        editDialogBuilder.show()
    }

    private fun savePayRateToSharedPreferences(PayRate: Double) {
        val sharedPreferences = context.getSharedPreferences("com.betfam.apptea.preferences", Context.MODE_PRIVATE)
        //val formattedPayRate = String.format("%.2f", sharedPreferences).toFloat() // Format the pay rate to two decimal places and convert to float
        val payRateFromPreferences = sharedPreferences.getFloat("pay_rate", PayRate.toFloat()).toDouble() // Assuming payRateKey is the key for stored pay rate
        val formattedPayRate = String.format("%.2f", payRateFromPreferences).toFloat() // Format the pay rate to two decimal places and convert to double

        with(sharedPreferences.edit()) {
            putFloat("pay_rate", formattedPayRate)
            apply()
        }

    }

    private fun generateTotalPayMessage(employeesForDay: Map<String, Double>, formattedPayRate: Double): Pair<String, Double> {
        var totalPayMessage = ""
        var totalAmount = 0.0

        for ((employeeName, kilos) in employeesForDay) {
            val payAmount = kilos * formattedPayRate
            totalPayMessage += "$employeeName: $kilos Kilos * Ksh $formattedPayRate = Ksh $payAmount\n"
            totalAmount += payAmount
        }

        return Pair(totalPayMessage, totalAmount)
    }
    fun handlePayment(context: Context,payRate: Double,day: String) {

        val recordsViewModel = RecordsViewModel.create(context)
        CoroutineScope(Dispatchers.Default).launch {
            Log.d("HandlePayment", "Coroutine launched")

            val dbHelper = DBHelper(context)
            val appDatabase = App.getDatabase(context)
            val pendingPaymentDataDao = appDatabase.pendingPaymentDao()

            val sharedPreferences = context.getSharedPreferences("com.betfam.apptea.preferences", Context.MODE_PRIVATE)
            val payRateFromPreferences = sharedPreferences.getFloat("pay_rate", payRate.toFloat()).toDouble()
            val formattedPayRate = BigDecimal(payRateFromPreferences).setScale(2, RoundingMode.HALF_UP)


                val teaRecords = withContext(Dispatchers.IO) {
                    dbHelper.getTeaRecordsForEmployeeforday(day )
                }
                Log.d("HandlePayment", "Fetched ${teaRecords.size} tea records for ")

                teaRecords.forEach { record ->
                    if (record.payment == 0.0) {
                        val paymentAmount = BigDecimal(record.kilos).multiply(formattedPayRate).setScale(2, RoundingMode.HALF_UP)
                        val paymentData = PendingPaymentData(
                            id = record.id,
                            date = record.date,
                            employeeName = record.employees,
                            paymentAmount = paymentAmount.toDouble()
                        )



                        withContext(Dispatchers.IO) {
                            // pendingPaymentDataDao.insert(paymentData)
                            dbHelper.updatePaymentInTeaRecords(record.id, paymentAmount.toDouble())


                        }
                    }
                }

            try {
               // recordsViewModel.syncAndCompareDataWithGoogleSheet()
                Log.d("HandlePayment", "Successfully synced with Google Sheets")
            } catch (e: Exception) {
                Log.e("HandlePayment", "Error syncing with Google Sheets", e)
            }

            // Refresh local records
            withContext(Dispatchers.Main) {
                recordsViewModel.refreshRecords()
            }
            Log.d("HandlePayment", "Payment process completed")
        }
    }



}
