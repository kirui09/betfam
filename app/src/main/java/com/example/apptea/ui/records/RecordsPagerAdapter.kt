package com.example.apptea.ui.records

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class RecordsPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TeaRecordsFragment() // Your Records Fragment
            1 -> PaymentFragment()// Your Payments Fragment
            else -> RecordsFragment() // Fallback to Records Fragment
        }
    }
}
