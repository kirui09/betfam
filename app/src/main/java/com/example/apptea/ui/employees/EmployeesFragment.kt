package com.example.apptea.ui.employees

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.apptea.DBHelper
import com.example.apptea.R
import com.example.apptea.databinding.FragmentEmployeesBinding
//import com.example.apptea.ui.records.AddRecordDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EmployeesFragment : Fragment(), AddEmployeeDialogFragment.OnEmployeeSavedListener,
    EmployeeAdapter.OnDeleteClickListener, EditEmployeeDialogFragment.OnEmployeeUpdatedListener {

    private var _binding: FragmentEmployeesBinding? = null
    private val binding get() = _binding!!

    private lateinit var employeesViewModel: EmployeesViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var employeeAdapter: EmployeeAdapter
    private lateinit var employeeList: MutableList<Employee>
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        employeesViewModel = ViewModelProvider(this).get(EmployeesViewModel::class.java)

        _binding = FragmentEmployeesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        swipeRefreshLayout = root.findViewById(R.id.employeeswipeRefreshLayout)

        // Set up RecyclerView
        recyclerView = root.findViewById(R.id.recyclerView)
        employeeAdapter = EmployeeAdapter(emptyList()) // Initialize the adapter
        recyclerView.adapter = employeeAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set the delete click listener in the adapter
        employeeAdapter.onDeleteClickListener = this

        // Observe changes in the employee list and update the adapter
        employeesViewModel.employeeList.observe(viewLifecycleOwner, Observer { employees ->
            Log.d("EmployeesFragment", "Observed ${employees.size} employees")
            employeeList = employees.toMutableList() // Store the employee list locally
            employeeAdapter.updateData(employees)
            swipeRefreshLayout.isRefreshing = false // Stop the refresh animation
        })

        // Fetch employees when the fragment is created
        employeesViewModel.fetchEmployees()

        // Access the FloatingActionButton
        val fabAddEmployee = root.findViewById<FloatingActionButton>(R.id.fabAddEmployee)

        fabAddEmployee.setOnClickListener {
            // Show the FormDialogFragment when FAB is clicked
            val formDialog = AddEmployeeDialogFragment()
            formDialog.employeeSavedListener = this
            formDialog.show(
                childFragmentManager,
                AddEmployeeDialogFragment::class.java.simpleName
            )
        }

        swipeRefreshLayout.setOnRefreshListener {
            // Refresh the employee list when swipe-to-refresh is triggered
            employeesViewModel.fetchEmployees()
        }

        return root
    }

    override fun onDestroyView() {
        // Ensure data is updated before destroying the view
        employeesViewModel.fetchEmployees()
        super.onDestroyView()
        _binding = null
    }

    override fun onEmployeeSaved() {
        // Handle the logic to refresh your fragment
        // For example, reload the data or re-fetch the records
        employeesViewModel.fetchEmployees()
        swipeRefreshLayout.isRefreshing = true // Start the refresh animation
    }

    override fun onDeleteClick(employee: Employee) {
        showDeleteConfirmationDialog(employee)
    }

    override fun onEmployeeUpdated(updatedEmployee: Employee) {
        // Update the RecyclerView with the edited employee
        val index = employeeList.indexOfFirst { it.id == updatedEmployee.id }
        if (index != -1) {
            employeeList[index] = updatedEmployee
            // Notify the adapter that the data at the specific position has changed
            employeeAdapter.notifyItemChanged(index)
        }
    }

    private fun showDeleteConfirmationDialog(employee: Employee) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Employee")
            .setMessage("Are you sure you want to delete ${employee.name}?")
            .setPositiveButton("Yes") { _, _ ->
                deleteEmployee(employee)
            }
            .setNegativeButton("No", null)
            .create()

        dialog.show()
    }

    private fun deleteEmployee(employee: Employee) {
        // Implement the delete operation in your DBHelper
        val dbHelper = DBHelper(requireContext())
        val success = dbHelper.deleteEmployee(employee)

        if (success) {
            Toast.makeText(
                requireContext(),
                "Employee deleted successfully",
                Toast.LENGTH_SHORT
            ).show()
            employeesViewModel.fetchEmployees() // Refresh the employee list after deletion
        } else {
            Toast.makeText(requireContext(), "Error deleting employee", Toast.LENGTH_SHORT).show()
        }

        dbHelper.close()
    }
}
