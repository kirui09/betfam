package com.example.apptea.ui.payment_types

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.apptea.R

interface EditSupervisorPaymentListener {
    fun onSupervisorPaymentEdited(newValue: String)
}

class EditSupervisorPaymentDialogFragment : DialogFragment() {

    private var listener: EditSupervisorPaymentListener? = null

    fun setEditSupervisorPaymentListener(listener: EditSupervisorPaymentListener) {
        this.listener = listener
    }

    companion object {
        const val ARG_DEFAULT_PAYMENT = "default_payment"

        fun newInstance(defaultPayment: String): EditSupervisorPaymentDialogFragment {
            return EditSupervisorPaymentDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DEFAULT_PAYMENT, defaultPayment)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_supervisor_payment_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the EditText and Button
        val editTextSupervisorPayment = view.findViewById<EditText>(R.id.editTextSupervisorPayment)
        val buttonSave = view.findViewById<Button>(R.id.buttonSaveSupervisorPayment)


        // Get the default payment value from arguments
        val defaultPayment = arguments?.getString(ARG_DEFAULT_PAYMENT) ?: ""

        // Set the default value to the EditText
        editTextSupervisorPayment.setText(defaultPayment)

        // Set OnClickListener for the Save button
        buttonSave.setOnClickListener {
            // Get the edited value from the EditText
            val newValue = editTextSupervisorPayment.text.toString()

            // Call the listener method to notify the parent fragment
            listener?.onSupervisorPaymentEdited(newValue)

            // Dismiss the dialog fragment
            dismiss()
        }
    }
}
