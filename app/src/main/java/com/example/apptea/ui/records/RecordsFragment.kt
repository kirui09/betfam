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

class RecordsFragment : Fragment(),
    AddRecordDialogFragment.AddRecordDialogFragmentListener,
    TeaRecordsAdapter.OnTeaRecordItemClickListener {

    private lateinit var recordsAdapter: TeaRecordsAdapter
    private lateinit var dbHelper: DBHelper

    // Define selectedDate as a class-level variable
    private var selectedDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_records, container, false)

        dbHelper = DBHelper(requireContext())

        val recyclerView: RecyclerView = view.findViewById(R.id.dailytearecordsrecyclerView)
        recordsAdapter = TeaRecordsAdapter()
        recordsAdapter.setOnTeaRecordItemClickListener(this) // Set the listener
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
        // Check if selectedDate is not null

        selectedDate?.let { date ->
            // Update the RecyclerView with the latest records from the database for the selected date
            val teaRecords = dbHelper.getEditableTeaRecordsByDate(date)
            recordsAdapter.updateRecords(teaRecords)
        }

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

    // Implementation of the OnTeaRecordItemClickListener interface
    override fun onUpdateButtonClick() {
        val fragmentManager = requireActivity().supportFragmentManager
        val editDialogFragment = EditRecordDialogFragment()

        // Pass the selected date to EditRecordDialogFragment
        val bundle = Bundle()
        bundle.putString("selectedDate", selectedDate) // Replace with the actual variable containing the selected date
        editDialogFragment.arguments = bundle

        // Show the dialog fragment
        editDialogFragment.show(fragmentManager, "EditRecordDialogFragment")
    }
}
