// UpdateCompanyDialogFragment.kt
package com.betfam.apptea.ui.companies

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

class UpdateCompanyDialogFragment : DialogFragment() {

    private var onCompanyUpdatedListener: OnCompanyUpdatedListener? = null

    fun setOnCompanyUpdatedListener(listener: OnCompanyUpdatedListener) {
        this.onCompanyUpdatedListener = listener
    }

    companion object {
        fun newInstance(company: Company): UpdateCompanyDialogFragment {
            val fragment = UpdateCompanyDialogFragment()
            val args = Bundle()
            args.putParcelable("company", company)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_update_company_dialog, container, false)

        val updateCompanyName = view.findViewById<EditText>(R.id.updateTextCompanyName)
        val updateCompanyLocation = view.findViewById<EditText>(R.id.updateTextCompanyLocation)
        val updateButtonCompany = view.findViewById<Button>(R.id.updateButtonCompany)

        val company = arguments?.getParcelable<Company>("company")

        // Check if company is null before accessing its properties
        if (company != null) {
            updateCompanyName.setText(company.name)
            updateCompanyLocation.setText(company.location)
        }

        updateButtonCompany.setOnClickListener {
            val updatedName = updateCompanyName.text.toString()
            val updatedLocation = updateCompanyLocation.text.toString()

            if (updatedName.isNotEmpty()) {
                // Create a Company object with the updated information
                val updatedCompany = Company(
                    id = company?.id ?: 0,  // Pass the existing ID
                    name = updatedName,
                    location = updatedLocation
                )

                // Update record in the database
                val dbHelper = DBHelper(requireContext())
                val success = dbHelper.updateCompany(updatedCompany)

                if (success) {
                    Toast.makeText(
                        requireContext(),
                        "Company updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Notify the listener that the company has been updated
                    onCompanyUpdatedListener?.onCompanyUpdated()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error updating company",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                dbHelper.close()
            }

            dismiss()
        }

        return view
    }

    interface OnCompanyUpdatedListener {
        fun onCompanyUpdated()
    }
}
