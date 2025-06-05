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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_konfirmasi_data_transaksi)

        initViews()
        setupRecyclerView()
        getDataFromIntent()
        setupButtonListeners()
        loadCabangData()
        loadPegawaiData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.konfirmasi_transaksi)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
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

        // Set default text
        textViewCabangTerpilih.text = "Belum dipilih"
        textViewPegawaiTerpilih.text = "Belum dipilih"
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
                Toast.makeText(this@KonfirmasiDataTransaksi,
                    "Error memuat data cabang: ${error.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@KonfirmasiDataTransaksi,
                    "Error memuat data pegawai: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCabangSelectionDialog() {
        if (listCabang.isEmpty()) {
            Toast.makeText(this, "Data cabang belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val cabangNames = listCabang.map {
            "${it.namaCabang} - ${it.namaToko}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Pilih Cabang")
            .setItems(cabangNames) { _, which ->
                selectedCabang = listCabang[which]
                textViewCabangTerpilih.text = "${selectedCabang?.namaCabang} - ${selectedCabang?.namaToko}"
                updateButtonStates()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showPegawaiSelectionDialog() {
        if (listPegawai.isEmpty()) {
            Toast.makeText(this, "Data pegawai belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val pegawaiNames = listPegawai.map { pegawai ->
            "${pegawai.namaPegawai} - ${pegawai.namaPegawai ?: "Staff"}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Pilih Pegawai")
            .setItems(pegawaiNames) { _, which ->
                selectedPegawai = listPegawai[which]
                textViewPegawaiTerpilih.text = "${selectedPegawai?.namaPegawai} - ${selectedPegawai?.namaPegawai ?: "Staff"}"
                updateButtonStates()
            }
            .setNegativeButton("Batal", null)
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

    private fun extractHargaFromText(harga: String): String {
        return harga.replace("Rp", "").replace(".", "").replace(",", ".").trim()
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
            if (selectedCabang == null || selectedPegawai == null) {
                Toast.makeText(this, "Silakan pilih cabang dan pegawai terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showPembayaranDialog()
        }

        // Set initial button state
        updateButtonStates()
    }

    private fun saveTransaksiToFirebase(metodePembayaran: String, idTransaksi: String) {
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
                Toast.makeText(this, "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show()

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
                Toast.makeText(this, "Error menyimpan transaksi: ${error.message}", Toast.LENGTH_LONG).show()
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

        val metodePembayaranListener = View.OnClickListener { view ->
            val metodePembayaran = when (view.id) {
                R.id.btnBayarNanti -> "Bayar Nanti"
                R.id.btnTunai -> "Tunai"
                R.id.btnQris -> "QRIS"
                R.id.btnDana -> "DANA"
                R.id.btnGopay -> "GoPay"
                R.id.btnOvo -> "OVO"
                else -> "Tidak Diketahui"
            }

            val idTransaksi = generateRandomId()

            // Simpan transaksi ke Firebase
            saveTransaksiToFirebase(metodePembayaran, idTransaksi)

            dialog.dismiss()
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