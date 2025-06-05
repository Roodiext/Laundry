package com.rudi.laundry.modeldata

data class modelLayananTambahan(
    val idLayanan: String = "",
    val namaLayanan: String? = null,
    val hargaLayanan: String? = null,
    var jenisLayanan: String = "Tambahan",
    val cabang: String = ""
) {
    constructor() : this("", null, null, "")
}
