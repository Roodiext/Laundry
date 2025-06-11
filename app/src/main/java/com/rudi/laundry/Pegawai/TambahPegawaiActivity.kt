package com.rudi.laundry.Pegawai

import android.os.Bundle
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
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelPegawai
import com.rudi.laundry.modeldata.modelCabang

class TambahPegawaiActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pegawai")
    private val cabangRef = database.getReference("cabang")

    private lateinit var etNama: TextInputEditText
    private lateinit var etAlamat: TextInputEditText
    private lateinit var etNoHP: TextInputEditText
    private lateinit var etCabang: AutoCompleteTextView
    private lateinit var btTambah: MaterialButton
    private lateinit var btBatal: MaterialButton

    // TextInputLayouts untuk update hint
    private lateinit var tilNama: TextInputLayout
    private lateinit var tilAlamat: TextInputLayout
    private lateinit var tilNoHP: TextInputLayout
    private lateinit var tilCabang: TextInputLayout

    // TextViews untuk update text
    private lateinit var tvJudul: TextView
    private lateinit var tvSubtitle: TextView

    private var idPegawai: String = ""
    private var isEditMode = false
    private var listCabang = arrayListOf<modelCabang>()
    private var cabangAdapter: ArrayAdapter<String>? = null
    private var selectedCabangId: String = ""
    private var currentLanguage = "id" // Default bahasa Indonesia

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "add_employee" to "Tambah Pegawai Baru",
            "edit_employee" to "Edit Data Pegawai",
            "complete_data" to "Lengkapi data pegawai dengan benar",
            "edit_data" to "Perbarui data pegawai dengan benar",
            "full_name" to "Nama Lengkap",
            "full_address" to "Alamat Lengkap",
            "active_phone" to "Nomor HP Aktif",
            "select_branch" to "Pilih Cabang",
            "cancel" to "Batal",
            "add" to "Tambah Pegawai",
            "update" to "Update Pegawai",
            "loading_branch" to "Memuat data cabang...",
            "no_branch_data" to "Tidak ada data cabang",
            "error_loading_branch" to "Error memuat cabang",
            "name_required" to "Nama tidak boleh kosong",
            "address_required" to "Alamat tidak boleh kosong",
            "phone_required" to "No HP tidak boleh kosong",
            "branch_required" to "Cabang harus dipilih",
            "branch_invalid" to "Pilih cabang dari daftar yang tersedia",
            "phone_format_invalid" to "Format nomor HP tidak valid (10-13 digit)",
            "employee_added" to "Pegawai berhasil ditambahkan",
            "add_failed" to "Gagal menambahkan pegawai",
            "employee_updated" to "Data pegawai berhasil diperbarui",
            "update_failed" to "Gagal memperbarui data",
            "invalid_id" to "ID Pegawai tidak valid",
            "saving" to "Menyimpan...",
            "updating" to "Mengupdate...",
            "failed_load_branch" to "Gagal memuat data cabang"
        ),
        "en" to mapOf(
            "add_employee" to "Add New Employee",
            "edit_employee" to "Edit Employee Data",
            "complete_data" to "Complete employee data correctly",
            "edit_data" to "Update employee data correctly",
            "full_name" to "Full Name",
            "full_address" to "Full Address",
            "active_phone" to "Active Phone Number",
            "select_branch" to "Select Branch",
            "cancel" to "Cancel",
            "add" to "Add Employee",
            "update" to "Update Employee",
            "loading_branch" to "Loading branch data...",
            "no_branch_data" to "No branch data available",
            "error_loading_branch" to "Error loading branches",
            "name_required" to "Name cannot be empty",
            "address_required" to "Address cannot be empty",
            "phone_required" to "Phone number cannot be empty",
            "branch_required" to "Branch must be selected",
            "branch_invalid" to "Select branch from available list",
            "phone_format_invalid" to "Invalid phone number format (10-13 digits)",
            "employee_added" to "Employee successfully added",
            "add_failed" to "Failed to add employee",
            "employee_updated" to "Employee data successfully updated",
            "update_failed" to "Failed to update data",
            "invalid_id" to "Invalid Employee ID",
            "saving" to "Saving...",
            "updating" to "Updating...",
            "failed_load_branch" to "Failed to load branch data"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pegawai)

        loadLanguagePreference()
        initViews()
        setupCabangDropdown()
        setupListeners()
        checkEditMode()
        loadCabangData()
        updateAllTexts()
    }

    private fun loadLanguagePreference() {
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"
    }

    private fun initViews() {
        etNama = findViewById(R.id.etnama_pegawai)
        etAlamat = findViewById(R.id.etalamat_pegawai)
        etNoHP = findViewById(R.id.etnohp_pegawai)
        etCabang = findViewById(R.id.etcabang_pegawai)
        btTambah = findViewById(R.id.bttambah)
        btBatal = findViewById(R.id.btbatal)

        // TextInputLayouts
        tilNama = etNama.parent.parent as TextInputLayout
        tilAlamat = etAlamat.parent.parent as TextInputLayout
        tilNoHP = etNoHP.parent.parent as TextInputLayout
        tilCabang = etCabang.parent.parent as TextInputLayout

        // TextViews
        tvJudul = findViewById(R.id.tvjudul_tambah_pegawai)
        tvSubtitle = findViewById(R.id.tv_subtitle) // Tambahkan ID ini di XML
    }

    private fun updateAllTexts() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Update hints
        tilNama.hint = texts["full_name"]
        tilAlamat.hint = texts["full_address"]
        tilNoHP.hint = texts["active_phone"]
        tilCabang.hint = texts["select_branch"]

        // Update button text
        btBatal.text = texts["cancel"]

        // Update title and subtitle based on mode
        if (isEditMode) {
            tvJudul.text = texts["edit_employee"]
            tvSubtitle.text = texts["edit_data"]
            btTambah.text = texts["update"]
        } else {
            tvJudul.text = texts["add_employee"]
            tvSubtitle.text = texts["complete_data"]
            btTambah.text = texts["add"]
        }
    }

    private fun setupCabangDropdown() {
        // Inisialisasi adapter kosong dulu
        cabangAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayListOf<String>())
        etCabang.setAdapter(cabangAdapter)

        // Set listener untuk dropdown selection
        etCabang.setOnItemClickListener { parent, view, position, id ->
            val selectedCabangName = parent.getItemAtPosition(position) as String

            // Cari ID cabang berdasarkan nama yang dipilih
            val selectedCabang = listCabang.find {
                "${it.namaToko} - ${it.namaCabang}" == selectedCabangName
            }

            selectedCabangId = selectedCabang?.idCabang ?: ""

            // Log untuk debugging
            android.util.Log.d("CabangSelection", "Selected: $selectedCabangName, ID: $selectedCabangId")
        }
    }

    private fun loadCabangData() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Show loading state
        etCabang.hint = texts["loading_branch"]

        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                val cabangNames = arrayListOf<String>()

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
                                if (cabang.namaCabang.isNotEmpty() || cabang.namaToko.isNotEmpty()) {
                                    listCabang.add(cabang)

                                    // Format nama untuk dropdown: "Nama Toko - Nama Cabang"
                                    val displayName = if (cabang.namaToko.isNotEmpty() && cabang.namaCabang.isNotEmpty()) {
                                        "${cabang.namaToko} - ${cabang.namaCabang}"
                                    } else if (cabang.namaToko.isNotEmpty()) {
                                        cabang.namaToko
                                    } else {
                                        cabang.namaCabang
                                    }

                                    cabangNames.add(displayName)
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("CabangLoad", "Error parsing cabang: ${e.message}")
                        }
                    }
                }

                // Update adapter dengan data baru
                cabangAdapter?.clear()
                cabangAdapter?.addAll(cabangNames)
                cabangAdapter?.notifyDataSetChanged()

                // Update hint
                tilCabang.hint = if (cabangNames.isNotEmpty()) {
                    texts["select_branch"]
                } else {
                    texts["no_branch_data"]
                }

                // Log untuk debugging
                android.util.Log.d("CabangLoad", "Loaded ${listCabang.size} cabang")

                // Jika dalam mode edit, set cabang yang sudah dipilih sebelumnya
                if (isEditMode && selectedCabangId.isNotEmpty()) {
                    setSelectedCabangForEdit()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("CabangLoad", "Database error: ${error.message}")
                Toast.makeText(this@TambahPegawaiActivity,
                    "${texts["failed_load_branch"]}: ${error.message}",
                    Toast.LENGTH_SHORT).show()

                tilCabang.hint = texts["error_loading_branch"]
            }
        })
    }

    private fun setSelectedCabangForEdit() {
        // Cari cabang berdasarkan ID yang tersimpan
        val selectedCabang = listCabang.find { it.idCabang == selectedCabangId }

        if (selectedCabang != null) {
            val displayName = if (selectedCabang.namaToko.isNotEmpty() && selectedCabang.namaCabang.isNotEmpty()) {
                "${selectedCabang.namaToko} - ${selectedCabang.namaCabang}"
            } else if (selectedCabang.namaToko.isNotEmpty()) {
                selectedCabang.namaToko
            } else {
                selectedCabang.namaCabang
            }

            etCabang.setText(displayName, false)
        }
    }

    private fun setupListeners() {
        btTambah.setOnClickListener {
            if (validateInput()) {
                if (isEditMode) {
                    updatePegawai()
                } else {
                    simpanPegawai()
                }
            }
        }

        btBatal.setOnClickListener {
            finish()
        }
    }

    private fun checkEditMode() {
        val judul = intent.getStringExtra("judul")
        if (judul == "Edit Pegawai" || judul == "Edit Employee") {
            isEditMode = true
            loadDataForEdit()
        } else {
            isEditMode = false
        }
    }

    private fun loadDataForEdit() {
        idPegawai = intent.getStringExtra("idPegawai") ?: ""
        val nama = intent.getStringExtra("namaPegawai") ?: ""
        val alamat = intent.getStringExtra("alamatPegawai") ?: ""
        val noHP = intent.getStringExtra("noHPPegawai") ?: ""
        selectedCabangId = intent.getStringExtra("idCabang") ?: ""

        etNama.setText(nama)
        etAlamat.setText(alamat)
        etNoHP.setText(noHP)

        // selectedCabangId akan digunakan di setSelectedCabangForEdit()
        // setelah data cabang selesai dimuat
    }

    private fun validateInput(): Boolean {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
        val nama = etNama.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val noHP = etNoHP.text.toString().trim()
        val cabangText = etCabang.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = texts["name_required"]
            etNama.requestFocus()
            return false
        }

        if (alamat.isEmpty()) {
            etAlamat.error = texts["address_required"]
            etAlamat.requestFocus()
            return false
        }

        if (noHP.isEmpty()) {
            etNoHP.error = texts["phone_required"]
            etNoHP.requestFocus()
            return false
        }

        if (cabangText.isEmpty()) {
            etCabang.error = texts["branch_required"]
            etCabang.requestFocus()
            return false
        }

        // Validasi apakah cabang yang dipilih valid
        if (selectedCabangId.isEmpty()) {
            etCabang.error = texts["branch_invalid"]
            etCabang.requestFocus()
            return false
        }

        // Validasi format nomor HP
        if (!noHP.matches(Regex("^[0-9]{10,13}$"))) {
            etNoHP.error = texts["phone_format_invalid"]
            etNoHP.requestFocus()
            return false
        }

        return true
    }

    private fun simpanPegawai() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
        val pegawaiBaru = myRef.push()
        val pegawaiId = pegawaiBaru.key ?: return

        val data = modelPegawai(
            idPegawai = pegawaiId,
            namaPegawai = etNama.text.toString().trim(),
            alamatPegawai = etAlamat.text.toString().trim(),
            noHPPegawai = etNoHP.text.toString().trim(),
            cabang = selectedCabangId // Menyimpan ID cabang, bukan nama
        )

        // Disable button untuk mencegah double click
        btTambah.isEnabled = false
        btTambah.text = texts["saving"]

        pegawaiBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, texts["employee_added"], Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "${texts["add_failed"]}: ${error.message}", Toast.LENGTH_SHORT).show()
                btTambah.isEnabled = true
                btTambah.text = texts["add"]
            }
    }

    private fun updatePegawai() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        if (idPegawai.isEmpty()) {
            Toast.makeText(this, texts["invalid_id"], Toast.LENGTH_SHORT).show()
            return
        }

        val updateData = mutableMapOf<String, Any>()
        updateData["namaPegawai"] = etNama.text.toString().trim()
        updateData["alamatPegawai"] = etAlamat.text.toString().trim()
        updateData["noHPPegawai"] = etNoHP.text.toString().trim()
        updateData["cabang"] = selectedCabangId // Menyimpan ID cabang, bukan nama

        // Disable button untuk mencegah double click
        btTambah.isEnabled = false
        btTambah.text = texts["updating"]

        myRef.child(idPegawai).updateChildren(updateData)
            .addOnSuccessListener {
                Toast.makeText(this, texts["employee_updated"], Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "${texts["update_failed"]}: ${error.message}", Toast.LENGTH_SHORT).show()
                btTambah.isEnabled = true
                btTambah.text = texts["update"]
            }
    }
}