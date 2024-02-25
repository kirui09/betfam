package com.example.apptea.ui.payment_types

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.apptea.DBHelper
import com.example.apptea.R

interface OnBasicPaymentUpdatedListener {
    fun onBasicPaymentUpdated(basicPayment: String)
}

class EditBasicPaymentDialogFragment : DialogFragment() {

    private var onBasicPaymentUpdatedListener: OnBasicPaymentUpdatedListener? = null

    fun setOnBasicPaymentUpdatedListener(listener: OnBasicPaymentUpdatedListener) {
        this.onBasicPaymentUpdatedListener = listener
    }

    companion object {
        fun newInstance(basicPayment: String): EditBasicPaymentDialogFragment {
            val fragment = EditBasicPaymentDialogFragment()
            val args = Bundle()
            args.putString("basicPayment", basicPayment)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_basic_payment_dialog, container, false)

        val editTextBasicPayment = view.findViewById<EditText>(R.id.editTextBasicPayment)
        val buttonSaveBasicPayment = view.findViewById<Button>(R.id.buttonSaveBasicPayment)

        val basicPayment = arguments?.getString("basicPayment")

        // Check if basic payment is null before accessing its value
        if (!basicPayment.isNullOrEmpty()) {
            editTextBasicPayment.setText(basicPayment)
        }

        buttonSaveBasicPayment.setOnClickListener {

            val updatedBasicPayment = editTextBasicPayment.text.toString()
            val updatedBasicPaymentInt = updatedBasicPayment.toIntOrNull()

            if (updatedBasicPaymentInt != null) {
                // Create a BasicPayment instance with the updated value
                val basicPayment = BasicPayment(updatedBasicPaymentInt)

                // Update record in the database
                val dbHelper = DBHelper(requireContext())
                val success = dbHelper.updateBasicPay(basicPayment)

                if (success) {
                    // Notify the listener that the basic payment has been updated
                    onBasicPaymentUpdatedListener?.onBasicPaymentUpdated(updatedBasicPayment)

                    // Dismiss the dialog
                    dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to update basic payment in the database",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter a valid numeric basic payment",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        return view
    }
}
