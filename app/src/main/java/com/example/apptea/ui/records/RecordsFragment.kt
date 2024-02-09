package com.example.apptea.ui.records


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apptea.DBHelper
import com.example.apptea.R
import com.example.apptea.databinding.FragmentRecordsBinding
import com.example.apptea.databinding.ItemExpandedDayBinding
import com.example.apptea.ui.records.DailyTeaRecord
import com.example.apptea.ui.records.EditButtonClickListener
import com.example.apptea.ui.records.EditRecordDialogFragment
import com.example.apptea.ui.records.TeaRecordsAdapter

interface AddButtonClickListener {
    fun onAddButtonClick()
    fun onAllRecordAdded(record: DailyTeaRecord)
}

class RecordsFragment : Fragment(), EditButtonClickListener, AddButtonClickListener {

    private lateinit var recordsAdapter: TeaRecordsAdapter
    private lateinit var dbHelper: DBHelper
    private lateinit var binding: FragmentRecordsBinding
    private lateinit var recordsViewModel: RecordsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DBHelper(requireContext())
        recordsViewModel = ViewModelProvider(this).get(RecordsViewModel::class.java)

        setupRecyclerView()

        // Set click listener for add button
        binding.fabAddRecord.setOnClickListener {
            onAddButtonClick()
        }

        observeRecords()
        fetchRecords()
    }

    private fun setupRecyclerView() {
        val itemTeaRecordBinding = ItemExpandedDayBinding.inflate(layoutInflater)
        val tableLayout = itemTeaRecordBinding.myTableLayout
        recordsAdapter = TeaRecordsAdapter(emptyMap(), tableLayout, this)
        binding.dailytearecordsrecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter
        }
    }

    private fun observeRecords() {
        recordsViewModel.teaRecords.observe(viewLifecycleOwner, Observer { teaRecords ->
            recordsAdapter.updateRecords(teaRecords.groupBy { it.date })
            recordsAdapter.notifyDataSetChanged()
        })
    }

    private fun fetchRecords() {
        recordsViewModel.fetchTeaRecords()
    }

    override fun onAddButtonClick() {
        val fragmentManager = requireActivity().supportFragmentManager
        val addDialogFragment = AddRecordDialogFragment()
        addDialogFragment.show(fragmentManager, "AddRecordDialogFragment")
    }

    override fun onEditButtonClick(record: DailyTeaRecord) {
        val fragmentManager = requireActivity().supportFragmentManager
        val editDialogFragment = EditRecordDialogFragment.newInstance(record)
        editDialogFragment.show(fragmentManager, "EditRecordDialogFragment")
    }

    override fun onAllRecordAdded(record: DailyTeaRecord) {
        // Save the record to the database
        updateRecordsList()
    }

    private fun updateRecordsList() {
        val teaRecords = dbHelper.getAllTeaRecords()
        val recordsByDay = teaRecords.groupBy { it.date }
        recordsAdapter.updateRecords(recordsByDay)
    }
}