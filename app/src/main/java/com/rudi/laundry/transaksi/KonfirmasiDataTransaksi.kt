package com.rudi.laundry.transaksi

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
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
import java.util.*

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

        textViewNamaPelanggan = findViewById(R.id.textViewNamaPelanggan)
        textViewNomorHP = findViewById(R.id.textViewNomorHP)
        textViewNamaLayanan = findViewById(R.id.textViewNamaLayanan)
        textViewHargaLayanan = findViewById(R.id.textViewHargaLayanan)
        recyclerViewLayananTambahan = findViewById(R.id.recyclerViewLayananTambahan)
        textViewTotalHarga = findViewById(R.id.textViewTotalHarga)
        buttonBatal = findViewById(R.id.buttonBatal)
        buttonPembayaran = findViewById(R.id.buttonPembayaran)

        setupRecyclerView()
        getDataFromIntent()
        setupButtonListeners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.konfirmasi_transaksi)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        adapterLayananTambahan = AdapterLayananTransaksi(listLayananTambahan) { }
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
        textViewHargaLayanan.text = formatRupiah(extractNumber(hargaLayanan))

        listLayananTambahan.clear()
        listLayananTambahan.addAll(layananTambahanList)
        adapterLayananTambahan.notifyDataSetChanged()

        hitungTotalHarga(hargaLayanan, layananTambahanList)
    }

    private fun hitungTotalHarga(hargaLayanan: String, layananTambahan: ArrayList<modelLayanan>) {
        var total = extractNumber(hargaLayanan)

        for (layanan in layananTambahan) {
            total += extractNumber(layanan.hargaLayanan ?: "0")
        }

        textViewTotalHarga.text = formatRupiah(total)
    }

    private fun extractNumber(hargaString: String): Double {
        val cleaned = hargaString.replace("Rp", "").trim()
        return if (cleaned.contains(",")) {
            cleaned.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
        } else {
            cleaned.toDoubleOrNull() ?: 0.0
        }
    }

    private fun formatRupiah(amount: Double): String {
        val localeID = Locale("in", "ID")
        return NumberFormat.getCurrencyInstance(localeID).format(amount)
    }

    private fun extractHargaFromText(harga: String): String {
        return harga.replace("Rp", "").replace(".", "").replace(",", ".").trim()
    }

    private fun generateRandomId(): String {
        return "-INV" + UUID.randomUUID().toString().take(8)
    }

    private fun setupButtonListeners() {
        buttonBatal.setOnClickListener {
            finish()
        }

        buttonPembayaran.setOnClickListener {
            showPembayaranDialog()
        }
    }

    private fun showPembayaranDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_payment_method)

        val window = dialog.window
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val btnBayarNanti = dialog.findViewById<Button>(R.id.btnBayarNanti)
        val btnTunai = dialog.findViewById<Button>(R.id.btnTunai)
        val btnQris = dialog.findViewById<Button>(R.id.btnQris)
        val btnDana = dialog.findViewById<Button>(R.id.btnDana)
        val btnGopay = dialog.findViewById<Button>(R.id.btnGopay)
        val btnOvo = dialog.findViewById<Button>(R.id.btnOvo)
        val tvBatal = dialog.findViewById<TextView>(R.id.tvBatal)

        val metodePembayaranListener = View.OnClickListener {
            val intent = Intent(this, InvoiceTransaksiActivity::class.java)
            intent.putExtra("nama_pelanggan", textViewNamaPelanggan.text.toString())
            intent.putExtra("nama_layanan", textViewNamaLayanan.text.toString())
            intent.putExtra("harga_layanan", extractNumber(textViewHargaLayanan.text.toString()))
            intent.putExtra("total_bayar", extractNumber(textViewTotalHarga.text.toString()))
            intent.putExtra("layanan_tambahan", listLayananTambahan)
            intent.putExtra("id_transaksi", generateRandomId())

            dialog.dismiss()
            startActivity(intent)
        }

        btnBayarNanti.setOnClickListener(metodePembayaranListener)
        btnTunai.setOnClickListener(metodePembayaranListener)
        btnQris.setOnClickListener(metodePembayaranListener)
        btnDana.setOnClickListener(metodePembayaranListener)
        btnGopay.setOnClickListener(metodePembayaranListener)
        btnOvo.setOnClickListener(metodePembayaranListener)
        tvBatal.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

}
