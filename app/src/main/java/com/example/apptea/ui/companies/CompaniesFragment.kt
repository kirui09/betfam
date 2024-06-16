package com.betfam.apptea.ui.companies

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.betfam.apptea.DBHelper
import com.betfam.apptea.R
import com.betfam.apptea.ui.employees.Employee
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CompaniesFragment : Fragment(), CompanyClickHandler {

    private lateinit var dbHelper: DBHelper
    private lateinit var companyAdapter: CompanyAdapter
    private lateinit var viewModel: CompaniesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_companies, container, false)

        dbHelper = DBHelper(requireContext())
        viewModel = CompaniesViewModel(dbHelper)

        // Set up RecyclerView
        val recyclerViewCompanies: RecyclerView = view.findViewById(R.id.companiesrecyclerView)
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerViewCompanies.layoutManager = layoutManager

        // Get the list of companies from the database
        val companies = dbHelper.getAllCompanies()

        // Initialize the adapter with the click handler
        companyAdapter = CompanyAdapter(companies, this)
        recyclerViewCompanies.adapter = companyAdapter

        // Set a click listener for the FAB button
        val fabAddCompany: FloatingActionButton = view.findViewById(R.id.fabAddCompany)
        fabAddCompany.setOnClickListener {
            // Open the AddCompanyDialogFragment when the FAB is clicked
            openAddCompanyDialog()
        }

        return view
    }

    private fun openAddCompanyDialog() {
        val addCompanyDialogFragment = AddCompanyDialogFragment()
        addCompanyDialogFragment.setAddCompanyDialogListener(object :
            AddCompanyDialogFragment.AddCompanyDialogListener {
            override fun onSaveCompanyClicked(name: String, location: String) {
                // Handle the save operation (e.g., add the company to the database)
                // You can use your DBHelper here
                // dbHelper.insertCompany(name, location)

                // Dismiss the dialog
                addCompanyDialogFragment.dismiss()

                // Refresh the company list after adding a new company
                updateCompanyList()
            }
        })

        addCompanyDialogFragment.show(requireFragmentManager(), "AddCompanyDialogFragment")
    }

    // Update the company list or perform any other UI updates as needed

    override fun onEditClick(company: Company) {
        // Handle the edit action here
        // For example, open the UpdateCompanyDialogFragment with the selected company data
        val updateCompanyDialogFragment =
            UpdateCompanyDialogFragment.newInstance(company)
        updateCompanyDialogFragment.show(
            requireFragmentManager(),
            "UpdateCompanyDialogFragment"
        )
    }

    override fun onDeleteClick(company: Company) {
        showDeleteConfirmationDialog(company)
    }

    private fun showDeleteConfirmationDialog(company: Company) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Company")
            .setMessage("Are you sure you want to delete ${company.name}?")
            .setPositiveButton("Yes") { _, _ ->
                // Delete the company from the database
                dbHelper.deleteCompany(company.id)

                updateCompanyList()
            }
            .setNegativeButton("No", null)
            .create()

        dialog.show()
    }

    private fun updateCompanyList() {
        try {
            Log.d("CompaniesFragment", "Updating company list")

            // Fetch the updated list of companies from the database
            val updatedCompanies = dbHelper.getAllCompanies()

            // Update the adapter with the new list of companies
            companyAdapter.updateData(updatedCompanies)
            companyAdapter.updateData(updatedCompanies)

            // Notify the adapter that the data set has changed
            companyAdapter.notifyDataSetChanged()
            companyAdapter.notifyDataSetChanged()

            Log.d("CompaniesFragment", "Company list updated successfully")
        } catch (e: Exception) {
            Log.e("CompaniesFragment", "Error updating company list: $e")
        }
    }



}
