package com.rudi.laundry.modeldata

data class modelLayananTambahan(
    val idLayanan: String = "",
    val namaLayanan: String? = null,
    val hargaLayanan: String? = null,
    val cabang: String = ""
) {
    constructor() : this("", null, null, "")
}
