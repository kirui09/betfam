// TeaRecordsAdapter.kt
package com.betfam.apptea.ui.records

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
import com.betfam.apptea.DBHelper
import com.betfam.apptea.R
import com.betfam.apptea.databinding.ItemExpandedDayBinding
import com.betfam.apptea.databinding.ItemGeneralDayBinding
import java.text.SimpleDateFormat
import java.util.Locale

interface EditButtonClickListener {
    fun onEditButtonClick(record: TeaPaymentRecord)
}

interface DeleteButtonClickListener {
    fun onDeleteButtonClick(record: TeaPaymentRecord)
}

class TeaRecordsAdapter(
    private var recordsByDay: Map<String, List<TeaPaymentRecord>>,
    private val tableLayout: TableLayout,
    private val editButtonClickListener: EditButtonClickListener,
    private val deleteButtonClickListener: DeleteButtonClickListener,
    private val supervisorPay: Double, // Pay rate for supervisors
    private val basicPay: Double, // Pay rate for basic employees
    private val dbHelper: DBHelper // Database helper to fetch employee type
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_COLLAPSED = 1
    private val VIEW_TYPE_EXPANDED = 2
    private var expandedPosition = RecyclerView.NO_POSITION

    private var records: Map<String, List<TeaPaymentRecord>> = recordsByDay

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_COLLAPSED) {
            val binding = ItemGeneralDayBinding.inflate(inflater, parent, false)
            CollapsedViewHolder(binding)
        } else {
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

    inner class CollapsedViewHolder(private val binding: ItemGeneralDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(day: String) {
            val formattedDate = formatDate(day)
            binding.generaldateTextView.text = formattedDate
            val recordsForDay = recordsByDay[day]
            val totalKilos = recordsForDay?.sumOf { it.kilos.toDouble() } ?: 0.0
            binding.totalKilofForDay.text = "Total Kilos: %.2f".format(totalKilos)
            binding.seeMoreButton.visibility =
                if (expandedPosition == absoluteAdapterPosition) View.GONE else View.VISIBLE

            // Set the click listener for the entire item view
            binding.root.setOnClickListener {
                // Update the expandedPosition when the CollapsedViewHolder is clicked
                expandedPosition = if (expandedPosition == absoluteAdapterPosition) {
                    RecyclerView.NO_POSITION
                } else {
                    absoluteAdapterPosition
                }
                notifyDataSetChanged()
            }

            binding.seeMoreButton.setOnClickListener {
                expandedPosition = absoluteAdapterPosition
                updateRecords(recordsByDay)
                notifyDataSetChanged()
            }
        }
    }

    inner class ExpandedViewHolder(private val binding: ItemExpandedDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(day: String) {
            binding.dateOfInputTextView.text = day
            val recordsForDay = recordsByDay[day]
            binding.seeLessButton.visibility =
                if (expandedPosition == absoluteAdapterPosition) View.VISIBLE else View.GONE
            binding.seeLessButton.setOnClickListener {
                expandedPosition = RecyclerView.NO_POSITION
                notifyDataSetChanged()
            }
            recordsForDay?.let {
                binding.myTableLayout.removeAllViews()
                populateTable(it)
            }
        }

        private fun populateTable(recordsForDay: List<TeaPaymentRecord>) {
            val tableLayout = binding.myTableLayout
            tableLayout.removeAllViews() // Clear existing views

            val employeeRecords = recordsForDay.groupBy { it.employees to it.company }

            employeeRecords.forEach { (key, records) ->
                val (employee, company) = key
                val row = TableRow(tableLayout.context)

                val employeesTextView = TextView(tableLayout.context)
                employeesTextView.text = "$employee - $company"
                employeesTextView.setTypeface(null, Typeface.BOLD)
                employeesTextView.setPadding(5, 5, 5, 5)
                employeesTextView.gravity = Gravity.CENTER_VERTICAL
                row.addView(employeesTextView)

                val totalKilos = records.sumOf { it.kilos.toDouble() }
                val kilosTextView = TextView(tableLayout.context)
                kilosTextView.text = String.format("%.2f Kgs", totalKilos)
                kilosTextView.setTypeface(null, Typeface.BOLD)
                kilosTextView.setPadding(5, 5, 5, 5)
                kilosTextView.gravity = Gravity.CENTER_VERTICAL
                row.addView(kilosTextView)

                val expandButton = ImageButton(tableLayout.context)
              //  expandButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                expandButton.setBackgroundColor(Color.TRANSPARENT)
                val expandParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
                row.addView(expandButton, expandParams)

                tableLayout.addView(row)

                // Create a details table for the employee's records
                val detailsTable = TableLayout(tableLayout.context)
                detailsTable.layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                detailsTable.setBackgroundColor(Color.parseColor("#B0BEC5")) // Light Blue
                detailsTable.visibility = View.GONE

                records.forEach { record ->
                    val detailRow = TableRow(tableLayout.context)

                    val companyTextView = TextView(tableLayout.context)
                    companyTextView.text = record.company
                    companyTextView.setPadding(15, 5, 5, 5)
                    detailRow.addView(companyTextView)

                    val recordKilosTextView = TextView(tableLayout.context)
                    recordKilosTextView.text = "${record.kilos} Kgs"
                    recordKilosTextView.setPadding(5, 5, 5, 5)
                    detailRow.addView(recordKilosTextView)

                    // Add edit and delete buttons
                    val editButton = ImageButton(tableLayout.context)
                    editButton.setImageResource(R.drawable.ic_baseline_edit_24)
                    editButton.setBackgroundColor(Color.TRANSPARENT)
                    editButton.setOnClickListener {
                        editButtonClickListener.onEditButtonClick(record)
                    }
                    detailRow.addView(editButton)

                    val deleteButton = ImageButton(tableLayout.context)
                    deleteButton.setImageResource(R.drawable.ic_baseline_delete_24)
                    deleteButton.setBackgroundColor(Color.TRANSPARENT)
                    deleteButton.setOnClickListener {
                        deleteButtonClickListener.onDeleteButtonClick(record)
                    }
                    detailRow.addView(deleteButton)

                    detailsTable.addView(detailRow)

                    // Add a separator line
                    val separator = View(tableLayout.context)
                    separator.setBackgroundColor(Color.GRAY)
                    val separatorParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        1 // Height of the separator line
                    )
                    detailsTable.addView(separator, separatorParams)
                }

                tableLayout.addView(detailsTable)

                employeesTextView.setOnClickListener {
                    if (detailsTable.visibility == View.VISIBLE) {
                     //   expandButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                        detailsTable.visibility = View.GONE
                    } else {
                      //  expandButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                        detailsTable.visibility = View.VISIBLE
                    }
                }
                kilosTextView.setOnClickListener {
                    if (detailsTable.visibility == View.VISIBLE) {
                        //   expandButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                        detailsTable.visibility = View.GONE
                    } else {
                        //  expandButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                        detailsTable.visibility = View.VISIBLE
                    }
                }
            }

            // Add total row
            val totalKilos = recordsForDay.sumOf { it.kilos.toDouble() }

            val totalsRow = TableRow(tableLayout.context)
            val totalsLabelTextView = TextView(tableLayout.context)
            totalsLabelTextView.text = "Totals:"
            totalsLabelTextView.setTypeface(null, Typeface.BOLD)
            totalsLabelTextView.setPadding(5, 5, 5, 5)
            totalsRow.addView(totalsLabelTextView)

            val totalKilosTextView = TextView(tableLayout.context)
            totalKilosTextView.text = String.format("%.2f Kgs", totalKilos)
            totalKilosTextView.setTypeface(null, Typeface.BOLD)
            totalKilosTextView.setPadding(5, 5, 5, 5)
            totalsRow.addView(totalKilosTextView)

            tableLayout.addView(totalsRow)
        }
    }

    fun updateRecords(newRecordsByDay: Map<String, List<TeaPaymentRecord>>) {
        recordsByDay = newRecordsByDay
        records = recordsByDay
        notifyDataSetChanged()
    }

    fun collapseExpandedItem() {
        if (expandedPosition != RecyclerView.NO_POSITION) {
            expandedPosition = RecyclerView.NO_POSITION
            notifyDataSetChanged()
        }
    }

    fun getRecords(): Map<String, List<TeaPaymentRecord>> {
        return records
    }

    private fun formatDate(dateString: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = dateFormat.parse(dateString)
        val formattedDateFormat = SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.ENGLISH)
        return formattedDateFormat.format(date)
    }


    fun filterRecordsByDate(date: String) {
        val filteredRecords = recordsByDay.filter { (dateKey, recordsList) ->
            // Compare the dateKey with the provided date
            dateKey == date
        }
        updateRecords(filteredRecords)
        notifyDataSetChanged()
    }

}