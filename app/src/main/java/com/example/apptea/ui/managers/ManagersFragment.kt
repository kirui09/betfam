package com.example.apptea.ui.managers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.apptea.R
import com.example.apptea.databinding.FragmentManagersBinding
import com.example.apptea.databinding.FragmentRecordsBinding
import com.example.apptea.ui.records.RecordsViewModel

class ManagersFragment : Fragment() {
    private var _binding: FragmentManagersBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val recordsViewModel =
            ViewModelProvider(this).get(ManagersViewModel::class.java)

        _binding = FragmentManagersBinding.inflate(inflater, container, false)
        val root: View = binding.root



        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}