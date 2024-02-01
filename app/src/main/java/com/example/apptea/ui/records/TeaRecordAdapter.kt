package com.example.apptea.ui.records

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.databinding.ItemTeaRecordBinding

class TeaRecordsAdapter : ListAdapter<DailyTeaRecord, TeaRecordsAdapter.TeaRecordViewHolder>(TeaRecordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeaRecordViewHolder {
        val binding = ItemTeaRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TeaRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeaRecordViewHolder, position: Int) {
        val teaRecord = getItem(position)
        holder.bind(teaRecord)
    }

    class TeaRecordViewHolder(private val binding: ItemTeaRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(teaRecord: DailyTeaRecord) {
            binding.dateOfInputTextView.text = teaRecord.date
            binding.employeesAtWorkTextView.text = teaRecord.employees.toString()
            binding.companiesPluckedToTextView.text = teaRecord.companies.toString()
            binding.totalKilosTextView.text = teaRecord.totalKilos.toString()
        }
    }

    private class TeaRecordDiffCallback : DiffUtil.ItemCallback<DailyTeaRecord>() {
        override fun areItemsTheSame(oldItem: DailyTeaRecord, newItem: DailyTeaRecord): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: DailyTeaRecord, newItem: DailyTeaRecord): Boolean {
            return oldItem == newItem
        }
    }

    // Provide a function to set the list in the adapter
    // Provide a function to update the list in the adapter
    fun updateRecords(recordList: List<DailyTeaRecord>) {
        submitList(recordList)
    }
}