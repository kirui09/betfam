// DataClasses.kt

package com.example.apptea.ui.records
import android.os.Parcel
import android.os.Parcelable

data class MonthlyPayment(
    val date: String,
    val employeeName: String,
    val paymentAmount: Double
)

data class EditableTeaRecord(
    val id: Int,
    val date: String,
    val companies: List<String>,
    val employees: List<String>,


    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.createStringArrayList() ?: emptyList(),

        )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(date)
        parcel.writeStringList(companies)
        parcel.writeStringList(employees)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EditableTeaRecord> {
        override fun createFromParcel(parcel: Parcel): EditableTeaRecord {
            return EditableTeaRecord(parcel)
        }

        override fun newArray(size: Int): Array<EditableTeaRecord?> {
            return arrayOfNulls(size)
        }
    }
}





data class DailyTeaRecord(
    val id: Long,
    val date: String,
    val companies: String,
    val employees: String,
    val kilos: Double
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(date)
        parcel.writeString(companies)
        parcel.writeString(employees)
        parcel.writeDouble(kilos)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DailyTeaRecord> {
        override fun createFromParcel(parcel: Parcel): DailyTeaRecord {
            return DailyTeaRecord(parcel)
        }

        override fun newArray(size: Int): Array<DailyTeaRecord?> {
            return arrayOfNulls(size)
        }
    }
}





data class TeaRecord(
    val date: String,
    val employeeName: String,
    val company: String,
    val kilos: Double
)

data class TeaRecordsForDate(
    val date: String,
    val teaRecords: List<TeaRecord>,
    val concatenatedEmployeeNames: String,
    val concatenatedCompanies: String,
    val concatenatedKilos: String
)


data class Employee(
    val id: Long?,
    val name: String?,
    val empType: String?,
    val age: String?,
    val phoneNumber: String?,
    val employeeId: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeString(empType)
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









