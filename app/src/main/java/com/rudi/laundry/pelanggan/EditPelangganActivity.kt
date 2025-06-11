package com.rudi.laundry.pelanggan

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelCabang
import com.rudi.laundry.modeldata.modelPelanggan

class EditPelangganActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pelanggan")
    private val cabangRef = database.getReference("cabang")

    private lateinit var tilNama: TextInputLayout
    private lateinit var tilAlamat: TextInputLayout
    private lateinit var tilNoHP: TextInputLayout
    private lateinit var tilCabang: TextInputLayout

    private lateinit var etNama: TextInputEditText
    private lateinit var etAlamat: TextInputEditText
    private lateinit var etNoHP: TextInputEditText
    private lateinit var actvCabang: AutoCompleteTextView

    private lateinit var btnSimpan: MaterialButton
    private lateinit var btnBatal: MaterialButton

    private var pelangganId: String = ""
    private var terdaftarDate: String = ""

    // Data cabang
    private var listCabang = arrayListOf<modelCabang>()
    private var cabangNames = arrayListOf<String>()
    private var cabangAdapter: ArrayAdapter<String>? = null

    // Language texts untuk EditPelangganActivity - Disederhanakan
    private val languageTexts = mapOf(
        "id" to mapOf(
            "full_name_hint" to "Nama Lengkap",
            "address_hint" to "Alamat Lengkap",
            "phone_hint" to "Nomor WhatsApp/HP",
            "branch_hint" to "Pilih Cabang",
            "cancel" to "Batal",
            "save" to "Simpan",
            "saving" to "Menyimpan...",
            "name_empty_error" to "Nama tidak boleh kosong",
            "name_min_error" to "Nama minimal 2 karakter",
            "address_empty_error" to "Alamat tidak boleh kosong",
            "address_min_error" to "Alamat minimal 5 karakter",
            "phone_empty_error" to "Nomor HP tidak boleh kosong",
            "phone_min_error" to "Nomor HP minimal 10 digit",
            "phone_format_error" to "Format nomor HP tidak valid",
            "branch_empty_error" to "Cabang tidak boleh kosong",
            "invalid_customer_id" to "ID pelanggan tidak valid",
            "success_update" to "Data pelanggan berhasil diperbarui",
            "failed_update" to "Gagal memperbarui data",
            "loading_branch_error" to "Error loading cabang"
        ),
        "en" to mapOf(
            "full_name_hint" to "Full Name",
            "address_hint" to "Complete Address",
            "phone_hint" to "WhatsApp/Phone Number",
            "branch_hint" to "Select Branch",
            "cancel" to "Cancel",
            "save" to "Save",
            "saving" to "Saving...",
            "name_empty_error" to "Name cannot be empty",
            "name_min_error" to "Name must be at least 2 characters",
            "address_empty_error" to "Address cannot be empty",
            "address_min_error" to "Address must be at least 5 characters",
            "phone_empty_error" to "Phone number cannot be empty",
            "phone_min_error" to "Phone number must be at least 10 digits",
            "phone_format_error" to "Invalid phone number format",
            "branch_empty_error" to "Branch cannot be empty",
            "invalid_customer_id" to "Invalid customer ID",
            "success_update" to "Customer data successfully updated",
            "failed_update" to "Failed to update data",
            "loading_branch_error" to "Error loading branch"
        )
    )

    private fun getCurrentLanguage(): String {
        val sharedPref = getSharedPreferences("language_pref", Context.MODE_PRIVATE)
        return sharedPref.getString("selected_language", "id") ?: "id"
    }

    private fun getTranslation(key: String): String {
        val currentLanguage = getCurrentLanguage()
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
        return texts[key] ?: key
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pelanggan)

        // Setup status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.button)

        initViews()
        setupWindowInsets()
        setupLanguageTexts()
        loadCabangData()
        getDataFromIntent()
        setupListeners()
    }

    private fun initViews() {
        tilNama = findViewById(R.id.tilNamaPelanggan)
        tilAlamat = findViewById(R.id.tilAlamatPelanggan)
        tilNoHP = findViewById(R.id.tilNoHPPelanggan)
        tilCabang = findViewById(R.id.tilCabangPelanggan)

        // TextInputEditTexts
        etNama = findViewById(R.id.etNamaPelanggan)
        etAlamat = findViewById(R.id.etAlamatPelanggan)
        etNoHP = findViewById(R.id.etNoHPPelanggan)
        actvCabang = findViewById(R.id.actvCabangPelanggan)

        // Buttons
        btnSimpan = findViewById(R.id.btnSimpanPerubahan)
        btnBatal = findViewById(R.id.btnBatal)
    }

    private fun setupLanguageTexts() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Set hints untuk TextInputLayouts
        tilNama.hint = getTranslation("full_name_hint")
        tilAlamat.hint = getTranslation("address_hint")
        tilNoHP.hint = getTranslation("phone_hint")
        tilCabang.hint = getTranslation("branch_hint")

        // Set text untuk buttons
        btnBatal.text = getTranslation("cancel")
        btnSimpan.text = getTranslation("save")
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadCabangData() {
        cabangRef.orderByChild("namaCabang")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listCabang.clear()
                    cabangNames.clear()

                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {
                            try {
                                val cabang = dataSnapshot.getValue(modelCabang::class.java)
                                if (cabang != null && cabang.namaCabang.isNotEmpty()) {
                                    // Pastikan ID cabang ter-set dari key Firebase
                                    if (cabang.idCabang.isEmpty()) {
                                        cabang.idCabang = dataSnapshot.key ?: ""
                                    }

                                    listCabang.add(cabang)

                                    // Format display name konsisten dengan EditPegawai
                                    val displayName = when {
                                        cabang.namaToko.isNotEmpty() && cabang.namaCabang.isNotEmpty() ->
                                            "${cabang.namaToko} - ${cabang.namaCabang}"
                                        cabang.namaToko.isNotEmpty() -> cabang.namaToko
                                        cabang.namaCabang.isNotEmpty() -> cabang.namaCabang
                                        else -> "Cabang ${cabang.idCabang}"
                                    }
                                    cabangNames.add(displayName)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("EditPelanggan", "Error loading cabang: ${e.message}")
                            }
                        }
                    }

                    // Setup adapter untuk dropdown
                    setupCabangDropdown()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditPelangganActivity,
                        "${getTranslation("loading_branch_error")}: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupCabangDropdown() {
        cabangAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cabangNames)
        actvCabang.setAdapter(cabangAdapter)

        // Set item click listener
        actvCabang.setOnItemClickListener { _, _, position, _ ->
            if (position < cabangNames.size && position < listCabang.size) {
                val selectedCabang = cabangNames[position]
                actvCabang.setText(selectedCabang, false)
            }
        }

        // Set current cabang if exists
        setSelectedCabang()
    }

    private fun setSelectedCabang() {
        val currentCabang = intent.getStringExtra(EXTRA_CABANG_PELANGGAN) ?: ""
        if (currentCabang.isNotEmpty()) {
            // Cari cabang yang sesuai
            val matchingIndex = listCabang.indexOfFirst { cabang ->
                cabang.namaCabang == currentCabang ||
                        "${cabang.namaToko} - ${cabang.namaCabang}" == currentCabang ||
                        cabang.namaToko == currentCabang
            }

            if (matchingIndex != -1 && matchingIndex < cabangNames.size) {
                actvCabang.setText(cabangNames[matchingIndex], false)
            } else {
                // Jika cabang lama tidak ditemukan, set sebagai custom text
                actvCabang.setText(currentCabang, false)
            }
        }
    }

    private fun getDataFromIntent() {
        pelangganId = intent.getStringExtra(EXTRA_ID_PELANGGAN) ?: ""
        val nama = intent.getStringExtra(EXTRA_NAMA_PELANGGAN) ?: ""
        val alamat = intent.getStringExtra(EXTRA_ALAMAT_PELANGGAN) ?: ""
        val noHP = intent.getStringExtra(EXTRA_NO_HP_PELANGGAN) ?: ""
        terdaftarDate = intent.getStringExtra(EXTRA_TERDAFTAR_PELANGGAN) ?: ""

        // Set data ke EditText
        etNama.setText(nama)
        etAlamat.setText(alamat)
        etNoHP.setText(noHP)

        // Validasi ID Pelanggan
        if (pelangganId.isEmpty()) {
            Toast.makeText(this, getTranslation("invalid_customer_id"), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupListeners() {


        btnSimpan.setOnClickListener {
            if (validateInputs()) {
                updatePelanggan()
            }
        }

        btnBatal.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (!validateNama()) isValid = false
        if (!validateAlamat()) isValid = false
        if (!validateNoHP()) isValid = false
        if (!validateCabang()) isValid = false

        return isValid
    }

    private fun validateNama(): Boolean {
        val nama = etNama.text.toString().trim()
        return when {
            nama.isEmpty() -> {
                tilNama.error = getTranslation("name_empty_error")
                etNama.requestFocus()
                false
            }
            nama.length < 2 -> {
                tilNama.error = getTranslation("name_min_error")
                etNama.requestFocus()
                false
            }
            else -> {
                tilNama.error = null
                true
            }
        }
    }

    private fun validateAlamat(): Boolean {
        val alamat = etAlamat.text.toString().trim()
        return when {
            alamat.isEmpty() -> {
                tilAlamat.error = getTranslation("address_empty_error")
                etAlamat.requestFocus()
                false
            }
            alamat.length < 5 -> {
                tilAlamat.error = getTranslation("address_min_error")
                etAlamat.requestFocus()
                false
            }
            else -> {
                tilAlamat.error = null
                true
            }
        }
    }

    private fun validateNoHP(): Boolean {
        val noHP = etNoHP.text.toString().trim()
        return when {
            noHP.isEmpty() -> {
                tilNoHP.error = getTranslation("phone_empty_error")
                etNoHP.requestFocus()
                false
            }
            noHP.length < 10 -> {
                tilNoHP.error = getTranslation("phone_min_error")
                etNoHP.requestFocus()
                false
            }
            !noHP.matches(Regex("^[0-9+\\-\\s()]+$")) -> {
                tilNoHP.error = getTranslation("phone_format_error")
                etNoHP.requestFocus()
                false
            }
            else -> {
                tilNoHP.error = null
                true
            }
        }
    }

    private fun validateCabang(): Boolean {
        val cabang = actvCabang.text.toString().trim()
        return when {
            cabang.isEmpty() -> {
                tilCabang.error = getTranslation("branch_empty_error")
                false
            }
            else -> {
                tilCabang.error = null
                true
            }
        }
    }

    private fun getSelectedCabangName(): String {
        val selectedText = actvCabang.text.toString().trim()

        // Cari cabang yang dipilih dari dropdown
        val selectedCabang = listCabang.find { cabang ->
            val displayName = when {
                cabang.namaToko.isNotEmpty() && cabang.namaCabang.isNotEmpty() ->
                    "${cabang.namaToko} - ${cabang.namaCabang}"
                cabang.namaToko.isNotEmpty() -> cabang.namaToko
                cabang.namaCabang.isNotEmpty() -> cabang.namaCabang
                else -> "Cabang ${cabang.idCabang}"
            }
            displayName == selectedText
        }

        // Jika ditemukan di dropdown, gunakan namaCabang saja
        // Jika tidak ditemukan (custom input), gunakan text yang diinput
        return selectedCabang?.namaCabang ?: selectedText
    }

    private fun updatePelanggan() {
        // Validasi input sekali lagi
        val nama = etNama.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val noHP = etNoHP.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = getTranslation("name_empty_error")
            etNama.requestFocus()
            return
        }

        if (alamat.isEmpty()) {
            etAlamat.error = getTranslation("address_empty_error")
            etAlamat.requestFocus()
            return
        }

        if (noHP.isEmpty()) {
            etNoHP.error = getTranslation("phone_empty_error")
            etNoHP.requestFocus()
            return
        }

        val selectedCabangName = getSelectedCabangName()
        if (selectedCabangName.isEmpty()) {
            tilCabang.error = getTranslation("branch_empty_error")
            return
        }

        // Clear error
        tilCabang.error = null

        // Disable button to prevent double click
        btnSimpan.isEnabled = false
        btnSimpan.text = getTranslation("saving")

        val updatedPelanggan = modelPelanggan(
            idPelanggan = pelangganId,
            namaPelanggan = nama,
            alamatPelanggan = alamat,
            noHPPelanggan = noHP,
            terdaftar = terdaftarDate,
            cabang = selectedCabangName
        )

        myRef.child(pelangganId).setValue(updatedPelanggan)
            .addOnSuccessListener {
                Toast.makeText(this, getTranslation("success_update"), Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "${getTranslation("failed_update")}: ${error.message}", Toast.LENGTH_SHORT).show()
                // Re-enable button on failure
                btnSimpan.isEnabled = true
                btnSimpan.text = getTranslation("save")
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove listeners untuk menghindari memory leak
        cabangRef.removeEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Function untuk refresh bahasa ketika bahasa berubah
    fun refreshLanguage() {
        setupLanguageTexts()
        // Clear validation errors dan set ulang dengan bahasa baru
        tilNama.error = null
        tilAlamat.error = null
        tilNoHP.error = null
        tilCabang.error = null
    }

    companion object {
        const val EXTRA_ID_PELANGGAN = "ID_PELANGGAN"
        const val EXTRA_NAMA_PELANGGAN = "NAMA_PELANGGAN"
        const val EXTRA_ALAMAT_PELANGGAN = "ALAMAT_PELANGGAN"
        const val EXTRA_NO_HP_PELANGGAN = "NO_HP_PELANGGAN"
        const val EXTRA_CABANG_PELANGGAN = "CABANG_PELANGGAN"
        const val EXTRA_TERDAFTAR_PELANGGAN = "TERDAFTAR_PELANGGAN"
    }
}