package com.rudi.laundry.modeldata

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class modelLayanan(
    var idLayanan: String = "",
    var namaLayanan: String? = null,
    var hargaLayanan: String? = null,
    var jenisLayanan: String = "Utama",
    var cabang: String = ""
) : Parcelable {

    // Constructor tanpa parameter untuk Firebase
    constructor() : this("", null, null, "", "")
}