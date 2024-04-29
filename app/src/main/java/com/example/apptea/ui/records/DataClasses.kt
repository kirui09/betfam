// DataClasses.kt

package com.example.apptea.ui.records
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

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




@Entity
data class DailyTeaRecord(
    @PrimaryKey val id: Int,
    val date: String,
    val companies: String,
    val employees: String,
    val kilos: Double
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
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


data class TeaPaymentRecord(
    val id: Int,
    val date: String,
    val company: String,
    val employees: String,
    val kilos: Double,
    val payment: Double
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TeaPaymentRecord

        if (id != other.id) return false
        if (date != other.date) return false
        if (company != other.company) return false
        if (employees != other.employees) return false
        if (kilos != other.kilos) return false
        if (payment != other.payment) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + date.hashCode()
        result = 31 * result + company.hashCode()
        result = 31 * result + employees.hashCode()
        result = 31 * result + kilos.hashCode()
        result = 31 * result + payment.hashCode()
        return result
    }
}











