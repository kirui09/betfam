package com.example.apptea.ui.records


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.R

interface EditButtonClickListener {
    fun onEditButtonClick(record: DailyTeaRecord)
}

interface DeleteButtonClickListener {
    fun onDeleteButtonClick(record: DailyTeaRecord)
}
class TeaRecordsAdapter : RecyclerView.Adapter<TeaRecordsAdapter.ViewHolder>() {

    private var recordsList: List<DailyTeaRecord> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expanded_day, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = recordsList[position]
        holder.bind(record)
    }

    override fun getItemCount(): Int {
        return recordsList.size
    }

    fun setRecords(records: List<DailyTeaRecord>) {
        recordsList = records
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.dateOfInputTextView)
        private val companiesTextView: TextView = itemView.findViewById(R.id.companiesPluckedToTextView)
        private val employeesTextView: TextView = itemView.findViewById(R.id.employeesAtWorkTextView)
        private val kilosTextView: TextView = itemView.findViewById(R.id.totalKilosTextView)

        fun bind(record: DailyTeaRecord) {
            dateTextView.text = record.date
            companiesTextView.text = record.companies
            employeesTextView.text = record.employees
            kilosTextView.text = record.kilos.toString()
        }
    }


}

