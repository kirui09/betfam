package com.betfam.apptea.ui.payment_types

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.betfam.apptea.DBHelper
import com.betfam.apptea.R

interface EditSupervisorPaymentListener {
    fun onSupervisorPaymentEdited(newValue: String)
}

class EditSupervisorPaymentDialogFragment : DialogFragment() {

    private var listener: EditSupervisorPaymentListener? = null

    fun setEditSupervisorPaymentListener(listener: EditSupervisorPaymentListener) {
        this.listener = listener
    }

    companion object {
        fun newInstance(supervisorPayment: String): EditSupervisorPaymentDialogFragment {
            val fragment = EditSupervisorPaymentDialogFragment()
            val args = Bundle()
            args.putString("supervisorPayment", supervisorPayment)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_edit_supervisor_payment_dialog,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the EditText and Button
        val editTextSupervisorPayment = view.findViewById<EditText>(R.id.editTextSupervisorPayment)
        val buttonSave = view.findViewById<Button>(R.id.buttonSaveSupervisorPayment)

        // Get the default payment value from arguments
        val supervisorPayment = arguments?.getString("supervisorPayment")

        // Check if supervisor payment is null before accessing its value
        if (!supervisorPayment.isNullOrEmpty()) {
            editTextSupervisorPayment.setText(supervisorPayment)
        }

        buttonSave.setOnClickListener {
            val updatedSupervisorPayment = editTextSupervisorPayment.text.toString()
            val updatedSupervisorPaymentInt = updatedSupervisorPayment.toIntOrNull()

            if (updatedSupervisorPaymentInt != null) {
                // Update record in the database
                val dbHelper = DBHelper(requireContext())
                val success = dbHelper.updateSupervisorPay(updatedSupervisorPaymentInt)

                if (success) {
                    // Notify the listener that the supervisor payment has been updated
                    listener?.onSupervisorPaymentEdited(updatedSupervisorPayment)

                    // Dismiss the dialog
                    dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to update supervisor payment in the database",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter a valid numeric supervisor payment",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
}



