package com.example.apptea.ui.records

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.DBHelper
import com.example.apptea.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

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
        val payments = groupedData[month]

        if (holder is GeneralViewHolder) {
            holder.bind(month, payments)
        } else if (holder is ExpandedViewHolder) {
            holder.bind(month, payments) // Pass the month parameter here
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

        fun bind(month: String, payments: ArrayList<MonthlyPayment>?) {
            val formattedMonth = getFormattedMonth(month)
            monthTextView.text = formattedMonth

            val totalPayment = payments?.sumByDouble { it.paymentAmount } ?: 0.0
            totalPaymentTextView.text = NumberFormat.getCurrencyInstance().format(totalPayment)

            showdetailsButton.setOnClickListener {
                expandedPosition = if (expandedPosition == adapterPosition) {
                    RecyclerView.NO_POSITION
                } else {
                    adapterPosition
                }
                notifyDataSetChanged()
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
            // Set onClickListener for the "Show Less" button
            showLessButton.setOnClickListener {
                // Collapse the expanded view
                expandedPosition = RecyclerView.NO_POSITION
                notifyDataSetChanged()
            }
        }

        fun bind(month: String, payments: ArrayList<MonthlyPayment>?) {
            val formattedMonth = getFormattedMonth(month)
            monthTextView.text = formattedMonth

            // Clear the existing content in the imageButtonContainer
            imageButtonContainer.removeAllViews()

            if (payments != null) {
                populateTable(
                    imageButtonContainer,
                    payments,
                    month
                ) // Pass the month parameter here
            }

            // Set the initial visibility of the expanded view
            imageButtonContainer.visibility = if (adapterPosition == expandedPosition) {
                View.VISIBLE
            } else {
                View.GONE
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

        // Continue with the rest of the table as before
        val employeePaymentMap = paymentList.groupBy { it.employeeName }

        employeePaymentMap.forEach { (employeeName, payments) ->
            // Create a TableRow to hold the row content
            val tableRow = TableRow(context)
            tableRow.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )

            // Add employee name to the second column
            val nameTextView = TextView(context)
            nameTextView.text = employeeName
            nameTextView.setPadding(5, 5, 5, 5)
            nameTextView.setTypeface(null, Typeface.BOLD)
            nameTextView.textSize = 16f
            tableRow.addView(nameTextView)

            // Calculate the total payment amount for the employee
            val totalPaymentAmount = payments.sumByDouble { it.paymentAmount }

            // Add total payment amount to the third column
            val paymentAmountTextView = TextView(context)
            paymentAmountTextView.text = NumberFormat.getInstance().format(totalPaymentAmount)
            paymentAmountTextView.setPadding(5, 5, 5, 5)
            paymentAmountTextView.setTypeface(null, Typeface.BOLD)
            paymentAmountTextView.textSize = 16f
            tableRow.addView(paymentAmountTextView)

            // Add image button to the fourth column
            val imageButton = ImageButton(context)
            imageButton.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
            imageButton.setBackgroundColor(Color.TRANSPARENT)
            tableRow.addView(imageButton)

            // Add the payment details (hidden initially) to a new row below the current row
            val detailsRow = TableRow(context)
            detailsRow.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            detailsRow.visibility = View.GONE

            val detailsTextView = TextView(context)
            val paymentDetailsString = buildPaymentDetailsString(employeeName, month)
            Log.d("PaymentDetails", "Payment details string: $paymentDetailsString")
            detailsTextView.text = paymentDetailsString
            detailsRow.addView(detailsTextView)


            // Set onClickListener for the image button
            // Set onClickListener for the image button
            imageButton.setOnClickListener {
                if (detailsRow.visibility == View.VISIBLE) {
                    // If detailsRow is visible, change image to arrow down and hide detailsRow
                    imageButton.setImageResource(R.drawable.baseline_keyboard_arrow_down_24)
                    detailsRow.visibility = View.GONE
                } else {
                    // If detailsRow is not visible, change image to arrow up and show detailsRow
                    imageButton.setImageResource(R.drawable.baseline_keyboard_arrow_up_24)
                    detailsRow.visibility = View.VISIBLE
                }
            }


            // Add the tableRow to the tableLayout
            tableLayout.addView(tableRow)
            tableLayout.addView(detailsRow)
        }
    }

    private fun buildPaymentDetailsString(employeeName: String, month: String): String {
        val paymentDetails = StringBuilder()

        // Parse the month string to extract the month and year values
        val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(month)
        val calendar = Calendar.getInstance()
        calendar.time = monthYear
        val monthValue = calendar.get(Calendar.MONTH) + 1 // Months are 0-based
        val yearValue = calendar.get(Calendar.YEAR)

        Log.d("PaymentDetails", "Fetching payment details for $employeeName, $monthValue/$yearValue")

        // Fetch the payment details for the employee and month from the database
        val payments = dbHelper.getPaymentDetailsForEmployeeAndMonth(employeeName, monthValue, yearValue)

        Log.d("PaymentDetails", "Number of payment records: ${payments.size}")

        // Build the payment details string
        payments.forEach { payment ->
            val dateFormat = SimpleDateFormat("EEE, d MMMM, yyyy", Locale.ENGLISH)
            val formattedDate = dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(payment.date))
            paymentDetails.append("$formattedDate: Ksh ${NumberFormat.getInstance().format(payment.paymentAmount)}\n")
        }

        val paymentDetailsString = paymentDetails.toString()
        Log.d("PaymentDetails", "Payment details: $paymentDetailsString")

        return paymentDetailsString
    }

}