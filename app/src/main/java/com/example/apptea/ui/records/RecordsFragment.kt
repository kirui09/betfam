package com.betfam.apptea.ui.records

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.betfam.apptea.R
import com.betfam.apptea.ui.employees.EmployeeAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

interface AddRecordButtonClickListener {
    fun onAddButtonClick()
}

class RecordsFragment : Fragment(), AddRecordButtonClickListener {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var employeeAdapter: EmployeeAdapter
    private lateinit var fabAddButton: FloatingActionButton
    private lateinit var fabSyncButton: FloatingActionButton

    private lateinit var viewModel: RecordsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_records, container, false)
        viewPager = root.findViewById(R.id.viewPager)
        tabLayout = root.findViewById(R.id.tabLayout)

        fabSyncButton = root.findViewById(R.id.fabSyncRecord)  // Initialize fabSyncButton

        val pagerAdapter = RecordsPagerAdapter(requireActivity())
        viewPager.adapter = pagerAdapter

        employeeAdapter = EmployeeAdapter(emptyList())  // Initialize employeeAdapter


        val context = requireContext()
        val factory = RecordsViewModelFactory(context)
        viewModel = ViewModelProvider(this, factory).get(RecordsViewModel::class.java)




        fabSyncButton.setOnClickListener {
            syncDataWithGoogleSheet()
        }


        // Set click listener for add button
        val fabAddRecord: FloatingActionButton = root.findViewById(R.id.fabAddRecord)
        fabAddRecord.setOnClickListener {
            onAddButtonClick()
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> " Records"
                1 -> "Payments"
                2-> "Monthly/ Yearly Payments"
                else -> ""
            }
        }.attach()

        return root
    }

    override fun onAddButtonClick() {
        Log.d("RecordsFragment", "Add button clicked")
        val addDialogFragment = AddRecordDialogFragment()
        addDialogFragment.show(childFragmentManager, "AddRecordDialogFragment")
    }

    private fun syncDataWithGoogleSheet() {
        viewModel.syncAndCompareDataWithGoogleSheet()
    }

}
