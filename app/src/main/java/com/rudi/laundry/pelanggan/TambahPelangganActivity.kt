package com.rudi.laundry.pelanggan

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
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

    private var listCabang = arrayListOf<modelCabang>()
    private var cabangNames = arrayListOf<String>()
    private var adapter: ArrayAdapter<String>? = null
    private var selectedCabangId: String = ""
    private var isDataLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pelanggan)

        initViews()
        setupListeners()
        loadCabangData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tambahpelanggan)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        etNama = findViewById(R.id.etnama_pelanggan)
        etAlamat = findViewById(R.id.etalamat_pelanggan)
        etNoHP = findViewById(R.id.etnohp_pelanggan)
        etCabang = findViewById(R.id.etnama_cabang)
        btSimpan = findViewById(R.id.bttambah)
        btBatal = findViewById(R.id.btbatal)
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
        // Show loading state
        etCabang.hint = "Memuat data cabang..."
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
                Toast.makeText(this@TambahPelangganActivity, "Gagal memuat data cabang: ${error.message}", Toast.LENGTH_SHORT).show()

                // Enable dropdown dengan pesan error
                etCabang.isEnabled = true
                etCabang.hint = "Gagal memuat data cabang"
                isDataLoaded = false
            }
        })
    }

    private fun setupDropdownAdapter() {
        runOnUiThread {
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
                "Tidak ada cabang tersedia"
            } else {
                "Pilih Cabang"
            }

            isDataLoaded = true

            // Refresh adapter
            adapter?.notifyDataSetChanged()
        }
    }

    private fun validateInputs(): Boolean {
        val nama = etNama.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val noHP = etNoHP.text.toString().trim()
        val cabang = etCabang.text.toString().trim()

        when {
            nama.isEmpty() -> {
                etNama.error = "Nama tidak boleh kosong"
                etNama.requestFocus()
                return false
            }
            nama.length < 2 -> {
                etNama.error = "Nama minimal 2 karakter"
                etNama.requestFocus()
                return false
            }
            alamat.isEmpty() -> {
                etAlamat.error = "Alamat tidak boleh kosong"
                etAlamat.requestFocus()
                return false
            }
            alamat.length < 5 -> {
                etAlamat.error = "Alamat minimal 5 karakter"
                etAlamat.requestFocus()
                return false
            }
            noHP.isEmpty() -> {
                etNoHP.error = "Nomor HP tidak boleh kosong"
                etNoHP.requestFocus()
                return false
            }
            noHP.length < 10 -> {
                etNoHP.error = "Nomor HP minimal 10 digit"
                etNoHP.requestFocus()
                return false
            }
            !noHP.matches(Regex("^[0-9+\\-\\s()]+$")) -> {
                etNoHP.error = "Format nomor HP tidak valid"
                etNoHP.requestFocus()
                return false
            }
            cabang.isEmpty() -> {
                etCabang.error = "Cabang harus dipilih"
                etCabang.requestFocus()
                Toast.makeText(this, "Silakan pilih cabang", Toast.LENGTH_SHORT).show()
                return false
            }
            selectedCabangId.isEmpty() -> {
                etCabang.error = "Pilih cabang dari daftar yang tersedia"
                etCabang.requestFocus()
                Toast.makeText(this, "Pilih cabang dari daftar dropdown", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun tambahPelangganBaru() {
        // Disable button untuk mencegah double click
        btSimpan.isEnabled = false
        btSimpan.text = "Menyimpan..."

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
                Toast.makeText(this, "Pelanggan berhasil ditambahkan ke cabang $namaCabang", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal menambahkan pelanggan: ${error.message}", Toast.LENGTH_SHORT).show()
                // Re-enable button jika gagal
                btSimpan.isEnabled = true
                btSimpan.text = "Tambah Pelanggan"
            }
    }
}