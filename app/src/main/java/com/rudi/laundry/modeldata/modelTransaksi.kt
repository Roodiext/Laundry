package com.rudi.laundry.modeldata

import java.io.Serializable

data class modelTransaksi(
    var idTransaksi: String = "",
    var namaPelanggan: String = "",
    var nomorHP: String = "",
    var namaLayanan: String = "",
    var hargaLayanan: Double = 0.0,
    var layananTambahan: ArrayList<modelLayanan> = ArrayList(),
    var totalBayar: Double = 0.0,
    var metodePembayaran: String = "",
    var status: String = "BELUM_BAYAR", // BELUM_BAYAR, SUDAH_BAYAR, SELESAI
    var tanggalTransaksi: String = "",
    var tanggalPengambilan: String = "",

    // Data Cabang
    var cabangId: String = "",
    var cabangNama: String = "",
    var cabangAlamat: String = "",

    // Data Pegawai
    var pegawaiId: String = "",
    var pegawaiNama: String = "",
    var pegawaiJabatan: String = "",

    // Timestamp untuk sorting
    var timestamp: Long = System.currentTimeMillis()
) : Serializable {

    // Enum untuk status transaksi
    companion object {
        const val STATUS_BELUM_BAYAR = "BELUM_BAYAR"
        const val STATUS_SUDAH_BAYAR = "SUDAH_BAYAR"
        const val STATUS_SELESAI = "SELESAI"
    }

    // Function untuk mendapatkan display status
    fun getDisplayStatus(): String {
        return when (status) {
            STATUS_BELUM_BAYAR -> "Belum Dibayar"
            STATUS_SUDAH_BAYAR -> "Sudah Dibayar"
            STATUS_SELESAI -> "Selesai"
            else -> "Unknown"
        }
    }

    // Function untuk mendapatkan warna status
    fun getStatusColor(): String {
        return when (status) {
            STATUS_BELUM_BAYAR -> "#F7374F"
            STATUS_SUDAH_BAYAR -> "#F7374F"
            STATUS_SELESAI -> "#3E7B27"
            else -> "#666666"
        }
    }

    // Function untuk mendapatkan background status
    fun getStatusBackground(): String {
        return when (status) {
            STATUS_BELUM_BAYAR -> "bg_status_merah"
            STATUS_SUDAH_BAYAR -> "bg_status_kuning"
            STATUS_SELESAI -> "bg_status_hijau"
            else -> "bg_status_default"
        }
    }

    // Function untuk mendapatkan text button
    fun getButtonText(): String {
        return when (status) {
            STATUS_BELUM_BAYAR -> "Bayar Sekarang"
            STATUS_SUDAH_BAYAR -> "Ambil Sekarang"
            STATUS_SELESAI -> ""
            else -> ""
        }
    }

    // Function untuk mendapatkan warna button
    fun getButtonColor(): String {
        return when (status) {
            STATUS_BELUM_BAYAR -> "#FF3D00"
            STATUS_SUDAH_BAYAR -> "#60B5FF"
            STATUS_SELESAI -> ""
            else -> "#666666"
        }
    }

    // Function untuk cek apakah button harus ditampilkan
    fun shouldShowButton(): Boolean {
        return status == STATUS_BELUM_BAYAR || status == STATUS_SUDAH_BAYAR
    }
}