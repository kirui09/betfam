
package com.betfam.apptea.ui.records

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.betfam.apptea.App
import com.betfam.apptea.DBHelper
import com.betfam.apptea.PendingPaymentData
import com.betfam.apptea.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class MonthlyPaymentAdapter(
    private val dbHelper: DBHelper,
    private val context: Context,
    private val groupedData: LinkedHashMap<String, ArrayList<MonthlyPayment>>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_GENERAL = 1
    private val VIEW_TYPE_EXPANDED = 2
    private var expandedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return if (viewType == VIEW_TYPE_GENERAL) {
            val view = inflater.inflate(R.layout.item_monthly_payment_general, parent, false)
            GeneralViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_monthly_payment, parent, false)
            ExpandedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val month = groupedData.keys.elementAt(position)
        Log.d("MonthlyPaymentFragment-Dickson", "Month extracted Dickson: $month")
        val payments = groupedData[month]

        if (holder is GeneralViewHolder) {
            holder.bind(month, payments)
        } else if (holder is ExpandedViewHolder) {
            holder.bind(month, payments)
        }
    }

    override fun getItemCount(): Int {
        return groupedData.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == expandedPosition) VIEW_TYPE_EXPANDED else VIEW_TYPE_GENERAL
    }

    inner class GeneralViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val monthTextView: TextView = itemView.findViewById(R.id.monthlypaymentdateTextView)
        private val totalPaymentTextView: TextView =
            itemView.findViewById(R.id.monthlypaymentTextView)
        private val showdetailsButton: ImageButton = itemView.findViewById(R.id.showmoredetails)
        val makePayment: ImageButton = itemView.findViewById(R.id.makePayment)

        fun bind(month: String, payments: ArrayList<MonthlyPayment>?) {
            val formattedMonth = getFormattedMonth(month)
            Log.d("monthly adapter", "Month extracted-Testing: $month $formattedMonth")
            monthTextView.text = formattedMonth

            val totalPayment = payments?.sumByDouble { it.paymentAmount } ?: 0.0
            totalPaymentTextView.text = NumberFormat.getCurrencyInstance(Locale("sw", "KE")).format(totalPayment)

            showdetailsButton.setOnClickListener {
                expandedPosition = if (expandedPosition == adapterPosition) {
                    RecyclerView.NO_POSITION
                } else {
                    adapterPosition
                }
                notifyDataSetChanged()
            }

            makePayment.setOnClickListener {
                val dbHelper = DBHelper(context) // Initialize your DBHelper instance
                val monthValue = parseMonth(month)
                val date = month
                val yearString = date.substring(0, 4)  // Extracts "2024"
                val year = yearString.toInt()  // Converts "2024" to 2024 as an integer

                val employeesOfTheMonth = dbHelper.getEmployeesAndKilosOfTheMonth(monthValue, year)

                val alertDialogBuilder = AlertDialog.Builder(context)

                var message = ""
                for ((employeeName, totalKilos) in employeesOfTheMonth) {
                    message += "$employeeName: $totalKilos kilos\n"
                }

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.MONTH, monthValue - 1) // month is 0-based
                val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US)

                alertDialogBuilder.setTitle("Employees to Be Paid")
                alertDialogBuilder.setMessage("Here are the employees of $monthName, $year:\n$message")

                // Add the Close button
                alertDialogBuilder.setNegativeButton("Close") { dialog, _ ->
                    dialog.dismiss()
                }

                // Add the Pay button
                alertDialogBuilder.setPositiveButton("Pay") { dialog, _ ->
                    // Show another dialog to confirm the pay rate
                    showPayRateConfirmationDialog(context, employeesOfTheMonth, month)
                }

                // Show the AlertDialog
                alertDialogBuilder.show()
            }


        }
    }

    inner class ExpandedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val monthTextView: TextView = itemView.findViewById(R.id.monthlypaymentdateTextView)
        private val employeeNameTextView: TextView =
            itemView.findViewById(R.id.monthlypaymentemployeeNameTextView)
        private val paymentAmountTextView: TextView =
            itemView.findViewById(R.id.monthlypaymentTextView)
        private val imageButtonContainer: TableLayout = itemView.findViewById(R.id.myTableLayout)
        private val showLessButton: ImageButton = itemView.findViewById(R.id.showlessdetails)

        init {
            showLessButton.setOnClickListener {
                expandedPosition = RecyclerView.NO_POSITION
                notifyDataSetChanged()
            }
        }

        fun bind(month: String, payments: ArrayList<MonthlyPayment>?) {
            val formattedMonth = getFormattedMonth(month)
            monthTextView.text = formattedMonth

            imageButtonContainer.removeAllViews()

            if (payments != null) {
                populateTable(imageButtonContainer, payments, month)
            }

            // Set initial visibility to GONE
            imageButtonContainer.visibility = if (adapterPosition == expandedPosition) View.VISIBLE else View.GONE

            // Animate the expansion
            if (adapterPosition == expandedPosition) {
                imageButtonContainer.post {
                    val animation = object : Animation() {
                        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                            imageButtonContainer.visibility = View.VISIBLE
                            imageButtonContainer.alpha = interpolatedTime
                        }

                        override fun willChangeBounds(): Boolean {
                            return true
                        }
                    }
                    animation.duration = 500 // Duration in milliseconds (adjust as needed)
                    imageButtonContainer.startAnimation(animation)
                }
            } else {
                imageButtonContainer.visibility = View.GONE
            }

            // Toggle expandedPosition when clicked
            imageButtonContainer.setOnClickListener {
                val wasExpanded = expandedPosition == adapterPosition
                expandedPosition = if (wasExpanded) RecyclerView.NO_POSITION else adapterPosition
                if (wasExpanded) {
                    val animation = object : Animation() {
                        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                            imageButtonContainer.alpha = 1 - interpolatedTime
                        }

                        override fun willChangeBounds(): Boolean {
                            return true
                        }
                    }
                    animation.duration = 500 // Duration in milliseconds (adjust as needed)
                    animation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {}
                        override fun onAnimationRepeat(animation: Animation?) {}
                        override fun onAnimationEnd(animation: Animation?) {
                            imageButtonContainer.visibility = View.GONE
                        }
                    })
                    imageButtonContainer.startAnimation(animation)
                }
                notifyDataSetChanged()
            }
        }


    }

    private fun getFormattedMonth(month: String): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val parsedMonth = sdf.parse(month)
        val calendar = Calendar.getInstance()
        calendar.time = parsedMonth ?: Date()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return monthFormat.format(calendar.time)
    }

    private fun populateTable(
        tableLayout: TableLayout,
        paymentList: ArrayList<MonthlyPayment>,
        month: String
    ) {

        // Create a TableRow for the date
        val dateRow = TableRow(context)
        dateRow.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )

        // Add the formatted date to the first column
        val dateTextView = TextView(context)
        dateTextView.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
            SimpleDateFormat(
                "yyyy-MM",
                Locale.getDefault()
            ).parse(month)!!
        )
        dateTextView.setPadding(5, 5, 5, 5)
        dateTextView.setTypeface(null, Typeface.BOLD)
        dateTextView.textSize = 16f
        dateRow.addView(dateTextView)

        // Calculate the total payment amount from the database
        val totalPaymentInDatabase = paymentList.sumByDouble { payment ->
            try {
                dbHelper.getPaymentAmountFromDatabase(payment.employeeName, payment.date)
            } catch (e: Exception) {
                Log.e("MonthlyPaymentAdapter", "Error calculating total payment from database: ${e.message}")
                0.0
            }
        }

        // Calculate the total calculated payment amount for the month
