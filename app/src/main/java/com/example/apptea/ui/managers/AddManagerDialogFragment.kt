//package com.example.apptea.ui.managers
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.EditText
//import androidx.fragment.app.DialogFragment
//import com.example.apptea.R
//
//class AddManagerDialogFragment : DialogFragment() {
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_add_manager_dialog, container, false)
//
//        // Find your EditText fields and save button
//        val nameEditText = view.findViewById<EditText>(R.id.editTextManagerName)
//        val ageEditText = view.findViewById<EditText>(R.id.editTextManagerAge)
//        val phoneNumberEditText = view.findViewById<EditText>(R.id.editTextManagerPhoneNumber)
//        val identificationEditText = view.findViewById<EditText>(R.id.editTextManagerID)
//        val saveButton = view.findViewById<Button>(R.id.buttonSaveManager)
//
//        // Set click listener for the save button
//        saveButton.setOnClickListener {
//            val name = nameEditText.text.toString()
//            val age = ageEditText.text.toString().toInt()
//            val phoneNumber = phoneNumberEditText.text.toString()
//            val identificationNumber = identificationEditText.text.toString()
//
//            // Call a listener method to handle the data
//            (activity as? AddManagerDialogListener)?.onSaveButtonClicked(
//                name, age, phoneNumber, identificationNumber
//            )
//
//            dismiss() // Close the dialog
//        }
//
//        return view
//    }
//
//    interface AddManagerDialogListener {
//        fun onSaveButtonClicked(name: String, age: Int, phoneNumber: String, identificationNumber: String)
//    }
//}
