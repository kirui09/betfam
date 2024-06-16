// CompaniesViewModel.kt
package com.betfam.apptea.ui.companies

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betfam.apptea.DBHelper
import kotlinx.coroutines.launch

class CompaniesViewModel(private val dbHelper: DBHelper) : ViewModel() {

    // Function to update a company
    fun updateCompany(company: Company) {
        Log.d("CompaniesViewModel", "Updating company: $company")
        viewModelScope.launch {
            dbHelper.updateCompany(company)
            Log.d("CompaniesViewModel", "Company updated successfully")
        }
}

}
