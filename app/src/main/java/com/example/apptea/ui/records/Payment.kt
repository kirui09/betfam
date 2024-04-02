package com.example.apptea.ui.records

data class Payment(
    val id:Int,
    val date: String,
    val employeeName: String,
    val kilos: Double,
    val paymentAmount: Double
)


data class PaymentDetail(
    val date: String,
    val paymentAmount: Double
)







