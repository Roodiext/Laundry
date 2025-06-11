package com.rudi.laundry.Layanan

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
import com.rudi.laundry.LayananTambahan.TambahLayananTambahan
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelLayanan
import com.rudi.laundry.modeldata.modelCabang
import java.text.NumberFormat
import java.util.*

class TambahLayananActivity : AppCompatActivity() {

    private lateinit var etNama: TextInputEditText
    private lateinit var etHarga: TextInputEditText
    private lateinit var etCabang: AutoCompleteTextView
    private lateinit var btSimpan: MaterialButton
    private lateinit var btBatal: MaterialButton

    // TextViews untuk multi-bahasa
    private lateinit var tvJudul: TextView
    private lateinit var tvSubjudul: TextView
    private lateinit var tilNama: TextInputLayout
    private lateinit var tilHarga: TextInputLayout
    private lateinit var tilCabang: TextInputLayout

    // Firebase reference untuk cabang
    private val database = FirebaseDatabase.getInstance()
    private val cabangRef = database.getReference("cabang")

    // List untuk menyimpan data cabang
    private var listCabang = arrayListOf<modelCabang>()
    private var cabangNames = arrayListOf<String>()

    // Bahasa
    private var currentLanguage = "id" // Default bahasa Indonesia

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "title" to "Tambah Layanan Baru",
            "subtitle" to "Lengkapi data layanan dengan benar",
            "service_name" to "Nama Layanan",
            "service_price" to "Harga Layanan",
            "select_branch" to "Pilih Cabang",
            "cancel" to "Batal",
            "add_service" to "Tambah Layanan",
            "saving" to "Menyimpan...",
            "loading_branches" to "Memuat data cabang...",
            "failed_load_branches" to "Gagal memuat cabang",
            "no_branches_available" to "Tidak ada cabang tersedia",
            "add_branch_first" to "Tidak ada data cabang. Silakan tambah cabang terlebih dahulu.",
            "service_name_required" to "Nama layanan tidak boleh kosong",
            "service_price_required" to "Harga layanan tidak boleh kosong",
            "branch_required" to "Cabang harus dipilih",
            "select_valid_branch" to "Pilih cabang dari daftar yang tersedia",
            "invalid_price_format" to "Format harga tidak valid",
            "service_saved" to "Layanan berhasil disimpan!",
            "failed_save_data" to "Gagal menyimpan data:",
            "failed_load_branch_data" to "Gagal memuat data cabang:"
        ),
        "en" to mapOf(
            "title" to "Add New Service",
            "subtitle" to "Fill in the service data correctly",
            "service_name" to "Service Name",
            "service_price" to "Service Price",
            "select_branch" to "Select Branch",
            "cancel" to "Cancel",
            "add_service" to "Add Service",
            "saving" to "Saving...",
            "loading_branches" to "Loading branch data...",
            "failed_load_branches" to "Failed to load branches",
            "no_branches_available" to "No branches available",
            "add_branch_first" to "No branch data available. Please add a branch first.",
            "service_name_required" to "Service name cannot be empty",
            "service_price_required" to "Service price cannot be empty",
            "branch_required" to "Branch must be selected",
            "select_valid_branch" to "Select branch from available list",
            "invalid_price_format" to "Invalid price format",
            "service_saved" to "Service successfully saved!",
            "failed_save_data" to "Failed to save data:",
            "failed_load_branch_data" to "Failed to load branch data:"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_layanan)

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

        // TextViews untuk multi-bahasa
        tvJudul = findViewById(R.id.tvjudul_tambah_Layanan)
        tvSubjudul = findViewById(R.id.tvsubjudul_tambah_layanan) // Pastikan ID ini ada di XML

        // TextInputLayouts
        tilNama = findViewById(R.id.tilnama_layanan) // Pastikan ID ini ada di XML
        tilHarga = findViewById(R.id.tilharga_layanan) // Pastikan ID ini ada di XML
        tilCabang = findViewById(R.id.tilcabang_layanan) // Pastikan ID ini ada di XML
    }

    private fun updateAllTexts() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Update texts
        tvJudul.text = texts["title"]
        tvSubjudul.text = texts["subtitle"]
        tilNama.hint = texts["service_name"]
        tilHarga.hint = texts["service_price"]
        tilCabang.hint = texts["select_branch"]
        btBatal.text = texts["cancel"]
        btSimpan.text = texts["add_service"]
    }

    private fun loadCabangData() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Disable dropdown sementara sambil loading data
        etCabang.isEnabled = false
        etCabang.hint = texts["loading_branches"]

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
                            android.util.Log.e("TambahLayanan", "Error parsing cabang: ${e.message}")
                        }
                    }
                }

                // Setup dropdown setelah data ter-load
                setupCabangDropdown()
            }

            override fun onCancelled(error: DatabaseError) {
                val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
                android.util.Log.e("TambahLayanan", "Database error: ${error.message}")
                Toast.makeText(this@TambahLayananActivity,
                    "${texts["failed_load_branch_data"]} ${error.message}",
                    Toast.LENGTH_SHORT).show()

                // Re-enable dropdown dan set hint error
                etCabang.isEnabled = true
                etCabang.hint = texts["failed_load_branches"]
            }
        })
    }

    private fun setupCabangDropdown() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Enable dropdown setelah data ter-load
        etCabang.isEnabled = true
        etCabang.hint = texts["select_branch"]

        if (cabangNames.isEmpty()) {
            etCabang.hint = texts["no_branches_available"]
            Toast.makeText(this, texts["add_branch_first"], Toast.LENGTH_LONG).show()
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
            saveLayanan()
        }

        btBatal.setOnClickListener {
            finish()
        }
    }

    private fun saveLayanan() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        val nama = etNama.text.toString().trim()
        val harga = etHarga.text.toString()
            .replace(".", "")
            .replace(",", ".")
            .trim()
        val cabang = etCabang.text.toString().trim()

        // Validasi input
        if (nama.isEmpty()) {
            etNama.error = texts["service_name_required"]
            etNama.requestFocus()
            return
        }

        if (harga.isEmpty()) {
            etHarga.error = texts["service_price_required"]
            etHarga.requestFocus()
            return
        }

        if (cabang.isEmpty()) {
            etCabang.error = texts["branch_required"]
            etCabang.requestFocus()
            return
        }

        // Validasi apakah cabang yang dipilih ada dalam list
        if (!cabangNames.contains(cabang)) {
            etCabang.error = texts["select_valid_branch"]
            etCabang.requestFocus()
            return
        }

        // Validasi harga harus berupa angka
        try {
            harga.toDouble()
        } catch (e: NumberFormatException) {
            etHarga.error = texts["invalid_price_format"]
            etHarga.requestFocus()
            return
        }

        // Simpan ke Firebase
        val layananRef = database.getReference("layanan").push()
        val id = layananRef.key ?: ""

        val data = modelLayanan(id, nama, harga, cabang, "Utama")

        // Disable button saat menyimpan
        btSimpan.isEnabled = false
        btSimpan.text = texts["saving"]

        layananRef.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, texts["service_saved"], Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "${texts["failed_save_data"]} ${error.message}", Toast.LENGTH_SHORT).show()
                // Re-enable button jika gagal
                btSimpan.isEnabled = true
                btSimpan.text = texts["add_service"]
            }
    }

    fun LayananTambahan(view: View) {
        val intent = Intent(this@TambahLayananActivity, TambahLayananTambahan::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Reload language preference ketika kembali ke activity
        loadLanguagePreference()
        updateAllTexts()
    }
}