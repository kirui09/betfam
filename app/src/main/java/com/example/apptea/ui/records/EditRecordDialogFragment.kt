package com.betfam.apptea.ui.records

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.betfam.apptea.App
import com.betfam.apptea.DBHelper
import com.betfam.apptea.PendingPaymentData
import com.betfam.apptea.databinding.FragmentEditRecordDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

interface RecordUpdateListener {
    fun onRecordUpdated()
}

class EditRecordDialogFragment : DialogFragment() {

    private var _binding: FragmentEditRecordDialogBinding? = null
    private val binding get() = _binding!!
    private var record: TeaPaymentRecord? = null
    private var recordUpdateListener: RecordUpdateListener? = null

    fun setRecordUpdateListener(listener: RecordUpdateListener) {
        recordUpdateListener = listener
    }

    private fun notifyRecordUpdated() {
        recordUpdateListener?.onRecordUpdated()
    }

    companion object {
        fun newInstance(record: TeaPaymentRecord): EditRecordDialogFragment {
            val fragment = EditRecordDialogFragment()
            val args = Bundle().apply {
                putParcelable("record", record)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditRecordDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateSpinners()
        record = arguments?.getParcelable("record")
        populateUI()

        binding.updateRecordButton.setOnClickListener {
            if (validateFields()) {
                updateRecord()
                notifyRecordUpdated()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Validation failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateUI() {
        record?.let { record ->
            binding.updaterecordEntryTime.setText(record.date)
            binding.spinnerCompanyName.setSelection(findIndexOfCompany(record.company))
            binding.spinnerEmployeeName.setSelection(findIndexOfEmployee(record.employees))
            binding.updateTextEmployeeKilos.setText(record.kilos.toString())
        }
    }

    private fun populateSpinners() {
        val safeContext = context ?: return
        try {
            val dbHelper = DBHelper(safeContext)

            val employeeNames = dbHelper.getAllEmployeeNames()
            val companyNames = dbHelper.getAllCompanyNames()

            val employeeAdapter = ArrayAdapter(
                safeContext,
                android.R.layout.simple_spinner_item,
                employeeNames
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.spinnerEmployeeName.adapter = employeeAdapter

            val companyAdapter = ArrayAdapter(
                safeContext,
                android.R.layout.simple_spinner_item,
                companyNames
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.spinnerCompanyName.adapter = companyAdapter
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                safeContext,
                "An error occurred while populating spinners",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun findIndexOfCompany(companyName: String): Int {
        val adapter = binding.spinnerCompanyName.adapter
        return (0 until adapter.count).firstOrNull { adapter.getItem(it) == companyName } ?: 0
    }

    private fun findIndexOfEmployee(employeeName: String): Int {
        val adapter = binding.spinnerEmployeeName.adapter
        return (0 until adapter.count).firstOrNull { adapter.getItem(it) == employeeName } ?: 0
    }

    private fun validateFields(): Boolean {
        // Add your validation logic here
        return true
    }

    private fun handlePayment(context: Context, payRate: Double, day: String, existingRecord: TeaPaymentRecord?, updatedKilos: Double, onPaymentUpdate: (Double) -> Unit) {
        val recordsViewModel = RecordsViewModel.create(context)
        CoroutineScope(Dispatchers.Default).launch {
            Log.d("HandlePayment", "Coroutine launched")

            val dbHelper = DBHelper(context)
            val appDatabase = App.getDatabase(context)
            val pendingPaymentDataDao = appDatabase.pendingPaymentDao()

            val sharedPreferences = context.getSharedPreferences("com.betfam.apptea.preferences", Context.MODE_PRIVATE)
            val payRateFromPreferences = sharedPreferences.getFloat("pay_rate", payRate.toFloat()).toDouble()
            val formattedPayRate = BigDecimal(payRateFromPreferences).setScale(2, RoundingMode.HALF_UP)

            val teaRecords = withContext(Dispatchers.IO) {
                dbHelper.getTeaRecordsForEmployeeforday(day)
            }
            Log.d("HandlePayment", "Fetched ${teaRecords.size} tea records for $day")

            teaRecords.forEach { record ->
                if (existingRecord != null && record.id == existingRecord.id) {
                    val paymentAmount = if (record.payment != 0.0) {
                        BigDecimal(updatedKilos).multiply(formattedPayRate).setScale(2, RoundingMode.HALF_UP).toDouble()
                    } else {
                        BigDecimal(record.kilos).multiply(formattedPayRate).setScale(2, RoundingMode.HALF_UP).toDouble()
                    }
                    withContext(Dispatchers.IO) {
                        dbHelper.updatePaymentInTeaRecords(record.id, paymentAmount)
                    }
                } else if (record.payment == 0.0) {
                    val paymentAmount = 0.0
                    val paymentData = PendingPaymentData(
                        id = record.id,
                        date = record.date,
                        employeeName = record.employees,
                        paymentAmount = paymentAmount
                    )
                    withContext(Dispatchers.IO) {
                        dbHelper.updatePaymentInTeaRecords(record.id, paymentAmount)
                    }
                }
            }

            try {
                // recordsViewModel.syncAndCompareDataWithGoogleSheet()
                Log.d("HandlePayment", "Successfully synced with Google Sheets")
            } catch (e: Exception) {
                Log.e("HandlePayment", "Error syncing with Google Sheets", e)
            }

            withContext(Dispatchers.Main) {
                recordsViewModel.refreshRecords()
            }
            Log.d("HandlePayment", "Payment process completed")
        }
    }

    private fun updateRecord() {
        val safeContext = context ?: return
        record?.let { existingRecord ->
            try {
                val dbHelper = DBHelper(safeContext)
                val recordsViewModel = RecordsViewModel.create(safeContext)
                val payRate = 8.0

                val updatedRecord = TeaPaymentRecord(
                    id = existingRecord.id,
                    date = binding.updaterecordEntryTime.text.toString(),
                    company = binding.spinnerCompanyName.selectedItem.toString(),
                    employees = binding.spinnerEmployeeName.selectedItem.toString(),
                    kilos = binding.updateTextEmployeeKilos.text.toString().toDouble(),
                    payment = existingRecord.payment
                )

                if (existingRecord.payment != 0.0) {
                    val sharedPreferences = safeContext.getSharedPreferences("com.betfam.apptea.preferences", Context.MODE_PRIVATE)
                    val payRate = 8
                    val payRateFromPreferences = sharedPreferences.getFloat("pay_rate", payRate.toFloat()).toDouble()
                    val formattedPayRate = String.format("%.2f", payRateFromPreferences).toDouble()

                    val confirmationDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    confirmationDialogBuilder.setTitle("Confirm Pay Rate")

                    val layout = LinearLayout(context)
                    layout.orientation = LinearLayout.VERTICAL

                    val messageTextView = TextView(context)
                    messageTextView.text = "The pay rate is Ksh .$formattedPayRate\n"
                    layout.addView(messageTextView)

                    val editButton = Button(context)
                    editButton.text = "Edit Pay Rate"
                    layout.addView(editButton)
                    editButton.setOnClickListener {
                        showEditPayRateDialog(safeContext, messageTextView)
                    }

                    confirmationDialogBuilder.setView(layout)

                        .setPositiveButton("Yes") { _, _ ->
                            val isUpdated = dbHelper.updateTeaRecord(updatedRecord)
                            handlePayment(safeContext, formattedPayRate, updatedRecord.date, existingRecord, updatedRecord.kilos) {}
                        }
                        .setNegativeButton("No") { _, _ ->
                            // If user declines, just update the kilos
                            val isUpdated = dbHelper.updateTeaRecord(updatedRecord)
                        }
                        .show()
                } else {
                    val isUpdated = dbHelper.updateTeaRecord(updatedRecord)
                    handlePayment(safeContext, 0.0, updatedRecord.date, null, updatedRecord.kilos) {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    safeContext,
                    "An error occurred while updating record",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun showEditPayRateDialog(context: Context, messageTextView: TextView) {
        val editDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(context)
        editDialogBuilder.setTitle("Edit Pay Rate")

        val editText = EditText(context)
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editText.hint = "Enter new pay rate"
        editDialogBuilder.setView(editText)

        editDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            val inputText = editText.text.toString()
            val newPayRate = inputText.toDoubleOrNull()
            if (newPayRate != null) {
                val formattedPayRate = String.format("%.2f", newPayRate).toFloat()
                val sharedPreferences = context.getSharedPreferences("com.betfam.apptea.preferences", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putFloat("pay_rate", formattedPayRate)
                    apply()
                }

                messageTextView.text = """
                The pay rate is Ksh $formattedPayRate.
                
      
            """.trimIndent()
            } else {
                Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        editDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        editDialogBuilder.show()
    }
}