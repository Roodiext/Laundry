package com.rudi.laundry.transaksi

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.adapter.AdapterLayananTransaksi
import com.rudi.laundry.modeldata.modelLayanan
import java.text.NumberFormat
import java.util.Locale

class KonfirmasiDataTransaksi : AppCompatActivity() {

    private lateinit var textViewNamaPelanggan: TextView
    private lateinit var textViewNomorHP: TextView
    private lateinit var textViewNamaLayanan: TextView
    private lateinit var textViewHargaLayanan: TextView
    private lateinit var recyclerViewLayananTambahan: RecyclerView
    private lateinit var textViewTotalHarga: TextView
    private lateinit var buttonBatal: Button
    private lateinit var buttonPembayaran: Button

    private lateinit var adapterLayananTambahan: AdapterLayananTransaksi
    private val listLayananTambahan = ArrayList<modelLayanan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_konfirmasi_data_transaksi)

        // Inisialisasi view
        textViewNamaPelanggan = findViewById(R.id.textViewNamaPelanggan)
        textViewNomorHP = findViewById(R.id.textViewNomorHP)
        textViewNamaLayanan = findViewById(R.id.textViewNamaLayanan)
        textViewHargaLayanan = findViewById(R.id.textViewHargaLayanan)
        recyclerViewLayananTambahan = findViewById(R.id.recyclerViewLayananTambahan)
        textViewTotalHarga = findViewById(R.id.textViewTotalHarga)
        buttonBatal = findViewById(R.id.buttonBatal)
        buttonPembayaran = findViewById(R.id.buttonPembayaran)

        // Setup RecyclerView
        setupRecyclerView()

        // Ambil data dari intent
        getDataFromIntent()

        // Setup button listener
        setupButtonListeners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.konfirmasi_transaksi)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        // Setup adapter dengan callback kosong karena hanya untuk menampilkan, tidak perlu delete
        adapterLayananTambahan = AdapterLayananTransaksi(listLayananTambahan) { /* tidak perlu action */ }
        recyclerViewLayananTambahan.layoutManager = LinearLayoutManager(this)
        recyclerViewLayananTambahan.adapter = adapterLayananTambahan
    }

    private fun getDataFromIntent() {
        val namaPelanggan = intent.getStringExtra("nama_pelanggan") ?: "Tidak ada data"
        val nomorHP = intent.getStringExtra("nomor_hp") ?: "Tidak ada data"
        val namaLayanan = intent.getStringExtra("nama_layanan") ?: "Tidak ada data"
        val hargaLayanan = intent.getStringExtra("harga_layanan") ?: "Rp0"

        @Suppress("UNCHECKED_CAST")
        val tambahanSerializable = intent.getSerializableExtra("layanan_tambahan")
        val layananTambahanList = tambahanSerializable as? ArrayList<modelLayanan> ?: ArrayList()

        textViewNamaPelanggan.text = namaPelanggan
        textViewNomorHP.text = nomorHP
        textViewNamaLayanan.text = namaLayanan
        textViewHargaLayanan.text = hargaLayanan

        listLayananTambahan.clear()
        listLayananTambahan.addAll(layananTambahanList)
        adapterLayananTambahan.notifyDataSetChanged()

        hitungTotalHarga(hargaLayanan, layananTambahanList)
    }


    private fun hitungTotalHarga(hargaLayanan: String, layananTambahan: ArrayList<modelLayanan>) {
        var total = extractNumber(hargaLayanan)


        for (layanan in layananTambahan) {
            val hargaLayanan: String = intent.getStringExtra("harga_layanan") ?: "Rp0"

        }

        // Format total harga
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        textViewTotalHarga.text = formatRupiah.format(total).replace("Rp", "Rp")
    }

    private fun extractNumber(hargaString: String): Double {
        // Extract angka dari string harga (contoh: "Rp40.000,00" -> 40000.0)
        val regex = Regex("[^0-9]")
        val numberString = regex.replace(hargaString, "")
        return if (numberString.isNotEmpty()) {
            numberString.toDouble()
        } else {
            0.0
        }
    }

    private fun setupButtonListeners() {
        buttonBatal.setOnClickListener {
            // Kembali ke halaman sebelumnya
            finish()
        }

        buttonPembayaran.setOnClickListener {
            // Proses pembayaran (bisa dibuat intent ke activity pembayaran nantinya)
            Toast.makeText(this, "Proses pembayaran", Toast.LENGTH_SHORT).show()
            // Contoh: Intent ke activity pembayaran
            // val intent = Intent(this, PembayaranActivity::class.java)
            // intent.putExtra("total_harga", textViewTotalHarga.text.toString())
            // startActivity(intent)
        }
    }
}