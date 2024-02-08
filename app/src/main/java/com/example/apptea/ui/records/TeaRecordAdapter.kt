package com.example.apptea.ui.records


import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.R
import com.example.apptea.databinding.ItemTeaRecordBinding
import com.example.apptea.ui.records.DailyTeaRecord

interface EditButtonClickListener {
    fun onEditButtonClick(record: DailyTeaRecord)
}

class TeaRecordsAdapter(
    private var recordsByDay: Map<String, List<DailyTeaRecord>>,
    private val tableLayout: TableLayout,
    private val editButtonClickListener: EditButtonClickListener
) : RecyclerView.Adapter<TeaRecordsAdapter.TeaRecordViewHolder>() {

    inner class TeaRecordViewHolder(private val binding: ItemTeaRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(day: String) {
            binding.dateOfInputTextView.text = day
            val recordsForDay = recordsByDay[day]
            populateTable(recordsForDay)
        }

        private fun populateTable(recordsForDay: List<DailyTeaRecord>?) {
            val tableLayout = binding.myTableLayout
            tableLayout.removeAllViews() // Clear previous rows

            // Group records by date
            val recordsByDate = recordsForDay?.groupBy { it.date }

            // Create a table for each unique date
            recordsByDate?.forEach { (date, records) ->
                // Create table header row with the date
                val headerRow = TableRow(tableLayout.context)
                val headerDateTextView = TextView(tableLayout.context)
                headerDateTextView.text = date // Set the date text
                headerDateTextView.setTypeface(null, Typeface.BOLD)
                headerDateTextView.setPadding(5, 5, 5, 5)
                headerRow.addView(headerDateTextView)
                tableLayout.addView(headerRow)

                // Create rows for each record
                records.forEach { record ->
                    val row = TableRow(tableLayout.context)
                    val params = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    row.layoutParams = params

                    val employeesTextView = TextView(tableLayout.context)
                    employeesTextView.text = record.employees
                    employeesTextView.setTypeface(null, Typeface.BOLD)
                    employeesTextView.setPadding(5, 5, 5, 5)
                    employeesTextView.gravity = Gravity.CENTER_VERTICAL // Align content vertically center
                    row.addView(employeesTextView)

                    val companyTextView = TextView(tableLayout.context)
                    companyTextView.text = record.companies
                    companyTextView.setTypeface(null, Typeface.BOLD)
                    companyTextView.setPadding(5, 5, 5, 5)
                    companyTextView.gravity = Gravity.CENTER_VERTICAL // Align content vertically center
                    row.addView(companyTextView)

                    val kilosTextView = TextView(tableLayout.context)
                    kilosTextView.text = record.kilos.toString()
                    kilosTextView.setTypeface(null, Typeface.BOLD)
                    kilosTextView.text = "${record.kilos} Kgs" // Add "Kgs" after kilos
                    kilosTextView.setPadding(5, 5, 5, 5)
                    kilosTextView.gravity = Gravity.CENTER_VERTICAL // Align content vertically center
                    row.addView(kilosTextView)

                    val payTextView = TextView(tableLayout.context)
                    val payAmount = record.kilos * 8 // Calculate pay amount
                    payTextView.setTypeface(null, Typeface.BOLD)
                    payTextView.text = "KSh.${payAmount}" // Display pay amount
                    payTextView.setPadding(5, 5, 5, 5)
                    payTextView.gravity = Gravity.CENTER_VERTICAL // Align content vertically center
                    row.addView(payTextView)

                    // Add edit button
                    val editButton = ImageButton(tableLayout.context)
                    editButton.setImageResource(R.drawable.ic_baseline_edit_24) // Set your vector image resource
                    editButton.setBackgroundColor(Color.TRANSPARENT) // Set background color to transparent
                    editButton.setOnClickListener {
                        // Pass the clicked record to the EditButtonClickListener
                        editButtonClickListener.onEditButtonClick(record)
                    }
                    row.addView(editButton)

                    tableLayout.addView(row)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeaRecordViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTeaRecordBinding.inflate(inflater, parent, false)
        return TeaRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeaRecordViewHolder, position: Int) {
        val days = recordsByDay.keys.toList()
        val day = days[position]
        holder.bind(day)
    }

    override fun getItemCount(): Int {
        return recordsByDay.size
    }

    fun updateRecords(newRecordsByDay: Map<String, List<DailyTeaRecord>>) {
        recordsByDay = newRecordsByDay
        notifyDataSetChanged()
    }
}
