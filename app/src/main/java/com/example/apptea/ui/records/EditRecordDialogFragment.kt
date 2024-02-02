package com.example.apptea.ui.records


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.apptea.R
import com.google.android.material.textfield.TextInputLayout

class EditRecordDialogFragment : DialogFragment() {

    private lateinit var textInputLayoutDate: TextInputLayout
    private lateinit var editrecordEntryTime: EditText
    private lateinit var editautoCompleteCompanyname: AutoCompleteTextView
    private lateinit var editautoCompleteEmployeeName: AutoCompleteTextView
    private lateinit var editfragmentTextEmployeeKilos: EditText
    private lateinit var editbuttonSaveRecord: Button
    private lateinit var editbuttonSaveAllRecords: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_record_dialog, container, false)

        textInputLayoutDate = view.findViewById(R.id.textInputLayoutDate)
        editrecordEntryTime = view.findViewById(R.id.editrecordEntryTime)
        editautoCompleteCompanyname = view.findViewById(R.id.editautoCompleteCompanyname)
        editautoCompleteEmployeeName = view.findViewById(R.id.editautoCompleteEmployeeName)
        editfragmentTextEmployeeKilos = view.findViewById(R.id.editfragmentTextEmployeeKilos)
        editbuttonSaveRecord = view.findViewById(R.id.editbuttonSaveRecord)
        editbuttonSaveAllRecords = view.findViewById(R.id.editbuttonSaveAllRecords)

        // Set up your listeners and additional logic here

        return view
    }

    // Add any additional methods or logic as needed

}


