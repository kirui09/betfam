package com.example.apptea.ui.records

data class DailyTeaRecord(
    val date: String,
    val employees: List<String>,
    val companies: List<String>,
    val totalKilos: Double
)
