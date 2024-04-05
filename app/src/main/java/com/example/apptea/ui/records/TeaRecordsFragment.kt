package com.example.apptea.ui.records

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

        val totalTextView = binding.totalsTextView


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
        // Set click listener for search button
        binding.searchButton.setOnClickListener {
            showDatePickerDialog()
        }

    }

    private fun setupRecyclerView() {
        val itemTeaRecordBinding = ItemExpandedDayBinding.inflate(layoutInflater)
        val tableLayout = itemTeaRecordBinding.myTableLayout
        recordsAdapter = TeaRecordsAdapter(
            emptyMap(),
            tableLayout,
            this,
            this,
            supervisorPay,
            basicPay,
            dbHelper
        ) // Pass pay values to the adapter
        binding.dailytearecordsrecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter
        }
    }

    private fun observeRecords() {
        recordsViewModel.teaRecords.observe(viewLifecycleOwner, Observer { teaRecords ->
            val recordsByDay = teaRecords.groupBy { it.date }
            recordsAdapter.updateRecords(recordsByDay)
            recordsAdapter.notifyDataSetChanged()
            calculateTotalsForCurrentDay() // Calculate totals for today's date initially
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

    private fun calculateTotalsForCurrentDay() {
        val currentDate = Calendar.getInstance().time
        val records = recordsAdapter.getRecords().values.flatten()

        val recordsForCurrentDay = records.filter { record ->
            try {
                val recordDate =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(record.date)
                recordDate == currentDate
            } catch (e: ParseException) {

                Log.d("TeaRecordsFragment", "Records: $records")
                false // Handle parsing error if date format is incorrect
            }
        }

        var totalKilos = 0.0
        var totalEmployees = 0

        for (record in recordsForCurrentDay) {
            totalKilos += record.kilos
            // Assuming 'employees' is a comma-separated string, count the number of employees
            totalEmployees += record.employees.split(",").size
        }

        val totalsString = "Total Kilos: $totalKilos kg:Total Employees: $totalEmployees"
        binding.totalsTextView.text = totalsString
    }

    private fun calculateTotalsForSelectedDate(selectedDate: String) {
        val recordsForSelectedDate = recordsAdapter.getRecords()[selectedDate] ?: emptyList()

        if (recordsForSelectedDate.isEmpty()) {
            // Show the "No Records" text view
            binding.noRecordsTextView.visibility = View.VISIBLE
            binding.totalsTextView.visibility = View.GONE
        } else {
            // Hide the "No Records" text view and show the totals
            binding.noRecordsTextView.visibility = View.GONE
            binding.totalsTextView.visibility = View.VISIBLE

            var totalKilos = 0.0
            var totalEmployees = 0

            for (record in recordsForSelectedDate) {
                totalKilos += record.kilos
                totalEmployees += record.employees.split(",").size
            }

            val totalsString = "Total Kilos: $totalKilos kg:Total Employees: $totalEmployees"
            binding.totalsTextView.text = totalsString
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = String.format(
                    Locale.getDefault(),
                    "%d-%02d-%02d",
                    year,
                    monthOfYear + 1,
                    dayOfMonth
                )

                // Filter the records by the selected date and update UI
                recordsAdapter.filterRecordsByDate(selectedDate)
                recordsAdapter.notifyDataSetChanged()

                // Recalculate the totals for the selected date
                calculateTotalsForSelectedDate(selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
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

