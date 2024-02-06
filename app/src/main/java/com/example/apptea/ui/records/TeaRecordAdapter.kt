package com.example.apptea.ui.records

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.databinding.ItemTeaRecordBinding

class TeaRecordsAdapter(diffCallback: DiffUtil.ItemCallback<DailyTeaRecord>) :
    ListAdapter<DailyTeaRecord, TeaRecordsAdapter.TeaRecordViewHolder>(diffCallback) {

    private var itemClickListener: OnTeaRecordItemClickListener? = null

    fun setOnTeaRecordItemClickListener(listener: OnTeaRecordItemClickListener) {
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeaRecordViewHolder {
        val binding = ItemTeaRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TeaRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeaRecordViewHolder, position: Int) {
        val teaRecord = getItem(position)
        holder.bind(teaRecord)

        // Set click listener for the "Update" button
//        holder.binding.updateRecordButton.setOnClickListener {
//            itemClickListener?.onUpdateButtonClick()
//        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun getItem(position: Int): DailyTeaRecord {
        return currentList[position]
    }

    class TeaRecordViewHolder(val binding: ItemTeaRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(teaRecord: DailyTeaRecord) {
            binding.dateOfInputTextView.text = teaRecord.date
            binding.employeesAtWorkTextView.text = teaRecord.employees.toString()
            binding.companiesPluckedToTextView.text = teaRecord.companies.toString()
            binding.totalKilosTextView.text = teaRecord.kilos.toString()
        }
    }

    interface OnTeaRecordItemClickListener {
        fun onUpdateButtonClick()
    }
}
