// CompanyAdapter.kt
package com.example.apptea.ui.companies

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.R
//import com.example.apptea.ui.employees.EditEmployeeDialogFragment

interface CompanyClickHandler {
    fun onEditClick(company: Company)
    fun onDeleteClick(company: Company)
}

class CompanyAdapter(private var companies: List<Company>, private val clickHandler: CompanyClickHandler) :
    RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder>() {

    inner class CompanyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.companynameTextView)
        val locationTextView: TextView = itemView.findViewById(R.id.companylocationTextView)
        val updateCompanyButton: ImageView = itemView.findViewById(R.id.update_company_button)
        val deleteCompanyButton: ImageView = itemView.findViewById(R.id.delete_company_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_company, parent, false)
        return CompanyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        val currentCompany = companies[position]

        holder.nameTextView.text = currentCompany.name
        holder.locationTextView.text = currentCompany.location

        // Set click listener for the edit button
        holder.updateCompanyButton.setOnClickListener {
            val fragmentManager =
                (holder.itemView.context as FragmentActivity).supportFragmentManager
            val updateCompanyFragment =
                UpdateCompanyDialogFragment.newInstance(currentCompany)
            updateCompanyFragment.show(fragmentManager, "UpdateCompanyDialogFragment")
        }

        // Set click listener for the delete button
        holder.deleteCompanyButton.setOnClickListener {
            clickHandler.onDeleteClick(currentCompany)
        }
    }

    override fun getItemCount(): Int {
        return companies.size
    }

    // Update the list of companies and refresh the adapter
    fun updateData(newList: List<Company>) {
        companies = newList
        notifyDataSetChanged()
    }
}
