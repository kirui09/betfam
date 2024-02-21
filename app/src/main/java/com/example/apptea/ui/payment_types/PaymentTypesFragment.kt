package com.example.apptea.ui.payment_types

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.apptea.R
import com.google.android.material.textview.MaterialTextView

class PaymentTypesFragment : Fragment() {

    private lateinit var viewModel: PaymentTypesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_types, container, false)


        // Find the MaterialTextViews
        val textSupervisorPayment = view.findViewById<MaterialTextView>(R.id.textSupervisorPayment)
        val textBasicPayment = view.findViewById<MaterialTextView>(R.id.textBasicPayment)

        // Set default payments
        textSupervisorPayment.text = "9 Kenya Shillings" // Default payment for Supervisor
        textBasicPayment.text = "8" // Default basic pay

        // Find the "Edit" buttons
        val buttonEditSupervisorPayment = view.findViewById<Button>(R.id.buttonEditSupervisorPayment)
        val buttonEditBasicPayment = view.findViewById<Button>(R.id.buttonEditBasicPayment)

        // Set OnClickListener to open the edit dialog fragment for Supervisor Payment
        buttonEditSupervisorPayment.setOnClickListener {
            val editDialog = EditSupervisorPaymentDialogFragment()
            editDialog.show(requireFragmentManager(), "EditSupervisorPaymentDialog")
        }

        // Set OnClickListener to open the edit dialog fragment for Basic Payment
        buttonEditBasicPayment.setOnClickListener {
            val editDialog = EditBasicPaymentDialogFragment()
            editDialog.show(requireFragmentManager(), "EditBasicPaymentDialog")
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PaymentTypesViewModel::class.java)
    }
}
