package com.rudi.laundry.pelanggan

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelCabang
import com.rudi.laundry.modeldata.modelPelanggan

class TambahPelangganActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pelanggan")
    private val cabangRef = database.getReference("cabang")

    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoHP: EditText
    private lateinit var etCabang: AutoCompleteTextView
    private lateinit var btSimpan: Button
    private lateinit var btBatal: Button
    private lateinit var tvJudul: TextView
    private lateinit var tvSubtitle: TextView

    private var listCabang = arrayListOf<modelCabang>()
    private var cabangNames = arrayListOf<String>()
    private var adapter: ArrayAdapter<String>? = null
    private var selectedCabangId: String = ""
    private var isDataLoaded = false
    private var currentLanguage = "id" // Default bahasa Indonesia

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "title" to "Tambah Pelanggan Baru",
            "subtitle" to "Lengkapi data pelanggan dengan benar",
            "hint_name" to "Nama Lengkap",
            "hint_address" to "Alamat Lengkap",
            "hint_phone" to "Nomor HP Aktif",
            "hint_branch" to "Pilih Cabang",
            "button_cancel" to "Batal",
            "button_add" to "Tambah Pelanggan",
            "button_saving" to "Menyimpan...",
            "loading_branch" to "Memuat data cabang...",
            "no_branch_available" to "Tidak ada cabang tersedia",
            "failed_load_branch" to "Gagal memuat data cabang",
            "select_branch" to "Silakan pilih cabang",
            "select_from_dropdown" to "Pilih cabang dari daftar dropdown",
            "error_name_empty" to "Nama tidak boleh kosong",
            "error_name_min" to "Nama minimal 2 karakter",
            "error_address_empty" to "Alamat tidak boleh kosong",
            "error_address_min" to "Alamat minimal 5 karakter",
            "error_phone_empty" to "Nomor HP tidak boleh kosong",
            "error_phone_min" to "Nomor HP minimal 10 digit",
            "error_phone_format" to "Format nomor HP tidak valid",
            "error_branch_empty" to "Cabang harus dipilih",
            "error_branch_select" to "Pilih cabang dari daftar yang tersedia",
            "success_add" to "Pelanggan berhasil ditambahkan ke cabang",
            "failed_add" to "Gagal menambahkan pelanggan",
            "failed_load_branch_toast" to "Gagal memuat data cabang"
        ),
        "en" to mapOf(
            "title" to "Add New Customer",
            "subtitle" to "Complete customer data correctly",
            "hint_name" to "Full Name",
            "hint_address" to "Full Address",
            "hint_phone" to "Active Phone Number",
            "hint_branch" to "Select Branch",
            "button_cancel" to "Cancel",
            "button_add" to "Add Customer",
            "button_saving" to "Saving...",
            "loading_branch" to "Loading branch data...",
            "no_branch_available" to "No branch available",
            "failed_load_branch" to "Failed to load branch data",
            "select_branch" to "Please select branch",
            "select_from_dropdown" to "Select branch from dropdown list",
            "error_name_empty" to "Name cannot be empty",
            "error_name_min" to "Name minimum 2 characters",
            "error_address_empty" to "Address cannot be empty",
            "error_address_min" to "Address minimum 5 characters",
            "error_phone_empty" to "Phone number cannot be empty",
            "error_phone_min" to "Phone number minimum 10 digits",
            "error_phone_format" to "Invalid phone number format",
            "error_branch_empty" to "Branch must be selected",
            "error_branch_select" to "Select branch from available list",
            "success_add" to "Customer successfully added to branch",
            "failed_add" to "Failed to add customer",
            "failed_load_branch_toast" to "Failed to load branch data"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pelanggan)

        // Load saved language preference
        loadLanguagePreference()

        initViews()
        setupListeners()
        loadCabangData()
        updateAllTexts()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tambahpelanggan)) { v, insets ->
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
        etNama = findViewById(R.id.etnama_pelanggan)
        etAlamat = findViewById(R.id.etalamat_pelanggan)
        etNoHP = findViewById(R.id.etnohp_pelanggan)
        etCabang = findViewById(R.id.etnama_cabang)
        btSimpan = findViewById(R.id.bttambah)
        btBatal = findViewById(R.id.btbatal)
        tvJudul = findViewById(R.id.tvjudul_tambah_pelanggan)

        // Tambahkan TextView subtitle jika ada di layout
        try {
            tvSubtitle = findViewById<TextView>(R.id.tvsubtitle_tambah_pelanggan)
        } catch (e: Exception) {
            // Jika tidak ada TextView subtitle, buat dummy
        }
    }

    private fun updateAllTexts() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Update title dan subtitle
        tvJudul.text = texts["title"]
        try {
            tvSubtitle.text = texts["subtitle"]
        } catch (e: Exception) {
            // Subtitle TextView tidak ada
        }

        // Update hints
        etNama.hint = texts["hint_name"]
        etAlamat.hint = texts["hint_address"]
        etNoHP.hint = texts["hint_phone"]

        // Update button texts
        btBatal.text = texts["button_cancel"]
        btSimpan.text = texts["button_add"]

        // Update cabang hint
        if (isDataLoaded) {
            etCabang.hint = if (cabangNames.isEmpty()) {
                texts["no_branch_available"]
            } else {
                texts["hint_branch"]
            }
        }
    }

    private fun setupListeners() {
        btSimpan.setOnClickListener {
            if (validateInputs()) {
                tambahPelangganBaru()
            }
        }

        btBatal.setOnClickListener {
            finish()
        }

        // Listener untuk dropdown cabang
        etCabang.setOnItemClickListener { parent, _, position, _ ->
            if (position < listCabang.size) {
                selectedCabangId = listCabang[position].idCabang
                val selectedCabang = listCabang[position]

                // Set text ke format "Nama Cabang - Nama Toko"
                etCabang.setText("${selectedCabang.namaCabang} - ${selectedCabang.namaToko}", false)

                // Log untuk debugging
                android.util.Log.d("CabangSelection", "Selected: ${selectedCabang.namaCabang} with ID: $selectedCabangId")

                // Clear focus untuk menghindari dropdown hilang
                etCabang.clearFocus()
            }
        }

        // Handle ketika user mengklik dropdown
        etCabang.setOnClickListener {
            if (isDataLoaded && adapter != null) {
                etCabang.showDropDown()
            }
        }

        // Handle ketika dropdown mendapat fokus
        etCabang.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && isDataLoaded && adapter != null) {
                etCabang.showDropDown()
            }
        }
    }

    private fun loadCabangData() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Show loading state
        etCabang.hint = texts["loading_branch"]
        etCabang.isEnabled = false

        // Gunakan addListenerForSingleValueEvent untuk memuat data sekali saja
        cabangRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                cabangNames.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        try {
                            val cabang = dataSnapshot.getValue(modelCabang::class.java)
                            if (cabang != null) {
                                // Pastikan ID ter-set dari key Firebase
                                if (cabang.idCabang.isEmpty()) {
                                    cabang.idCabang = dataSnapshot.key ?: ""
                                }

                                // Validasi data tidak kosong
                                if (cabang.namaCabang.isNotEmpty() && cabang.namaToko.isNotEmpty()) {
                                    listCabang.add(cabang)
                                    // Format untuk ditampilkan: "Nama Cabang - Nama Toko"
                                    cabangNames.add("${cabang.namaCabang} - ${cabang.namaToko}")
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("CabangLoad", "Error parsing cabang: ${e.message}")
                        }
                    }
                }

                // Setup adapter untuk dropdown
                setupDropdownAdapter()

                android.util.Log.d("CabangLoad", "Loaded ${listCabang.size} cabang")
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("CabangLoad", "Database error: ${error.message}")
                val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
                Toast.makeText(this@TambahPelangganActivity, "${texts["failed_load_branch_toast"]}: ${error.message}", Toast.LENGTH_SHORT).show()

                // Enable dropdown dengan pesan error
                etCabang.isEnabled = true
                etCabang.hint = texts["failed_load_branch"]
                isDataLoaded = false
            }
        })
    }

    private fun setupDropdownAdapter() {
        runOnUiThread {
            val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

            // Buat adapter baru dengan data yang sudah dimuat
            adapter = ArrayAdapter(
                this@TambahPelangganActivity,
                android.R.layout.simple_dropdown_item_1line,
                ArrayList(cabangNames) // Buat copy dari cabangNames
            )

            etCabang.setAdapter(adapter)

            // Set threshold untuk menampilkan semua item
            etCabang.threshold = 1

            // Enable dropdown dan update hint
            etCabang.isEnabled = true
            etCabang.hint = if (cabangNames.isEmpty()) {
                texts["no_branch_available"]
            } else {
                texts["hint_branch"]
            }

            isDataLoaded = true

            // Refresh adapter
            adapter?.notifyDataSetChanged()
        }
    }

    private fun validateInputs(): Boolean {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
        val nama = etNama.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val noHP = etNoHP.text.toString().trim()
        val cabang = etCabang.text.toString().trim()

        when {
            nama.isEmpty() -> {
                etNama.error = texts["error_name_empty"]
                etNama.requestFocus()
                return false
            }
            nama.length < 2 -> {
                etNama.error = texts["error_name_min"]
                etNama.requestFocus()
                return false
            }
            alamat.isEmpty() -> {
                etAlamat.error = texts["error_address_empty"]
                etAlamat.requestFocus()
                return false
            }
            alamat.length < 5 -> {
                etAlamat.error = texts["error_address_min"]
                etAlamat.requestFocus()
                return false
            }
            noHP.isEmpty() -> {
                etNoHP.error = texts["error_phone_empty"]
                etNoHP.requestFocus()
                return false
            }
            noHP.length < 10 -> {
                etNoHP.error = texts["error_phone_min"]
                etNoHP.requestFocus()
                return false
            }
            !noHP.matches(Regex("^[0-9+\\-\\s()]+$")) -> {
                etNoHP.error = texts["error_phone_format"]
                etNoHP.requestFocus()
                return false
            }
            cabang.isEmpty() -> {
                etCabang.error = texts["error_branch_empty"]
                etCabang.requestFocus()
                Toast.makeText(this, texts["select_branch"], Toast.LENGTH_SHORT).show()
                return false
            }
            selectedCabangId.isEmpty() -> {
                etCabang.error = texts["error_branch_select"]
                etCabang.requestFocus()
                Toast.makeText(this, texts["select_from_dropdown"], Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun tambahPelangganBaru() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Disable button untuk mencegah double click
        btSimpan.isEnabled = false
        btSimpan.text = texts["button_saving"]

        val pelangganBaru = myRef.push()
        val pelangganId = pelangganBaru.key ?: "Unknown"

        // Cari nama cabang berdasarkan selectedCabangId
        val selectedCabang = listCabang.find { it.idCabang == selectedCabangId }
        val namaCabang = selectedCabang?.namaCabang ?: etCabang.text.toString()

        val data = modelPelanggan(
            idPelanggan = pelangganId,
            namaPelanggan = etNama.text.toString().trim(),
            alamatPelanggan = etAlamat.text.toString().trim(),
            noHPPelanggan = etNoHP.text.toString().trim(),
            terdaftar = System.currentTimeMillis().toString(),
            cabang = namaCabang // Simpan nama cabang
        )

        android.util.Log.d("PelangganSave", "Saving pelanggan with cabang: $namaCabang (ID: $selectedCabangId)")

        pelangganBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "${texts["success_add"]} $namaCabang", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "${texts["failed_add"]}: ${error.message}", Toast.LENGTH_SHORT).show()
                // Re-enable button jika gagal
                btSimpan.isEnabled = true
                btSimpan.text = texts["button_add"]
            }
    }

    override fun onResume() {
        super.onResume()
        // Cek apakah bahasa berubah saat activity kembali aktif
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("selected_language", "id") ?: "id"

        if (savedLanguage != currentLanguage) {
            currentLanguage = savedLanguage
            updateAllTexts()
        }
    }
}