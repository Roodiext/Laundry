package com.rudi.laundry

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*
import com.rudi.laundry.Layanan.DataLayananActivity
import com.rudi.laundry.LayananTambahan.DataLayananTambahanActivity
import com.rudi.laundry.Pegawai.DataPegawaiActivity
import com.rudi.laundry.cabang.DataCabangActivity
import com.rudi.laundry.laporan.DataLaporanActivity
import com.rudi.laundry.pelanggan.DataPelangganActivity
import com.rudi.laundry.transaksi.transaksi
import com.rudi.laundry.modeldata.modelTransaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Laundry : AppCompatActivity() {

    private lateinit var totalPendapatanTextView: TextView
    private lateinit var switchLanguage: Switch
    private lateinit var greetingTextView: TextView
    private lateinit var estimasiTextView: TextView
    private lateinit var transaksiTextView: TextView
    private lateinit var customerTextView: TextView
    private lateinit var reportTextView: TextView
    private lateinit var txt1TextView: TextView
    private lateinit var txt2TextView: TextView

    // TextViews untuk GridLayout
    private lateinit var accountTextView: TextView
    private lateinit var layananTextView: TextView
    private lateinit var tambahTextView: TextView
    private lateinit var pegawaiTextView: TextView
    private lateinit var cabangTextView: TextView
    private lateinit var printerTextView: TextView

    private var valueEventListener: ValueEventListener? = null
    private var currentLanguage = "id" // Default bahasa Indonesia

    // Firebase reference
    private val database = FirebaseDatabase.getInstance()
    private val transaksiRef: DatabaseReference = database.getReference("transaksi")

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "good_morning" to "Selamat Pagi",
            "good_afternoon" to "Selamat Siang",
            "good_evening" to "Selamat Sore",
            "good_night" to "Selamat Malam",
            "today_estimate" to "Estimasi Hari Ini",
            "transaction" to "Transaksi",
            "customer" to "Pelanggan",
            "report" to "Laporan",
            "ready_to_serve" to "Siap melayani pelanggan dengan setulus hati",
            "dont_disappoint" to "Jangan kecewakan pelanggan!",
            "account" to "Akun",
            "service" to "Layanan",
            "add" to "Tambah",
            "employee" to "Pegawai",
            "branch" to "Cabang",
            "printer" to "Printer",
            "language_label" to "Bahasa Indonesia"
        ),
        "en" to mapOf(
            "good_morning" to "Good Morning",
            "good_afternoon" to "Good Afternoon",
            "good_evening" to "Good Evening",
            "good_night" to "Good Night",
            "today_estimate" to "Today's Estimate",
            "transaction" to "Transaction",
            "customer" to "Customer",
            "report" to "Report",
            "ready_to_serve" to "Ready to serve customers wholeheartedly",
            "dont_disappoint" to "Don't disappoint customers!",
            "account" to "Account",
            "service" to "Service",
            "add" to "Add",
            "employee" to "Employee",
            "branch" to "Branch",
            "printer" to "Printer",
            "language_label" to "English"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laundry)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupLanguageSwitch()
        setupGreetingAndDate()
        setupRealTimeTotalPendapatan()
        updateAllTexts()
    }

    private fun initViews() {
        greetingTextView = findViewById<TextView>(R.id.name)
        val dateTextView = findViewById<TextView>(R.id.date)
        totalPendapatanTextView = findViewById<TextView>(R.id.totalPendapatan)
        switchLanguage = findViewById<Switch>(R.id.switchLanguage)
        estimasiTextView = findViewById<TextView>(R.id.Estimasi)
        transaksiTextView = findViewById<TextView>(R.id.transaksi_txt)
        customerTextView = findViewById<TextView>(R.id.customer_txt)
        reportTextView = findViewById<TextView>(R.id.report_txt)
        txt1TextView = findViewById<TextView>(R.id.txt1)
        txt2TextView = findViewById<TextView>(R.id.txt2)

        // Init GridLayout TextViews
        accountTextView = findViewById<TextView>(R.id.account_txt)
        layananTextView = findViewById<TextView>(R.id.layanan_txt)
        tambahTextView = findViewById<TextView>(R.id.tambah_txt)
        pegawaiTextView = findViewById<TextView>(R.id.pegawai_txt)
        cabangTextView = findViewById<TextView>(R.id.cabang_txt)
        printerTextView = findViewById<TextView>(R.id.printer_txt)
    }

    private fun setupLanguageSwitch() {
        // Load saved language preference
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"
        switchLanguage.isChecked = currentLanguage == "en"

        switchLanguage.setOnCheckedChangeListener { _, isChecked ->
            currentLanguage = if (isChecked) "en" else "id"

            // Save language preference
            with(sharedPref.edit()) {
                putString("selected_language", currentLanguage)
                apply()
            }

            updateAllTexts()
            setupGreetingAndDate() // Update greeting dengan bahasa baru
        }
    }

    private fun setupGreetingAndDate() {
        val dateTextView = findViewById<TextView>(R.id.date)
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val nama = prefs.getString("nama", "Pengguna")

        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        val greeting = when (hour) {
            in 4..10 -> "${texts["good_morning"]}, $nama"
            in 11..15 -> "${texts["good_afternoon"]}, $nama"
            in 16..18 -> "${texts["good_evening"]}, $nama"
            else -> "${texts["good_night"]}, $nama"
        }

        greetingTextView.text = greeting

        // Format tanggal sesuai bahasa
        val locale = if (currentLanguage == "en") Locale.ENGLISH else Locale("id", "ID")
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", locale)
        val currentDate = dateFormat.format(calendar.time)
        dateTextView.text = currentDate
    }

    private fun updateAllTexts() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Update main texts
        estimasiTextView.text = texts["today_estimate"]
        transaksiTextView.text = texts["transaction"]
        customerTextView.text = texts["customer"]
        reportTextView.text = texts["report"]
        txt1TextView.text = texts["ready_to_serve"]
        txt2TextView.text = texts["dont_disappoint"]

        // Update GridLayout texts
        accountTextView.text = texts["account"]
        layananTextView.text = texts["service"]
        tambahTextView.text = texts["add"]
        pegawaiTextView.text = texts["employee"]
        cabangTextView.text = texts["branch"]
        printerTextView.text = texts["printer"]

        // Update switch label
        val switchLabel = findViewById<TextView>(R.id.language_label)
        switchLabel.text = texts["language_label"]
    }

    private fun setupRealTimeTotalPendapatan() {
        // Hapus listener lama jika ada
        if (valueEventListener != null) {
            transaksiRef.removeEventListener(valueEventListener!!)
        }

        // Buat listener baru
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalPendapatan = 0.0
                val todayDate = getTodayDateString()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val transaksi = dataSnapshot.getValue(modelTransaksi::class.java)
                        if (transaksi != null) {
                            // Hitung pendapatan dari transaksi yang sudah selesai
                            if (transaksi.status == modelTransaksi.STATUS_SELESAI) {
                                // Untuk menghitung hanya hari ini, uncomment baris di bawah
                                // val transaksiDate = getDateFromTimestamp(transaksi.tanggalTransaksi)
                                // if (transaksiDate == todayDate) {
                                totalPendapatan += transaksi.totalBayar
                                // }
                            }
                        }
                    }
                }

                // Update tampilan total pendapatan
                updateTotalPendapatanDisplay(totalPendapatan)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error jika diperlukan
                totalPendapatanTextView.text = "ᴿᵖ.0,-"
            }
        }

        // Tambahkan listener ke Firebase
        transaksiRef.addValueEventListener(valueEventListener!!)
    }

    private fun updateTotalPendapatanDisplay(total: Double) {
        val formattedTotal = formatRupiah(total)
        totalPendapatanTextView.text = formattedTotal
    }

    private fun formatRupiah(amount: Double): String {
        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        return formatter.format(amount).replace("Rp", "ᴿᵖ")
    }

    private fun getTodayDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Calendar.getInstance().time)
    }

    private fun getDateFromTimestamp(timestamp: String): String {
        return try {
            // Ambil bagian tanggal saja (yyyy-MM-dd) dari timestamp
            if (timestamp.contains(" ")) {
                timestamp.split(" ")[0]
            } else {
                timestamp
            }
        } catch (e: Exception) {
            ""
        }
    }

    // Fungsi untuk menghitung pendapatan hari ini saja
    fun loadTotalPendapatanHariIni() {
        // Hapus listener lama jika ada
        if (valueEventListener != null) {
            transaksiRef.removeEventListener(valueEventListener!!)
        }

        // Buat listener baru untuk hari ini saja
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalPendapatanHariIni = 0.0
                val todayDate = getTodayDateString()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val transaksi = dataSnapshot.getValue(modelTransaksi::class.java)
                        if (transaksi != null && transaksi.status == modelTransaksi.STATUS_SELESAI) {
                            val transaksiDate = getDateFromTimestamp(transaksi.tanggalTransaksi)
                            if (transaksiDate == todayDate) {
                                totalPendapatanHariIni += transaksi.totalBayar
                            }
                        }
                    }
                }

                updateTotalPendapatanDisplay(totalPendapatanHariIni)
            }

            override fun onCancelled(error: DatabaseError) {
                totalPendapatanTextView.text = "ᴿᵖ.0,-"
            }
        }

        // Tambahkan listener ke Firebase
        transaksiRef.addValueEventListener(valueEventListener!!)
    }

    fun account(view: View?) {
        val intent: Intent = Intent(
            this@Laundry,
            profile::class.java
        )
        startActivity(intent)
    }

    fun Cabang(view: View?) {
        val intent: Intent = Intent(
            this@Laundry,
            DataCabangActivity::class.java
        )
        startActivity(intent)
    }

    fun pelanggan(view: View?) {
        val intent: Intent = Intent(
            this@Laundry,
            DataPelangganActivity::class.java
        )
        startActivity(intent)
    }

    fun pegawai(view: View?) {
        val intent: Intent = Intent(
            this@Laundry,
            DataPegawaiActivity::class.java
        )
        startActivity(intent)
    }

    fun layanan(view: View?) {
        val intent: Intent = Intent(
            this@Laundry,
            DataLayananActivity::class.java
        )
        startActivity(intent)
    }

    fun layanantambahan(view: View) {
        val intent: Intent = Intent(
            this@Laundry,
            DataLayananTambahanActivity::class.java
        )
        startActivity(intent)
    }

    fun transaksi(view: View?) {
        val intent: Intent = Intent(
            this@Laundry,
            transaksi::class.java
        )
        startActivity(intent)
    }

    fun laporan(view: View?) {
        val intent: Intent = Intent(
            this@Laundry,
            DataLaporanActivity::class.java
        )
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Setup ulang listener ketika activity kembali aktif
        setupRealTimeTotalPendapatan()
        updateAllTexts()
        setupGreetingAndDate()
    }

    override fun onPause() {
        super.onPause()
        // Hapus listener ketika activity tidak aktif untuk menghemat resource
        if (valueEventListener != null) {
            transaksiRef.removeEventListener(valueEventListener!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Pastikan listener dihapus ketika activity di-destroy
        if (valueEventListener != null) {
            transaksiRef.removeEventListener(valueEventListener!!)
        }
    }
}