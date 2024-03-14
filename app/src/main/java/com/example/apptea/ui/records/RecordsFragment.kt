package com.example.apptea.ui.records

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.apptea.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class RecordsFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

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
}
