package com.rudi.laundry.Layanan

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
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

    // Firebase reference untuk cabang
    private val database = FirebaseDatabase.getInstance()
    private val cabangRef = database.getReference("cabang")

    // List untuk menyimpan data cabang
    private var listCabang = arrayListOf<modelCabang>()
    private var cabangNames = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_layanan)

        initViews()
        loadCabangData()
        setupHargaFormatter()
        setupListeners()
    }

    private fun initViews() {
        etNama = findViewById(R.id.etnama_layanan)
        etHarga = findViewById(R.id.etharga_layanan)
        etCabang = findViewById(R.id.etcabang_layanan)
        btSimpan = findViewById(R.id.bttambah)
        btBatal = findViewById(R.id.btbatal)
    }

    private fun loadCabangData() {
        // Disable dropdown sementara sambil loading data
        etCabang.isEnabled = false
        etCabang.hint = "Memuat data cabang..."

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
                android.util.Log.e("TambahLayanan", "Database error: ${error.message}")
                Toast.makeText(this@TambahLayananActivity,
                    "Gagal memuat data cabang: ${error.message}",
                    Toast.LENGTH_SHORT).show()

                // Re-enable dropdown dan set hint error
                etCabang.isEnabled = true
                etCabang.hint = "Gagal memuat cabang"
            }
        })
    }

    private fun setupCabangDropdown() {
        // Enable dropdown setelah data ter-load
        etCabang.isEnabled = true
        etCabang.hint = "Pilih Cabang"

        if (cabangNames.isEmpty()) {
            etCabang.hint = "Tidak ada cabang tersedia"
            Toast.makeText(this, "Tidak ada data cabang. Silakan tambah cabang terlebih dahulu.",
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
            saveLayanan()
        }

        btBatal.setOnClickListener {
            finish()
        }
    }

    private fun saveLayanan() {
        val nama = etNama.text.toString().trim()
        val harga = etHarga.text.toString()
            .replace(".", "")
            .replace(",", ".")
            .trim()
        val cabang = etCabang.text.toString().trim()

        // Validasi input
        if (nama.isEmpty()) {
            etNama.error = "Nama layanan tidak boleh kosong"
            etNama.requestFocus()
            return
        }

        if (harga.isEmpty()) {
            etHarga.error = "Harga layanan tidak boleh kosong"
            etHarga.requestFocus()
            return
        }

        if (cabang.isEmpty()) {
            etCabang.error = "Cabang harus dipilih"
            etCabang.requestFocus()
            return
        }

        // Validasi apakah cabang yang dipilih ada dalam list
        if (!cabangNames.contains(cabang)) {
            etCabang.error = "Pilih cabang dari daftar yang tersedia"
            etCabang.requestFocus()
            return
        }

        // Validasi harga harus berupa angka
        try {
            harga.toDouble()
        } catch (e: NumberFormatException) {
            etHarga.error = "Format harga tidak valid"
            etHarga.requestFocus()
            return
        }

        // Simpan ke Firebase
        val layananRef = database.getReference("layanan").push()
        val id = layananRef.key ?: ""

        val data = modelLayanan(id, nama, harga, cabang, "Utama")

        // Disable button saat menyimpan
        btSimpan.isEnabled = false
        btSimpan.text = "Menyimpan..."

        layananRef.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Layanan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal menyimpan data: ${error.message}", Toast.LENGTH_SHORT).show()
                // Re-enable button jika gagal
                btSimpan.isEnabled = true
                btSimpan.text = "Tambah Layanan"
            }
    }

    fun LayananTambahan(view: View) {
        val intent = Intent(this@TambahLayananActivity, TambahLayananTambahan::class.java)
        startActivity(intent)
    }
}