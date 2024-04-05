package com.example.apptea.ui.records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.DBHelper
import com.example.apptea.R
import com.example.apptea.SharedPreferencesHelper

class MonthlyPaymentFragment : Fragment() {
    private lateinit var dbHelper: DBHelper
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var paymentAdapter: MonthlyPaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_monthly_payment, container, false)
        recyclerView = view.findViewById(R.id.monthlyPaymentRecyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHelper(requireContext())
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
        paymentAdapter = MonthlyPaymentAdapter(dbHelper, requireContext(), LinkedHashMap())
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = paymentAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        showProgressBar(requireView())
        fetchData()
    }

    private fun fetchData() {
        val monthlyPaymentsMap = getMonthlyPayments()
        paymentAdapter.updateData(monthlyPaymentsMap)
        hideProgressBar(requireView())
    }

    private fun getMonthlyPayments(): LinkedHashMap<String, ArrayList<MonthlyPayment>> {
        val monthlyPaymentsMap = LinkedHashMap<String, ArrayList<MonthlyPayment>>()
        val allMonthlyPayments = dbHelper.getSumOfKilosForEachEmployee()
        for (payment in allMonthlyPayments) {
            val month = payment.date.substring(0, 7) // Extract yyyy-MM from the date
            if (!monthlyPaymentsMap.containsKey(month)) {
                monthlyPaymentsMap[month] = ArrayList()
            }
            monthlyPaymentsMap[month]?.add(payment)
        }
        return monthlyPaymentsMap
    }

    private fun showProgressBar(view: View) {
        val progressBar = view.findViewById<RelativeLayout>(R.id.monthlypaymentFragmentProgressBar)
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar(view: View) {
        val progressBar = view.findViewById<RelativeLayout>(R.id.monthlypaymentFragmentProgressBar)
        progressBar.visibility = View.GONE
    }



}