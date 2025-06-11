package com.rudi.laundry.transaksi

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.adapter.AdapterLayananTransaksi
import com.rudi.laundry.modeldata.modelLayanan
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID

class InvoiceTransaksiActivity : AppCompatActivity() {

    private lateinit var tvTanggal: TextView
    private lateinit var tvIdTransaksi: TextView
    private lateinit var tvNamaPelanggan: TextView
    private lateinit var tvLayananUtama: TextView
    private lateinit var tvHargaLayanan: TextView
    private lateinit var rvTambahan: RecyclerView
    private lateinit var tvSubtotalTambahan: TextView
    private lateinit var tvTotalBayar: TextView
    private lateinit var btnCetak: Button
    private lateinit var btnKirimWhatsapp: Button

    // Tambahan untuk cabang dan pegawai
    private lateinit var tvCabangInvoice: TextView
    private lateinit var tvAlamatCabang: TextView
    private lateinit var tvNamaKaryawan: TextView
    // Tambahan TextView untuk metode pembayaran
    private lateinit var tvMetodePembayaran: TextView

    // TextViews untuk label yang perlu diterjemahkan - sesuaikan dengan XML
    private lateinit var tvTitle: TextView
    private lateinit var tvInvoice: TextView
    private lateinit var tvLabelDetailTransaksi: TextView
    private lateinit var tvLabelIdTransaksi: TextView
    private lateinit var tvLabelTanggal: TextView
    private lateinit var tvLabelPelanggan: TextView
    private lateinit var tvLabelKaryawan: TextView
    private lateinit var tvLabelLayananUtama: TextView
    private lateinit var tvLabelRincianTambahan: TextView
    private lateinit var tvLabelSubtotalTambahan: TextView
    private lateinit var tvLabelTotalBayar: TextView
    private lateinit var tvFooterText: TextView
    // Tambahan label untuk metode pembayaran
    private lateinit var tvLabelMetodePembayaran: TextView

    private val listTambahan = ArrayList<modelLayanan>()
    private lateinit var adapter: AdapterLayananTransaksi

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private val printerMAC = "DC:0D:51:A7:FF:7A"
    private val printerUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    // Data cabang dan pegawai
    private var cabangNama: String = ""
    private var cabangAlamat: String = ""
    private var pegawaiNama: String = ""
    private var metodePembayaran: String = ""

    // Bahasa
    private var currentLanguage = "id"

