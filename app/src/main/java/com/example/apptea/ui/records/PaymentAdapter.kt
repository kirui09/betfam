package com.example.apptea.ui.records

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.DBHelper
import com.example.apptea.R
import java.text.SimpleDateFormat
import java.util.Locale

class PaymentAdapter(
    private val context: Context,
    private val groupedData: LinkedHashMap<String, ArrayList<Payment>>,
    private val dbHelper: DBHelper // Database helper to fetch employee type
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
        val payments = groupedData[day]

        if (holder is PaymentViewHolder) {
            holder.paymentDateTextView.text = day
            holder.paymentEmployeeNameTextView.text = ""
            holder.paymentKilosTextView.text = ""
            holder.paymentTextView.text = ""

            // Concatenate employee names, kilos, and payment for the day
            payments?.forEach { payment ->
                holder.paymentEmployeeNameTextView.append(payment.employeeName + "\n")
                holder.paymentKilosTextView.append(payment.kilos.toString() + "\n")

                val employeeType = dbHelper.getEmployeeType(payment.employeeName)

                // Use the calculatePay function to calculate payment based on employee type and kilos
                val paymentAmount = calculatePay(payment, employeeType)
                holder.paymentTextView.append(paymentAmount.toString() + "\n")
            }

            holder.seeLessButton.setOnClickListener {
                // Handle collapsing here
                expandedPosition = RecyclerView.NO_POSITION
                notifyDataSetChanged()
            }
        } else if (holder is GeneralPayViewHolder) {
            // Set data for general pay view holder
            holder.generalPayDateTextView.text = day // Set the date for the general pay
            holder.totalPayForDayTextView.text = "" // Set the total pay for the day
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
        return if (position == expandedPosition) VIEW_TYPE_GENERAL_PAY else VIEW_TYPE_ITEM_PAYMENT
    }

    fun updateData(newGroupedData: LinkedHashMap<String, ArrayList<Payment>>) {
        groupedData.clear()
        groupedData.putAll(newGroupedData)
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = dateFormat.parse(dateString)
        val formattedDateFormat = SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.ENGLISH)
        return formattedDateFormat.format(date)
    }

    private fun calculatePay(payment: Payment, employeeType: String): Double {
        val kilos = payment.kilos

        // Check if the payment type is either "Basic" or "Supervisor"
        if (employeeType.equals("Basic", ignoreCase = true) || employeeType.equals("Supervisor", ignoreCase = true)) {
            val payRate = dbHelper.getPaymentTypes()[employeeType] ?: return 0.0 // Use 0.0 if pay rate not found
            return kilos * payRate
        } else {
            // Handle other payment types here if needed
            return 0.0 // Return 0.0 for unknown payment types
        }
    }
}

