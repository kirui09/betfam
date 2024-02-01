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
    TeaRecordsAdapter.OnTeaRecordItemClickListener,
    EditRecordDialogFragment.OnRecordEditedListener {

    private lateinit var recordsAdapter: TeaRecordsAdapter
    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_records, container, false)

        dbHelper = DBHelper(requireContext())

        val recyclerView: RecyclerView = view.findViewById(R.id.dailyTeaRecordsrecyclerView)
        recordsAdapter = TeaRecordsAdapter(this)
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
        addRecordDialog.recordSavedListener =
            object : AddRecordDialogFragment.AddRecordDialogFragmentListener {
                override fun onRecordSaved() {
                    updateRecordsList()
                }

                override fun onAllRecordsSaved() {
                    Toast.makeText(requireContext(), "All records saved", Toast.LENGTH_SHORT)
                        .show()
                    updateRecordsList()
                }
            }
        addRecordDialog.show(parentFragmentManager, "AddRecordDialogFragment")
    }

    private fun updateRecordsList() {
        val teaRecords = dbHelper.getAllTeaRecords()
        recordsAdapter.updateRecords(teaRecords)
    }

    override fun onRecordSaved() {
        Toast.makeText(requireContext(), "Record saved", Toast.LENGTH_SHORT).show()
        updateRecordsList()
    }

    override fun onAllRecordsSaved() {
        Toast.makeText(requireContext(), "All records saved", Toast.LENGTH_SHORT).show()
        updateRecordsList()
    }

    override fun onRecordEdited(editedRecord: DailyTeaRecord) {
        // Handle the edited record, update the data, and refresh the UI
        // You can call dbHelper.updateTeaRecord(editedRecord) and then updateRecordsList()
        updateRecordsList()
    }

    override fun onUpdateButtonClick(teaRecord: DailyTeaRecord) {
        // Handle the "Update Record" button click
        openEditScreen(teaRecord)
    }

    private fun openEditScreen(teaRecord: DailyTeaRecord) {
        val editDialog = EditRecordDialogFragment.newInstance(teaRecord)
        editDialog.show(parentFragmentManager, "EditRecordDialogFragment")
    }
}
