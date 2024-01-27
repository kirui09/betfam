package com.example.apptea.ui.companies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apptea.DBHelper
import com.example.apptea.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CompaniesFragment : Fragment() {

    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_companies, container, false)

        dbHelper = DBHelper(requireContext())

        // Set up RecyclerView
        val recyclerViewCompanies: RecyclerView = view.findViewById(R.id.companiesrecyclerView)
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerViewCompanies.layoutManager = layoutManager

        // Get the list of companies from the database
        val companies = dbHelper.getAllCompanies()


        // Create and set the adapter
        val companyAdapter = CompanyAdapter(companies)
        recyclerViewCompanies.adapter = companyAdapter

        // Find the FAB button
        val fabAddCompany: FloatingActionButton = view.findViewById(R.id.fabAddCompany)

        // Set a click listener for the FAB button
        fabAddCompany.setOnClickListener {
            // Open the AddCompanyDialogFragment when the FAB is clicked
            openAddCompanyDialog()
        }

        return view
    }

    private fun openAddCompanyDialog() {
        val addCompanyDialogFragment = AddCompanyDialogFragment()
        addCompanyDialogFragment.setAddCompanyDialogListener(object : AddCompanyDialogFragment.AddCompanyDialogListener {
            override fun onSaveCompanyClicked(name: String, location: String) {
                // Handle the save operation (e.g., add the company to the database)
                // You can use your DBHelper here
                // dbHelper.insertCompany(name, location)

                // Optionally, update the UI or refresh the company list
                // updateCompanyList()

                // Dismiss the dialog
                addCompanyDialogFragment.dismiss()
            }
        })

        addCompanyDialogFragment.show(requireFragmentManager(), "AddCompanyDialogFragment")
    }
}
