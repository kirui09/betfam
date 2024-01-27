package com.example.apptea.ui.records

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.R

class TeaRecordAdapter(private var teaRecords: List<DailyTeaRecord>) : RecyclerView.Adapter<TeaRecordAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateOfInputTextView)
        val
        val kilosTextView: TextView = itemView.findViewById(R.id.totalKilosTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tea_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val teaRecord = teaRecords[position]

        // Bind data to views
        holder.dateTextView.text = teaRecord.date
        holder.kilosTextView.text = teaRecord.totalKilos.toString()
    }

    override fun getItemCount(): Int {
        return teaRecords.size
    }

    fun updateData(newList: List<DailyTeaRecord>) {
        teaRecords = ArrayList(newList)
        notifyDataSetChanged()
    }
}
