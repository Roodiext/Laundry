package com.rudi.laundry.transaksi

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.adapter.AdapterLayananTransaksi
import com.rudi.laundry.modeldata.modelLayanan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class InvoiceTransaksiActivity : AppCompatActivity() {

    private lateinit var tvTanggal: TextView
    private lateinit var tvIdTransaksi: TextView
    private lateinit var tvNamaPelanggan: TextView
    private lateinit var tvLayananUtama: TextView
    private lateinit var tvHargaLayanan: TextView
    private lateinit var rvTambahan: RecyclerView
    private lateinit var tvSubtotalTambahan: TextView
    private lateinit var tvTotalBayar: TextView

    private val listTambahan = ArrayList<modelLayanan>()
    private lateinit var adapter: AdapterLayananTransaksi

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

        setupRecyclerView()
        loadDataFromIntent()
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

        @Suppress("UNCHECKED_CAST")
        val tambahan = intent.getSerializableExtra("layanan_tambahan") as? ArrayList<modelLayanan> ?: arrayListOf()

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val tanggal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        tvTanggal.text = tanggal
        tvIdTransaksi.text = idTransaksi
        tvNamaPelanggan.text = namaPelanggan
        tvLayananUtama.text = namaLayanan
        tvHargaLayanan.text = formatter.format(hargaLayanan)

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
}
