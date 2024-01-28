package com.example.apptea.ui.records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.R
import com.example.apptea.ui.records.Record
import com.example.apptea.ui.records.RecordsViewModel
import com.example.apptea.databinding.FragmentRecordsBinding
import com.example.apptea.ui.records.AddRecordDialogFragment
import com.example.apptea.ui.records.DailyTeaRecord

import com.google.android.material.floatingactionbutton.FloatingActionButton

class RecordsFragment : Fragment(), AddRecordDialogFragment.AddRecordDialogListener {

    private var _binding: FragmentRecordsBinding? = null
    private lateinit var recordsViewModel: RecordsViewModel
    private lateinit var teaRecordsAdapter: TeaRecordsAdapter

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        recordsViewModel = ViewModelProvider(this).get(RecordsViewModel::class.java)
        recordsViewModel.initialize(requireContext()) // Initialize DBHelper in ViewModel

        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // RecyclerView setup
        val recyclerView: RecyclerView = root.findViewById(R.id.dailyTeaRecordsrecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize and set up the adapter
        teaRecordsAdapter = TeaRecordsAdapter()
        recyclerView.adapter = teaRecordsAdapter

        // Access the FloatingActionButton
        val fabAddRecord = root.findViewById<FloatingActionButton>(R.id.fabAddRecord)

        fabAddRecord.setOnClickListener {
            // Show the AddRecordDialogFragment when FAB is clicked
            val addRecordDialog = AddRecordDialogFragment()
            addRecordDialog.setAddRecordDialogListener(this)
            addRecordDialog.show(childFragmentManager, AddRecordDialogFragment::class.java.simpleName)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe tea records and update the adapter when the data changes

        recordsViewModel.getAllTeaRecordsLiveData().observe(viewLifecycleOwner, { teaRecords ->
            teaRecordsAdapter.submitList(teaRecords) })
    }

    override fun onSaveRecordClicked(date: String, employeename: String, company: String, kilos: String) {
        // Handle saving a single record here if needed
        // For example: recordsViewModel.saveRecord(date, employeename, company, kilos)
    }

    override fun onSaveAllRecordsClicked(recordsList: List<Record>) {
        // Handle saving all records here
        // For example: recordsViewModel.saveAllRecords(recordsList)
        recordsViewModel.getAllTeaRecords()
    }
}