    // Language texts untuk Invoice
    private val languageTexts = mapOf(
        "id" to mapOf(
            "invoice_title" to "Laundry SukaMakmur",
            "invoice_badge" to "INVOICE",
            "detail_transaction" to "Detail Transaksi",
            "transaction_id" to "ID Transaksi",
            "date" to "Tanggal",
            "customer" to "Pelanggan",
            "employee" to "Karyawan",
            "payment_method" to "Metode Pembayaran",
            "cash" to "Tunai",
            "transfer" to "Transfer",
            "credit_card" to "Kartu Kredit",
            "debit_card" to "Kartu Debit",
            "ewallet" to "E-Wallet",
            "main_service" to "Layanan Utama",
            "additional_details" to "Rincian Tambahan",
            "additional_subtotal" to "Subtotal Tambahan",
            "total_payment" to "Total Bayar",
            "print" to "🖨️ Cetak",
            "send_whatsapp" to "📱Kirim WhatsApp",
            "footer_text" to "Terima kasih telah menggunakan layanan Laundry SukaMakmur 💙",
            "print_success" to "Berhasil mencetak invoice",
            "print_failed" to "Gagal mencetak",
            "bluetooth_permission_needed" to "Izin Bluetooth diperlukan untuk mencetak",
            "bluetooth_permission_granted" to "Izin Bluetooth diberikan",
            "bluetooth_permission_denied" to "Izin Bluetooth ditolak",
            "whatsapp_greeting" to "*Hai Halo*",
            "whatsapp_invoice_title" to "*Invoice SukaMakmur Laundry*",
            "whatsapp_branch" to "🏪 Cabang:",
            "whatsapp_address" to "📍 Alamat:",
            "whatsapp_served_by" to "👤 Dilayani oleh:",
            "whatsapp_payment" to "💳 Pembayaran:",
            "whatsapp_service_details" to "*Berikut rincian laundry Anda:*",
            "whatsapp_main_service" to "• Layanan Utama:",
            "whatsapp_price" to "• Harga:",
            "whatsapp_additional_details" to "*Rincian Tambahan:*",
            "whatsapp_total_payment" to "*Total Bayar:*",
            "whatsapp_footer" to "Terima kasih telah menggunakan layanan SukaMakmur Laundry 💟",
            "print_header" to "SukaMakmur Laundry",
            "print_id" to "ID:",
            "print_date" to "Tanggal:",
            "print_customer" to "Pelanggan:",
            "print_employee" to "Karyawan:",
            "print_payment" to "Pembayaran:",
            "print_additional_details" to "Rincian Tambahan:",
            "print_additional_subtotal" to "Subtotal Tmb:",
            "print_total_payment" to "TOTAL BAYAR:",
            "print_footer_thanks" to "Terima kasih telah memilih"
        ),
        "en" to mapOf(
            "invoice_title" to "SukaMakmur Laundry",
            "invoice_badge" to "INVOICE",
            "detail_transaction" to "Transaction Details",
            "transaction_id" to "Transaction ID",
            "date" to "Date",
            "customer" to "Customer",
            "employee" to "Employee",
            "payment_method" to "Payment Method",
            "cash" to "Cash",
            "transfer" to "Bank Transfer",
            "credit_card" to "Credit Card",
            "debit_card" to "Debit Card",
            "ewallet" to "E-Wallet",
            "main_service" to "Main Service",
            "additional_details" to "Additional Details",
            "additional_subtotal" to "Additional Subtotal",
            "total_payment" to "Total Payment",
            "print" to "🖨️ Print",
            "send_whatsapp" to "📱Send WhatsApp",
            "footer_text" to "Thank you for using SukaMakmur Laundry services 💙",
            "print_success" to "Invoice printed successfully",
            "print_failed" to "Failed to print",
            "bluetooth_permission_needed" to "Bluetooth permission required for printing",
            "bluetooth_permission_granted" to "Bluetooth permission granted",
            "bluetooth_permission_denied" to "Bluetooth permission denied",
            "whatsapp_greeting" to "*Hello*",
            "whatsapp_invoice_title" to "*SukaMakmur Laundry Invoice*",
            "whatsapp_branch" to "🏪 Branch:",
            "whatsapp_address" to "📍 Address:",
            "whatsapp_served_by" to "👤 Served by:",
            "whatsapp_payment" to "💳 Payment:",
            "whatsapp_service_details" to "*Here are your laundry details:*",
            "whatsapp_main_service" to "• Main Service:",
            "whatsapp_price" to "• Price:",
            "whatsapp_additional_details" to "*Additional Details:*",
            "whatsapp_total_payment" to "*Total Payment:*",
            "whatsapp_footer" to "Thank you for using SukaMakmur Laundry services 💟",
            "print_header" to "Athh Laundry",
            "print_id" to "ID:",
            "print_date" to "Date:",
            "print_customer" to "Customer:",
            "print_employee" to "Employee:",
            "print_payment" to "Payment:",
            "print_additional_details" to "Additional Details:",
            "print_additional_subtotal" to "Additional Sub:",
            "print_total_payment" to "TOTAL PAYMENT:",
            "print_footer_thanks" to "Thank you for choosing"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice_transaksi)

        // Load bahasa yang tersimpan
        loadLanguageSetting()

        initViews()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 100)
        }

        setupRecyclerView()
        loadDataFromIntent()
        setupPrintButton()
        setupWhatsappButton()
        updateLanguageTexts()
    }

    private fun initViews() {
        // Data TextViews
        tvTanggal = findViewById(R.id.tvTanggal)
        tvIdTransaksi = findViewById(R.id.tvIdTransaksi)
        tvNamaPelanggan = findViewById(R.id.tvNamaPelanggan)
        tvLayananUtama = findViewById(R.id.tvLayananUtama)
        tvHargaLayanan = findViewById(R.id.tvHargaLayanan)
        rvTambahan = findViewById(R.id.rvRincianTambahan)
        tvSubtotalTambahan = findViewById(R.id.tvSubtotalTambahan)
        tvTotalBayar = findViewById(R.id.tvTotalBayar)
        btnCetak = findViewById(R.id.btnCetak)
        btnKirimWhatsapp = findViewById(R.id.btnKirimWhatsapp)

        // Views untuk cabang dan pegawai
        tvCabangInvoice = findViewById(R.id.tvCabangInvoice)
        tvAlamatCabang = findViewById(R.id.tvAlamatCabang)
        tvNamaKaryawan = findViewById(R.id.tvNamaKaryawan)
        // Inisialisasi TextView metode pembayaran
        tvMetodePembayaran = findViewById(R.id.tvMetodePembayaran)

        // Label TextViews yang perlu diterjemahkan - sesuai dengan ID di XML
        tvTitle = findViewById(R.id.tvTitle)
        tvInvoice = findViewById(R.id.tvinvoice)
        tvLabelDetailTransaksi = findViewById(R.id.tvLabelDetailTransaksi)
        tvLabelIdTransaksi = findViewById(R.id.tvLabelIdTransaksi)
        tvLabelTanggal = findViewById(R.id.tvLabelTanggal)
        tvLabelPelanggan = findViewById(R.id.tvLabelPelanggan)
        tvLabelKaryawan = findViewById(R.id.tvLabelKaryawan)
        tvLabelLayananUtama = findViewById(R.id.tvLabelLayananUtama)
        tvLabelRincianTambahan = findViewById(R.id.tvLabelRincianTambahan)
        tvLabelSubtotalTambahan = findViewById(R.id.tvLabelSubtotalTambahan)
        tvLabelTotalBayar = findViewById(R.id.tvLabelTotalBayar)
        tvFooterText = findViewById(R.id.tvFooterText)
        // Inisialisasi label metode pembayaran
        tvLabelMetodePembayaran = findViewById(R.id.tvLabelMetodePembayaran)
    }

    private fun loadLanguageSetting() {
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"
    }

    private fun updateLanguageTexts() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Update semua label texts
        tvTitle.text = texts["invoice_title"]
        tvInvoice.text = texts["invoice_badge"]
        tvLabelDetailTransaksi.text = texts["detail_transaction"]
        tvLabelIdTransaksi.text = texts["transaction_id"]
        tvLabelTanggal.text = texts["date"]
        tvLabelPelanggan.text = texts["customer"]
        tvLabelKaryawan.text = texts["employee"]
        tvLabelMetodePembayaran.text = texts["payment_method"]
        tvLabelLayananUtama.text = texts["main_service"]
        tvLabelRincianTambahan.text = texts["additional_details"]
        tvLabelSubtotalTambahan.text = texts["additional_subtotal"]
        tvLabelTotalBayar.text = texts["total_payment"]
        tvFooterText.text = texts["footer_text"]

        // Update button texts
        btnCetak.text = texts["print"]
        btnKirimWhatsapp.text = texts["send_whatsapp"]
    }

    private fun setupRecyclerView() {
        adapter = AdapterLayananTransaksi(listTambahan) { }
        rvTambahan.layoutManager = LinearLayoutManager(this)
        rvTambahan.adapter = adapter
    }

    private fun loadDataFromIntent() {
        val namaPelanggan = intent.getStringExtra("nama_pelanggan") ?: "-"
        val namaLayanan = intent.getStringExtra("nama_layanan") ?: "-"
        val hargaLayanan = intent.getDoubleExtra("harga_layanan", 0.0)
        val totalBayar = intent.getDoubleExtra("total_bayar", 0.0)
        val idTransaksi = intent.getStringExtra("id_transaksi") ?: UUID.randomUUID().toString()

        // Ambil data cabang dan pegawai
        cabangNama = intent.getStringExtra("cabang_nama") ?: "Manahan"
        cabangAlamat = intent.getStringExtra("cabang_alamat") ?: ""
        pegawaiNama = intent.getStringExtra("pegawai_nama") ?: "Admin"

        // Ambil metode pembayaran dari intent dan terjemahkan
        val paymentMethodFromIntent = intent.getStringExtra("metode_pembayaran") ?: "Tunai"
        metodePembayaran = translatePaymentMethod(paymentMethodFromIntent)

        @Suppress("UNCHECKED_CAST")
        val tambahan = intent.getSerializableExtra("layanan_tambahan") as? ArrayList<modelLayanan> ?: arrayListOf()

        val locale = if (currentLanguage == "en") Locale.US else Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(locale)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", if (currentLanguage == "en") Locale.ENGLISH else Locale("in", "ID"))
        val tanggal = dateFormat.format(Date())

        tvTanggal.text = tanggal
        tvIdTransaksi.text = idTransaksi
        tvNamaPelanggan.text = namaPelanggan
        tvLayananUtama.text = namaLayanan
        tvHargaLayanan.text = formatter.format(hargaLayanan)

        // Set data cabang dan pegawai ke views
        tvCabangInvoice.text = cabangNama
        tvAlamatCabang.text = cabangAlamat
        tvNamaKaryawan.text = pegawaiNama
        // Set metode pembayaran yang sudah diterjemahkan
        tvMetodePembayaran.text = metodePembayaran

        listTambahan.clear()
        listTambahan.addAll(tambahan)
        adapter.notifyDataSetChanged()

        val subtotal = tambahan.sumOf { extractHargaFromString(it.hargaLayanan ?: "0") }
        tvSubtotalTambahan.text = formatter.format(subtotal)
        tvTotalBayar.text = formatter.format(totalBayar)
    }

    private fun translatePaymentMethod(paymentMethod: String): String {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        return when (paymentMethod.lowercase()) {
            "tunai", "cash" -> texts["cash"] ?: "Tunai"
            "transfer", "bank transfer" -> texts["transfer"] ?: "Transfer"
            "kartu kredit", "credit card" -> texts["credit_card"] ?: "Kartu Kredit"
            "kartu debit", "debit card" -> texts["debit_card"] ?: "Kartu Debit"
            "e-wallet", "ewallet" -> texts["ewallet"] ?: "E-Wallet"
            else -> paymentMethod // Jika tidak ada terjemahan, kembalikan asli
        }
    }

    private fun getLocalizedText(key: String, defaultText: String): String {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
        return texts[key] ?: defaultText
    }

    private fun extractHargaFromString(harga: String): Double {
        val cleaned = harga.replace("Rp", "")
            .replace("IDR.", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    private fun setupPrintButton() {
        btnCetak.setOnClickListener {
            val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

            val message = buildString {
                // Gunakan formatter custom untuk mengganti $ dengan IDR
                val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                if (currentLanguage == "en") {
                    formatter.currency = Currency.getInstance("IDR")
                }

                // Function untuk center text dengan lebar 32 karakter (standar thermal printer)
                fun centerText(text: String, width: Int = 32): String {
                    return if (text.length >= width) {
                        text.take(width)
                    } else {
                        val padding = (width - text.length) / 2
                        " ".repeat(padding) + text + " ".repeat(width - text.length - padding)
                    }
                }

                // Ambil data asli dari intent untuk mendapatkan nilai yang akurat
                val hargaLayananAsli = intent.getDoubleExtra("harga_layanan", 0.0)
                val totalBayarAsli = intent.getDoubleExtra("total_bayar", 0.0)

                append("\n")
                append(centerText(texts["print_header"] ?: "SukaMakmur Laundry") + "\n")
                append(centerText(cabangNama) + "\n")
                if (cabangAlamat.isNotEmpty()) {
                    append(centerText(cabangAlamat) + "\n")
                }
                append("============================\n")
                append("${texts["print_id"]} ${tvIdTransaksi.text}\n")
                append("${texts["print_date"]} ${tvTanggal.text}\n")
                append("${texts["print_customer"]} ${tvNamaPelanggan.text}\n")
                append("${texts["print_employee"]} $pegawaiNama\n")
                append("${texts["print_payment"]} $metodePembayaran\n")
                append("----------------------------\n")

                val namaUtama = tvLayananUtama.text.toString().take(20).padEnd(20)
                val hargaUtamaFormatted = formatter.format(hargaLayananAsli).padStart(12)
                append("$namaUtama$hargaUtamaFormatted\n\n")

                // Rincian Tambahan
                if (listTambahan.isNotEmpty()) {
                    append("${texts["print_additional_details"]}\n")
                    listTambahan.forEachIndexed { index, item ->
                        val nama = "${index + 1}. ${item.namaLayanan}".take(20).padEnd(20)
                        val hargaItem = extractHargaFromString(item.hargaLayanan ?: "0")
                        val hargaFormatted = formatter.format(hargaItem).padStart(12)
                        append("$nama$hargaFormatted\n")
                    }
                    append("\n")
                }

                append("----------------------------\n")
                if (listTambahan.isNotEmpty()) {
                    val subtotalTambahan = listTambahan.sumOf { extractHargaFromString(it.hargaLayanan ?: "0") }
                    val subtotalFormatted = formatter.format(subtotalTambahan)
                    append("${texts["print_additional_subtotal"]}       $subtotalFormatted\n")
                }
                val totalFormatted = formatter.format(totalBayarAsli)
                append("${texts["print_total_payment"]}        $totalFormatted\n")
                append("============================\n")
                append(centerText(texts["print_footer_thanks"] ?: "Terima kasih telah memilih") + "\n")
                append(centerText(texts["print_header"] ?: "SukaMakmur Laundry") + "\n")
                append(centerText(cabangNama) + "\n")
                append("\n\n\n")
            }
            printToBluetooth(message)
        }
    }

    private fun setupWhatsappButton() {
        btnKirimWhatsapp.setOnClickListener {
            val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

            val message = buildString {
                append("${texts["whatsapp_greeting"]} ${tvNamaPelanggan.text} 👋\n\n")
                append("${texts["whatsapp_invoice_title"]}\n")
                append("${texts["whatsapp_branch"]} $cabangNama\n")
                if (cabangAlamat.isNotEmpty()) {
                    append("${texts["whatsapp_address"]} $cabangAlamat\n")
                }
                append("${texts["whatsapp_served_by"]} $pegawaiNama\n")
                append("${texts["whatsapp_payment"]} $metodePembayaran\n\n")

                append("${texts["whatsapp_service_details"]}\n")
                append("${texts["whatsapp_main_service"]} ${tvLayananUtama.text}\n")
                append("${texts["whatsapp_price"]} ${tvHargaLayanan.text}\n\n")

                if (listTambahan.isNotEmpty()) {
                    append("${texts["whatsapp_additional_details"]}\n")
                    val locale = if (currentLanguage == "en") Locale.US else Locale("in", "ID")
                    val formatter = NumberFormat.getCurrencyInstance(locale)

                    listTambahan.forEachIndexed { index, item ->
                        val hargaFormatted = formatter.format(extractHargaFromString(item.hargaLayanan ?: "0"))
                        append("${index + 1}. ${item.namaLayanan} - $hargaFormatted\n")
                    }
                    append("\n")
                }

                append("${texts["whatsapp_total_payment"]} ${tvTotalBayar.text}\n\n")
                append("${texts["whatsapp_footer"]}")
            }

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/?text=" + Uri.encode(message))
            startActivity(intent)
        }
    }

    private fun printToBluetooth(text: String) {
        Thread {
            try {
                val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this, texts["bluetooth_permission_needed"], Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(printerMAC)
                bluetoothSocket = device?.createRfcommSocketToServiceRecord(printerUUID)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream

                outputStream?.write(text.toByteArray())
                outputStream?.flush()

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, texts["print_success"], Toast.LENGTH_SHORT).show()
                }

                outputStream?.close()
                bluetoothSocket?.close()
            } catch (e: Exception) {
                val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "${texts["print_failed"]}: ${e.message}", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }
        }.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, texts["bluetooth_permission_granted"], Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, texts["bluetooth_permission_denied"], Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Cek ulang pengaturan bahasa saat activity kembali aktif
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        val newLanguage = sharedPref.getString("selected_language", "id") ?: "id"

        if (newLanguage != currentLanguage) {
            currentLanguage = newLanguage
            updateLanguageTexts()
            // Refresh data dengan bahasa baru
            loadDataFromIntent()
        }
    }
}