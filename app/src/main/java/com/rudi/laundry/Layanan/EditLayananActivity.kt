package com.rudi.laundry.Layanan

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

class EditLayananActivity : AppCompatActivity() {

    // Firebase
    private val database = FirebaseDatabase.getInstance()
    private val layananRef: DatabaseReference = database.getReference("layanan")
    private val cabangRef: DatabaseReference = database.getReference("cabang")

    // Views
    private lateinit var tilNamaLayanan: TextInputLayout
    private lateinit var etNamaLayanan: TextInputEditText
    private lateinit var tilHargaLayanan: TextInputLayout
    private lateinit var etHargaLayanan: TextInputEditText
    private lateinit var tilJenisLayanan: TextInputLayout
    private lateinit var actvJenisLayanan: AutoCompleteTextView
    private lateinit var tilCabangLayanan: TextInputLayout
    private lateinit var actvCabangLayanan: AutoCompleteTextView
    private lateinit var btnBatal: MaterialButton
    private lateinit var btnSimpanPerubahan: MaterialButton

    // Data
    private var layananData: modelLayanan? = null
    private var listCabang = arrayListOf<modelCabang>()
    private var cabangNames = arrayListOf<String>()

    // Dropdown options untuk jenis layanan (sesuai dengan model)
    private val jenisLayananOptions = arrayOf(
        "Cuci Kering",
        "Cuci Setrika",
        "Setrika Saja",
        "Cuci Sepatu",
        "Cuci Karpet",
        "Cuci Boneka",
        "Dry Clean"
    )

