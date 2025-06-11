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
import com.google.android.material.textfield.TextInputLayout
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

    // TextInputLayouts for hint changes
    private lateinit var tilNamaToko: TextInputLayout
    private lateinit var tilNamaCabang: TextInputLayout
    private lateinit var tilAlamat: TextInputLayout
    private lateinit var tilTelepon: TextInputLayout

    // Section TextViews
    private lateinit var tvInfoDasar: TextView
    private lateinit var tvJamOperasional: TextView
    private lateinit var tvJamBuka: TextView
    private lateinit var tvJamTutup: TextView
    private lateinit var tvHariOperasional: TextView

    // Chips
    private lateinit var chipSenin: Chip
    private lateinit var chipSelasa: Chip
    private lateinit var chipRabu: Chip
    private lateinit var chipKamis: Chip
    private lateinit var chipJumat: Chip
    private lateinit var chipSabtu: Chip
    private lateinit var chipMinggu: Chip

    // Data
    private var jamBuka = "08:00"
    private var jamTutup = "22:00"
    private var hariOperasional = mutableListOf<Int>()
    private var currentLanguage = "id" // Default bahasa Indonesia

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "toolbar_title" to "Tambah Cabang",
            "basic_info" to "Informasi Dasar",
            "store_name" to "Nama Toko",
            "branch_name" to "Nama Cabang",
            "full_address" to "Alamat Lengkap",
            "phone_number" to "Nomor Telepon",
            "operational_hours" to "Jam Operasional",
            "open_time" to "Jam Buka",
            "close_time" to "Jam Tutup",
            "operational_days" to "Hari Operasional",
            "monday" to "Senin",
            "tuesday" to "Selasa",
            "wednesday" to "Rabu",
            "thursday" to "Kamis",
            "friday" to "Jumat",
            "saturday" to "Sabtu",
            "sunday" to "Minggu",
            "save_branch" to "Simpan Cabang",
            "store_name_required" to "Nama toko tidak boleh kosong",
            "branch_name_required" to "Nama cabang tidak boleh kosong",
            "address_required" to "Alamat tidak boleh kosong",
            "select_day_required" to "Pilih minimal satu hari operasional",
            "branch_added_success" to "Cabang berhasil ditambahkan",
            "branch_add_failed" to "Gagal menambahkan cabang"
        ),
        "en" to mapOf(
            "toolbar_title" to "Add Branch",
            "basic_info" to "Basic Information",
            "store_name" to "Store Name",
            "branch_name" to "Branch Name",
            "full_address" to "Full Address",
            "phone_number" to "Phone Number",
            "operational_hours" to "Operational Hours",
            "open_time" to "Open Time",
            "close_time" to "Close Time",
            "operational_days" to "Operational Days",
            "monday" to "Monday",
            "tuesday" to "Tuesday",
            "wednesday" to "Wednesday",
            "thursday" to "Thursday",
            "friday" to "Friday",
            "saturday" to "Saturday",
            "sunday" to "Sunday",
            "save_branch" to "Save Branch",
            "store_name_required" to "Store name cannot be empty",
            "branch_name_required" to "Branch name cannot be empty",
            "address_required" to "Address cannot be empty",
            "select_day_required" to "Select at least one operational day",
            "branch_added_success" to "Branch added successfully",
            "branch_add_failed" to "Failed to add branch"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_cabang)

        // Load saved language preference
        loadLanguagePreference()

        initViews()
        setupToolbar()
        setupDefaultValues()
        setupListeners()
        updateAllTexts()
    }

    private fun loadLanguagePreference() {
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"
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

        // TextInputLayouts
        tilNamaToko = findViewById(R.id.tilNamaToko)
        tilNamaCabang = findViewById(R.id.tilNamaCabang)
        tilAlamat = findViewById(R.id.tilAlamat)
        tilTelepon = findViewById(R.id.tilTelepon)

        // Section TextViews
        tvInfoDasar = findViewById(R.id.tvInfoDasar)
        tvJamOperasional = findViewById(R.id.tvJamOperasional)
        tvJamBuka = findViewById(R.id.tvJamBuka)
        tvJamTutup = findViewById(R.id.tvJamTutup)
        tvHariOperasional = findViewById(R.id.tvHariOperasional)

        // Chips
        chipSenin = findViewById(R.id.chipSenin)
        chipSelasa = findViewById(R.id.chipSelasa)
        chipRabu = findViewById(R.id.chipRabu)
        chipKamis = findViewById(R.id.chipKamis)
        chipJumat = findViewById(R.id.chipJumat)
        chipSabtu = findViewById(R.id.chipSabtu)
        chipMinggu = findViewById(R.id.chipMinggu)
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

        setupChipListeners()

        btnSimpan.setOnClickListener {
            simpanCabang()
        }
    }

    private fun setupChipListeners() {
        val chipIds = listOf(
            R.id.chipSenin to 1,
            R.id.chipSelasa to 2,
            R.id.chipRabu to 3,
            R.id.chipKamis to 4,
            R.id.chipJumat to 5,
            R.id.chipSabtu to 6,
            R.id.chipMinggu to 7
        )

        chipIds.forEach { (chipId, dayValue) ->
            val chip = findViewById<Chip>(chipId)
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!hariOperasional.contains(dayValue)) {
                        hariOperasional.add(dayValue)
                    }
                } else {
                    hariOperasional.remove(dayValue)
                }

                android.util.Log.d("HariOperasional", "Chip ${chip.text} ${if (isChecked) "checked" else "unchecked"}")
                android.util.Log.d("HariOperasional", "Current days: ${hariOperasional.sorted()}")
            }
        }
    }

    private fun updateAllTexts() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Update toolbar title
        toolbar.title = texts["toolbar_title"]

        // Update section titles
        tvInfoDasar.text = texts["basic_info"]
        tvJamOperasional.text = texts["operational_hours"]
        tvJamBuka.text = texts["open_time"]
        tvJamTutup.text = texts["close_time"]
        tvHariOperasional.text = texts["operational_days"]

        // Update input field hints
        tilNamaToko.hint = texts["store_name"]
        tilNamaCabang.hint = texts["branch_name"]
        tilAlamat.hint = texts["full_address"]
        tilTelepon.hint = texts["phone_number"]

        // Update chip texts
        chipSenin.text = texts["monday"]
        chipSelasa.text = texts["tuesday"]
        chipRabu.text = texts["wednesday"]
        chipKamis.text = texts["thursday"]
        chipJumat.text = texts["friday"]
        chipSabtu.text = texts["saturday"]
        chipMinggu.text = texts["sunday"]

        // Update button text
        btnSimpan.text = texts["save_branch"]
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
        chipGroupHari.clearCheck()

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
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        val namaToko = etNamaToko.text.toString().trim()
        val namaCabang = etNamaCabang.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val telepon = etTelepon.text.toString().trim()

        // Validasi input with localized messages
        if (namaToko.isEmpty()) {
            etNamaToko.error = texts["store_name_required"]
            etNamaToko.requestFocus()
            return
        }

        if (namaCabang.isEmpty()) {
            etNamaCabang.error = texts["branch_name_required"]
            etNamaCabang.requestFocus()
            return
        }

        if (alamat.isEmpty()) {
            etAlamat.error = texts["address_required"]
            etAlamat.requestFocus()
            return
        }

        android.util.Log.d("ValidationDebug", "Hari operasional saat validasi: ${hariOperasional.sorted()}")
        android.util.Log.d("ValidationDebug", "Size: ${hariOperasional.size}")

        if (hariOperasional.isEmpty()) {
            Toast.makeText(this, texts["select_day_required"], Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        val cabangId = myRef.push().key ?: ""
        val terdaftar = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val hariOperasionalString = hariOperasional.sorted().joinToString(",")

        android.util.Log.d("SaveDebug", "Saving cabang with days: $hariOperasionalString")

        val cabang = modelCabang(
            idCabang = cabangId,
            namaToko = namaToko,
            namaCabang = namaCabang,
            alamatCabang = alamat,
            noTelpCabang = telepon,
            statusOperasional = "TUTUP",
            jamBuka = jamBuka,
            jamTutup = jamTutup,
            hariOperasional = hariOperasionalString,
            terdaftar = terdaftar
        )

        myRef.child(cabangId).setValue(cabang)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, texts["branch_added_success"], Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { error ->
                showLoading(false)
                Toast.makeText(this, "${texts["branch_add_failed"]}: ${error.message}", Toast.LENGTH_SHORT).show()
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

    override fun onResume() {
        super.onResume()
        // Check if language changed while away
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("selected_language", "id") ?: "id"
        if (savedLanguage != currentLanguage) {
            currentLanguage = savedLanguage
            updateAllTexts()
        }
    }
}