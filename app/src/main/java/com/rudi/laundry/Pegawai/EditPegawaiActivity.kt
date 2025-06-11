package com.rudi.laundry.Pegawai

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

import com.google.firebase.database.*
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelCabang
import com.rudi.laundry.modeldata.modelPegawai

class EditPegawaiActivity : AppCompatActivity() {

    // Firebase
    private val database = FirebaseDatabase.getInstance()
    private val pegawaiRef: DatabaseReference = database.getReference("pegawai")
    private val cabangRef: DatabaseReference = database.getReference("cabang")

    // Views
    private lateinit var etNamaPegawai: TextInputEditText
    private lateinit var etAlamatPegawai: TextInputEditText
    private lateinit var etNoHPPegawai: TextInputEditText
    private lateinit var actvCabangPegawai: AutoCompleteTextView
    private lateinit var tilCabangPegawai: TextInputLayout
    private lateinit var btnBatal: MaterialButton
    private lateinit var btnSimpanPerubahan: MaterialButton

    // Data
    private var idPegawai: String = ""
    private var selectedCabangId: String = ""
    private val listCabang = arrayListOf<modelCabang>()
    private val cabangDisplayList = arrayListOf<String>()
    private lateinit var cabangAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pegawai)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        setupCabangDropdown()
        getDataFromIntent()
        setupListeners()
        loadCabangData()
    }

    private fun initViews() {

        etNamaPegawai = findViewById(R.id.etNamaPegawai)
        etAlamatPegawai = findViewById(R.id.etAlamatPegawai)
        etNoHPPegawai = findViewById(R.id.etNoHPPegawai)
        actvCabangPegawai = findViewById(R.id.actvCabangPegawai)
        tilCabangPegawai = findViewById(R.id.tilCabangPegawai)
        btnBatal = findViewById(R.id.btnBatal)
        btnSimpanPerubahan = findViewById(R.id.btnSimpanPerubahan)
    }


    private fun setupCabangDropdown() {
        cabangAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cabangDisplayList)
        actvCabangPegawai.setAdapter(cabangAdapter)

        actvCabangPegawai.setOnItemClickListener { _, _, position, _ ->
            if (position < listCabang.size) {
                selectedCabangId = listCabang[position].idCabang
            }
        }
    }

    private fun getDataFromIntent() {
        // Ambil data dari Intent yang dikirim dari adapter
        idPegawai = intent.getStringExtra("idPegawai") ?: ""
        val namaPegawai = intent.getStringExtra("namaPegawai") ?: ""
        val alamatPegawai = intent.getStringExtra("alamatPegawai") ?: ""
        val noHPPegawai = intent.getStringExtra("noHPPegawai") ?: ""
        selectedCabangId = intent.getStringExtra("idCabang") ?: ""

        // Set data ke views
        etNamaPegawai.setText(namaPegawai)
        etAlamatPegawai.setText(alamatPegawai)
        etNoHPPegawai.setText(noHPPegawai)

        // Validasi ID Pegawai
        if (idPegawai.isEmpty()) {
            Toast.makeText(this, "Error: ID Pegawai tidak valid", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadCabangData() {
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                cabangDisplayList.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        try {
                            val cabang = dataSnapshot.getValue(modelCabang::class.java)
                            if (cabang != null) {
                                // Set ID cabang dari key Firebase jika kosong
                                if (cabang.idCabang.isEmpty()) {
                                    cabang.idCabang = dataSnapshot.key ?: ""
                                }

                                listCabang.add(cabang)

                                // Format display name
                                val displayName = when {
                                    cabang.namaToko.isNotEmpty() && cabang.namaCabang.isNotEmpty() ->
                                        "${cabang.namaToko} - ${cabang.namaCabang}"
                                    cabang.namaToko.isNotEmpty() -> cabang.namaToko
                                    cabang.namaCabang.isNotEmpty() -> cabang.namaCabang
                                    else -> "Cabang ${cabang.idCabang}"
                                }
                                cabangDisplayList.add(displayName)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("EditPegawai", "Error loading cabang: ${e.message}")
                        }
                    }
                }

                // Update adapter
                cabangAdapter.notifyDataSetChanged()

                // Set selected cabang jika ada
                setSelectedCabang()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditPegawaiActivity,
                    "Error loading cabang: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setSelectedCabang() {
        if (selectedCabangId.isNotEmpty()) {
            // Cari index cabang yang sesuai
            val cabangIndex = listCabang.indexOfFirst { it.idCabang == selectedCabangId }

            if (cabangIndex != -1 && cabangIndex < cabangDisplayList.size) {
                // Set text dropdown dengan nama cabang
                actvCabangPegawai.setText(cabangDisplayList[cabangIndex], false)
            } else {
                // Jika cabang tidak ditemukan
                actvCabangPegawai.setText("Cabang Tidak Ditemukan", false)
            }
        }
    }

    private fun setupListeners() {
        btnBatal.setOnClickListener {
            finish()
        }

        btnSimpanPerubahan.setOnClickListener {
            updatePegawai()
        }
    }

    private fun updatePegawai() {
        // Validasi input
        val nama = etNamaPegawai.text.toString().trim()
        val alamat = etAlamatPegawai.text.toString().trim()
        val noHP = etNoHPPegawai.text.toString().trim()

        if (nama.isEmpty()) {
            etNamaPegawai.error = "Nama pegawai tidak boleh kosong"
            etNamaPegawai.requestFocus()
            return
        }

        if (alamat.isEmpty()) {
            etAlamatPegawai.error = "Alamat tidak boleh kosong"
            etAlamatPegawai.requestFocus()
            return
        }

        if (noHP.isEmpty()) {
            etNoHPPegawai.error = "Nomor HP tidak boleh kosong"
            etNoHPPegawai.requestFocus()
            return
        }

        if (selectedCabangId.isEmpty()) {
            tilCabangPegawai.error = "Pilih cabang terlebih dahulu"
            return
        }

        // Clear error
        tilCabangPegawai.error = null

        // Update data ke Firebase
        val updatedPegawai = modelPegawai(
            idPegawai = idPegawai,
            namaPegawai = nama,
            alamatPegawai = alamat,
            noHPPegawai = noHP,
            cabang = selectedCabangId
        )

        // Disable button sementara
        btnSimpanPerubahan.isEnabled = false
        btnSimpanPerubahan.text = "Menyimpan..."

        pegawaiRef.child(idPegawai).setValue(updatedPegawai)
            .addOnSuccessListener {
                Toast.makeText(this, "Data pegawai berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal memperbarui data: ${error.message}", Toast.LENGTH_LONG).show()

                // Re-enable button
                btnSimpanPerubahan.isEnabled = true
                btnSimpanPerubahan.text = "Simpan"
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove listeners untuk menghindari memory leak
        cabangRef.removeEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}