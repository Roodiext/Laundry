package com.rudi.laundry.Pegawai

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
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

    private var idPegawai: String = ""
    private var isEditMode = false
    private var listCabang = arrayListOf<modelCabang>()
    private var cabangAdapter: ArrayAdapter<String>? = null
    private var selectedCabangId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pegawai)

        initViews()
        setupCabangDropdown()
        setupListeners()
        checkEditMode()
        loadCabangData()
    }

    private fun initViews() {
        etNama = findViewById(R.id.etnama_pegawai)
        etAlamat = findViewById(R.id.etalamat_pegawai)
        etNoHP = findViewById(R.id.etnohp_pegawai)
        etCabang = findViewById(R.id.etcabang_pegawai)
        btTambah = findViewById(R.id.bttambah)
        btBatal = findViewById(R.id.btbatal)
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
        // Show loading state
        etCabang.hint = "Memuat data cabang..."

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
                etCabang.hint = if (cabangNames.isNotEmpty()) {
                    "Pilih Cabang"
                } else {
                    "Tidak ada data cabang"
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
                    "Gagal memuat data cabang: ${error.message}",
                    Toast.LENGTH_SHORT).show()

                etCabang.hint = "Error memuat cabang"
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
        if (judul == "Edit Pegawai") {
            isEditMode = true
            btTambah.text = "Update Pegawai"
            loadDataForEdit()
        } else {
            isEditMode = false
            btTambah.text = "Tambah Pegawai"
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
        val nama = etNama.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val noHP = etNoHP.text.toString().trim()
        val cabangText = etCabang.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Nama tidak boleh kosong"
            etNama.requestFocus()
            return false
        }

        if (alamat.isEmpty()) {
            etAlamat.error = "Alamat tidak boleh kosong"
            etAlamat.requestFocus()
            return false
        }

        if (noHP.isEmpty()) {
            etNoHP.error = "No HP tidak boleh kosong"
            etNoHP.requestFocus()
            return false
        }

        if (cabangText.isEmpty()) {
            etCabang.error = "Cabang harus dipilih"
            etCabang.requestFocus()
            return false
        }

        // Validasi apakah cabang yang dipilih valid
        if (selectedCabangId.isEmpty()) {
            etCabang.error = "Pilih cabang dari daftar yang tersedia"
            etCabang.requestFocus()
            return false
        }

        // Validasi format nomor HP
        if (!noHP.matches(Regex("^[0-9]{10,13}$"))) {
            etNoHP.error = "Format nomor HP tidak valid (10-13 digit)"
            etNoHP.requestFocus()
            return false
        }

        return true
    }

    private fun simpanPegawai() {
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
        btTambah.text = "Menyimpan..."

        pegawaiBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Pegawai berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal menambahkan pegawai: ${error.message}", Toast.LENGTH_SHORT).show()
                btTambah.isEnabled = true
                btTambah.text = "Tambah Pegawai"
            }
    }

    private fun updatePegawai() {
        if (idPegawai.isEmpty()) {
            Toast.makeText(this, "ID Pegawai tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val updateData = mutableMapOf<String, Any>()
        updateData["namaPegawai"] = etNama.text.toString().trim()
        updateData["alamatPegawai"] = etAlamat.text.toString().trim()
        updateData["noHPPegawai"] = etNoHP.text.toString().trim()
        updateData["cabang"] = selectedCabangId // Menyimpan ID cabang, bukan nama

        // Disable button untuk mencegah double click
        btTambah.isEnabled = false
        btTambah.text = "Mengupdate..."

        myRef.child(idPegawai).updateChildren(updateData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data pegawai berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal memperbarui data: ${error.message}", Toast.LENGTH_SHORT).show()
                btTambah.isEnabled = true
                btTambah.text = "Update Pegawai"
            }
    }
}