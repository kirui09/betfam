package com.example.apptea.ui.employees

import android.os.Parcel
import android.os.Parcelable

data class Employee(
    val id: Long?,  // Make id nullable
    val name: String?,
    val age: String?,
    val phoneNumber: String?,
    val employeeId: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeString(age)
        parcel.writeString(phoneNumber)
        parcel.writeString(employeeId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Employee> {
        override fun createFromParcel(parcel: Parcel): Employee {
            return Employee(parcel)
        }

        override fun newArray(size: Int): Array<Employee?> {
            return arrayOfNulls(size)
        }
    }
}
