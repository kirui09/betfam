package com.example.apptea.ui.records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.DBHelper
import com.example.apptea.R
import com.example.apptea.SharedPreferencesHelper
import java.util.*
import kotlin.collections.ArrayList

class MonthlyPaymentFragment : Fragment() {

    private lateinit var dbHelper: DBHelper
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var paymentAdapter: MonthlyPaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_monthly_payment, container, false)
        recyclerView = view.findViewById(R.id.monthlyPaymentRecyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHelper(requireContext())
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        val monthlyPayments = getMonthlyPayments()
        paymentAdapter = MonthlyPaymentAdapter(requireContext(), monthlyPayments)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = paymentAdapter
        }
    }

    private fun getMonthlyPayments(): ArrayList<Payment> {
        // Dummy data, replace this with your actual data retrieval logic
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Month starts from 0
        return dbHelper.getAllPayments(currentYear, currentMonth)
    }
}