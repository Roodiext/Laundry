package com.rudi.laundry.LayananTambahan

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import com.rudi.laundry.Layanan.TambahLayananActivity
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelLayanan
import com.rudi.laundry.modeldata.modelCabang
import java.text.NumberFormat
import java.util.*

class TambahLayananTambahan : AppCompatActivity() {

    private lateinit var etNama: TextInputEditText
    private lateinit var etHarga: TextInputEditText
    private lateinit var etCabang: AutoCompleteTextView
    private lateinit var btSimpan: MaterialButton
    private lateinit var btBatal: MaterialButton

    // TextViews yang perlu diubah bahasanya
    private lateinit var tvJudul: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tilNama: TextInputLayout
    private lateinit var tilHarga: TextInputLayout
    private lateinit var tilCabang: TextInputLayout

    // Firebase reference untuk cabang
    private val database = FirebaseDatabase.getInstance()
    private val cabangRef = database.getReference("cabang")

    // List untuk menyimpan data cabang
    private var listCabang = arrayListOf<modelCabang>()
    private var cabangNames = arrayListOf<String>()

    // Language settings
    private var currentLanguage = "id"

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "title" to "Tambah Layanan Tambahan",
            "subtitle" to "Lengkapi data layanan tambahan dengan benar",
            "service_name_hint" to "Nama Layanan Tambahan",
            "service_price_hint" to "Harga Layanan Tambahan",
            "select_branch_hint" to "Pilih Cabang",
            "cancel_button" to "Batal",
            "add_button" to "Tambah Layanan Tambahan",
            "saving_button" to "Menyimpan...",
            "loading_branch" to "Memuat data cabang...",
            "failed_load_branch" to "Gagal memuat cabang",
            "no_branch_available" to "Tidak ada cabang tersedia",
            "no_branch_message" to "Tidak ada data cabang. Silakan tambah cabang terlebih dahulu.",
            "name_empty_error" to "Nama layanan tambahan tidak boleh kosong",
            "price_empty_error" to "Harga layanan tambahan tidak boleh kosong",
            "branch_empty_error" to "Cabang harus dipilih",
            "branch_invalid_error" to "Pilih cabang dari daftar yang tersedia",
            "price_invalid_error" to "Format harga tidak valid",
            "save_success" to "Layanan tambahan berhasil disimpan!",
            "save_failed" to "Gagal menyimpan data",
            "database_error" to "Gagal memuat data cabang"
        ),
        "en" to mapOf(
            "title" to "Add Additional Service",
            "subtitle" to "Complete additional service data correctly",
            "service_name_hint" to "Additional Service Name",
            "service_price_hint" to "Additional Service Price",
            "select_branch_hint" to "Select Branch",
            "cancel_button" to "Cancel",
            "add_button" to "Add Additional Service",
            "saving_button" to "Saving...",
            "loading_branch" to "Loading branch data...",
            "failed_load_branch" to "Failed to load branch",
            "no_branch_available" to "No branch available",
            "no_branch_message" to "No branch data. Please add branch first.",
            "name_empty_error" to "Additional service name cannot be empty",
            "price_empty_error" to "Additional service price cannot be empty",
            "branch_empty_error" to "Branch must be selected",
            "branch_invalid_error" to "Select branch from available list",
            "price_invalid_error" to "Invalid price format",
            "save_success" to "Additional service saved successfully!",
            "save_failed" to "Failed to save data",
            "database_error" to "Failed to load branch data"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_layanan_tambahan)

        // Load language preference
        loadLanguagePreference()

        initViews()
        updateAllTexts()
        loadCabangData()
        setupHargaFormatter()
        setupListeners()
    }

    private fun loadLanguagePreference() {
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"
    }

    private fun initViews() {
        etNama = findViewById(R.id.etnama_layanan)
        etHarga = findViewById(R.id.etharga_layanan)
        etCabang = findViewById(R.id.etcabang_layanan)
        btSimpan = findViewById(R.id.bttambah)
        btBatal = findViewById(R.id.btbatal)

        // Initialize TextViews untuk language update
        tvJudul = findViewById(R.id.tvjudul_tambah_layanan_tambahan)
        tvSubtitle = findViewById(R.id.tvsubtitle_tambah_layanan_tambahan)

        // Initialize TextInputLayouts
        tilNama = etNama.parent.parent as TextInputLayout
        tilHarga = etHarga.parent.parent as TextInputLayout
        tilCabang = etCabang.parent.parent as TextInputLayout
    }

    private fun updateAllTexts() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Update title dan subtitle
        tvJudul.text = texts["title"]
        tvSubtitle.text = texts["subtitle"]

        // Update hints
        tilNama.hint = texts["service_name_hint"]
        tilHarga.hint = texts["service_price_hint"]
        tilCabang.hint = texts["select_branch_hint"]

        // Update buttons
        btBatal.text = texts["cancel_button"]
        btSimpan.text = texts["add_button"]
    }

    private fun loadCabangData() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Disable dropdown sementara sambil loading data
        etCabang.isEnabled = false
        etCabang.hint = texts["loading_branch"]

        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                cabangNames.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        try {
                            val cabang = dataSnapshot.getValue(modelCabang::class.java)
                            if (cabang != null) {
                                // Pastikan ID cabang ter-set dari key Firebase
                                if (cabang.idCabang.isEmpty()) {
                                    cabang.idCabang = dataSnapshot.key ?: ""
                                }

                                // Validasi data tidak kosong
                                if (cabang.namaCabang.isNotEmpty()) {
                                    listCabang.add(cabang)
                                    // Tambahkan nama cabang ke list untuk dropdown
                                    cabangNames.add(cabang.namaCabang)
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("TambahLayananTambahan", "Error parsing cabang: ${e.message}")
                        }
                    }
                }

                // Setup dropdown setelah data ter-load
                setupCabangDropdown()
            }

            override fun onCancelled(error: DatabaseError) {
                val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

                android.util.Log.e("TambahLayananTambahan", "Database error: ${error.message}")
                Toast.makeText(this@TambahLayananTambahan,
                    "${texts["database_error"]}: ${error.message}",
                    Toast.LENGTH_SHORT).show()

                // Re-enable dropdown dan set hint error
                etCabang.isEnabled = true
                etCabang.hint = texts["failed_load_branch"]
            }
        })
    }

    private fun setupCabangDropdown() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Enable dropdown setelah data ter-load
        etCabang.isEnabled = true
        etCabang.hint = texts["select_branch_hint"]

        if (cabangNames.isEmpty()) {
            etCabang.hint = texts["no_branch_available"]
            Toast.makeText(this, texts["no_branch_message"],
                Toast.LENGTH_LONG).show()
            return
        }

        // Sort cabang berdasarkan nama untuk tampilan yang lebih rapi
        cabangNames.sort()

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cabangNames)
        etCabang.setAdapter(adapter)

        // Set threshold untuk menampilkan dropdown
        etCabang.threshold = 0

        // Tampilkan dropdown saat diklik
        etCabang.setOnClickListener {
            etCabang.showDropDown()
        }
    }

    private fun setupHargaFormatter() {
        // Format harga otomatis ke Rupiah dengan koma di belakang
        etHarga.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etHarga.removeTextChangedListener(this)

                    val localeID = Locale("in", "ID")
                    val cleanString = s.toString().replace("[Rp,.\\s]".toRegex(), "")

                    if (cleanString.isNotEmpty()) {
                        try {
                            val parsed = cleanString.toDouble()
                            val formatted = NumberFormat.getInstance(localeID).format(parsed)
                            current = formatted
                            etHarga.setText(formatted)
                            etHarga.setSelection(formatted.length)
                        } catch (e: NumberFormatException) {
                            // Handle invalid number format
                        }
                    }

                    etHarga.addTextChangedListener(this)
                }
            }
        })
    }

    private fun setupListeners() {
        btSimpan.setOnClickListener {
            saveLayananTambahan()
        }

        btBatal.setOnClickListener {
            finish()
        }
    }

    private fun saveLayananTambahan() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        val nama = etNama.text.toString().trim()
        val harga = etHarga.text.toString()
            .replace(".", "")
            .replace(",", ".")
            .trim()
        val cabang = etCabang.text.toString().trim()

        // Validasi input
        if (nama.isEmpty()) {
            etNama.error = texts["name_empty_error"]
            etNama.requestFocus()
            return
        }

        if (harga.isEmpty()) {
            etHarga.error = texts["price_empty_error"]
            etHarga.requestFocus()
            return
        }

        if (cabang.isEmpty()) {
            etCabang.error = texts["branch_empty_error"]
            etCabang.requestFocus()
            return
        }

        // Validasi apakah cabang yang dipilih ada dalam list
        if (!cabangNames.contains(cabang)) {
            etCabang.error = texts["branch_invalid_error"]
            etCabang.requestFocus()
            return
        }

        // Validasi harga harus berupa angka
        try {
            harga.toDouble()
        } catch (e: NumberFormatException) {
            etHarga.error = texts["price_invalid_error"]
            etHarga.requestFocus()
            return
        }

        // Simpan ke Firebase dengan reference layanan_tambahan
        val layananTambahanRef = database.getReference("layanan_tambahan").push()
        val id = layananTambahanRef.key ?: ""

        // Menggunakan modelLayanan dengan jenisLayanan = "Tambahan"
        val data = modelLayanan(id, nama, harga, cabang, "Tambahan")

        // Disable button saat menyimpan
        btSimpan.isEnabled = false
        btSimpan.text = texts["saving_button"]

        layananTambahanRef.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, texts["save_success"], Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "${texts["save_failed"]}: ${error.message}", Toast.LENGTH_SHORT).show()
                // Re-enable button jika gagal
                btSimpan.isEnabled = true
                btSimpan.text = texts["add_button"]
            }
    }

    fun layanan(view: View) {
        val intent = Intent(this@TambahLayananTambahan, TambahLayananActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Reload language preference dan update teks saat kembali ke activity
        loadLanguagePreference()
        updateAllTexts()
    }
}