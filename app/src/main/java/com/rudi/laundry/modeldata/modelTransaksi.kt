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

        // Language texts untuk model
        private val languageTexts = mapOf(
            "id" to mapOf(
                "status_belum_bayar" to "Belum Dibayar",
                "status_sudah_bayar" to "Sudah Dibayar",
                "status_selesai" to "Selesai",
                "button_bayar" to "Bayar Sekarang",
                "button_ambil" to "Ambil Sekarang",
                "layanan_tambahan" to "Layanan Tambahan",
                "total_bayar" to "Total Bayar"
            ),
            "en" to mapOf(
                "status_belum_bayar" to "Unpaid",
                "status_sudah_bayar" to "Paid",
                "status_selesai" to "Completed",
                "button_bayar" to "Pay Now",
                "button_ambil" to "Pick Up Now",
                "layanan_tambahan" to "Additional Services",
                "total_bayar" to "Total Payment"
            )
        )

        fun getLanguageText(language: String, key: String): String {
            return languageTexts[language]?.get(key) ?: languageTexts["id"]?.get(key) ?: ""
        }
    }

    // Function untuk mendapatkan display status dengan bahasa
    fun getDisplayStatus(language: String = "id"): String {
        return when (status) {
            STATUS_BELUM_BAYAR -> getLanguageText(language, "status_belum_bayar")
            STATUS_SUDAH_BAYAR -> getLanguageText(language, "status_sudah_bayar")
            STATUS_SELESAI -> getLanguageText(language, "status_selesai")
            else -> "Unknown"
        }
    }

    // Function untuk mendapatkan warna status
    fun getStatusColor(): String {
        return when (status) {
            STATUS_BELUM_BAYAR -> "#FFFFFF"
            STATUS_SUDAH_BAYAR -> "#FFFFFF"
            STATUS_SELESAI -> "#FFFFFF"
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

    // Function untuk mendapatkan text button dengan bahasa
    fun getButtonText(language: String = "id"): String {
        return when (status) {
            STATUS_BELUM_BAYAR -> getLanguageText(language, "button_bayar")
            STATUS_SUDAH_BAYAR -> getLanguageText(language, "button_ambil")
            STATUS_SELESAI -> ""
            else -> ""
        }
    }

    // Function untuk mendapatkan warna button
    fun getButtonColor(): String {
        return when (status) {
            STATUS_BELUM_BAYAR -> "#007AFF"
            STATUS_SUDAH_BAYAR -> "#007AFF"
            STATUS_SELESAI -> ""
            else -> "#666666"
        }
    }

    // Function untuk cek apakah button harus ditampilkan
    fun shouldShowButton(): Boolean {
        return status == STATUS_BELUM_BAYAR || status == STATUS_SUDAH_BAYAR
    }

    // Function untuk mendapatkan text layanan tambahan dengan bahasa
    fun getLayananTambahanText(language: String = "id"): String {
        return getLanguageText(language, "layanan_tambahan")
    }

    // Function untuk mendapatkan text total bayar dengan bahasa
    fun getTotalBayarText(language: String = "id"): String {
        return getLanguageText(language, "total_bayar")
    }
}