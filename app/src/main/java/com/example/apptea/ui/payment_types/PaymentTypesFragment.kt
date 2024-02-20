package com.example.apptea.ui.payment_types

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.apptea.R

class PaymentTypesFragment : Fragment() {

    private lateinit var viewModel: PaymentTypesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_types, container, false)

        // Find the "Edit" button
        val buttonEditSupervisorPayment = view.findViewById<Button>(R.id.buttonEditSupervisorPayment)

        // Set OnClickListener to open the edit dialog fragment
        buttonEditSupervisorPayment.setOnClickListener {
            val editDialog = EditSupervisorPaymentDialogFragment()
            editDialog.show(requireFragmentManager(), "EditSupervisorPaymentDialog")
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PaymentTypesViewModel::class.java)
    }
}
