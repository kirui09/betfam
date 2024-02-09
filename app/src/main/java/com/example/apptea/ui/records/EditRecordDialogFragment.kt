package com.example.apptea.ui.records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.apptea.databinding.FragmentEditRecordDialogBinding
import com.example.apptea.ui.records.DailyTeaRecord

class EditRecordDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentEditRecordDialogBinding
    private var record: DailyTeaRecord? = null

    companion object {
        fun newInstance(record: DailyTeaRecord): EditRecordDialogFragment {
            val fragment = EditRecordDialogFragment()
            val args = Bundle().apply {
                // Pass the record data as arguments
                putParcelable("record", record)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditRecordDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the clicked record from arguments
        record = arguments?.getParcelable("record")

        // Populate the UI with the data from the clicked record
        populateUI()

        // Set click listener for the save button
        binding.updatebuttonSaveAllRecords.setOnClickListener {
            // Validate fields and update record
            if (validateFields()) {
                updateRecord()
                dismiss() // Close the dialog after updating the record
            } else {
                Toast.makeText(context, "Validation failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to populate the UI fields with record data
    private fun populateUI() {
        record?.let { record ->
            binding.updaterecordEntryTime.setText(record.date)
            binding.updateautoCompleteCompanyname.setText(record.companies)
            binding.updateautoCompleteEmployeeName.setText(record.employees)
            binding.updateTextEmployeeKilos.setText(record.kilos.toString())
        }
    }

    // Function to validate input fields
    private fun validateFields(): Boolean {
        // Add your validation logic here
        // Return true if all fields are valid, otherwise false
        return true
    }

    // Function to update the record in the database
    private fun updateRecord() {

    }
}
