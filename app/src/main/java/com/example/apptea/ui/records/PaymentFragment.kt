package com.example.apptea.ui.records

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.R
class PaymentFragment : Fragment() {

    private lateinit var paymentAdapter: PaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and adapter
        val paymentRecyclerView = view.findViewById<RecyclerView>(R.id.paymentRecyclerView)
//        paymentAdapter = PaymentAdapter(getPaymentData())
        paymentRecyclerView.adapter = paymentAdapter
        paymentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }



}
