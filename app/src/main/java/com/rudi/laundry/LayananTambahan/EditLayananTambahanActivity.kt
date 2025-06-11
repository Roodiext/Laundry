package com.rudi.laundry.LayananTambahan

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.rudi.laundry.modeldata.modelLayanan
import com.rudi.laundry.modeldata.modelCabang
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class EditLayananTambahanActivity : AppCompatActivity() {

    // Firebase
    private val database = FirebaseDatabase.getInstance()
    private val layananTambahanRef: DatabaseReference = database.getReference("layanan_tambahan")
    private val cabangRef: DatabaseReference = database.getReference("cabang")

    // Views
    private lateinit var tilNamaLayananTambahan: TextInputLayout
    private lateinit var etNamaLayananTambahan: TextInputEditText
    private lateinit var tilHargaLayananTambahan: TextInputLayout
    private lateinit var etHargaLayananTambahan: TextInputEditText
    private lateinit var tilCabangLayananTambahan: TextInputLayout
    private lateinit var actvCabangLayananTambahan: AutoCompleteTextView
    private lateinit var btnBatalTambahan: MaterialButton
    private lateinit var btnSimpanPerubahanTambahan: MaterialButton

    // Data
    private var layananTambahanData: modelLayanan? = null
    private var listCabang = arrayListOf<modelCabang>()
    private var cabangNames = arrayListOf<String>()

    // TextWatcher untuk format rupiah
    private var isFormatting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_layanan_tambahan)

        initViews()
        setupPriceFormatter()
        getDataFromIntent()
        getCabangData()
        setupListeners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        tilNamaLayananTambahan = findViewById(R.id.tilNamaLayananTambahan)
        etNamaLayananTambahan = findViewById(R.id.etNamaLayananTambahan)
        tilHargaLayananTambahan = findViewById(R.id.tilHargaLayananTambahan)
        etHargaLayananTambahan = findViewById(R.id.etHargaLayananTambahan)
        tilCabangLayananTambahan = findViewById(R.id.tilCabangLayananTambahan)
        actvCabangLayananTambahan = findViewById(R.id.actvCabangLayananTambahan)
        btnBatalTambahan = findViewById(R.id.btnBatalTambahan)
        btnSimpanPerubahanTambahan = findViewById(R.id.btnSimpanPerubahanTambahan)
    }


    private fun setupPriceFormatter() {
        etHargaLayananTambahan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true

                val text = s.toString()
                val cleanText = text.replace("[^\\d]".toRegex(), "")

                if (cleanText.isNotEmpty()) {
                    val formatted = formatRupiah(cleanText.toLong())
                    etHargaLayananTambahan.setText(formatted)
                    etHargaLayananTambahan.setSelection(formatted.length)
                }

                isFormatting = false
            }
        })
    }

    private fun formatRupiah(amount: Long): String {
        val symbols = DecimalFormatSymbols(Locale("id", "ID"))
        symbols.groupingSeparator = '.'
        val decimalFormat = DecimalFormat("#,###", symbols)
        return decimalFormat.format(amount)
    }

    private fun getCabangData() {
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                cabangNames.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val cabang = dataSnapshot.getValue(modelCabang::class.java)
                        if (cabang != null) {
                            // Pastikan ID cabang ter-set
                            if (cabang.idCabang.isEmpty()) {
                                cabang.idCabang = dataSnapshot.key ?: ""
                            }

                            listCabang.add(cabang)
                            // Format: "Nama Cabang (Nama Toko)"
                            val displayName = if (cabang.namaToko.isNotEmpty()) {
                                "${cabang.namaCabang} (${cabang.namaToko})"
                            } else {
                                cabang.namaCabang
                            }
                            cabangNames.add(displayName)
                        }
                    }

                    // Setup adapter untuk cabang setelah data loaded
                    setupCabangAdapter()

                    // Fill form setelah data cabang tersedia
                    layananTambahanData?.let { layanan ->
                        fillFormWithData(layanan)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditLayananTambahanActivity,
                    "Gagal memuat data cabang: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupCabangAdapter() {
        val cabangAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cabangNames)
        actvCabangLayananTambahan.setAdapter(cabangAdapter)
    }

    private fun getDataFromIntent() {
        // Ambil data layanan tambahan dari intent
        layananTambahanData = intent.getParcelableExtra("layanan_tambahan_data")
    }

    private fun fillFormWithData(layanan: modelLayanan) {
        // Isi nama layanan tambahan
        etNamaLayananTambahan.setText(layanan.namaLayanan ?: "")

        // Isi harga layanan tambahan dengan format yang benar
        val harga = layanan.hargaLayanan
        if (!harga.isNullOrEmpty()) {
            // Jika harga berformat currency, ambil angkanya saja
            val cleanHarga = harga.replace("[^\\d]".toRegex(), "")
            if (cleanHarga.isNotEmpty()) {
                val formattedHarga = formatRupiah(cleanHarga.toLong())
                etHargaLayananTambahan.setText(formattedHarga)
            }
        }

        // Set cabang - cari berdasarkan nama cabang yang ada di data
        val cabangLayanan = layanan.cabang
        if (cabangLayanan.isNotEmpty()) {
            // Cari nama cabang yang sesuai di dalam list
            val matchingCabang = cabangNames.find { displayName ->
                // Cek apakah nama cabang ada di display name
                displayName.contains(cabangLayanan, ignoreCase = true) ||
                        // Atau cek dengan nama cabang saja (tanpa toko)
                        displayName.startsWith(cabangLayanan, ignoreCase = true)
            }

            if (matchingCabang != null) {
                actvCabangLayananTambahan.setText(matchingCabang, false)
            } else {
                // Jika tidak ditemukan yang cocok, set langsung
                actvCabangLayananTambahan.setText(cabangLayanan, false)
            }
        }
    }

    private fun setupListeners() {
        btnBatalTambahan.setOnClickListener {
            finish()
        }

        btnSimpanPerubahanTambahan.setOnClickListener {
            if (validateInput()) {
                updateLayananTambahan()
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        // Validasi nama layanan tambahan
        val namaLayananTambahan = etNamaLayananTambahan.text.toString().trim()
        if (namaLayananTambahan.isEmpty()) {
            tilNamaLayananTambahan.error = "Nama layanan tambahan tidak boleh kosong"
            isValid = false
        } else {
            tilNamaLayananTambahan.error = null
        }

        // Validasi harga layanan tambahan
        val hargaLayananTambahan = etHargaLayananTambahan.text.toString().trim()
        if (hargaLayananTambahan.isEmpty()) {
            tilHargaLayananTambahan.error = "Harga layanan tambahan tidak boleh kosong"
            isValid = false
        } else {
            // Ambil angka dari format rupiah
            val cleanHarga = hargaLayananTambahan.replace("[^\\d]".toRegex(), "")
            if (cleanHarga.isEmpty() || cleanHarga.toLongOrNull() == null || cleanHarga.toLong() <= 0) {
                tilHargaLayananTambahan.error = "Harga layanan tambahan harus berupa angka yang valid"
                isValid = false
            } else {
                tilHargaLayananTambahan.error = null
            }
        }

        // Validasi cabang
        val cabang = actvCabangLayananTambahan.text.toString().trim()
        if (cabang.isEmpty()) {
            tilCabangLayananTambahan.error = "Pilih cabang"
            isValid = false
        } else {
            tilCabangLayananTambahan.error = null
        }

        return isValid
    }

    private fun updateLayananTambahan() {
        layananTambahanData?.let { originalLayanan ->
            // Ambil data dari form
            val namaLayananTambahan = etNamaLayananTambahan.text.toString().trim()
            val hargaText = etHargaLayananTambahan.text.toString().trim()
            val cabangSelected = actvCabangLayananTambahan.text.toString().trim()

            // Convert harga dari format rupiah ke angka plain
            val cleanHarga = hargaText.replace("[^\\d]".toRegex(), "")

            // Ekstrak nama cabang saja dari format "Nama Cabang (Nama Toko)"
            val cabangName = if (cabangSelected.contains("(") && cabangSelected.contains(")")) {
                cabangSelected.substring(0, cabangSelected.indexOf("(")).trim()
            } else {
                cabangSelected
            }

            // Buat object layanan tambahan yang sudah diupdate
            val updatedLayananTambahan = modelLayanan(
                idLayanan = originalLayanan.idLayanan,
                namaLayanan = namaLayananTambahan,
                hargaLayanan = cleanHarga, // Simpan sebagai angka plain
                jenisLayanan = "Tambahan", // Selalu "Tambahan" untuk layanan tambahan
                cabang = cabangName
            )

            // Tampilkan loading
            btnSimpanPerubahanTambahan.isEnabled = false
            btnSimpanPerubahanTambahan.text = "Menyimpan..."

            // Update ke Firebase
            layananTambahanRef.child(originalLayanan.idLayanan).setValue(updatedLayananTambahan)
                .addOnSuccessListener {
                    Toast.makeText(this, "Layanan tambahan berhasil diperbarui", Toast.LENGTH_SHORT).show()

                    // Set result untuk memberi tahu activity sebelumnya bahwa data berhasil diupdate
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Gagal memperbarui layanan tambahan: ${error.message}", Toast.LENGTH_LONG).show()

                    // Reset button
                    btnSimpanPerubahanTambahan.isEnabled = true
                    btnSimpanPerubahanTambahan.text = "Simpan"
                }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}