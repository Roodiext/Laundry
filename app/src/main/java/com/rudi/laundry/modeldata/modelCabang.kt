package com.rudi.laundry.modeldata

import android.os.Parcel
import android.os.Parcelable
import java.text.SimpleDateFormat
import java.util.*

data class modelCabang(
    var idCabang: String = "",
    var namaToko: String = "",
    var namaCabang: String = "",
    var alamatCabang: String = "",
    var noTelpCabang: String = "",
    var statusOperasional: String = "",
    var jamBuka: String = "08:00", // Format HH:mm
    var jamTutup: String = "22:00", // Format HH:mm
    var hariOperasional: String = "1,2,3,4,5,6,7", // 1=Senin, 7=Minggu (comma separated)
    var terdaftar: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "08:00",
        parcel.readString() ?: "22:00",
        parcel.readString() ?: "1,2,3,4,5,6,7",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idCabang)
        parcel.writeString(namaToko)
        parcel.writeString(namaCabang)
        parcel.writeString(alamatCabang)
        parcel.writeString(noTelpCabang)
        parcel.writeString(statusOperasional)
        parcel.writeString(jamBuka)
        parcel.writeString(jamTutup)
        parcel.writeString(hariOperasional)
        parcel.writeString(terdaftar)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<modelCabang> {
        override fun createFromParcel(parcel: Parcel): modelCabang {
            return modelCabang(parcel)
        }

        override fun newArray(size: Int): Array<modelCabang?> {
            return arrayOfNulls(size)
        }
    }

    // Function untuk mengecek apakah toko sedang buka berdasarkan waktu real-time
    fun isCurrentlyOpen(): Boolean {
        return try {
            val currentTime = Calendar.getInstance()
            val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
            val currentMinute = currentTime.get(Calendar.MINUTE)
            val currentDayOfWeek = currentTime.get(Calendar.DAY_OF_WEEK)

            // Convert day of week (Calendar uses 1=Sunday, 2=Monday, etc.)
            // We use 1=Monday, 7=Sunday
            val dayOfWeek = when(currentDayOfWeek) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }

            // Check if today is operational day
            val operationalDays = hariOperasional.split(",").map { it.trim().toIntOrNull() ?: 0 }
            if (!operationalDays.contains(dayOfWeek)) {
                return false
            }

            // Parse jam buka dan tutup
            val bukaParts = jamBuka.split(":")
            val tutupParts = jamTutup.split(":")

            if (bukaParts.size != 2 || tutupParts.size != 2) {
                return false
            }

            val jamBukaHour = bukaParts[0].toIntOrNull() ?: 0
            val jamBukaMinute = bukaParts[1].toIntOrNull() ?: 0
            val jamTutupHour = tutupParts[0].toIntOrNull() ?: 23
            val jamTutupMinute = tutupParts[1].toIntOrNull() ?: 59

            // Convert to minutes for easier comparison
            val currentTimeInMinutes = currentHour * 60 + currentMinute
            val bukaTimeInMinutes = jamBukaHour * 60 + jamBukaMinute
            val tutupTimeInMinutes = jamTutupHour * 60 + jamTutupMinute

            // Handle case where closing time is next day (e.g., 22:00 - 02:00)
            if (tutupTimeInMinutes < bukaTimeInMinutes) {
                // Toko buka melewati tengah malam
                return currentTimeInMinutes >= bukaTimeInMinutes || currentTimeInMinutes <= tutupTimeInMinutes
            } else {
                // Normal operating hours
                return currentTimeInMinutes >= bukaTimeInMinutes && currentTimeInMinutes <= tutupTimeInMinutes
            }

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Function untuk mendapatkan status real-time
    fun getRealTimeStatus(): String {
        return if (isCurrentlyOpen()) "BUKA" else "TUTUP"
    }

    // Function untuk mendapatkan info jam operasional
    fun getOperationalHoursInfo(): String {
        val days = when(hariOperasional) {
            "1,2,3,4,5,6,7" -> "Setiap Hari"
            "1,2,3,4,5" -> "Senin - Jumat"
            "6,7" -> "Weekend"
            else -> {
                val dayNames = hariOperasional.split(",").mapNotNull { dayNum ->
                    when(dayNum.trim()) {
                        "1" -> "Sen"
                        "2" -> "Sel"
                        "3" -> "Rab"
                        "4" -> "Kam"
                        "5" -> "Jum"
                        "6" -> "Sab"
                        "7" -> "Min"
                        else -> null
                    }
                }.joinToString(", ")
                dayNames.ifEmpty { "Tidak ada info" }
            }
        }
        return "$days • $jamBuka - $jamTutup"
    }
}