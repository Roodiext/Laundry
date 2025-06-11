package com.rudi.laundry.laporan

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.rudi.laundry.R
import com.rudi.laundry.adapter.AdapterlaporanTransaksi
import com.rudi.laundry.modeldata.modelTransaksi
import java.text.SimpleDateFormat
import java.util.*

class DataLaporanActivity : AppCompatActivity() {

    private lateinit var tabBelumBayar: TextView
    private lateinit var tabSudahBayar: TextView
    private lateinit var tabSelesai: TextView
    private lateinit var indicator: View
    private lateinit var recyclerViewTransaksi: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var headerTitle: TextView
    private lateinit var headerSubtitle: TextView
    private lateinit var emptyStateTitle: TextView
    private lateinit var emptyStateSubtitle: TextView

    private lateinit var adapterTransaksi: AdapterlaporanTransaksi
    private val allTransaksiList = ArrayList<modelTransaksi>()
    private val filteredTransaksiList = ArrayList<modelTransaksi>()

    private var currentTab = modelTransaksi.STATUS_BELUM_BAYAR
    private var currentLanguage = "id" // Default bahasa Indonesia

    // Firebase reference
    private val database = FirebaseDatabase.getInstance()
    private val transaksiRef: DatabaseReference = database.getReference("transaksi")

    // Language texts for DataLaporanActivity
    private val languageTexts = mapOf(
        "id" to mapOf(
            "header_title" to "Data Laporan Transaksi",
            "header_subtitle" to "Kelola semua transaksi laundry",
            "tab_unpaid" to "Belum Bayar",
            "tab_paid" to "Sudah Bayar",
            "tab_completed" to "Selesai",
            "empty_title" to "Tidak ada transaksi",
            "empty_subtitle" to "Belum ada data transaksi untuk kategori ini",
            "confirm_payment_title" to "Konfirmasi Pembayaran",
            "confirm_payment_message" to "Apakah transaksi {nama} sudah dibayar?",
            "confirm_pickup_title" to "Konfirmasi Pengambilan",
            "confirm_pickup_message" to "Apakah laundry {nama} sudah diambil?",
            "btn_paid" to "Sudah Bayar",
            "btn_picked_up" to "Sudah Diambil",
            "btn_cancel" to "Batal",
            "btn_close" to "Tutup",
            "status_updated" to "Status transaksi berhasil diupdate",
            "error_load_data" to "Error memuat data transaksi",
            "error_update" to "Error",
            "detail_transaction_id" to "ID Transaksi",
            "detail_customer_name" to "Nama Pelanggan",
            "detail_phone_number" to "Nomor HP",
            "detail_transaction_date" to "Tanggal Transaksi",
            "detail_status" to "Status",
            "detail_payment_method" to "Metode Pembayaran"
        ),
        "en" to mapOf(
            "header_title" to "Transaction Report Data",
            "header_subtitle" to "Manage all laundry transactions",
            "tab_unpaid" to "Unpaid",
            "tab_paid" to "Paid",
            "tab_completed" to "Completed",
            "empty_title" to "No transactions",
            "empty_subtitle" to "No transaction data for this category yet",
            "confirm_payment_title" to "Payment Confirmation",
            "confirm_payment_message" to "Has {nama} transaction been paid?",
            "confirm_pickup_title" to "Pickup Confirmation",
            "confirm_pickup_message" to "Has {nama} laundry been picked up?",
            "btn_paid" to "Paid",
            "btn_picked_up" to "Picked Up",
            "btn_cancel" to "Cancel",
            "btn_close" to "Close",
            "status_updated" to "Transaction status successfully updated",
            "error_load_data" to "Error loading transaction data",
            "error_update" to "Error",
            "detail_transaction_id" to "Transaction ID",
            "detail_customer_name" to "Customer Name",
            "detail_phone_number" to "Phone Number",
            "detail_transaction_date" to "Transaction Date",
            "detail_status" to "Status",
            "detail_payment_method" to "Payment Method"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_laporan)

        // Load saved language preference
        loadLanguagePreference()

        initViews()
        setupRecyclerView()
        setupTabListeners()
        loadTransaksiData()
        updateAllTexts()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadLanguagePreference() {
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"
    }

    private fun initViews() {
        tabBelumBayar = findViewById(R.id.tabBelumBayar)
        tabSudahBayar = findViewById(R.id.tabSudahBayar)
        tabSelesai = findViewById(R.id.tabSelesai)
        indicator = findViewById(R.id.indicator)
        recyclerViewTransaksi = findViewById(R.id.recyclerViewTransaksi)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)

        // Initialize header TextViews (assuming they exist in the layout)
        headerTitle = findViewById(R.id.headerTitle) // You may need to add ID to XML
        headerSubtitle = findViewById(R.id.headerSubtitle) // You may need to add ID to XML
        emptyStateTitle = findViewById(R.id.emptyStateTitle) // You may need to add ID to XML
        emptyStateSubtitle = findViewById(R.id.emptyStateSubtitle) // You may need to add ID to XML
    }

