package com.rudi.laundry.modeldata

data class modelPegawai(
    var idPegawai: String = "",
    var namaPegawai: String = "",
    var alamatPegawai: String = "",
    var noHPPegawai: String = "",
    var cabang: String = ""
) {
    // Constructor kosong diperlukan untuk Firebase
    constructor() : this("", "", "", "", "")
}