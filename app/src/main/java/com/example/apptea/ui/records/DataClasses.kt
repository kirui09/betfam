// DataClasses.kt

package com.example.apptea.ui.records

import java.io.Serializable




data class EditableTeaRecord(
    val date: String,
    val companies: List<String>,
    val employees: List<String>,
    val kilos: Double
) : Serializable



data class DailyTeaRecord(
    val date: String,
    val companies: List<String>,
    val employees: List<String>,
    val totalKilos: Double
)
