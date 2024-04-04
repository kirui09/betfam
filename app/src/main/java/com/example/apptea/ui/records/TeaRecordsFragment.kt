package com.example.apptea.ui.records

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.apptea.DBHelper
import com.example.apptea.R
import com.example.apptea.databinding.FragmentTeaRecordsBinding
import com.example.apptea.databinding.ItemExpandedDayBinding
import com.example.apptea.ui.employees.EmployeeAdapter
interface AddButtonClickListener {
    fun onAddButtonClick()
    fun onAllRecordAdded(record: DailyTeaRecord)
}




class TeaRecordsFragment : Fragment(), EditButtonClickListener, AddButtonClickListener, DeleteButtonClickListener {

    private lateinit var recordsAdapter: TeaRecordsAdapter
    private lateinit var dbHelper: DBHelper
    private lateinit var binding: FragmentTeaRecordsBinding
    private lateinit var recordsViewModel: RecordsViewModel
    private lateinit var employeeAdapter: EmployeeAdapter
    private var supervisorPay: Double = 0.0
    private var basicPay: Double = 0.0






    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeaRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHelper(requireContext())
        recordsViewModel = ViewModelProvider(this).get(RecordsViewModel::class.java)
        employeeAdapter = EmployeeAdapter(emptyList())
        fetchPaymentTypes() // Fetch supervisor and basic pay from the database
        setupRecyclerView()
        // Set click listener for add button
        binding.fabAddRecord.setOnClickListener {
            onAddButtonClick()
        }
        observeRecords()
        fetchRecords()
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            // Call the function to refresh the records
            fetchRecords()
            // Hide the refreshing indicator after refresh is complete
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupRecyclerView() {
        val itemTeaRecordBinding = ItemExpandedDayBinding.inflate(layoutInflater)
        val tableLayout = itemTeaRecordBinding.myTableLayout
        recordsAdapter = TeaRecordsAdapter(emptyMap(), tableLayout, this, this, supervisorPay, basicPay,dbHelper) // Pass pay values to the adapter
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
        showProgressBar() // Show progress bar when fetching data
        recordsViewModel.fetchTeaRecords()


    }

    override fun onAddButtonClick() {
        val fragmentManager = requireActivity().supportFragmentManager
        val addDialogFragment = AddRecordDialogFragment()
        addDialogFragment.employeeAdapter = employeeAdapter
        addDialogFragment.show(fragmentManager, "AddRecordDialogFragment")
    }

    override fun onEditButtonClick(record: DailyTeaRecord) {
        val fragmentManager = requireActivity().supportFragmentManager
        val editDialogFragment = EditRecordDialogFragment.newInstance(record)
        editDialogFragment.show(fragmentManager, "EditRecordDialogFragment")
    }

    override fun onDeleteButtonClick(record: DailyTeaRecord) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.apply {
            setTitle("Delete Record")
            setMessage("Are you sure you want to delete the record for ${record.companies}?")
            setPositiveButton("Delete") { dialog, which ->
                // Delete the record from the database
                dbHelper.deleteRecord(record.id)
                // Update the UI after the record is deleted
                updateRecordsList()
            }
            setNegativeButton("Cancel") { dialog, which ->
                // Dismiss the dialog
                dialog.dismiss()
            }
        }
        // Create and show the AlertDialog
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    override fun onAllRecordAdded(record: DailyTeaRecord) {
        showProgressBar() // Show progress bar when adding record
        fetchRecords() // Refresh the records
        updateRecordsList()
        hideProgressBar() // Hide progress bar after record is added
    }

    private fun updateRecordsList() {
        val teaRecords = dbHelper.getAllTeaRecords()
        val recordsByDay = teaRecords.groupBy { it.date }
        recordsAdapter.updateRecords(recordsByDay)
    }

    // Fetch supervisor and basic pay from the database
    private fun fetchPaymentTypes() {
        supervisorPay = dbHelper.getSupervisorPay()
        basicPay = dbHelper.getBasicPay()


    }


    private fun showProgressBar() {
        binding.teaRecordsFragmentProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.teaRecordsFragmentProgressBar.visibility = View.GONE
    }



//    private fun isInternetAvailable(context: Context): Boolean {
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val activeNetwork = connectivityManager.activeNetworkInfo
//        return activeNetwork != null && activeNetwork.isConnected
//    }
//
//
//    fun syncUnsyncedRecords(context: Context) {
//        val dbHelper = DBHelper(context)
//        val unsyncedRecords = dbHelper.getUnsyncedRecords() // Implement this method in DBHelper
//        if (unsyncedRecords.isNotEmpty()) {
//            // Use the existing sendRecordsToGoogleSheet method to sync
//            sendRecordsToGoogleSheet(unsyncedRecords)
//            // After successful sync, mark these records as 'synced' in your local database
//        }
//    }



}

