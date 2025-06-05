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

    private lateinit var adapterTransaksi: AdapterlaporanTransaksi
    private val allTransaksiList = ArrayList<modelTransaksi>()
    private val filteredTransaksiList = ArrayList<modelTransaksi>()

    private var currentTab = modelTransaksi.STATUS_BELUM_BAYAR

    // Firebase reference
    private val database = FirebaseDatabase.getInstance()
    private val transaksiRef: DatabaseReference = database.getReference("transaksi")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_laporan)

        initViews()
        setupRecyclerView()
        setupTabListeners()
        loadTransaksiData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        tabBelumBayar = findViewById(R.id.tabBelumBayar)
        tabSudahBayar = findViewById(R.id.tabSudahBayar)
        tabSelesai = findViewById(R.id.tabSelesai)
        indicator = findViewById(R.id.indicator)
        recyclerViewTransaksi = findViewById(R.id.recyclerViewTransaksi)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
    }

    private fun setupRecyclerView() {
        adapterTransaksi = AdapterlaporanTransaksi(
            filteredTransaksiList,
            onItemClick = { transaksi ->
                showDetailTransaksi(transaksi)
            },
            onButtonClick = { transaksi ->
                handleButtonClick(transaksi)
            }
        )

        recyclerViewTransaksi.layoutManager = LinearLayoutManager(this)
        recyclerViewTransaksi.adapter = adapterTransaksi
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
                tabBelumBayar.setTextColor(Color.parseColor("#FF3D00"))
                tabBelumBayar.setTypeface(null, android.graphics.Typeface.BOLD)
                moveIndicator(tabBelumBayar)
            }
            modelTransaksi.STATUS_SUDAH_BAYAR -> {
                tabSudahBayar.setTextColor(Color.parseColor("#FF3D00"))
                tabSudahBayar.setTypeface(null, android.graphics.Typeface.BOLD)
                moveIndicator(tabSudahBayar)
            }
            modelTransaksi.STATUS_SELESAI -> {
                tabSelesai.setTextColor(Color.parseColor("#FF3D00"))
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
                Toast.makeText(this@DataLaporanActivity,
                    "Error memuat data transaksi: ${error.message}", Toast.LENGTH_SHORT).show()
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
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pembayaran")
            .setMessage("Apakah transaksi ${transaksi.namaPelanggan} sudah dibayar?")
            .setPositiveButton("Sudah Bayar") { _, _ ->
                updateTransaksiStatus(transaksi, modelTransaksi.STATUS_SUDAH_BAYAR)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showConfirmPickupDialog(transaksi: modelTransaksi) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pengambilan")
            .setMessage("Apakah laundry ${transaksi.namaPelanggan} sudah diambil?")
            .setPositiveButton("Sudah Diambil") { _, _ ->
                val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                transaksi.tanggalPengambilan = currentTime
                updateTransaksiStatus(transaksi, modelTransaksi.STATUS_SELESAI)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateTransaksiStatus(transaksi: modelTransaksi, newStatus: String) {
        transaksi.status = newStatus

        transaksiRef.child(transaksi.idTransaksi).setValue(transaksi)
            .addOnSuccessListener {
                Toast.makeText(this, "Status transaksi berhasil diupdate", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
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

        // Set data transaksi ke dialog
        dialog.findViewById<TextView>(R.id.txtIdTransaksi)?.text = transaksi.idTransaksi
        dialog.findViewById<TextView>(R.id.txtNamaPelanggan)?.text = transaksi.namaPelanggan
        dialog.findViewById<TextView>(R.id.txtNomorHP)?.text = transaksi.nomorHP
        dialog.findViewById<TextView>(R.id.txtTanggalTransaksi)?.text = transaksi.tanggalTransaksi
        dialog.findViewById<TextView>(R.id.txtStatus)?.text = transaksi.getDisplayStatus()
        dialog.findViewById<TextView>(R.id.txtMetodePembayaran)?.text = transaksi.metodePembayaran

        val btnTutup = dialog.findViewById<Button>(R.id.btnTutup)
        btnTutup?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}