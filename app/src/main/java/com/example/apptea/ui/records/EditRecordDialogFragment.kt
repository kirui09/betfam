package com.example.apptea.ui.records

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.apptea.DBHelper
import com.example.apptea.databinding.FragmentEditRecordDialogBinding
import com.example.apptea.ui.records.DailyTeaRecord

interface RecordUpdateListener {
    fun onRecordUpdated()
}

class EditRecordDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentEditRecordDialogBinding
    private var record: DailyTeaRecord? = null

    private var recordUpdateListener: RecordUpdateListener? = null

    fun setRecordUpdateListener(listener: RecordUpdateListener) {
        recordUpdateListener = listener
    }
    // Call this method after successfully updating the record
    private fun notifyRecordUpdated() {
        recordUpdateListener?.onRecordUpdated()
    }

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
        binding.updateRecordButton.setOnClickListener {
            // Validate fields and update record
            if (validateFields()) {
                updateRecord()
                notifyRecordUpdated() // Notify the parent fragment about the record update
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
    // Function to update the record in the database
    // Function to update the record in the database
    // Function to update the record in the database
    private fun updateRecord() {
        record?.let { existingRecord ->
            try {
                val dbHelper = DBHelper(requireContext())

                // Create a DailyTeaRecord object with the updated values including the ID
                val updatedRecord = DailyTeaRecord(
                    id = existingRecord.id, // Set the ID of the existing record
                    date = binding.updaterecordEntryTime.text.toString(),
                    companies = binding.updateautoCompleteCompanyname.text.toString(),
                    employees = binding.updateautoCompleteEmployeeName.text.toString(),
                    kilos = binding.updateTextEmployeeKilos.text.toString().toDouble()
                )

                // Log the update operation details
                Log.d("EditRecordDialog", "Updating record:")
                Log.d("EditRecordDialog", "ID: ${updatedRecord.id}")
                Log.d("EditRecordDialog", "Date: ${updatedRecord.date}")
                Log.d("EditRecordDialog", "Companies: ${updatedRecord.companies}")
                Log.d("EditRecordDialog", "Employees: ${updatedRecord.employees}")
                Log.d("EditRecordDialog", "Kilos: ${updatedRecord.kilos}")

                // Update the record in the database
                val isUpdated = dbHelper.updateTeaRecord(updatedRecord)

                if (isUpdated) {
                    Toast.makeText(context, "Record updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to update record", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle any exceptions here (e.g., log or notify)
                e.printStackTrace()
                Toast.makeText(context, "An error occurred while updating record", Toast.LENGTH_SHORT).show()
            }
        }
    }
}






