package com.example.apptea.ui.records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.DBHelper
import com.example.apptea.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.DiffUtil

class RecordsFragment : Fragment(), AddRecordDialogFragment.AddRecordDialogFragmentListener,
    TeaRecordsAdapter.OnTeaRecordItemClickListener {

    private lateinit var recordsAdapter: TeaRecordsAdapter
    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_records, container, false)

        dbHelper = DBHelper(requireContext())

        val recyclerView: RecyclerView = view.findViewById(R.id.dailytearecordsrecyclerView)
        recordsAdapter = TeaRecordsAdapter(TeaRecordDiffCallback())
        recordsAdapter.setOnTeaRecordItemClickListener(this)
        recyclerView.adapter = recordsAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val fabAddRecord: FloatingActionButton = view.findViewById(R.id.fabAddRecord)
        fabAddRecord.setOnClickListener {
            showAddRecordDialog()
        }

        // Load records when the fragment view is created
        updateRecordsList()

        return view
    }

    private fun showAddRecordDialog() {
        val addRecordDialog = AddRecordDialogFragment()
        addRecordDialog.recordSavedListener = this
        addRecordDialog.show(parentFragmentManager, "AddRecordDialogFragment")
    }

    private fun updateRecordsList() {
        // Update the RecyclerView with the latest records from the database
        val teaRecords = dbHelper.getAllTeaRecords()
        recordsAdapter.submitList(teaRecords)
    }

    override fun onRecordSaved() {
        Toast.makeText(requireContext(), "Record saved", Toast.LENGTH_SHORT).show()
        updateRecordsList()
    }

    override fun onAllRecordsSaved() {
        Toast.makeText(requireContext(), "All records saved", Toast.LENGTH_SHORT).show()
        updateRecordsList()
    }

    override fun onUpdateButtonClick() {
        // Handle the "Update" button click if needed
    }

    // Define the DiffUtil.ItemCallback for TeaRecordsAdapter
    private class TeaRecordDiffCallback : DiffUtil.ItemCallback<DailyTeaRecord>() {
        override fun areItemsTheSame(oldItem: DailyTeaRecord, newItem: DailyTeaRecord): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: DailyTeaRecord, newItem: DailyTeaRecord): Boolean {
            return oldItem == newItem
        }
    }
}
