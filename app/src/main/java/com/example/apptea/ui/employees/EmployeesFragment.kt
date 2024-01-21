package com.example.apptea.ui.employees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.apptea.R
import com.example.apptea.databinding.FragmentEmployeesBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EmployeesFragment : Fragment() {

    private var _binding: FragmentEmployeesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val employeesViewModel = ViewModelProvider(this).get(EmployeesViewModel::class.java)

        _binding = FragmentEmployeesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Access the FloatingActionButton
        val fabAddEmployee = root.findViewById<FloatingActionButton>(R.id.fabAddEmployee)

        fabAddEmployee.setOnClickListener {
            // Show the FormDialogFragment when FAB is clicked
            val formDialog = FormDialogFragment()
            formDialog.show(childFragmentManager, FormDialogFragment::class.java.simpleName)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
