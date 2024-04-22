package com.example.apptea.ui.records

import android.os.Bundle
import android.util.Log
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

class PaymentFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var paymentAdapter: PaymentAdapter
    private lateinit var dbHelper: DBHelper
    private var supervisorPay: Double = 0.00
    private var basicPay: Double = 0.00
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper // Added sharedPreferencesHelper property


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)
        dbHelper = DBHelper(requireContext())
        recyclerView = view.findViewById(R.id.paymentRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch payment types
        val paymentTypes = dbHelper.getPaymentTypes()

        sharedPreferencesHelper = SharedPreferencesHelper(requireContext()) // Initialize sharedPreferencesHelper

        paymentAdapter = PaymentAdapter(requireContext(), LinkedHashMap(), dbHelper, sharedPreferencesHelper) // Initialize paymentAdapter

        recyclerView.adapter = paymentAdapter

        // Show the progress bar
        showProgressBar(view)
        fetchPaymentTypes()
        fetchData()

        return view
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun fetchData() {
        val paymentsLiveData = dbHelper.getAllPayments()
        paymentsLiveData.observe(viewLifecycleOwner) { payments ->
            val groupedPayments = LinkedHashMap<String, ArrayList<Payment>>()
            for (payment in payments) {
                val date = payment.date //
                if (groupedPayments.containsKey(date)) {
                    groupedPayments[date]?.add(payment)
                } else {
                    val newList = ArrayList<Payment>()
                    newList.add(payment)
                    groupedPayments[date] = newList
                }
            }
            paymentAdapter.updateData(groupedPayments)

            // Hide the progress bar after data is updated
            hideProgressBar(requireView())

        }
    }

    private fun fetchPaymentTypes() {
        supervisorPay = dbHelper.getSupervisorPay()
        basicPay = dbHelper.getBasicPay()
    }


    private fun showProgressBar(view: View) {
        val progressBar = view.findViewById<RelativeLayout>(R.id.paymentFragmentProgressBar)
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar(view: View) {
        val progressBar = view.findViewById<RelativeLayout>(R.id.paymentFragmentProgressBar)
        progressBar.visibility = View.GONE
    }

    fun PaymentFragment.refreshData() {
        // Show the progress bar
        showProgressBar(requireView())

        fetchData()
    }


}
