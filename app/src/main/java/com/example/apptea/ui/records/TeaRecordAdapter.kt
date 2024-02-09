package com.example.apptea.ui.records

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.view.View

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.R
import com.example.apptea.databinding.ItemExpandedDayBinding
import com.example.apptea.databinding.ItemGeneralDayBinding
import com.example.apptea.ui.records.DailyTeaRecord

interface EditButtonClickListener {
    fun onEditButtonClick(record: DailyTeaRecord)
}

class TeaRecordsAdapter(
    private var recordsByDay: Map<String, List<DailyTeaRecord>>,
    private val tableLayout: TableLayout,
    private val editButtonClickListener: EditButtonClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_COLLAPSED = 1
    private val VIEW_TYPE_EXPANDED = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_COLLAPSED) {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemGeneralDayBinding.inflate(inflater, parent, false)
            CollapsedViewHolder(binding)
        } else {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemExpandedDayBinding.inflate(inflater, parent, false)
            ExpandedViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val day = recordsByDay.keys.toList()[position]
        if (holder is CollapsedViewHolder) {
            holder.bind(day)
        } else if (holder is ExpandedViewHolder) {
            holder.bind(day)
        }
    }

    override fun getItemCount(): Int {
        return recordsByDay.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == expandedPosition) VIEW_TYPE_EXPANDED else VIEW_TYPE_COLLAPSED
    }

    private var expandedPosition = RecyclerView.NO_POSITION

    inner class CollapsedViewHolder(private val binding: ItemGeneralDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(day: String) {
            binding.generaldateTextView.text = day
            val recordsForDay = recordsByDay[day]
            val totalKilos = recordsForDay?.sumBy { it.kilos.toInt() } ?: 0
            binding.totalKilofForDay.text = "Total Kilos: $totalKilos"

            binding.seeMoreButton.visibility = if (expandedPosition == absoluteAdapterPosition) View.GONE else View.VISIBLE
            binding.seeLessButton.visibility = if (expandedPosition == absoluteAdapterPosition) View.VISIBLE else View.GONE




            binding.seeMoreButton.setOnClickListener {
                expandedPosition = adapterPosition
                notifyDataSetChanged()
            }

            binding.seeLessButton.setOnClickListener {
                expandedPosition = RecyclerView.NO_POSITION
                notifyDataSetChanged()
            }
        }
    }

    inner class ExpandedViewHolder(private val binding: ItemExpandedDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(day: String) {
            binding.dateOfInputTextView.text = day
            val recordsForDay = recordsByDay[day]
            recordsForDay?.let {
                binding.myTableLayout.removeAllViews() // Clear previous rows
                // Populate table with records
                populateTable(it)
            }
        }

        private fun populateTable(recordsForDay: List<DailyTeaRecord>) {
            val tableLayout = binding.myTableLayout

            // Group records by date
            val recordsByDate = recordsForDay.groupBy { it.date }

            // Create rows for each record
            recordsByDate.forEach { (date, records) ->
                // Create table header row with the date
                val headerRow = TableRow(tableLayout.context)
                val headerDateTextView = TextView(tableLayout.context)
                headerDateTextView.text = date // Set the date text
                headerDateTextView.setTypeface(null, Typeface.BOLD)
                headerDateTextView.setPadding(5, 5, 5, 5)
                headerRow.addView(headerDateTextView)
                tableLayout.addView(headerRow)

                records.forEach { record ->
                    val row = TableRow(tableLayout.context)

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
                    kilosTextView.text = "${record.kilos} Kgs"
                    kilosTextView.setTypeface(null, Typeface.BOLD)
                    kilosTextView.setPadding(5, 5, 5, 5)
                    kilosTextView.gravity = Gravity.CENTER_VERTICAL // Align content vertically center
                    row.addView(kilosTextView)

                    val payTextView = TextView(tableLayout.context)
                    val payAmount = record.kilos * 8 // Calculate pay amount
                    payTextView.text = "KSh.$payAmount" // Display pay amount
                    payTextView.setTypeface(null, Typeface.BOLD)
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

    fun updateRecords(newRecordsByDay: Map<String, List<DailyTeaRecord>>) {
        recordsByDay = newRecordsByDay
        notifyDataSetChanged()
    }

    fun collapseExpandedItem() {
        if (expandedPosition != RecyclerView.NO_POSITION) {
            expandedPosition = RecyclerView.NO_POSITION
            notifyDataSetChanged()
        }
    }
}
