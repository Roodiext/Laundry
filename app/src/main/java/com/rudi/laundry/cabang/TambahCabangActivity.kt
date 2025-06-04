package com.rudi.laundry.cabang

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelCabang
import java.text.SimpleDateFormat
import java.util.*

class TambahCabangActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("cabang")

    // Views
    private lateinit var toolbar: MaterialToolbar
    private lateinit var etNamaToko: TextInputEditText
    private lateinit var etNamaCabang: TextInputEditText
    private lateinit var etAlamat: TextInputEditText
    private lateinit var etTelepon: TextInputEditText
    private lateinit var btnJamBuka: MaterialButton
    private lateinit var btnJamTutup: MaterialButton
    private lateinit var chipGroupHari: ChipGroup
    private lateinit var btnSimpan: MaterialButton
    private lateinit var progressBar: ProgressBar

    // Data
    private var jamBuka = "08:00"
    private var jamTutup = "22:00"
    private var hariOperasional = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_cabang)

        initViews()
        setupToolbar()
        setupDefaultValues()
        setupListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        etNamaToko = findViewById(R.id.etNamaToko)
        etNamaCabang = findViewById(R.id.etNamaCabang)
        etAlamat = findViewById(R.id.etAlamat)
        etTelepon = findViewById(R.id.etTelepon)
        btnJamBuka = findViewById(R.id.btnJamBuka)
        btnJamTutup = findViewById(R.id.btnJamTutup)
        chipGroupHari = findViewById(R.id.chipGroupHari)
        btnSimpan = findViewById(R.id.btnSimpan)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupDefaultValues() {
        // Set default jam operasional
        btnJamBuka.text = jamBuka
        btnJamTutup.text = jamTutup

        // Set default hari operasional (Senin - Minggu)
        for (i in 1..7) {
            hariOperasional.add(i)
        }
        updateChipSelection()
    }

    private fun setupListeners() {
        btnJamBuka.setOnClickListener {
            showTimePickerDialog(true)
        }

        btnJamTutup.setOnClickListener {
            showTimePickerDialog(false)
        }

        // Setup chip group listener
        chipGroupHari.setOnCheckedStateChangeListener { group, checkedIds ->
            hariOperasional.clear()
            for (id in checkedIds) {
                val chip = findViewById<Chip>(id)
                val day = when (chip.text.toString()) {
                    "Sen" -> 1
                    "Sel" -> 2
                    "Rab" -> 3
                    "Kam" -> 4
                    "Jum" -> 5
                    "Sab" -> 6
                    "Min" -> 7
                    else -> 0
                }
                if (day != 0) {
                    hariOperasional.add(day)
                }
            }
        }

        btnSimpan.setOnClickListener {
            simpanCabang()
        }
    }

    private fun showTimePickerDialog(isOpenTime: Boolean) {
        val currentTime = if (isOpenTime) jamBuka else jamTutup
        val timeParts = currentTime.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
                if (isOpenTime) {
                    jamBuka = timeString
                    btnJamBuka.text = jamBuka
                } else {
                    jamTutup = timeString
                    btnJamTutup.text = jamTutup
                }
            },
            hour,
            minute,
            true
        ).show()
    }

    private fun updateChipSelection() {
        // Clear all selections first
        chipGroupHari.clearCheck()

        // Select chips based on hariOperasional
        for (day in hariOperasional) {
            val chipId = when (day) {
                1 -> R.id.chipSenin
                2 -> R.id.chipSelasa
                3 -> R.id.chipRabu
                4 -> R.id.chipKamis
                5 -> R.id.chipJumat
                6 -> R.id.chipSabtu
                7 -> R.id.chipMinggu
                else -> 0
            }
            if (chipId != 0) {
                chipGroupHari.check(chipId)
            }
        }
    }

    private fun simpanCabang() {
        val namaToko = etNamaToko.text.toString().trim()
        val namaCabang = etNamaCabang.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val telepon = etTelepon.text.toString().trim()

        // Validasi input
        if (namaToko.isEmpty()) {
            etNamaToko.error = "Nama toko tidak boleh kosong"
            etNamaToko.requestFocus()
            return
        }

        if (namaCabang.isEmpty()) {
            etNamaCabang.error = "Nama cabang tidak boleh kosong"
            etNamaCabang.requestFocus()
            return
        }

        if (alamat.isEmpty()) {
            etAlamat.error = "Alamat tidak boleh kosong"
            etAlamat.requestFocus()
            return
        }

        if (hariOperasional.isEmpty()) {
            Toast.makeText(this, "Pilih minimal satu hari operasional", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Generate ID unik
        val cabangId = myRef.push().key ?: ""
        val terdaftar = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Convert hari operasional ke string
        val hariOperasionalString = hariOperasional.sorted().joinToString(",")

        val cabang = modelCabang(
            idCabang = cabangId,
            namaToko = namaToko,
            namaCabang = namaCabang,
            alamatCabang = alamat,
            noTelpCabang = telepon,
            statusOperasional = "TUTUP", // Will be calculated based on real-time
            jamBuka = jamBuka,
            jamTutup = jamTutup,
            hariOperasional = hariOperasionalString,
            terdaftar = terdaftar
        )

        myRef.child(cabangId).setValue(cabang)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Cabang berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { error ->
                showLoading(false)
                Toast.makeText(this, "Gagal menambahkan cabang: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSimpan.isEnabled = !show
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}