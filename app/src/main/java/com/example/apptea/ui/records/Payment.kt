package com.betfam.apptea.ui.records

data class Payment(
    val id:Int,
    val date: String,
    val employeeName: String,
    val kilos: Double,
    val paymentAmount: Double
)


data class PaymentDetail(
    val date: String,
    val kilos: Double,
    val paymentAmount: Double,
    val isPaymentCompleted: Boolean // New property to indicate payment status
)











