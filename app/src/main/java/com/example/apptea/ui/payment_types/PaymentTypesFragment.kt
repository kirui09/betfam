package com.betfam.apptea.ui.payment_types

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.betfam.apptea.DBHelper
import com.betfam.apptea.R
import com.google.android.material.textview.MaterialTextView

class PaymentTypesFragment : Fragment(), EditSupervisorPaymentListener, OnBasicPaymentUpdatedListener {

    private lateinit var textSupervisorPayment: MaterialTextView
    private lateinit var textBasicPayment: MaterialTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_types, container, false)

        // Initialize MaterialTextViews
        textSupervisorPayment = view.findViewById(R.id.textSupervisorPayment)
        textBasicPayment = view.findViewById(R.id.textBasicPayment)

        // Fetch payment types and amounts from the database
        val dbHelper = DBHelper(requireContext())
        val paymentTypes = dbHelper.getPaymentTypes()

        // Log payment types for debugging
        Log.d("PaymentTypesFragment", "Payment Types: $paymentTypes")

        // Update text views with retrieved payment values
        paymentTypes["Supervisor"]?.let {
            updatePaymentText(textSupervisorPayment, "$it ")
        }

        paymentTypes["Basic"]?.let {
            updatePaymentText(textBasicPayment, "$it ")
        }

        // Find the "Edit" buttons
        val buttonEditSupervisorPayment = view.findViewById<ImageButton>(R.id.buttonEditSupervisorPayment)
        val buttonEditBasicPayment = view.findViewById<ImageButton>(R.id.buttonEditBasicPayment)

        // Set OnClickListener to open the edit dialog fragment for Supervisor Payment
        buttonEditSupervisorPayment.setOnClickListener {
            val currentSupervisorPayment = textSupervisorPayment.text.toString().split(" ")[0]
            val editDialog = EditSupervisorPaymentDialogFragment.newInstance(currentSupervisorPayment)
            editDialog.setEditSupervisorPaymentListener(this)
            editDialog.show(childFragmentManager, "EditSupervisorPaymentDialog")
        }

        // Set OnClickListener to open the edit dialog fragment for Basic Payment
        buttonEditBasicPayment.setOnClickListener {
            val currentBasicPayment = textBasicPayment.text.toString().split(" ")[0]
            val editDialog = EditBasicPaymentDialogFragment.newInstance(currentBasicPayment)
            editDialog.setOnBasicPaymentUpdatedListener(this)
            editDialog.show(childFragmentManager, "EditBasicPaymentDialog")
        }

        return view
    }

    private fun updatePaymentText(textView: MaterialTextView, payment: String) {
        textView.text = payment
    }

    // Implementation of the listener function for Supervisor Payment edit
    override fun onSupervisorPaymentEdited(newValue: String) {
        // Update the payment text view
        updatePaymentText(textSupervisorPayment, "$newValue ")
        // Show a toast message
        Toast.makeText(requireContext(), "Supervisor Payment updated: $newValue", Toast.LENGTH_SHORT).show()
    }

    // Implementation of the listener function for Basic Payment edit
    override fun onBasicPaymentUpdated(basicPayment: String) {
        // Update the payment text view
        updatePaymentText(textBasicPayment, "$basicPayment ")
        // Show a toast message
        Toast.makeText(requireContext(), "Basic Payment updated: $basicPayment", Toast.LENGTH_SHORT).show()
    }
}
