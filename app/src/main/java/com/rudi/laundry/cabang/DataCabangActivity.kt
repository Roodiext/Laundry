package com.rudi.laundry.cabang

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.rudi.laundry.R
import com.rudi.laundry.adapter.AdapterDataCabang
import com.rudi.laundry.modeldata.modelCabang

class DataCabangActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("cabang")

    // Views
    private lateinit var toolbar: MaterialToolbar
    private lateinit var etSearch: TextInputEditText
    private lateinit var tvTotalCabang: TextView
    private lateinit var tvCabangBuka: TextView
    private lateinit var rvCabang: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var fabTambahCabang: FloatingActionButton
    private lateinit var progressBar: ProgressBar

    // Data dan Adapter
    private var listCabang = arrayListOf<modelCabang>()
    private var originalListCabang = arrayListOf<modelCabang>()
    private var adapter: AdapterDataCabang? = null

    // Handler untuk update real-time status
    private val statusUpdateHandler = Handler(Looper.getMainLooper())
    private var statusUpdateRunnable: Runnable? = null

    // Activity Result Launcher untuk menangani hasil dari TambahCabangActivity
    private val tambahCabangLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Cabang berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            // Data akan otomatis terupdate melalui Firebase listener
        }
    }

    // Activity Result Launcher untuk menangani hasil dari EditCabangActivity
    private val editCabangLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Data cabang berhasil diperbarui", Toast.LENGTH_SHORT).show()
            // Data akan otomatis terupdate melalui Firebase listener
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_cabang)

        initViews()
        setupToolbar()
        setupRecyclerView()
        setupSearchListener()
        setupListeners()
        getData()
        startRealTimeStatusUpdate()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        etSearch = findViewById(R.id.etSearch)
        tvTotalCabang = findViewById(R.id.tvTotalCabang)
        tvCabangBuka = findViewById(R.id.tvCabangBuka)
        rvCabang = findViewById(R.id.rvCabang)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        fabTambahCabang = findViewById(R.id.fabTambahCabang)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvCabang.layoutManager = layoutManager
        rvCabang.setHasFixedSize(true)
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupListeners() {
        fabTambahCabang.setOnClickListener {
            val intent = Intent(this, TambahCabangActivity::class.java)
            tambahCabangLauncher.launch(intent)
        }
    }

    private fun getData() {
        showLoading(true)

        val query = myRef.orderByChild("terdaftar").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                originalListCabang.clear()

                android.util.Log.d("FirebaseDebug", "Snapshot exists: ${snapshot.exists()}")
                android.util.Log.d("FirebaseDebug", "Children count: ${snapshot.childrenCount}")

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        try {
                            val cabang = dataSnapshot.getValue(modelCabang::class.java)

                            if (cabang != null) {
                                // Pastikan ID cabang ter-set dari key Firebase
                                if (cabang.idCabang.isEmpty()) {
                                    cabang.idCabang = dataSnapshot.key ?: ""
                                }

                                // Debug setiap data yang di-load
                                android.util.Log.d("FirebaseDebug", "Loaded cabang:")
                                android.util.Log.d("FirebaseDebug", "  - Key: ${dataSnapshot.key}")
                                android.util.Log.d("FirebaseDebug", "  - ID: '${cabang.idCabang}'")
                                android.util.Log.d("FirebaseDebug", "  - namaToko: '${cabang.namaToko}'")
                                android.util.Log.d("FirebaseDebug", "  - namaCabang: '${cabang.namaCabang}'")
                                android.util.Log.d("FirebaseDebug", "  - alamatCabang: '${cabang.alamatCabang}'")
                                android.util.Log.d("FirebaseDebug", "  - noTelpCabang: '${cabang.noTelpCabang}'")

                                // Validasi data tidak kosong sebelum menambahkan
                                if (cabang.namaCabang.isNotEmpty() || cabang.namaToko.isNotEmpty()) {
                                    listCabang.add(cabang)
                                    originalListCabang.add(cabang)
                                } else {
                                    android.util.Log.w("FirebaseDebug", "Skipping empty cabang data")
                                }
                            } else {
                                android.util.Log.w("FirebaseDebug", "Failed to parse cabang from snapshot")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("FirebaseDebug", "Error parsing cabang: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }

                android.util.Log.d("FirebaseDebug", "Total loaded: ${listCabang.size}")

                setupAdapter()
                updateStats()
                updateEmptyState()
                showLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                android.util.Log.e("FirebaseDebug", "Database error: ${error.message}")
                Toast.makeText(this@DataCabangActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupAdapter() {
        val newAdapter = AdapterDataCabang(
            listCabang,
            onDeleteClick = { cabang ->
                showDeleteConfirmationDialog(cabang)
            },
            onEditClick = { cabang ->
                editCabang(cabang)
            }
        )

        adapter = newAdapter
        rvCabang.adapter = newAdapter
        newAdapter.notifyDataSetChanged()
    }

    private fun filterData(query: String) {
        android.util.Log.d("SearchDebug", "Search query: '$query'")
        android.util.Log.d("SearchDebug", "Original list size: ${originalListCabang.size}")

        if (query.isEmpty()) {
            listCabang.clear()
            listCabang.addAll(originalListCabang)
            android.util.Log.d("SearchDebug", "Query empty, showing all data: ${listCabang.size}")
        } else {
            // Debug data sebelum filter
            originalListCabang.forEachIndexed { index, cabang ->
                android.util.Log.d("SearchDebug", "Item $index:")
                android.util.Log.d("SearchDebug", "  - namaToko: '${cabang.namaToko}'")
                android.util.Log.d("SearchDebug", "  - namaCabang: '${cabang.namaCabang}'")
                android.util.Log.d("SearchDebug", "  - alamatCabang: '${cabang.alamatCabang}'")
                android.util.Log.d("SearchDebug", "  - noTelpCabang: '${cabang.noTelpCabang}'")
            }

            val filteredList = originalListCabang.filter { cabang ->
                // Pastikan tidak ada null values dan trim whitespace
                val namaToko = cabang.namaToko.trim()
                val namaCabang = cabang.namaCabang.trim()
                val alamatCabang = cabang.alamatCabang.trim()
                val noTelpCabang = cabang.noTelpCabang.trim()
                val searchQuery = query.trim()

                val matchNamaToko = namaToko.contains(searchQuery, ignoreCase = true)
                val matchNamaCabang = namaCabang.contains(searchQuery, ignoreCase = true)
                val matchAlamat = alamatCabang.contains(searchQuery, ignoreCase = true)
                val matchTelp = noTelpCabang.contains(searchQuery, ignoreCase = true)

                // Log hasil matching untuk debugging
                android.util.Log.d("SearchDebug", "Checking '${cabang.namaCabang}':")
                android.util.Log.d("SearchDebug", "  - namaToko '$namaToko' match: $matchNamaToko")
                android.util.Log.d("SearchDebug", "  - namaCabang '$namaCabang' match: $matchNamaCabang")
                android.util.Log.d("SearchDebug", "  - alamat '$alamatCabang' match: $matchAlamat")
                android.util.Log.d("SearchDebug", "  - telp '$noTelpCabang' match: $matchTelp")

                val result = matchNamaToko || matchNamaCabang || matchAlamat || matchTelp
                android.util.Log.d("SearchDebug", "  - Final match result: $result")

                result
            }

            android.util.Log.d("SearchDebug", "Filtered results: ${filteredList.size}")

            listCabang.clear()
            listCabang.addAll(filteredList)
        }

        // Update adapter dengan notifikasi yang lebih spesifik
        adapter?.let { adapter ->
            adapter.updateData(ArrayList(listCabang))
            android.util.Log.d("SearchDebug", "Adapter updated with: ${listCabang.size} items")
        }

        updateStats()
        updateEmptyState()
    }

    private fun updateStats() {
        val total = listCabang.size
        // Menggunakan status real-time untuk menghitung cabang yang buka
        val buka = listCabang.count { it.getRealTimeStatus() == "BUKA" }

        tvTotalCabang.text = total.toString()
        tvCabangBuka.text = buka.toString()
    }

    private fun updateEmptyState() {
        if (listCabang.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
            rvCabang.visibility = View.GONE
        } else {
            layoutEmptyState.visibility = View.GONE
            rvCabang.visibility = View.VISIBLE
        }
    }

    private fun showDeleteConfirmationDialog(cabang: modelCabang) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus cabang '${cabang.namaCabang}'?")
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Hapus") { _, _ ->
                hapusCabang(cabang)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusCabang(cabang: modelCabang) {
        if (cabang.idCabang.isNotEmpty()) {
            showLoading(true)

            myRef.child(cabang.idCabang).removeValue()
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(this, "Cabang '${cabang.namaCabang}' berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    showLoading(false)
                    Toast.makeText(this, "Gagal menghapus cabang: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "ID cabang tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editCabang(cabang: modelCabang) {
        // Untuk saat ini, kita bisa buat EditCabangActivity nanti
        // Atau bisa menggunakan TambahCabangActivity dengan mode edit
        Toast.makeText(this, "Fitur edit akan segera tersedia", Toast.LENGTH_SHORT).show()

        // Contoh implementasi jika ingin menggunakan TambahCabangActivity untuk edit:
        /*
        val intent = Intent(this, TambahCabangActivity::class.java).apply {
            putExtra("EDIT_MODE", true)
            putExtra("CABANG_DATA", cabang)
        }
        editCabangLauncher.launch(intent)
        */
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        fabTambahCabang.isEnabled = !show
    }

    // Method untuk memulai update status real-time
    private fun startRealTimeStatusUpdate() {
        statusUpdateRunnable = object : Runnable {
            override fun run() {
                // Update adapter untuk refresh status
                adapter?.refreshRealTimeStatus()
                // Update statistik
                updateStats()

                // Schedule next update in 1 minute
                statusUpdateHandler.postDelayed(this, 60000) // 60 detik
            }
        }

        // Start the first update after 1 minute
        statusUpdateHandler.postDelayed(statusUpdateRunnable!!, 60000)
    }

    // Method untuk menghentikan update status real-time
    private fun stopRealTimeStatusUpdate() {
        statusUpdateRunnable?.let { runnable ->
            statusUpdateHandler.removeCallbacks(runnable)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh status ketika activity kembali aktif
        adapter?.refreshRealTimeStatus()
        updateStats()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRealTimeStatusUpdate()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}