package com.rudi.laundry.transaksi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.Layanan.PilihLayananActivity
import com.rudi.laundry.LayananTambahan.PilihLayananTambahan
import com.rudi.laundry.adapter.AdapterLayananTransaksi
import com.rudi.laundry.modeldata.modelLayanan
import com.rudi.laundry.pelanggan.PilihPelangganActivity
import java.io.Serializable

class transaksi : AppCompatActivity() {

    private lateinit var namaPelangganText: TextView
    private lateinit var noHPPelangganText: TextView
    private lateinit var namaLayananText: TextView
    private lateinit var hargaLayananText: TextView
    private lateinit var rvLayananTambahan: RecyclerView
    private lateinit var buttonProses: Button
    private lateinit var adapterTambahan: AdapterLayananTransaksi
    private lateinit var emptyState: LinearLayout
    private lateinit var countLayananTambahan: TextView
    private lateinit var emptyStateText: TextView
    private lateinit var emptyStateSubText: TextView
    private lateinit var titleDataPelangganText: TextView
    private lateinit var titleLayananUtamaText: TextView
    private lateinit var labelLayananTambahanText: TextView
    private lateinit var btnPilihPelangganText: Button
    private lateinit var btnPilihLayananText: Button
    private lateinit var btnTambahanText: Button

    private val listTambahan = mutableListOf<modelLayanan>()
    private var currentLanguage = "id" // Default bahasa Indonesia

