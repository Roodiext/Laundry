package com.rudi.laundry.pelanggan

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
    private lateinit var actvCabang: AutoCompleteTextView // Changed to AutoCompleteTextView

    private lateinit var btnSimpan: MaterialButton
    private lateinit var btnBatal: MaterialButton

    private var pelangganId: String = ""
    private var terdaftarDate: String = ""

    // Data cabang
    private var listCabang = arrayListOf<modelCabang>()
    private var cabangNames = arrayListOf<String>()
    private var cabangAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pelanggan)

        // Setup status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.button)

        initViews()
        setupWindowInsets()
        loadCabangData() // Load cabang data first
        getDataFromIntent()
        setupListeners()
    }

    private fun initViews() {
        // TextInputLayouts
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
                            val cabang = dataSnapshot.getValue(modelCabang::class.java)
                            if (cabang != null && cabang.namaCabang.isNotEmpty()) {
                                // Pastikan ID cabang ter-set dari key Firebase
                                if (cabang.idCabang.isEmpty()) {
                                    cabang.idCabang = dataSnapshot.key ?: ""
                                }

                                listCabang.add(cabang)
                                // Format: "Nama Cabang - Nama Toko"
                                val displayName = if (cabang.namaToko.isNotEmpty()) {
                                    "${cabang.namaCabang} - ${cabang.namaToko}"
                                } else {
                                    cabang.namaCabang
                                }
                                cabangNames.add(displayName)
                            }
                        }
                    }

                    // Setup adapter untuk dropdown
                    setupCabangDropdown()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditPelangganActivity,
                        "Error loading cabang: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupCabangDropdown() {
        cabangAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cabangNames)
        actvCabang.setAdapter(cabangAdapter)

        // Set current cabang if exists
        val currentCabang = intent.getStringExtra(EXTRA_CABANG_PELANGGAN) ?: ""
        if (currentCabang.isNotEmpty()) {
            // Cari cabang yang sesuai
            val matchingCabang = listCabang.find {
                it.namaCabang == currentCabang ||
                        "${it.namaCabang} - ${it.namaToko}" == currentCabang
            }

            if (matchingCabang != null) {
                val displayName = if (matchingCabang.namaToko.isNotEmpty()) {
                    "${matchingCabang.namaCabang} - ${matchingCabang.namaToko}"
                } else {
                    matchingCabang.namaCabang
                }
                actvCabang.setText(displayName, false)
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

        // Cabang akan di-set di setupCabangDropdown() setelah data cabang dimuat
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

        // Add text change listeners for validation
        etNama.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateNama()
        }

        etNoHP.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateNoHP()
        }

        // Listener untuk dropdown cabang
        actvCabang.setOnItemClickListener { _, _, position, _ ->
            if (position < cabangNames.size) {
                val selectedCabang = cabangNames[position]
                actvCabang.setText(selectedCabang, false)
            }
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
                tilNama.error = "Nama tidak boleh kosong"
                false
            }
            nama.length < 2 -> {
                tilNama.error = "Nama minimal 2 karakter"
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
                tilAlamat.error = "Alamat tidak boleh kosong"
                false
            }
            alamat.length < 5 -> {
                tilAlamat.error = "Alamat minimal 5 karakter"
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
                tilNoHP.error = "Nomor HP tidak boleh kosong"
                false
            }
            noHP.length < 10 -> {
                tilNoHP.error = "Nomor HP minimal 10 digit"
                false
            }
            !noHP.matches(Regex("^[0-9+\\-\\s()]+$")) -> {
                tilNoHP.error = "Format nomor HP tidak valid"
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
                tilCabang.error = "Cabang tidak boleh kosong"
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
            val displayName = if (cabang.namaToko.isNotEmpty()) {
                "${cabang.namaCabang} - ${cabang.namaToko}"
            } else {
                cabang.namaCabang
            }
            displayName == selectedText
        }

        // Jika ditemukan di dropdown, gunakan namaCabang saja
        // Jika tidak ditemukan (custom input), gunakan text yang diinput
        return selectedCabang?.namaCabang ?: selectedText
    }

    private fun updatePelanggan() {
        if (pelangganId.isEmpty()) {
            Toast.makeText(this, "ID pelanggan tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button to prevent double click
        btnSimpan.isEnabled = false
        btnSimpan.text = "Menyimpan..."

        val selectedCabangName = getSelectedCabangName()

        val updatedPelanggan = modelPelanggan(
            idPelanggan = pelangganId,
            namaPelanggan = etNama.text.toString().trim(),
            alamatPelanggan = etAlamat.text.toString().trim(),
            noHPPelanggan = etNoHP.text.toString().trim(),
            terdaftar = terdaftarDate,
            cabang = selectedCabangName
        )

        myRef.child(pelangganId).setValue(updatedPelanggan)
            .addOnSuccessListener {
                Toast.makeText(this, "Data pelanggan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal memperbarui data: ${error.message}", Toast.LENGTH_SHORT).show()
                // Re-enable button on failure
                btnSimpan.isEnabled = true
                btnSimpan.text = "Simpan Perubahan"
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
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