    private fun updateAllTexts() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Update header texts
        headerTitle.text = texts["header_title"]
        headerSubtitle.text = texts["header_subtitle"]

        // Update tab texts
        tabBelumBayar.text = texts["tab_unpaid"]
        tabSudahBayar.text = texts["tab_paid"]
        tabSelesai.text = texts["tab_completed"]

        // Update empty state texts
        emptyStateTitle.text = texts["empty_title"]
        emptyStateSubtitle.text = texts["empty_subtitle"]

        // Update adapter language
        updateAdapterLanguage()
    }

    private fun setupRecyclerView() {
        adapterTransaksi = AdapterlaporanTransaksi(
            filteredTransaksiList,
            onItemClick = { transaksi ->
                showDetailTransaksi(transaksi)
            },
            onButtonClick = { transaksi ->
                handleButtonClick(transaksi)
            },
            context = this // Tambahkan parameter context
        )

        recyclerViewTransaksi.layoutManager = LinearLayoutManager(this)
        recyclerViewTransaksi.adapter = adapterTransaksi
    }

    private fun updateAdapterLanguage() {
        if (::adapterTransaksi.isInitialized) {
            adapterTransaksi.updateLanguage(currentLanguage)
        }
    }

    private fun setupTabListeners() {
        tabBelumBayar.setOnClickListener {
            selectTab(modelTransaksi.STATUS_BELUM_BAYAR)
        }

        tabSudahBayar.setOnClickListener {
            selectTab(modelTransaksi.STATUS_SUDAH_BAYAR)
        }

        tabSelesai.setOnClickListener {
            selectTab(modelTransaksi.STATUS_SELESAI)
        }
    }

    private fun selectTab(status: String) {
        currentTab = status

        // Reset semua tab
        resetAllTabs()

        // Set tab aktif
        when (status) {
            modelTransaksi.STATUS_BELUM_BAYAR -> {
                tabBelumBayar.setTextColor(Color.parseColor("#007AFF"))
                tabBelumBayar.setTypeface(null, android.graphics.Typeface.BOLD)
                moveIndicator(tabBelumBayar)
            }
            modelTransaksi.STATUS_SUDAH_BAYAR -> {
                tabSudahBayar.setTextColor(Color.parseColor("#007AFF"))
                tabSudahBayar.setTypeface(null, android.graphics.Typeface.BOLD)
                moveIndicator(tabSudahBayar)
            }
            modelTransaksi.STATUS_SELESAI -> {
                tabSelesai.setTextColor(Color.parseColor("#007AFF"))
                tabSelesai.setTypeface(null, android.graphics.Typeface.BOLD)
                moveIndicator(tabSelesai)
            }
        }

        filterTransaksiByStatus()
    }

    private fun resetAllTabs() {
        val defaultColor = Color.parseColor("#666666")
        tabBelumBayar.setTextColor(defaultColor)
        tabSudahBayar.setTextColor(defaultColor)
        tabSelesai.setTextColor(defaultColor)

        tabBelumBayar.setTypeface(null, android.graphics.Typeface.NORMAL)
        tabSudahBayar.setTypeface(null, android.graphics.Typeface.NORMAL)
        tabSelesai.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    private fun moveIndicator(targetTab: TextView) {
        val params = indicator.layoutParams as ConstraintLayout.LayoutParams

        when (targetTab.id) {
            R.id.tabBelumBayar -> {
                params.startToStart = R.id.tabBelumBayar
                params.endToEnd = R.id.tabBelumBayar
            }
            R.id.tabSudahBayar -> {
                params.startToStart = R.id.tabSudahBayar
                params.endToEnd = R.id.tabSudahBayar
            }
            R.id.tabSelesai -> {
                params.startToStart = R.id.tabSelesai
                params.endToEnd = R.id.tabSelesai
            }
        }

        indicator.layoutParams = params
    }

    private fun loadTransaksiData() {
        transaksiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allTransaksiList.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val transaksi = dataSnapshot.getValue(modelTransaksi::class.java)
                        if (transaksi != null) {
                            // Pastikan ID transaksi ter-set
                            if (transaksi.idTransaksi.isEmpty()) {
                                transaksi.idTransaksi = dataSnapshot.key ?: ""
                            }
                            allTransaksiList.add(transaksi)
                        }
                    }
                }

                // Sort berdasarkan timestamp terbaru
                allTransaksiList.sortByDescending { it.timestamp }

                // Filter berdasarkan tab yang aktif
                filterTransaksiByStatus()
            }

            override fun onCancelled(error: DatabaseError) {
                val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
                Toast.makeText(this@DataLaporanActivity,
                    "${texts["error_load_data"]}: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterTransaksiByStatus() {
        filteredTransaksiList.clear()

        for (transaksi in allTransaksiList) {
            if (transaksi.status == currentTab) {
                filteredTransaksiList.add(transaksi)
            }
        }

        adapterTransaksi.notifyDataSetChanged()

        // Show/hide empty state
        if (filteredTransaksiList.isEmpty()) {
            recyclerViewTransaksi.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            recyclerViewTransaksi.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
        }
    }

    private fun handleButtonClick(transaksi: modelTransaksi) {
        when (transaksi.status) {
            modelTransaksi.STATUS_BELUM_BAYAR -> {
                showConfirmPaymentDialog(transaksi)
            }
            modelTransaksi.STATUS_SUDAH_BAYAR -> {
                showConfirmPickupDialog(transaksi)
            }
        }
    }

    private fun showConfirmPaymentDialog(transaksi: modelTransaksi) {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
        val message = texts["confirm_payment_message"]?.replace("{nama}", transaksi.namaPelanggan) ?: ""

        AlertDialog.Builder(this)
            .setTitle(texts["confirm_payment_title"])
            .setMessage(message)
            .setPositiveButton(texts["btn_paid"]) { _, _ ->
                updateTransaksiStatus(transaksi, modelTransaksi.STATUS_SUDAH_BAYAR)
            }
            .setNegativeButton(texts["btn_cancel"], null)
            .show()
    }

    private fun showConfirmPickupDialog(transaksi: modelTransaksi) {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
        val message = texts["confirm_pickup_message"]?.replace("{nama}", transaksi.namaPelanggan) ?: ""

        AlertDialog.Builder(this)
            .setTitle(texts["confirm_pickup_title"])
            .setMessage(message)
            .setPositiveButton(texts["btn_picked_up"]) { _, _ ->
                val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                transaksi.tanggalPengambilan = currentTime
                updateTransaksiStatus(transaksi, modelTransaksi.STATUS_SELESAI)
            }
            .setNegativeButton(texts["btn_cancel"], null)
            .show()
    }

    private fun updateTransaksiStatus(transaksi: modelTransaksi, newStatus: String) {
        transaksi.status = newStatus

        transaksiRef.child(transaksi.idTransaksi).setValue(transaksi)
            .addOnSuccessListener {
                val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
                Toast.makeText(this, texts["status_updated"], Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
                Toast.makeText(this, "${texts["error_update"]}: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDetailTransaksi(transaksi: modelTransaksi) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_detail_transaksi)

        val window = dialog.window
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Set data transaksi ke dialog
        dialog.findViewById<TextView>(R.id.txtIdTransaksi)?.text = transaksi.idTransaksi
        dialog.findViewById<TextView>(R.id.txtNamaPelanggan)?.text = transaksi.namaPelanggan
        dialog.findViewById<TextView>(R.id.txtNomorHP)?.text = transaksi.nomorHP
        dialog.findViewById<TextView>(R.id.txtTanggalTransaksi)?.text = transaksi.tanggalTransaksi
        dialog.findViewById<TextView>(R.id.txtStatus)?.text = transaksi.getDisplayStatus()
        dialog.findViewById<TextView>(R.id.txtMetodePembayaran)?.text = transaksi.metodePembayaran

        val btnTutup = dialog.findViewById<Button>(R.id.btnTutup)
        btnTutup?.text = texts["btn_close"]
        btnTutup?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        // Reload language preference when activity resumes
        val newLanguage = getSharedPreferences("language_pref", MODE_PRIVATE)
            .getString("selected_language", "id") ?: "id"

        if (newLanguage != currentLanguage) {
            currentLanguage = newLanguage
            updateAllTexts()
            // Refresh data untuk memastikan format yang konsisten
            filterTransaksiByStatus()
        }
    }
}