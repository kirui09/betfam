package com.example.apptea.ui.employees

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.R

class EmployeeAdapter(private var employeeList: List<Employee>) :
    RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {

    private val employeeNames: MutableList<String> = mutableListOf()

    init {
        // Initialize the employeeNames list
        employeeNames.addAll(employeeList.mapNotNull { it.name })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_employee, parent, false)
        return EmployeeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val currentEmployee = employeeList[position]

        holder.nameTextView.text = currentEmployee.name
        holder.empTypeTextView.text = currentEmployee.empType
        holder.ageTextView.text = currentEmployee.age
        holder.phoneNumberTextView.text = currentEmployee.phoneNumber
        holder.employeeIdTextView.text = currentEmployee.employeeId

        // Handle the Edit button click
        holder.editButton.setOnClickListener {
            val fragmentManager =
                (holder.itemView.context as FragmentActivity).supportFragmentManager
            val editEmployeeFragment =
                EditEmployeeDialogFragment.newInstance(currentEmployee)
            editEmployeeFragment.show(fragmentManager, "EditEmployeeDialogFragment")
        }

        // Handle the Delete button click
        holder.deleteButton.setOnClickListener {
            // Notify the listener about the delete button click
            onDeleteClickListener?.onDeleteClick(currentEmployee)
        }

    }

    interface OnDeleteClickListener {
        fun onDeleteClick(employee: Employee)
    }

    // Add a property to store the listener
    var onDeleteClickListener: OnDeleteClickListener? = null

    override fun getItemCount() = employeeList.size

    // Update LiveData in Place
    fun updateData(newList: List<Employee>) {
        // Clear the employeeNames list
        employeeNames.clear()
        // Update the employeeList and add new names to the employeeNames list
        employeeList = newList
        employeeNames.addAll(employeeList.mapNotNull { it.name })
        notifyDataSetChanged()
        // Log the employee names
        logEmployeeNames()
    }

    // Get the list of employee names
    fun getAllEmployeeNames(): List<String> {
        val names = mutableListOf<String>()
        for (employee in employeeList) {
            employee.name?.let { names.add(it) }
        }
        return names
    }

    // Method to log employee names
    private fun logEmployeeNames() {
        try {
            Log.d("EmployeeAdapter", "Employee Names:")
            employeeNames.forEachIndexed { index, name ->
                Log.d("EmployeeAdapter", "Employee ${index + 1}: $name")
            }
        } catch (e: Exception) {
            Log.e("EmployeeAdapter", "Error logging employee names: ${e.message}")
        }
    }


    inner class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val empTypeTextView: TextView = itemView.findViewById(R.id.empTypeTextView)
        val ageTextView: TextView = itemView.findViewById(R.id.ageTextView)
        val phoneNumberTextView: TextView = itemView.findViewById(R.id.phoneNumberTextView)
        val employeeIdTextView: TextView = itemView.findViewById(R.id.employeeIdTextView)
        val editButton: ImageView = itemView.findViewById(R.id.update_button)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_employee_button)
    }
}
