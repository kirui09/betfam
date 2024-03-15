package com.example.apptea.ui.records

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
    class MonthlyPaymentAdapter(
        private val context: Context,
        private val groupedData: LinkedHashMap<String, ArrayList<MonthlyPayment>>
    ) : RecyclerView.Adapter<MonthlyPaymentAdapter.PaymentViewHolder>() {

        inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val monthlyDateTextView: TextView = itemView.findViewById(R.id.monthlypaymentdateTextView)
            val employeeNameTextView: TextView = itemView.findViewById(R.id.monthlypaymentemployeeNameTextView)
            val paymentAmountTextView: TextView = itemView.findViewById(R.id.monthlypaymentTextView)
            val seeMoreButton: ImageButton = itemView.findViewById(R.id.seemoremonthlypayments)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_monthly_payment, parent, false)
            return PaymentViewHolder(view)
        }

        override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
            val month = groupedData.keys.elementAt(position)
            val payments = groupedData[month]

            if (payments != null) {
                val formattedMonth = getFormattedMonth(month)
                holder.monthlyDateTextView.text = formattedMonth

                val employeeNames = StringBuilder()
                val paymentAmounts = StringBuilder()

                payments.forEach { payment ->
                    employeeNames.append(payment.employeeName).append("\n")
                    paymentAmounts.append("${NumberFormat.getInstance().format(payment.paymentAmount)}\n")

                }

                holder.employeeNameTextView.text = employeeNames.toString().trim()
                holder.paymentAmountTextView.text = paymentAmounts.toString().trim()
                // Set click listener for the image button
                holder.seeMoreButton.setOnClickListener {
                    // Handle image button click event
                    // You can implement your desired functionality here
                }

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
    }
