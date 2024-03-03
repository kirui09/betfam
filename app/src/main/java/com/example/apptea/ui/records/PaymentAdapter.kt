package com.example.apptea.ui.records

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.R

class PaymentAdapter(private val payments: List<Payment>) :
    RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = payments[position]
        holder.bind(payment)
    }

    override fun getItemCount(): Int = payments.size

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(payment: Payment) {
            // Bind payment data to views in the item layout
            itemView.findViewById<TextView>(R.id.employeeNameTextView).text = payment.employeeName
            itemView.findViewById<TextView>(R.id.dateTextView).text = payment.date
            itemView.findViewById<TextView>(R.id.kilosTextView).text = payment.kilos.toString()
            // Bind more payment details if needed
        }
    }
}
