package com.example.apptea.ui.records

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
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
            holder.paymentDateTextView.text = formattedDate
            holder.paymentEmployeeNameTextView.text = ""
            holder.paymentKilosTextView.text = ""
            holder.paymentTextView.text = ""

            // Concatenate employee names, kilos, and payment for the day
            payments?.forEach { payment ->
                holder.paymentEmployeeNameTextView.append(payment.employeeName + "\n")
                holder.paymentKilosTextView.append("${NumberFormat.getInstance().format(payment.kilos)} Kilos\n")
                val employeeType = dbHelper.getEmployeeType(payment.employeeName)
                // Use the calculatePay function to calculate payment based on employee type and kilos
                val paymentAmount = calculatePay(payment, employeeType)
                holder.paymentTextView.append("Ksh ${NumberFormat.getInstance().format(paymentAmount)}\n")
            }

            holder.seeLessButton.setOnClickListener {
                // Handle collapsing here
                expandedPosition = RecyclerView.NO_POSITION
                notifyDataSetChanged()
            }
        } else if (holder is GeneralPayViewHolder) {
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
                val alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder.setTitle("Confirm")
                alertDialogBuilder.setMessage("Are you sure you want to save these payments?")
                alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                    // Iterate through payments and save each payment to TeaRecords table
                    payments?.forEach { payment ->
                        // Perform database operation to save payment to TeaRecords table
                        savePaymentToTeaRecords(payment)
                    }
                    // Update the shared preferences to reflect the checkbox state
                    sharedPreferencesHelper.saveCheckBoxState(true)
                    // Show a toast message to indicate that data is saved
                    Toast.makeText(context, "Payments saved to database", Toast.LENGTH_SHORT).show()
                }
                alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    holder.checkBox.isChecked = false
                }
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
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
        // Fetch the pay rate based on the employee type
        val payRate = dbHelper.getPaymentTypes()[employeeType] ?: return 0.0 // Use 0.0 if pay rate not found
        return kilos * payRate
    }

    private fun savePaymentToTeaRecords(payment: Payment) {
        // Assuming dbHelper is an instance of your DBHelper class
        dbHelper.insertPaymentToTeaRecords(payment)
    }
}
