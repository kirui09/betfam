package com.example.apptea.ui.records

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.R

class MonthlyPaymentAdapter(
    private val context: Context,
    private val monthlyPayments: ArrayList<Payment>
) : RecyclerView.Adapter<MonthlyPaymentAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val employeeNameTextView: TextView = itemView.findViewById(R.id.employeeNameTextView)
        val paymentAmountTextView: TextView = itemView.findViewById(R.id.paymentAmountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_monthly_payment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = monthlyPayments[position]
        holder.employeeNameTextView.text = payment.employeeName
        holder.paymentAmountTextView.text = "${payment.paymentAmount}"
    }

    override fun getItemCount(): Int {
        return monthlyPayments.size
    }
}
