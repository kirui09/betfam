package com.betfam.apptea.ui.managers

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.betfam.apptea.R
import com.betfam.apptea.databinding.FragmentManagersBinding

class ManagersFragment : Fragment() {

    private var _binding: FragmentManagersBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val managersViewModel =
            ViewModelProvider(this).get(ManagersViewModel::class.java)

        _binding = FragmentManagersBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Declare the button and set its click listener


        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}