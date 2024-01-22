package com.example.apptea.ui.employees

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.R

class EmployeeAdapter(private var employeeList: List<Employee>) :
    RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {

    inner class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val ageTextView: TextView = itemView.findViewById(R.id.ageTextView)
        val phoneNumberTextView: TextView = itemView.findViewById(R.id.phoneNumberTextView)
        val employeeIdTextView: TextView = itemView.findViewById(R.id.employeeIdTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_employee, parent, false)
        return EmployeeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val currentEmployee = employeeList[position]
        holder.nameTextView.text = currentEmployee.name
        holder.ageTextView.text = currentEmployee.age
        holder.phoneNumberTextView.text = currentEmployee.phoneNumber
        holder.employeeIdTextView.text = currentEmployee.employeeId
    }

    override fun getItemCount() = employeeList.size

    fun updateData(newList: List<Employee>) {
        employeeList = ArrayList(newList)
        notifyDataSetChanged()
    }
}