    // Untuk menyimpan data pelanggan dan layanan
    private var namaPelanggan: String = ""
    private var nomorHP: String = ""
    private var namaLayanan: String = ""
    private var hargaLayanan: String = ""

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "customer_data" to "👤 Data Pelanggan",
            "main_service" to "⭐ Layanan Utama",
            "additional_services" to "➕ Layanan Tambahan",
            "customer_name" to "Nama Pelanggan",
            "phone_number" to "No HP",
            "service_name" to "Nama Layanan",
            "price" to "Harga",
            "no_additional_services" to "Belum ada layanan tambahan",
            "tap_to_add" to "Tap tombol 'Tambahan' untuk menambahkan",
            "choose_customer" to "Pilih Pelanggan",
            "choose_service" to "Pilih Layanan",
            "additional" to "➕ Tambahan",
            "process" to "➤ Proses",
            "item" to "item",
            "please_choose_customer" to "Silakan pilih pelanggan terlebih dahulu",
            "please_choose_service" to "Silakan pilih layanan utama terlebih dahulu",
            "adding" to "Menambahkan",
            "invalid_additional_data" to "Data layanan tambahan tidak valid",
            "removed" to "dihapus"
        ),
        "en" to mapOf(
            "customer_data" to "👤 Customer Data",
            "main_service" to "⭐ Main Service",
            "additional_services" to "➕ Additional Services",
            "customer_name" to "Customer Name",
            "phone_number" to "Phone Number",
            "service_name" to "Service Name",
            "price" to "Price",
            "no_additional_services" to "No additional services yet",
            "tap_to_add" to "Tap 'Additional' button to add",
            "choose_customer" to "Choose Customer",
            "choose_service" to "Choose Service",
            "additional" to "➕ Additional",
            "process" to "➤ Process",
            "item" to "item",
            "please_choose_customer" to "Please choose a customer first",
            "please_choose_service" to "Please choose a main service first",
            "adding" to "Adding",
            "invalid_additional_data" to "Invalid additional service data",
            "removed" to "removed"
        )
    )

    private val pilihPelangganLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            namaPelanggan = data?.getStringExtra("nama") ?: ""
            nomorHP = data?.getStringExtra("hp") ?: ""
            updateCustomerDisplay()
        }
    }

    private val pilihLayananLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            namaLayanan = data?.getStringExtra("nama") ?: ""
            hargaLayanan = data?.getStringExtra("harga") ?: ""
            updateServiceDisplay()
        }
    }

    private val pilihTambahanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val nama = data?.getStringExtra("namaTambahan")
            val harga = data?.getStringExtra("hargaTambahan")

            if (nama != null && harga != null) {
                val layanan = modelLayanan(namaLayanan = nama, hargaLayanan = harga)
                listTambahan.add(layanan)
                adapterTambahan.notifyDataSetChanged()

                // Update empty state setelah menambah item
                updateEmptyState()

                val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
                Toast.makeText(this, "${texts["adding"]}: $nama - $harga", Toast.LENGTH_SHORT).show()
            } else {
                val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
                Toast.makeText(this, texts["invalid_additional_data"], Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaksi)

        // Load saved language preference
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"

        // Inisialisasi semua view
        initViews()
        setupRecyclerView()
        updateEmptyState()
        updateAllTexts()

        // Setup tombol proses
        buttonProses.setOnClickListener {
            if (validateData()) {
                // Kirim data ke KonfirmasiDataTransaksi
                val intent = Intent(this@transaksi, KonfirmasiDataTransaksi::class.java)
                intent.putExtra("nama_pelanggan", namaPelanggan)
                intent.putExtra("nomor_hp", nomorHP)
                intent.putExtra("nama_layanan", namaLayanan)
                intent.putExtra("harga_layanan", hargaLayanan)
                intent.putExtra("layanan_tambahan", ArrayList(listTambahan) as Serializable)
                startActivity(intent)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transaksi)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        namaPelangganText = findViewById(R.id.label_nama)
        noHPPelangganText = findViewById(R.id.label_hp)
        namaLayananText = findViewById(R.id.label_layanan)
        hargaLayananText = findViewById(R.id.label_harga)
        rvLayananTambahan = findViewById(R.id.rvLayananTambahan)
        buttonProses = findViewById(R.id.btn_proses)
        emptyState = findViewById(R.id.empty_state)
        countLayananTambahan = findViewById(R.id.count_layanan_tambahan)

        // Inisialisasi TextView berdasarkan layout XML yang sebenarnya
        titleDataPelangganText = findViewById(R.id.title_data_pelanggan)
        titleLayananUtamaText = findViewById(R.id.title_layanan_utama)
        labelLayananTambahanText = findViewById(R.id.label_layanan_tambahan)
        btnPilihPelangganText = findViewById(R.id.btn_pilih_pelanggan)
        btnPilihLayananText = findViewById(R.id.btn_pilih_layanan)
        btnTambahanText = findViewById(R.id.btn_tambahan)

        // Untuk empty state text, kita perlu mengakses TextView di dalam empty_state LinearLayout
        // Karena tidak ada ID khusus, kita akses berdasarkan index
        val emptyStateLayout = findViewById<LinearLayout>(R.id.empty_state)
        emptyStateText = emptyStateLayout.getChildAt(1) as TextView // TextView kedua (index 1)
        emptyStateSubText = emptyStateLayout.getChildAt(2) as TextView // TextView ketiga (index 2)
    }

    private fun updateAllTexts() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Update section titles
        titleDataPelangganText.text = texts["customer_data"]
        titleLayananUtamaText.text = texts["main_service"]
        labelLayananTambahanText.text = texts["additional_services"]

        // Update button texts
        btnPilihPelangganText.text = texts["choose_customer"]
        btnPilihLayananText.text = texts["choose_service"]
        btnTambahanText.text = texts["additional"]
        buttonProses.text = texts["process"]

        // Update empty state texts
        emptyStateText.text = texts["no_additional_services"]
        emptyStateSubText.text = texts["tap_to_add"]

        // Update dynamic texts
        updateCustomerDisplay()
        updateServiceDisplay()
        updateEmptyState()
    }

    private fun updateCustomerDisplay() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
        if (namaPelanggan.isNotEmpty()) {
            namaPelangganText.text = "${texts["customer_name"]}: $namaPelanggan"
            noHPPelangganText.text = "${texts["phone_number"]}: $nomorHP"
        } else {
            namaPelangganText.text = "${texts["customer_name"]}: -"
            noHPPelangganText.text = "${texts["phone_number"]}: -"
        }
    }

    private fun updateServiceDisplay() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
        if (namaLayanan.isNotEmpty()) {
            namaLayananText.text = "${texts["service_name"]}: $namaLayanan"
            hargaLayananText.text = "${texts["price"]}: $hargaLayanan"
        } else {
            namaLayananText.text = "${texts["service_name"]}: -"
            hargaLayananText.text = "${texts["price"]}: -"
        }
    }

    private fun validateData(): Boolean {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        if (namaPelanggan.isEmpty() || nomorHP.isEmpty()) {
            Toast.makeText(this, texts["please_choose_customer"], Toast.LENGTH_SHORT).show()
            return false
        }

        if (namaLayanan.isEmpty() || hargaLayanan.isEmpty()) {
            Toast.makeText(this, texts["please_choose_service"], Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun setupRecyclerView() {
        adapterTambahan = AdapterLayananTransaksi(listTambahan) { layanan ->
            listTambahan.remove(layanan)
            adapterTambahan.notifyDataSetChanged()

            // Update empty state setelah menghapus item
            updateEmptyState()

            val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
            Toast.makeText(this, "${layanan.namaLayanan} ${texts["removed"]}", Toast.LENGTH_SHORT).show()
        }

        rvLayananTambahan.layoutManager = LinearLayoutManager(this)
        rvLayananTambahan.adapter = adapterTambahan
    }

    // Method untuk mengupdate empty state
    private fun updateEmptyState() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        if (listTambahan.isEmpty()) {
            // Jika list kosong, tampilkan empty state dan sembunyikan RecyclerView
            emptyState.visibility = View.VISIBLE
            rvLayananTambahan.visibility = View.GONE
            countLayananTambahan.text = "0 ${texts["item"]}"
        } else {
            // Jika ada item, sembunyikan empty state dan tampilkan RecyclerView
            emptyState.visibility = View.GONE
            rvLayananTambahan.visibility = View.VISIBLE
            countLayananTambahan.text = "${listTambahan.size} ${texts["item"]}"
        }
    }

    override fun onResume() {
        super.onResume()
        // Cek apakah bahasa berubah ketika kembali ke activity ini
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("selected_language", "id") ?: "id"

        if (savedLanguage != currentLanguage) {
            currentLanguage = savedLanguage
            updateAllTexts()
        }
    }

    fun PilihPelanggan(view: View?) {
        val intent = Intent(this@transaksi, PilihPelangganActivity::class.java)
        pilihPelangganLauncher.launch(intent)
    }

    fun PilihLayanan(view: View?) {
        val intent = Intent(this@transaksi, PilihLayananActivity::class.java)
        pilihLayananLauncher.launch(intent)
    }

    fun DataLayananTambahan(view: View?) {
        val intent = Intent(this@transaksi, PilihLayananTambahan::class.java)
        pilihTambahanLauncher.launch(intent)
    }
}