//package com.betfam.apptea.ui.managers
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.betfam.apptea.databinding.ItemManagerBinding
//
//class ManagersAdapter : ListAdapter<Manager, ManagersAdapter.ViewHolder>(ManagerDiffCallback()) {
//
//    class ViewHolder(private val binding: ItemManagerBinding) : RecyclerView.ViewHolder(binding.root) {
//        val nameTextView = binding.managerNameTextView
//        val phoneTextView = binding.managerphoneNumberTextView
//        // Add other views as needed
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val binding = ItemManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return ViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val manager = getItem(position)
//
//        holder.nameTextView.text = "${manager.firstName} ${manager.lastName}"
//        holder.phoneTextView.text = manager.phone
//        // Bind other data as needed
//    }
//}
