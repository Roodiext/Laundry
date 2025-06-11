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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.*
import com.rudi.laundry.R
import com.rudi.laundry.adapter.AdapterLayananTransaksi
import com.rudi.laundry.modeldata.modelLayanan
import com.rudi.laundry.modeldata.modelCabang
import com.rudi.laundry.modeldata.modelPegawai
import com.rudi.laundry.modeldata.modelTransaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class KonfirmasiDataTransaksi : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var textViewNamaPelanggan: TextView
    private lateinit var textViewNomorHP: TextView
    private lateinit var textViewNamaLayanan: TextView
    private lateinit var textViewHargaLayanan: TextView
    private lateinit var recyclerViewLayananTambahan: RecyclerView
    private lateinit var textViewTotalHarga: TextView
    private lateinit var buttonBatal: Button
    private lateinit var buttonPembayaran: Button

    // Views untuk cabang dan pegawai
    private lateinit var textViewCabangTerpilih: TextView
    private lateinit var textViewPegawaiTerpilih: TextView
    private lateinit var buttonPilihCabang: Button
    private lateinit var buttonPilihPegawai: Button

    // Label TextViews - sesuaikan dengan ID di XML
    private lateinit var labelDataPelanggan: TextView
    private lateinit var labelNama: TextView
    private lateinit var labelNoHP: TextView
    private lateinit var labelCabangPegawai: TextView
    private lateinit var labelPilihCabang: TextView
    private lateinit var labelPilihPegawai: TextView
    private lateinit var labelLayananUtama: TextView
    private lateinit var labelLayanan: TextView
    private lateinit var labelHarga: TextView
    private lateinit var labelLayananTambahan: TextView
    private lateinit var labelTotalPembayaran: TextView
    private lateinit var textViewNoAdditionalServices: TextView

    private lateinit var adapterLayananTambahan: AdapterLayananTransaksi
    private val listLayananTambahan = ArrayList<modelLayanan>()

    // Firebase references
    private val database = FirebaseDatabase.getInstance()
    private val cabangRef: DatabaseReference = database.getReference("cabang")
    private val pegawaiRef: DatabaseReference = database.getReference("pegawai")
    private val transaksiRef: DatabaseReference = database.getReference("transaksi")

    // Data lists
    private val listCabang = ArrayList<modelCabang>()
    private val listPegawai = ArrayList<modelPegawai>()

    // Selected data
    private var selectedCabang: modelCabang? = null
    private var selectedPegawai: modelPegawai? = null

    // Language support
    private var currentLanguage = "id" // Default bahasa Indonesia

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "toolbar_title" to "Konfirmasi Transaksi",
            "customer_data" to "Data Pelanggan",
            "name" to "Nama",
            "phone" to "No. HP",
            "branch_staff" to "Cabang dan Pegawai",
            "select_branch" to "Pilih Cabang",
            "select_staff" to "Pilih Pegawai",
            "main_service" to "Layanan Utama",
            "service" to "Layanan",
            "price" to "Harga",
            "additional_services" to "Layanan Tambahan",
            "total_payment" to "Total Pembayaran",
            "no_additional_services" to "Tidak ada layanan tambahan",
            "not_selected" to "Belum dipilih",
            "select" to "Pilih",
            "cancel" to "Batal",
            "continue_payment" to "Lanjut Bayar",
            "branch_data_unavailable" to "Data cabang belum tersedia",
            "staff_data_unavailable" to "Data pegawai belum tersedia",
            "select_branch_staff_first" to "Silakan pilih cabang dan pegawai terlebih dahulu",
            "transaction_saved" to "Transaksi berhasil disimpan",
            "error_loading_branch" to "Error memuat data cabang",
            "error_loading_staff" to "Error memuat data pegawai",
            "error_saving_transaction" to "Error menyimpan transaksi",
            "select_branch_title" to "Pilih Cabang",
            "select_staff_title" to "Pilih Pegawai",
            "payment_method" to "Metode Pembayaran",
            "Choose_payment" to "Pilih cara pembayaran yang mudah untuk Anda",
            "pay_later" to "Bayar Nanti",
            "cash" to "Tunai",
            "qris" to "QRIS",
            "dana" to "DANA",
            "gopay" to "GoPay",
            "shopeepay" to "ShopeePay"
        ),
        "en" to mapOf(
            "toolbar_title" to "Transaction Confirmation",
            "customer_data" to "Customer Data",
            "name" to "Name",
            "phone" to "Phone No.",
            "branch_staff" to "Branch and Staff",
            "select_branch" to "Select Branch",
            "select_staff" to "Select Staff",
            "main_service" to "Main Service",
            "service" to "Service",
            "price" to "Price",
            "additional_services" to "Additional Services",
            "total_payment" to "Total Payment",
            "no_additional_services" to "No additional services",
            "not_selected" to "Not selected",
            "select" to "Select",
            "cancel" to "Cancel",
            "continue_payment" to "Continue Payment",
            "branch_data_unavailable" to "Branch data not available",
            "staff_data_unavailable" to "Staff data not available",
            "select_branch_staff_first" to "Please select branch and staff first",
            "transaction_saved" to "Transaction saved successfully",
            "error_loading_branch" to "Error loading branch data",
            "error_loading_staff" to "Error loading staff data",
            "error_saving_transaction" to "Error saving transaction",
            "select_branch_title" to "Select Branch",
            "select_staff_title" to "Select Staff",
            "payment_method" to "Payment Method",
            "Choose_payment" to "Choose a payment method that is easy for you",
            "pay_later" to "Pay Later",
            "cash" to "Cash",
            "qris" to "QRIS",
            "dana" to "DANA",
            "gopay" to "GoPay",
            "shopeepay" to "ShopeePay"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_konfirmasi_data_transaksi)

        // Load language preference
        loadLanguagePreference()

        initViews()
        setupRecyclerView()
        getDataFromIntent()
        setupButtonListeners()
        loadCabangData()
        loadPegawaiData()
        updateAllTexts()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.konfirmasi_transaksi)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadLanguagePreference() {
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        textViewNamaPelanggan = findViewById(R.id.textViewNamaPelanggan)
        textViewNomorHP = findViewById(R.id.textViewNomorHP)
        textViewNamaLayanan = findViewById(R.id.textViewNamaLayanan)
        textViewHargaLayanan = findViewById(R.id.textViewHargaLayanan)
        recyclerViewLayananTambahan = findViewById(R.id.recyclerViewLayananTambahan)
        textViewTotalHarga = findViewById(R.id.textViewTotalHarga)
        buttonBatal = findViewById(R.id.buttonBatal)
        buttonPembayaran = findViewById(R.id.buttonPembayaran)

        // Inisialisasi views untuk cabang dan pegawai
        textViewCabangTerpilih = findViewById(R.id.textViewCabangTerpilih)
        textViewPegawaiTerpilih = findViewById(R.id.textViewPegawaiTerpilih)
        buttonPilihCabang = findViewById(R.id.buttonPilihCabang)
        buttonPilihPegawai = findViewById(R.id.buttonPilihPegawai)

        // Inisialisasi label TextViews - sesuaikan dengan ID di XML
        labelDataPelanggan = findViewById(R.id.labeldataPelanggan)
        labelNama = findViewById(R.id.labelNama)
        labelNoHP = findViewById(R.id.labelNoHP)
        labelCabangPegawai = findViewById(R.id.labelCabangPegawai)
        labelPilihCabang = findViewById(R.id.labelPilihCabang)
        labelPilihPegawai = findViewById(R.id.labelPilihPegawai)
        labelLayananUtama = findViewById(R.id.labelLayananUtama)
        labelLayanan = findViewById(R.id.labelLayanan)
        labelHarga = findViewById(R.id.labelHarga)
        labelLayananTambahan = findViewById(R.id.labelLayananTambahan)
        labelTotalPembayaran = findViewById(R.id.labelTotalPembayaran)
        textViewNoAdditionalServices = findViewById(R.id.textViewNoAdditionalServices)

        // Setup toolbar navigation
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun updateAllTexts() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Update toolbar title
        toolbar.title = texts["toolbar_title"]

        // Update all label texts
        labelDataPelanggan.text = texts["customer_data"]
        labelNama.text = texts["name"]
        labelNoHP.text = texts["phone"]
        labelCabangPegawai.text = texts["branch_staff"]
        labelPilihCabang.text = texts["select_branch"]
        labelPilihPegawai.text = texts["select_staff"]
        labelLayananUtama.text = texts["main_service"]
        labelLayanan.text = texts["service"]
        labelHarga.text = texts["price"]
        labelLayananTambahan.text = texts["additional_services"]
        labelTotalPembayaran.text = texts["total_payment"]

        // Update button texts
        buttonPilihCabang.text = texts["select"]
        buttonPilihPegawai.text = texts["select"]
        buttonBatal.text = texts["cancel"]
        buttonPembayaran.text = texts["continue_payment"]

        // Update default selection texts
        if (selectedCabang == null) {
            textViewCabangTerpilih.text = texts["not_selected"]
        }
        if (selectedPegawai == null) {
            textViewPegawaiTerpilih.text = texts["not_selected"]
        }

        // Update no additional services text
        textViewNoAdditionalServices.text = texts["no_additional_services"]
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

    private fun loadCabangData() {
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val cabang = dataSnapshot.getValue(modelCabang::class.java)
                        if (cabang != null) {
                            // Pastikan ID cabang ter-set dari key Firebase
                            if (cabang.idCabang.isEmpty()) {
                                cabang.idCabang = dataSnapshot.key ?: ""
                            }
                            listCabang.add(cabang)
                        }
                    }
                }

                // Sort berdasarkan nama cabang
                listCabang.sortBy { it.namaCabang }
            }

            override fun onCancelled(error: DatabaseError) {
                val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
                Toast.makeText(this@KonfirmasiDataTransaksi,
                    "${texts["error_loading_branch"]}: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadPegawaiData() {
        pegawaiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listPegawai.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val pegawai = dataSnapshot.getValue(modelPegawai::class.java)
                        if (pegawai != null && !pegawai.idPegawai.isNullOrEmpty()) {
                            listPegawai.add(pegawai)
                        }
                    }
                }

                // Sort berdasarkan nama pegawai
                listPegawai.sortBy { it.namaPegawai }
            }

            override fun onCancelled(error: DatabaseError) {
                val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
                Toast.makeText(this@KonfirmasiDataTransaksi,
                    "${texts["error_loading_staff"]}: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCabangSelectionDialog() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        if (listCabang.isEmpty()) {
            Toast.makeText(this, texts["branch_data_unavailable"], Toast.LENGTH_SHORT).show()
            return
        }

        val cabangNames = listCabang.map {
            "${it.namaCabang} - ${it.namaToko}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(texts["select_branch_title"])
            .setItems(cabangNames) { _, which ->
                selectedCabang = listCabang[which]
                textViewCabangTerpilih.text = "${selectedCabang?.namaCabang} - ${selectedCabang?.namaToko}"
                updateButtonStates()
            }
            .setNegativeButton(texts["cancel"], null)
            .show()
    }

    private fun showPegawaiSelectionDialog() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        if (listPegawai.isEmpty()) {
            Toast.makeText(this, texts["staff_data_unavailable"], Toast.LENGTH_SHORT).show()
            return
        }

        val pegawaiNames = listPegawai.map { pegawai ->
            "${pegawai.namaPegawai} - ${pegawai.namaPegawai ?: "Staff"}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(texts["select_staff_title"])
            .setItems(pegawaiNames) { _, which ->
                selectedPegawai = listPegawai[which]
                textViewPegawaiTerpilih.text = "${selectedPegawai?.namaPegawai} - ${selectedPegawai?.namaPegawai ?: "Staff"}"
                updateButtonStates()
            }
            .setNegativeButton(texts["cancel"], null)
            .show()
    }

    private fun updateButtonStates() {
        // Enable pembayaran button hanya jika cabang dan pegawai sudah dipilih
        val isSelectionComplete = selectedCabang != null && selectedPegawai != null
        buttonPembayaran.isEnabled = isSelectionComplete

        if (isSelectionComplete) {
            buttonPembayaran.alpha = 1.0f
        } else {
            buttonPembayaran.alpha = 0.5f
        }
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

    private fun generateRandomId(): String {
        return "INV" + System.currentTimeMillis().toString().takeLast(8)
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun setupButtonListeners() {
        buttonBatal.setOnClickListener {
            finish()
        }

        buttonPilihCabang.setOnClickListener {
            showCabangSelectionDialog()
        }

        buttonPilihPegawai.setOnClickListener {
            showPegawaiSelectionDialog()
        }

        buttonPembayaran.setOnClickListener {
            val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

            if (selectedCabang == null || selectedPegawai == null) {
                Toast.makeText(this, texts["select_branch_staff_first"], Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showPembayaranDialog()
        }

        // Set initial button state
        updateButtonStates()
    }

    private fun saveTransaksiToFirebase(metodePembayaran: String, idTransaksi: String) {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        val transaksi = modelTransaksi(
            idTransaksi = idTransaksi,
            namaPelanggan = textViewNamaPelanggan.text.toString(),
            nomorHP = textViewNomorHP.text.toString(),
            namaLayanan = textViewNamaLayanan.text.toString(),
            hargaLayanan = extractNumber(textViewHargaLayanan.text.toString()),
            layananTambahan = listLayananTambahan,
            totalBayar = extractNumber(textViewTotalHarga.text.toString()),
            metodePembayaran = metodePembayaran,
            status = modelTransaksi.STATUS_BELUM_BAYAR, // Default status
            tanggalTransaksi = getCurrentDateTime(),
            tanggalPengambilan = "", // Kosong karena belum diambil

            // Data Cabang
            cabangId = selectedCabang?.idCabang ?: "",
            cabangNama = selectedCabang?.namaCabang ?: "",
            cabangAlamat = selectedCabang?.alamatCabang ?: "",

            // Data Pegawai
            pegawaiId = selectedPegawai?.idPegawai ?: "",
            pegawaiNama = selectedPegawai?.namaPegawai ?: "",
            pegawaiJabatan = selectedPegawai?.namaPegawai ?: "", // Sesuaikan dengan field yang benar

            // Timestamp untuk sorting
            timestamp = System.currentTimeMillis()
        )

        // Simpan ke Firebase dengan push untuk generate unique key
        val newTransaksiRef = transaksiRef.push()
        val firebaseKey = newTransaksiRef.key ?: idTransaksi

        // Update ID transaksi dengan key Firebase
        transaksi.idTransaksi = firebaseKey

        newTransaksiRef.setValue(transaksi)
            .addOnSuccessListener {
                Toast.makeText(this, texts["transaction_saved"], Toast.LENGTH_SHORT).show()

                // Lanjut ke Invoice Activity
                val intent = Intent(this, InvoiceTransaksiActivity::class.java)
                intent.putExtra("nama_pelanggan", textViewNamaPelanggan.text.toString())
                intent.putExtra("nomor_hp", textViewNomorHP.text.toString())
                intent.putExtra("nama_layanan", textViewNamaLayanan.text.toString())
                intent.putExtra("harga_layanan", extractNumber(textViewHargaLayanan.text.toString()))
                intent.putExtra("total_bayar", extractNumber(textViewTotalHarga.text.toString()))
                intent.putExtra("layanan_tambahan", listLayananTambahan)
                intent.putExtra("id_transaksi", firebaseKey)
                intent.putExtra("metode_pembayaran", metodePembayaran)

                // Tambahkan data cabang dan pegawai
                intent.putExtra("cabang_id", selectedCabang?.idCabang)
                intent.putExtra("cabang_nama", selectedCabang?.namaCabang)
                intent.putExtra("cabang_alamat", selectedCabang?.alamatCabang)
                intent.putExtra("pegawai_id", selectedPegawai?.idPegawai)
                intent.putExtra("pegawai_nama", selectedPegawai?.namaPegawai)
                intent.putExtra("pegawai_jabatan", selectedPegawai?.namaPegawai)

                startActivity(intent)
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "${texts["error_saving_transaction"]}: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showPembayaranDialog() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_payment_method)

        val window = dialog.window
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        // Update dialog texts
        val titleTextView = dialog.findViewById<TextView>(R.id.dialogTitle)
        titleTextView?.text = texts["payment_method"]

        val SubtitleTextView = dialog.findViewById<TextView>(R.id.subTitle)
        SubtitleTextView?.text = texts["Choose_payment"]

        val tvBayarNanti = dialog.findViewById<TextView>(R.id.tvBayarNanti)
        val tvTunai = dialog.findViewById<TextView>(R.id.tvTunai)
        val tvQRIS = dialog.findViewById<TextView>(R.id.tvQRIS)
        val tvDana = dialog.findViewById<TextView>(R.id.tvDana)
        val tvGopay = dialog.findViewById<TextView>(R.id.tvGopay)
        val tvShopeePay = dialog.findViewById<TextView>(R.id.tvShopeePay)

        tvBayarNanti?.text = texts["pay_later"]
        tvTunai?.text = texts["cash"]
        tvQRIS?.text = texts["qris"]
        tvDana?.text = texts["dana"]
        tvGopay?.text = texts["gopay"]
        tvShopeePay?.text = texts["shopeepay"]

        // Get CardViews
        val btnBayarNanti = dialog.findViewById<CardView>(R.id.cardBayarNanti)
        val btnTunai = dialog.findViewById<CardView>(R.id.cardTunai)
        val btnQris = dialog.findViewById<CardView>(R.id.cardQris)
        val btnDana = dialog.findViewById<CardView>(R.id.cardDana)
        val btnGopay = dialog.findViewById<CardView>(R.id.cardGopay)
        val btnShopeePay = dialog.findViewById<CardView>(R.id.cardShopeePay)
        val tvBatal = dialog.findViewById<TextView>(R.id.tvBatal)

        tvBatal?.text = texts["cancel"]

        val metodePembayaranListener = View.OnClickListener { view ->
            val metodePembayaran = when (view.id) {
                R.id.cardBayarNanti -> texts["pay_later"]!!
                R.id.cardTunai -> texts["cash"]!!
                R.id.cardQris -> texts["qris"]!!
                R.id.cardDana -> texts["dana"]!!
                R.id.cardGopay -> texts["gopay"]!!
                R.id.cardShopeePay -> texts["shopeepay"]!!
                else -> "Unknown"
            }

            val idTransaksi = generateRandomId()

            // Simpan transaksi ke Firebase
            saveTransaksiToFirebase(metodePembayaran, idTransaksi)

            dialog.dismiss()
        }

        btnBayarNanti?.setOnClickListener(metodePembayaranListener)
        btnTunai?.setOnClickListener(metodePembayaranListener)
        btnQris?.setOnClickListener(metodePembayaranListener)
        btnDana?.setOnClickListener(metodePembayaranListener)
        btnGopay?.setOnClickListener(metodePembayaranListener)
        btnShopeePay?.setOnClickListener(metodePembayaranListener)
        tvBatal?.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        // Reload language preference when activity resumes
        loadLanguagePreference()
        updateAllTexts()
    }
}