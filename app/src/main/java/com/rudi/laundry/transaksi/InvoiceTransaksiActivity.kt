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

    private val listTambahan = ArrayList<modelLayanan>()
    private lateinit var adapter: AdapterLayananTransaksi

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private val printerMAC = "DC:0D:51:A7:FF:7A"
    // Ganti dengan alamat MAC printermu
    private val printerUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    // Data cabang dan pegawai
    private var cabangNama: String = ""
    private var cabangAlamat: String = ""
    private var pegawaiNama: String = ""
    private var metodePembayaran: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice_transaksi)

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

        // Inisialisasi views untuk cabang dan pegawai
        tvCabangInvoice = findViewById(R.id.tvCabangInvoice)
        tvAlamatCabang = findViewById(R.id.tvAlamatCabang)
        tvNamaKaryawan = findViewById(R.id.tvNamaKaryawan)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 100)
        }

        setupRecyclerView()
        loadDataFromIntent()
        setupPrintButton()
        setupWhatsappButton()
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
        metodePembayaran = intent.getStringExtra("metode_pembayaran") ?: "Tunai"

        @Suppress("UNCHECKED_CAST")
        val tambahan = intent.getSerializableExtra("layanan_tambahan") as? ArrayList<modelLayanan> ?: arrayListOf()

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val tanggal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        tvTanggal.text = tanggal
        tvIdTransaksi.text = idTransaksi
        tvNamaPelanggan.text = namaPelanggan
        tvLayananUtama.text = namaLayanan
        tvHargaLayanan.text = formatter.format(hargaLayanan)

        // Set data cabang dan pegawai ke views
        tvCabangInvoice.text = cabangNama
        tvAlamatCabang.text = cabangAlamat
        tvNamaKaryawan.text = pegawaiNama

        listTambahan.clear()
        listTambahan.addAll(tambahan)
        adapter.notifyDataSetChanged()

        val subtotal = tambahan.sumOf { extractHargaFromString(it.hargaLayanan ?: "0") }
        tvSubtotalTambahan.text = formatter.format(subtotal)
        tvTotalBayar.text = formatter.format(totalBayar)
    }

    private fun extractHargaFromString(harga: String): Double {
        val cleaned = harga.replace("Rp", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    private fun setupPrintButton() {
        btnCetak.setOnClickListener {
            val message = buildString {
                val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

                append("\nAthh Laundry\n")
                append("$cabangNama\n")
                if (cabangAlamat.isNotEmpty()) {
                    append("$cabangAlamat\n")
                }
                append("============================\n")
                append("ID: ${tvIdTransaksi.text}\n")
                append("Tanggal: ${tvTanggal.text}\n")
                append("Pelanggan: ${tvNamaPelanggan.text}\n")
                append("Karyawan: $pegawaiNama\n")
                append("Pembayaran: $metodePembayaran\n")
                append("----------------------------\n")

                val namaUtama = tvLayananUtama.text.toString().take(20).padEnd(20)
                val hargaUtama = formatter.format(extractHargaFromString(tvHargaLayanan.text.toString())).padStart(12)
                append("$namaUtama$hargaUtama\n\n")

                // Rincian Tambahan
                if (listTambahan.isNotEmpty()) {
                    append("Rincian Tambahan:\n")
                    listTambahan.forEachIndexed { index, item ->
                        val nama = "${index + 1}. ${item.namaLayanan}".take(20).padEnd(20)
                        val harga = "Rp${extractHargaFromString(item.hargaLayanan ?: "0").toInt()}".padStart(12)
                        append("$nama$harga\n")
                    }
                    append("\n")
                }

                append("----------------------------\n")
                if (listTambahan.isNotEmpty()) {
                    val subtotalFormatted = formatter.format(extractHargaFromString(tvSubtotalTambahan.text.toString()))
                    append("Subtotal Tmb:       $subtotalFormatted\n")
                }
                val totalFormatted = formatter.format(extractHargaFromString(tvTotalBayar.text.toString()))
                append("TOTAL BAYAR:        $totalFormatted\n")
                append("============================\n")
                append("Terima kasih telah memilih\n")
                append("Athh Laundry\n")
                append("$cabangNama\n")
                append("\n\n\n")
            }
            printToBluetooth(message)
        }
    }

    private fun setupWhatsappButton() {
        btnKirimWhatsapp.setOnClickListener {
            val message = buildString {
                append("*Hai Halo* ${tvNamaPelanggan.text} 👋\n\n")
                append("*Invoice Athh Laundry*\n")
                append("🏪 Cabang: $cabangNama\n")
                if (cabangAlamat.isNotEmpty()) {
                    append("📍 Alamat: $cabangAlamat\n")
                }
                append("👤 Dilayani oleh: $pegawaiNama\n")
                append("💳 Pembayaran: $metodePembayaran\n\n")

                append("*Berikut rincian laundry Anda:*\n")
                append("• Layanan Utama: ${tvLayananUtama.text}\n")
                append("• Harga: ${tvHargaLayanan.text}\n\n")

                if (listTambahan.isNotEmpty()) {
                    append("*Rincian Tambahan:*\n")
                    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

                    listTambahan.forEachIndexed { index, item ->
                        val hargaFormatted = formatter.format(extractHargaFromString(item.hargaLayanan ?: "0"))
                        append("${index + 1}. ${item.namaLayanan} - $hargaFormatted\n")
                    }
                    append("\n")
                }

                append("*Total Bayar:* ${tvTotalBayar.text}\n\n")
                append("Terima kasih telah menggunakan layanan Athh Laundry 💟")
            }

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/?text=" + Uri.encode(message))
            startActivity(intent)
        }
    }

    private fun printToBluetooth(text: String) {
        Thread {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this, "Izin Bluetooth diperlukan untuk mencetak", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Berhasil mencetak invoice", Toast.LENGTH_SHORT).show()
                }

                outputStream?.close()
                bluetoothSocket?.close()
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "Gagal mencetak: ${e.message}", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }
        }.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Izin Bluetooth diberikan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Izin Bluetooth ditolak", Toast.LENGTH_SHORT).show()
        }
    }
}