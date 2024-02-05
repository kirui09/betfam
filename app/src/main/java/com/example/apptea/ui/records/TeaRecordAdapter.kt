package com.example.apptea.ui.records

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.databinding.ItemDailyTeaRecordBinding

class TeaRecordsAdapter : ListAdapter<EditableTeaRecord, TeaRecordsAdapter.TeaRecordViewHolder>(TeaRecordDiffCallback()) {

    // Listener for item click events
    private var itemClickListener: OnTeaRecordItemClickListener? = null

    // Setter function for the listener
    fun setOnTeaRecordItemClickListener(listener: OnTeaRecordItemClickListener) {
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeaRecordViewHolder {
        val binding = ItemDailyTeaRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TeaRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeaRecordViewHolder, position: Int) {
        val teaRecord = getItem(position)
        holder.bind(teaRecord)
    }

    class TeaRecordViewHolder(val binding: ItemDailyTeaRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(teaRecord: EditableTeaRecord) {
            try {
                binding.dateofinput.text = teaRecord.date
                binding.employeeatfarm.text = teaRecord.employees.joinToString(", ")
                binding.companyPluckedTo.text = teaRecord.companies.joinToString(", ")
                binding.kilosForEmployee.text = teaRecord.kilos.toString()
                binding.pay.text = teaRecord.pay.toString()
            } catch (e: Exception) {
                // Log any exception that might occur during binding
                e.printStackTrace()
            }
        }
    }

    private class TeaRecordDiffCallback : DiffUtil.ItemCallback<EditableTeaRecord>() {
        override fun areItemsTheSame(oldItem: EditableTeaRecord, newItem: EditableTeaRecord): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: EditableTeaRecord, newItem: EditableTeaRecord): Boolean {
            return oldItem == newItem
        }
    }

    // Provide a function to set the list in the adapter
    // Provide a function to update the list in the adapter
    fun updateRecords(recordList: List<EditableTeaRecord>) {
        submitList(recordList)
    }

    // Interface for handling item click events
    interface OnTeaRecordItemClickListener {
        fun onUpdateButtonClick()
    }
}
