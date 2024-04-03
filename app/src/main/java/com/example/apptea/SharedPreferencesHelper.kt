// SharedPreferencesHelper.kt
package com.example.apptea

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {
    private val PREF_NAME = "UserPrefs"
    private val KEY_PHONE_NUMBER = "phone_number"
    private val KEY_USER_ID = "UUID"

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun savePhoneNumber(phoneNumber: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_PHONE_NUMBER, phoneNumber)
        editor.apply()
    }

    fun getPhoneNumber(): String {
        return sharedPreferences.getString(KEY_PHONE_NUMBER, "") ?: ""
    }


    fun getCheckBoxState(): Boolean {
        return sharedPreferences.getBoolean("VERIFIED_STATE", true)
    }

    fun saveCheckBoxState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("CHECKBOX_STATE", true) // Always save as true
        editor.apply()
    }



}
