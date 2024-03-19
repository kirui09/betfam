
package com.example.apptea.ui.records


import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.DBHelper
import com.example.apptea.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MonthlyPaymentAdapter(
    private val dbHelper: DBHelper,
    private val context: Context,
    private val groupedData: LinkedHashMap<String, ArrayList<MonthlyPayment>>
) : RecyclerView.Adapter<MonthlyPaymentAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val monthlyDateTextView: TextView = itemView.findViewById(R.id.monthlypaymentdateTextView)
        val employeeNameTextView: TextView =
            itemView.findViewById(R.id.monthlypaymentemployeeNameTextView)
        val paymentAmountTextView: TextView = itemView.findViewById(R.id.monthlypaymentTextView)
        val imageButtonContainer: TableLayout = itemView.findViewById(R.id.myTableLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_monthly_payment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val month = groupedData.keys.elementAt(position)
        val payments = groupedData[month]

        payments?.let { paymentList ->
            val formattedMonth = getFormattedMonth(month)
            holder.monthlyDateTextView.text = formattedMonth

            // Populate the table
            populateTable(holder.imageButtonContainer, paymentList)
        }
    }

    override fun getItemCount(): Int {
        return groupedData.size
    }

    private fun getFormattedMonth(month: String): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val parsedMonth = sdf.parse(month)
        val calendar = Calendar.getInstance()
        calendar.time = parsedMonth ?: Date()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return monthFormat.format(calendar.time)
    }

    private fun populateTable(tableLayout: TableLayout, paymentList: ArrayList<MonthlyPayment>) {
        val employeePaymentMap = paymentList.groupBy { it.employeeName }

        employeePaymentMap.forEach { (employeeName, payments) ->
            // Create a TableRow to hold the row content
            val tableRow = TableRow(context)
            tableRow.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )

            // Add employee name to the first column
            val nameTextView = TextView(context)
            nameTextView.text = employeeName
            nameTextView.setPadding(5, 5, 5, 5)
            nameTextView.setTypeface(null, Typeface.BOLD)
            nameTextView.textSize = 16f
            tableRow.addView(nameTextView)

            // Calculate the total payment amount for the employee
            val totalPaymentAmount = payments.sumByDouble { it.paymentAmount }

            // Add total payment amount to the second column
            val paymentAmountTextView = TextView(context)
            paymentAmountTextView.text = NumberFormat.getInstance().format(totalPaymentAmount)
            paymentAmountTextView.setPadding(5, 5, 5, 5)
            paymentAmountTextView.setTypeface(null, Typeface.BOLD)
            paymentAmountTextView.textSize = 16f
            tableRow.addView(paymentAmountTextView)

            // Add image button to the third column
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
            detailsTextView.text = "Payment Details"
            detailsRow.addView(detailsTextView)

            // Set onClickListener for the image button
            imageButton.setOnClickListener {
                detailsRow.visibility = if (detailsRow.visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            // Add the tableRow to the tableLayout
            tableLayout.addView(tableRow)
            tableLayout.addView(detailsRow)
        }
    }



    private fun togglePaymentDetails(container: LinearLayout, payments: List<MonthlyPayment>) {
        val paymentDetailsLayout = container.getChildAt(container.childCount - 1) as LinearLayout
        if (paymentDetailsLayout.visibility == View.VISIBLE) {
            paymentDetailsLayout.visibility = View.GONE
        } else {
            paymentDetailsLayout.visibility = View.VISIBLE
            // Populate payment details here (e.g., for each day of the month)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            payments.forEach { payment ->
                val calendar = Calendar.getInstance()
                calendar.time = dateFormat.parse(payment.date) ?: Date()

                for (i in 1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    val dayTextView = TextView(container.context)
                    dayTextView.text = "Day $i: ${payment.paymentAmount}" // Replace ${payment.paymentAmount} with the actual payment amount for that day
                    dayTextView.setPadding(5, 5, 5, 5)
                    paymentDetailsLayout.addView(dayTextView)
                }
            }
        }
    }




}