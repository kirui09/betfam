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

class RecordsFragment : Fragment(), AddRecordDialogFragment.AddRecordDialogFragmentListener {

    private lateinit var recordsAdapter: TeaRecordsAdapter
    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_records, container, false)

        dbHelper = DBHelper(requireContext())

        val recyclerView: RecyclerView = view.findViewById(R.id.dailyTeaRecordsrecyclerView)
        recordsAdapter = TeaRecordsAdapter()
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
        addRecordDialog.recordSavedListener = object : AddRecordDialogFragment.AddRecordDialogFragmentListener {
            override fun onRecordSaved() {
                // Handle any specific actions after a record is saved, if needed
                // For now, let's update the RecyclerView with the latest records
                updateRecordsList()
            }

            override fun onAllRecordsSaved() {
                // Handle any specific actions after all records are saved, if needed
                // For now, let's display a Toast message
                Toast.makeText(requireContext(), "All records saved", Toast.LENGTH_SHORT).show()

                // Update the RecyclerView with the latest records from the database
                updateRecordsList()
            }
        }
        addRecordDialog.show(parentFragmentManager, "AddRecordDialogFragment")
    }

    private fun updateRecordsList() {
        // Update the RecyclerView with the latest records from the database
        val teaRecords = dbHelper.getAllTeaRecords()
        recordsAdapter.submitList(teaRecords)
        recordsAdapter.notifyDataSetChanged()
    }

    override fun onRecordSaved() {
        // Handle any specific actions after a record is saved, if needed
        // For now, let's display a Toast message
        Toast.makeText(requireContext(), "Record saved", Toast.LENGTH_SHORT).show()

        // Update the RecyclerView with the latest records from the database
        updateRecordsList()
    }

    override fun onAllRecordsSaved() {
        // Handle any specific actions after all records are saved, if needed
        // For now, let's display a Toast message
        Toast.makeText(requireContext(), "All records saved", Toast.LENGTH_SHORT).show()

        // Update the RecyclerView with the latest records from the database
        updateRecordsList()
    }
}