package com.example.apptea.ui.records

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.example.apptea.R
import com.example.apptea.databinding.FragmentRecordsBinding
import com.example.apptea.databinding.FragmentTeaRecordsBinding
import com.example.apptea.ui.employees.EmployeeAdapter
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_records, container, false)
        viewPager = root.findViewById(R.id.viewPager)
        tabLayout = root.findViewById(R.id.tabLayout)

        val pagerAdapter = RecordsPagerAdapter(requireActivity())
        viewPager.adapter = pagerAdapter

        employeeAdapter = EmployeeAdapter(emptyList())  // Initialize employeeAdapter

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
}
