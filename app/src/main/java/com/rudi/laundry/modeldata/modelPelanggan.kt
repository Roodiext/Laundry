package com.rudi.laundry.modeldata

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class modelPelanggan(
    val idPelanggan: String = "",
    val namaPelanggan: String? = null,
    val alamatPelanggan: String? = null,
    val noHPPelanggan: String? = null,
    val terdaftar: String = "",
    val cabang: String = ""
) : Parcelable {

    // Constructor tanpa parameter untuk Firebase
    constructor() : this("", null, null, null, "", "")
}