//        val totalCalculatedPayment = paymentList.sumByDouble { paymentDetails ->
//            calculatePay(paymentDetails.kilos, dbHelper.getEmployeeType(payment.employeeName))
//        }

        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        progressBar.layoutParams =
            TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        progressBar.progressDrawable = context.resources.getDrawable(R.drawable.progress_bar_white, null) // Set custom drawable

        // Calculate the progress percentage
//        val progressPercentage = if (totalCalculatedPayment != 0.0) {
//            ((totalPaymentInDatabase / totalCalculatedPayment) * 100).toInt()
//        } else {
//            0
//        }
//        progressBar.progress = progressPercentage

        // Add the progress bar to the row
        dateRow.addView(progressBar)

        // Add an empty cell for the employee name column
        val emptyCell = TextView(context)
        emptyCell.text = ""
        dateRow.addView(emptyCell)

        // Add the date row to the table layout
        tableLayout.addView(dateRow)

        // Add a separator line
        val separator = View(context)
        separator.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            1
        )
        separator.setBackgroundColor(Color.BLACK)
        tableLayout.addView(separator)

        val employeePaymentMap = paymentList.groupBy { it.employeeName }


        employeePaymentMap.forEach { (employeeName, payments) ->
            val tableRow = TableRow(context)
            tableRow.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )

            val nameTextView = TextView(context)
            nameTextView.text = employeeName
            nameTextView.setPadding(5, 5, 5, 5)
            nameTextView.setTypeface(null, Typeface.BOLD)
            nameTextView.textSize = 16f
            tableRow.addView(nameTextView)


            val totalPaymentAmount = payments.sumByDouble { it.paymentAmount }
            val paymentAmountTextView = TextView(context)
            paymentAmountTextView.text = "TOT: Ksh ${NumberFormat.getInstance().format(totalPaymentAmount)}"
            paymentAmountTextView.setPadding(5, 5, 5, 5)
            paymentAmountTextView.setTypeface(null, Typeface.BOLD)
            paymentAmountTextView.textSize = 16f
            tableRow.addView(paymentAmountTextView)



            val imageButton = ImageButton(context)
            imageButton.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
            imageButton.setBackgroundColor(Color.TRANSPARENT)
            tableRow.addView(imageButton)


            val detailsTable = TableLayout(context)
            detailsTable.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            detailsTable.visibility = View.GONE

            val paymentDetailsList = buildPaymentDetailsList(employeeName, month)
            paymentDetailsList.forEach { paymentDetail ->
                val detailsRow = TableRow(context)
                detailsRow.layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )

                // Create a TextView for the date
                val dateTextView = TextView(context)
                dateTextView.text = paymentDetail.date
                dateTextView.setPadding(5, 5, 5, 5)
                dateTextView.setTypeface(null, Typeface.BOLD)
                val dateLayoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                dateLayoutParams.setMargins(0, 0, 20, 0) // Add a right margin of 20px
                dateTextView.layoutParams = dateLayoutParams
                detailsRow.addView(dateTextView)

                // Create a TextView for the payment amount
                val paymentAmountTextView = TextView(context)
                paymentAmountTextView.text = "Ksh ${NumberFormat.getInstance().format(paymentDetail.paymentAmount)} - Kilos: ${NumberFormat.getInstance().format(paymentDetail.kilos)}"
                paymentAmountTextView.setPadding(5, 5, 5, 5)
                paymentAmountTextView.setTypeface(null, Typeface.BOLD)
                val paymentLayoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                paymentLayoutParams.setMargins(20, 0, 0, 0) // Add a left margin of 20px
                paymentAmountTextView.layoutParams = paymentLayoutParams
                detailsRow.addView(paymentAmountTextView)

                // Create a check ImageButton or "Pending"
                val checkImageButton = ImageButton(context)
                checkImageButton.setBackgroundColor(Color.TRANSPARENT)
                val checkLayoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                checkLayoutParams.setMargins(20, 0, 0, 0) // Add a left margin of 20px
                checkImageButton.layoutParams = checkLayoutParams

                if (paymentDetail.isPaymentCompleted) {
                    checkImageButton.setImageResource(R.drawable.baseline_check_24)
                    checkImageButton.setOnClickListener {
                        Toast.makeText(context, "Paid", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    checkImageButton.setImageResource(R.drawable.baseline_pending_24)
                    checkImageButton.setOnClickListener {
                        Toast.makeText(context, "Payment pending", Toast.LENGTH_SHORT).show()
                    }
                }

                detailsRow.addView(checkImageButton)

                // Add a separator line
                val separator = View(context)
                separator.layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1
                )
                separator.setBackgroundColor(Color.BLACK)
                tableLayout.addView(separator)

                detailsTable.addView(detailsRow)
            }




            imageButton.setOnClickListener {
                if (detailsTable.visibility == View.VISIBLE) {
                    imageButton.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
                    detailsTable.visibility = View.GONE
                } else {
                    imageButton.setImageResource(R.drawable.baseline_keyboard_arrow_up_24)
                    detailsTable.visibility = View.VISIBLE
                }
            }


            tableLayout.addView(tableRow)
            tableLayout.addView(detailsTable)
        }
    }

    private fun buildPaymentDetailsList(employeeName: String, month: String): List<PaymentDetail> {
        val paymentDetailsList = mutableListOf<PaymentDetail>()
        // Parse the month string to extract the month and year values
        val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(month)
        val calendar = Calendar.getInstance()
        calendar.time = monthYear
        val monthValue = calendar.get(Calendar.MONTH) + 1 // Months are 0-based
        val yearValue = calendar.get(Calendar.YEAR)

        Log.d("PaymentDetails", "Fetching payment details for $employeeName, $monthValue/$yearValue")

        // Fetch the payment details for the employee and month from the database
        val payments = dbHelper.getPaymentDetailsForEmployeeAndMonth(employeeName, monthValue, yearValue)




        // Fetch the employee type from the database
        val employeeType = dbHelper.getEmployeeType(employeeName)

        Log.d("PaymentDetails", "Number of payment records: ${payments.size}")

        // Build the payment details list

        payments.forEach { (date, kilos, pay) ->
            val dateFormat = SimpleDateFormat("EEE, d MMMM ", Locale.ENGLISH)
            val formattedDate = dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(date))

            try {
                val actualPaymentAmount = dbHelper.getPaymentAmountFromDatabase(employeeName, date)
                Log.d("ActPaymentDetails", "Fetched payment amount for $employeeName on $date: $actualPaymentAmount")
                val isPaymentCompleted = actualPaymentAmount > 0
                paymentDetailsList.add(PaymentDetail(formattedDate, kilos, pay, isPaymentCompleted))
            } catch (e: Exception) {
                Log.e("ActPaymentDetails", "Error fetching payment amount for $employeeName on $date: ${e.message}")
                // Handle the error or skip adding the payment detail to the list
            }
        }


        return paymentDetailsList
    }

    private fun calculatePay(kilos: Double, employeeType: String): Double {
        val payRate = dbHelper.getPaymentTypes()[employeeType] ?: return 0.0 // Use 0.0 if pay rate not found
        Log.d("MonthlyPaymentAdapter", "Calculating pay for $employeeType: $kilos * $payRate")
        return kilos * payRate
    }

    fun updateData(newData: LinkedHashMap<String, ArrayList<MonthlyPayment>>) {
        groupedData.clear()
        groupedData.putAll(newData)
        notifyDataSetChanged()
    }

    fun parseMonth(monthString: String): Int {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val parsedMonth = sdf.parse(monthString)
        val calendar = Calendar.getInstance()
        calendar.time = parsedMonth ?: Date()
        return calendar.get(Calendar.MONTH) + 1 // Month is 0-based in Calendar, so add 1
    }



    fun showPayRateConfirmationDialog(context: Context, employeesOfTheMonth: Map<String, Double>, month: String) {
        val sharedPreferences = getSharedPreferences()
        val defaultPayRate = String.format("%.2f", sharedPreferences.getFloat("pay_rate", 8.0f).toDouble()).toDouble()



        // Fetch unpaid records and the total of paid records
        val (unpaidRecords, totalPaidAmount) = getUnpaidRecordsAndTotalPaid(context, employeesOfTheMonth,month)

        val (unpaidMessage, unpaidAmount) = generateTotalPayMessage(unpaidRecords, defaultPayRate)

        val confirmationDialogBuilder = AlertDialog.Builder(context)
        confirmationDialogBuilder.setTitle("Confirm Pay Rate")

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val messageTextView = TextView(context)
        messageTextView.text = """
        The pay rate is Ksh $defaultPayRate.
        
        Unpaid Records:
        $unpaidMessage
        Total Unpaid Amount: Ksh $unpaidAmount
        
        Note: Ksh $totalPaidAmount has already been paid for this period $month.
    """.trimIndent()
        layout.addView(messageTextView)

        val editButton = Button(context)
        editButton.text = "Edit Pay Rate"
        layout.addView(editButton)

        confirmationDialogBuilder.setView(layout)

        editButton.setOnClickListener {
            showEditPayRateDialog(context, unpaidRecords, totalPaidAmount, messageTextView)
        }

        confirmationDialogBuilder.setPositiveButton("Confirm") { dialog, _ ->
            handlePayment(context, unpaidRecords, defaultPayRate,month)
            dialog.dismiss()
        }
        confirmationDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        confirmationDialogBuilder.show()
    }
