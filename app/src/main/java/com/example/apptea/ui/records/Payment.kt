package com.example.apptea.ui.records

data class Payment(
    val id:Int,
    val date: String,
    val employeeName: String,
    val kilos: Double,
    val paymentAmount: Double
)


data class PaymentDetail(
    val date: String, // Date of the payment
    val kilos: Double, // Kilos value for the payment
    val paymentAmount: Double, // Amount of the payment
)









