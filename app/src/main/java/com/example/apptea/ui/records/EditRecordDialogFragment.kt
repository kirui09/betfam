import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.apptea.DBHelper
import com.example.apptea.R
import com.example.apptea.ui.records.DailyTeaRecord
import com.example.apptea.ui.records.Record
import com.example.apptea.ui.records.RecordsFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class EditRecordDialogFragment : Fragment() {

    private lateinit var dbHelper: DBHelper
    private lateinit var teaRecordDate: String
    private lateinit var editTextDate: TextInputEditText
    private lateinit var editTextCompanies: TextInputEditText
    private lateinit var editTextEmployees: TextInputEditText
    private lateinit var editTextKilos: TextInputEditText
    private lateinit var buttonSave: MaterialButton
    private lateinit var buttonSaveAll: MaterialButton

    // Maintain a list of records to be saved
    private val recordsToSave = mutableListOf<DailyTeaRecord>()

    // Declare teaRecord as nullable
    private var teaRecord: DailyTeaRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            teaRecordDate = it.getString("teaRecordDate", "")
        }

        dbHelper = DBHelper(requireContext())

        // Fetch the record from the database for editing
        teaRecord = dbHelper.getTeaRecordByDate(teaRecordDate)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_record_dialog, container, false)

        editTextDate = view.findViewById(R.id.editrecordEntryTime)
        editTextCompanies = view.findViewById(R.id.editautoCompleteCompanyname)
        editTextEmployees = view.findViewById(R.id.editautoCompleteEmployeeName)
        editTextKilos = view.findViewById(R.id.editfragmentTextEmployeeKilos)
        buttonSave = view.findViewById(R.id.editbuttonSaveRecord)
        buttonSaveAll = view.findViewById(R.id.editbuttonSaveAllRecords)

        // Fetch the record from the database for editing
        teaRecord = dbHelper.getTeaRecordByDate(teaRecordDate)

        // Populate the UI with the fetched record
        editTextDate.setText(teaRecord?.date)
        editTextCompanies.setText(teaRecord?.companies?.joinToString(", "))
        editTextEmployees.setText(teaRecord?.employees?.joinToString(", "))
        editTextKilos.setText(teaRecord?.totalKilos?.toString())

        buttonSave.setOnClickListener {
            // Save the updated record to the list
            saveRecordToMemory()
        }

        buttonSaveAll.setOnClickListener {
            // Save all records from the list to the database
            saveAllRecordsToDatabase()
        }

        return view
    }

    private fun saveRecordToMemory() {
        val updatedRecord = teaRecord?.copy(
            date = editTextDate.text.toString(),
            companies = editTextCompanies.text.toString().split(","),
            employees = editTextEmployees.text.toString().split(","),
            totalKilos = editTextKilos.text.toString().toDoubleOrNull() ?: 0.0
        )

        // Add the record to the list if it is not null
        if (updatedRecord != null) {
            recordsToSave.add(updatedRecord)
            // Notify the user or perform any necessary action
        }
    }

    private fun saveAllRecordsToDatabase() {
        // Save all records from the list to the database
        dbHelper.insertTeaRecords(recordsToSave)

        // Notify the listener (RecordsFragment) about the edited records
        (activity as? RecordsFragment.OnRecordEditedListener)?.onRecordsEdited(recordsToSave)

        // Clear the list after saving
        recordsToSave.clear()

        // Dismiss the dialog
        dismiss()
    }
}
