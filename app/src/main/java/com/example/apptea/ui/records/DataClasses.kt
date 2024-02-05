// DataClasses.kt

package com.example.apptea.ui.records
import android.os.Parcel
import android.os.Parcelable



data class EditableTeaRecord(
    val id: Int,
    val date: String,
    val companies: List<String>,
    val employees: List<String>,
    val kilos: Double,
    val pay: Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt() ,
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(date)
        parcel.writeStringList(companies)
        parcel.writeStringList(employees)
        parcel.writeDouble(kilos)
        parcel.writeDouble(pay)
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
    val companies: List<String>,
    val employees: List<String>,
    val totalKilos: Double
)




