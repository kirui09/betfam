// CompanyAdapter.kt
package com.example.apptea.ui.companies

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.R

interface CompanyClickHandler {
    fun onEditClick(company: Company)
}

class CompanyAdapter(private val companies: List<Company>, private val clickHandler: CompanyClickHandler) :
    RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder>() {

    class CompanyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.companynameTextView)
        val locationTextView: TextView = itemView.findViewById(R.id.companylocationTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_company, parent, false)
        return CompanyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        val company = companies[position]
        holder.nameTextView.text = company.name
        holder.locationTextView.text = company.location

        // Set click listener for the edit button
        holder.itemView.findViewById<View>(R.id.update_company_button).setOnClickListener {
            clickHandler.onEditClick(company)
        }
    }

    override fun getItemCount(): Int {
        return companies.size
    }
}
