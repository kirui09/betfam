package com.example.apptea.ui.records

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.apptea.R
import com.example.apptea.databinding.FragmentRecordsBinding
import com.example.apptea.ui.employees.AddEmployeeDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val recordsViewModel =
            ViewModelProvider(this).get(RecordsViewModel::class.java)

        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Declare the button and set its click listener
        // Access the FloatingActionButton

        val fabAddRecord = root.findViewById<FloatingActionButton>(R.id.fabAddRecord)

        fabAddRecord.setOnClickListener {
            // Show the FormDialogFragment when FAB is clicked
            val formDialog = AddRecordDialogFragment()
            formDialog.show(childFragmentManager, AddRecordDialogFragment::class.java.simpleName)
        }



        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}