//    fun getTotalPaymentForDay(date: String): Pair<Double, Double> {
//        val db = dbHelper.readableDatabase
//        var totalKilos: Double = 0.0
//        var totalPayment: Double = 0.0
//
//        val cursor = db.rawQuery("SELECT SUM(kilos), SUM(pay) FROM TeaRecords WHERE date = ?", arrayOf(date))
//        if (cursor.moveToFirst()) {
//            totalKilos = cursor.getDouble(0)
//            totalPayment = cursor.getDouble(1)
//        }
//        cursor.close()
//        db.close()
//
//        return Pair(totalKilos, totalPayment)
//    }
fun getUnpaidRecordsAndTotalPaid(context: Context, employeesOfTheMonth: Map<String, Double>,month: String): Pair<Map<String, Double>, Double> {
    val dbHelper = DBHelper(context)
    val unpaidRecords = mutableMapOf<String, Double>()
    var totalPaidAmount = 0.0
    Log.d("getUnpaidRecordsAndtotalPaid", "Month extracted Dickson Tested: $month")
    for ((employeeName, _) in employeesOfTheMonth) {
        val teaRecords = dbHelper.getTeaRecordsForEmployee(employeeName, month)
        var unpaidKilos = 0.0

        for (record in teaRecords) {
            Log.d("TeaRecord", "ID: ${record.id}, Date: ${record.date}, Company: ${record.company}, Employee: ${record.employees}, Kilos: ${record.kilos}, Payment: ${record.payment}")
            val paymentAmount = record.payment // Assuming payment is stored in the record

            if (paymentAmount == 0.0) {
                unpaidKilos += record.kilos
            } else {
                totalPaidAmount += paymentAmount
            }
        }

        if (unpaidKilos > 0) {
            unpaidRecords[employeeName] = unpaidKilos
        }
    }

    return Pair(unpaidRecords, totalPaidAmount)
}

    fun generateTotalPayMessage(unpaidRecords: Map<String, Double>, payRate: Double): Pair<String, Double> {
        var totalPayMessage = ""
        var totalAmount = 0.0
        for ((employeeName, totalKilos) in unpaidRecords) {
            val totalPay = totalKilos * payRate
            totalPayMessage += "$employeeName: $totalKilos kilos * Ksh $payRate = Ksh $totalPay\n"
            totalAmount += totalPay
        }
        return Pair(totalPayMessage, totalAmount)
    }

    fun showEditPayRateDialog(context: Context, unpaidRecords: Map<String, Double>, totalPaidAmount: Double, messageTextView: TextView) {
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

                val sharedPreferences = getSharedPreferences()
                with(sharedPreferences.edit()) {
                    putFloat("pay_rate", formattedPayRate)
                    apply()
                }

                val (updatedUnpaidMessage, updatedUnpaidAmount) = generateTotalPayMessage(unpaidRecords, formattedPayRate.toDouble())

                messageTextView.text = """
                The pay rate is Ksh $formattedPayRate.
                
                Unpaid Records:
                $updatedUnpaidMessage
                Total Unpaid Amount: Ksh $updatedUnpaidAmount
                
                Note: Ksh $totalPaidAmount has already been paid for this period.
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

// The handlePayment function remains the same as in the previous response



    fun handlePayment(context: Context, employeesOfTheMonth: Map<String, Double>, payRate: Double,month: String) {
        Log.d("HandlePayment", "Starting payment process...$month")
        val recordsViewModel = RecordsViewModel.create(context)
        CoroutineScope(Dispatchers.Default).launch {
            Log.d("HandlePayment", "Coroutine launched")

            val dbHelper = DBHelper(context)
            val appDatabase = App.getDatabase(context)
            val pendingPaymentDataDao = appDatabase.pendingPaymentDao()

            val sharedPreferences = context.getSharedPreferences("com.betfam.apptea.preferences", Context.MODE_PRIVATE)
            val payRateFromPreferences = sharedPreferences.getFloat("pay_rate", payRate.toFloat()).toDouble()
            val formattedPayRate = BigDecimal(payRateFromPreferences).setScale(2, RoundingMode.HALF_UP)

            val (unpaidRecords, _) = getUnpaidRecordsAndTotalPaid(context, employeesOfTheMonth,month)

            for ((employeeName, totalUnpaidKilos) in unpaidRecords) {
                Log.d("HandlePayment", "Processing employee: $employeeName, Total Unpaid Kilos: $totalUnpaidKilos")

                val teaRecords = withContext(Dispatchers.IO) {
                    dbHelper.getTeaRecordsForEmployee(employeeName,month )
                }
                Log.d("HandlePayment", "Fetched ${teaRecords.size} tea records for $employeeName")

                teaRecords.forEach { record ->
                    if (record.payment == 0.0) {
                        val paymentAmount = BigDecimal(record.kilos).multiply(formattedPayRate).setScale(2, RoundingMode.HALF_UP)
                        val paymentData = PendingPaymentData(
                            id = record.id,
                            date = record.date,
                            employeeName = employeeName,
                            paymentAmount = paymentAmount.toDouble()
                        )

                        Log.d("HandlePayment", "Saving payment for $employeeName on ${record.date}: Ksh ${paymentAmount.toPlainString()} (${record.kilos} kilos) with ID ${record.id}")

                        withContext(Dispatchers.IO) {
                            // pendingPaymentDataDao.insert(paymentData)
                            dbHelper.updatePaymentInTeaRecords(record.id, paymentAmount.toDouble())

                            Log.d("HandlePayment", "Record updated in TeaRecords: Employee: $employeeName, Date: ${record.date}, Amount: Ksh ${paymentAmount.toPlainString()}, Record ID: ${record.id}")
                        }
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

    private fun updateUIWithPaidRecords(paidRecords: List<TeaPaymentRecord>) {
        // Implement this method to update your UI with the paid records
        // For example, you might want to update your RecyclerView or show a summary
        // This will depend on how you want to display the paid records in your UI
    }



    private fun getSharedPreferences(): SharedPreferences {
        return context.getSharedPreferences("com.betfam.apptea.preferences", Context.MODE_PRIVATE)
    }


}