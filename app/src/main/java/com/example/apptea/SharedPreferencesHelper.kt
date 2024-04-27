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


    fun getCheckBoxState(day: String): Boolean {
        return sharedPreferences.getBoolean("CheckBox_$day", false)
    }

    fun saveCheckBoxState(day: String, isChecked: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("CheckBox_$day", isChecked)
        editor.apply()
    }

    fun getLastPromptTime(): Long {
        return sharedPreferences.getLong("lastPromptTime", 0)
    }

    fun saveLastPromptTime(time: Long) {
        with(sharedPreferences.edit()) {
            putLong("lastPromptTime", time)
            apply()
        }
    }



}