    // TextWatcher untuk format rupiah
    private var isFormatting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_layanan)

        initViews()
        setupDropdowns()
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
        tilNamaLayanan = findViewById(R.id.tilNamaLayanan)
        etNamaLayanan = findViewById(R.id.etNamaLayanan)
        tilHargaLayanan = findViewById(R.id.tilHargaLayanan)
        etHargaLayanan = findViewById(R.id.etHargaLayanan)
        tilJenisLayanan = findViewById(R.id.tilJenisLayanan)
        actvJenisLayanan = findViewById(R.id.actvJenisLayanan)
        tilCabangLayanan = findViewById(R.id.tilCabangLayanan)
        actvCabangLayanan = findViewById(R.id.actvCabangLayanan)
        btnBatal = findViewById(R.id.btnBatal)
        btnSimpanPerubahan = findViewById(R.id.btnSimpanPerubahan)
    }


    private fun setupDropdowns() {
        // Setup Jenis Layanan Dropdown
        val jenisAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, jenisLayananOptions)
        actvJenisLayanan.setAdapter(jenisAdapter)
    }

    private fun setupPriceFormatter() {
        etHargaLayanan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true

                val text = s.toString()
                val cleanText = text.replace("[^\\d]".toRegex(), "")

                if (cleanText.isNotEmpty()) {
                    val formatted = formatRupiah(cleanText.toLong())
                    etHargaLayanan.setText(formatted)
                    etHargaLayanan.setSelection(formatted.length)
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
                    layananData?.let { layanan ->
                        fillFormWithData(layanan)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditLayananActivity,
                    "Gagal memuat data cabang: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupCabangAdapter() {
        val cabangAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cabangNames)
        actvCabangLayanan.setAdapter(cabangAdapter)
    }

    private fun getDataFromIntent() {
        // Ambil data layanan dari intent
        layananData = intent.getParcelableExtra("layanan_data")
    }

    private fun fillFormWithData(layanan: modelLayanan) {
        // Isi nama layanan
        etNamaLayanan.setText(layanan.namaLayanan ?: "")

        // Isi harga layanan dengan format yang benar
        val harga = layanan.hargaLayanan
        if (!harga.isNullOrEmpty()) {
            // Jika harga berformat currency, ambil angkanya saja
            val cleanHarga = harga.replace("[^\\d]".toRegex(), "")
            if (cleanHarga.isNotEmpty()) {
                val formattedHarga = formatRupiah(cleanHarga.toLong())
                etHargaLayanan.setText(formattedHarga)
            }
        }

        // Set jenis layanan
        val jenisLayanan = layanan.jenisLayanan
        if (jenisLayanan.isNotEmpty()) {
            actvJenisLayanan.setText(jenisLayanan, false)
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
                actvCabangLayanan.setText(matchingCabang, false)
            } else {
                // Jika tidak ditemukan yang cocok, set langsung
                actvCabangLayanan.setText(cabangLayanan, false)
            }
        }
    }

    private fun setupListeners() {
        btnBatal.setOnClickListener {
            finish()
        }

        btnSimpanPerubahan.setOnClickListener {
            if (validateInput()) {
                updateLayanan()
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        // Validasi nama layanan
        val namaLayanan = etNamaLayanan.text.toString().trim()
        if (namaLayanan.isEmpty()) {
            tilNamaLayanan.error = "Nama layanan tidak boleh kosong"
            isValid = false
        } else {
            tilNamaLayanan.error = null
        }

        // Validasi harga layanan
        val hargaLayanan = etHargaLayanan.text.toString().trim()
        if (hargaLayanan.isEmpty()) {
            tilHargaLayanan.error = "Harga layanan tidak boleh kosong"
            isValid = false
        } else {
            // Ambil angka dari format rupiah
            val cleanHarga = hargaLayanan.replace("[^\\d]".toRegex(), "")
            if (cleanHarga.isEmpty() || cleanHarga.toLongOrNull() == null || cleanHarga.toLong() <= 0) {
                tilHargaLayanan.error = "Harga layanan harus berupa angka yang valid"
                isValid = false
            } else {
                tilHargaLayanan.error = null
            }
        }

        // Validasi jenis layanan
        val jenisLayanan = actvJenisLayanan.text.toString().trim()
        if (jenisLayanan.isEmpty()) {
            tilJenisLayanan.error = "Pilih jenis layanan"
            isValid = false
        } else {
            tilJenisLayanan.error = null
        }

        // Validasi cabang
        val cabang = actvCabangLayanan.text.toString().trim()
        if (cabang.isEmpty()) {
            tilCabangLayanan.error = "Pilih cabang"
            isValid = false
        } else {
            tilCabangLayanan.error = null
        }

        return isValid
    }

    private fun updateLayanan() {
        layananData?.let { originalLayanan ->
            // Ambil data dari form
            val namaLayanan = etNamaLayanan.text.toString().trim()
            val hargaText = etHargaLayanan.text.toString().trim()
            val jenisLayanan = actvJenisLayanan.text.toString().trim()
            val cabangSelected = actvCabangLayanan.text.toString().trim()

            // Convert harga dari format rupiah ke angka plain
            val cleanHarga = hargaText.replace("[^\\d]".toRegex(), "")

            // Ekstrak nama cabang saja dari format "Nama Cabang (Nama Toko)"
            val cabangName = if (cabangSelected.contains("(") && cabangSelected.contains(")")) {
                cabangSelected.substring(0, cabangSelected.indexOf("(")).trim()
            } else {
                cabangSelected
            }

            // Buat object layanan yang sudah diupdate
            val updatedLayanan = modelLayanan(
                idLayanan = originalLayanan.idLayanan,
                namaLayanan = namaLayanan,
                hargaLayanan = cleanHarga, // Simpan sebagai angka plain
                jenisLayanan = jenisLayanan,
                cabang = cabangName
            )

            // Tampilkan loading
            btnSimpanPerubahan.isEnabled = false
            btnSimpanPerubahan.text = "Menyimpan..."

            // Update ke Firebase
            layananRef.child(originalLayanan.idLayanan).setValue(updatedLayanan)
                .addOnSuccessListener {
                    Toast.makeText(this, "Layanan berhasil diperbarui", Toast.LENGTH_SHORT).show()

                    // Set result untuk memberi tahu activity sebelumnya bahwa data berhasil diupdate
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Gagal memperbarui layanan: ${error.message}", Toast.LENGTH_LONG).show()

                    // Reset button
                    btnSimpanPerubahan.isEnabled = true
                    btnSimpanPerubahan.text = "Simpan"
                }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}