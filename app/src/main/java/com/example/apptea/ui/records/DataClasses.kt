// DataClasses.kt

package com.example.apptea.ui.records
import android.os.Parcel
import android.os.Parcelable



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

    val date: String,
    val companies: String,
    val employees: String,
    val kilos: Double
)

data class TeaRecord(
    val date: String,
    val employeeName: List<String>,
    val company: List<String>,
    val kilos: List<Double>)






