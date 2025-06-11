package com.rudi.laundry.cabang

import android.content.Intent
import android.content.SharedPreferences
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
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import com.rudi.laundry.R
import com.rudi.laundry.adapter.AdapterDataCabang
import com.rudi.laundry.modeldata.modelCabang

class DataCabangActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("cabang")

    // Views
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tilSearch: TextInputLayout
    private lateinit var etSearch: TextInputEditText
    private lateinit var tvTotalCabang: TextView
    private lateinit var tvCabangBuka: TextView
    private lateinit var tvTotalCabangLabel: TextView
    private lateinit var tvCabangBukaLabel: TextView
    private lateinit var rvCabang: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySubtitle: TextView
    private lateinit var fabTambahCabang: FloatingActionButton
    private lateinit var progressBar: ProgressBar

    // Data dan Adapter
    private var listCabang = arrayListOf<modelCabang>()
    private var originalListCabang = arrayListOf<modelCabang>()
    private var adapter: AdapterDataCabang? = null

    // Handler untuk update real-time status
    private val statusUpdateHandler = Handler(Looper.getMainLooper())
    private var statusUpdateRunnable: Runnable? = null

    // Language Support
    private lateinit var sharedPref: SharedPreferences
    private var currentLanguage = "id"

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "title" to "Data Cabang",
            "search_hint" to "Cari cabang...",
            "total_branch" to "Total Cabang",
            "currently_open" to "Sedang Buka",
            "empty_title" to "Belum ada data cabang",
            "empty_subtitle" to "Tambahkan cabang pertama Anda",
            "add_branch" to "Tambah Cabang",
            "confirm_delete_title" to "Konfirmasi Hapus",
            "confirm_delete_message" to "Apakah Anda yakin ingin menghapus cabang",
            "delete" to "Hapus",
            "cancel" to "Batal",
            "branch_added_success" to "Cabang berhasil ditambahkan",
            "branch_updated_success" to "Data cabang berhasil diperbarui",
            "branch_deleted_success" to "berhasil dihapus",
            "delete_failed" to "Gagal menghapus cabang",
            "invalid_id" to "ID cabang tidak valid",
            "edit_feature_soon" to "Fitur edit akan segera tersedia",
            "error" to "Error"
        ),
        "en" to mapOf(
            "title" to "Branch Data",
            "search_hint" to "Search branch...",
            "total_branch" to "Total Branch",
            "currently_open" to "Currently Open",
            "empty_title" to "No branch data yet",
            "empty_subtitle" to "Add your first branch",
            "add_branch" to "Add Branch",
            "confirm_delete_title" to "Confirm Delete",
            "confirm_delete_message" to "Are you sure you want to delete branch",
            "delete" to "Delete",
            "cancel" to "Cancel",
            "branch_added_success" to "Branch successfully added",
            "branch_updated_success" to "Branch data successfully updated",
            "branch_deleted_success" to "successfully deleted",
            "delete_failed" to "Failed to delete branch",
            "invalid_id" to "Invalid branch ID",
            "edit_feature_soon" to "Edit feature coming soon",
            "error" to "Error"
        )
    )

    // Activity Result Launcher untuk menangani hasil dari TambahCabangActivity
    private val tambahCabangLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
            Toast.makeText(this, texts["branch_added_success"], Toast.LENGTH_SHORT).show()
            // Data akan otomatis terupdate melalui Firebase listener
        }
    }

    // Activity Result Launcher untuk menangani hasil dari EditCabangActivity
    private val editCabangLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
            Toast.makeText(this, texts["branch_updated_success"], Toast.LENGTH_SHORT).show()
            // Data akan otomatis terupdate melalui Firebase listener
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_cabang)

        initLanguageSupport()
        initViews()
        setupToolbar()
        setupRecyclerView()
        setupSearchListener()
        setupListeners()
        updateAllTexts()
        getData()
        startRealTimeStatusUpdate()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initLanguageSupport() {
        sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tilSearch = findViewById(R.id.tilSearch)
        etSearch = findViewById(R.id.etSearch)
        tvTotalCabang = findViewById(R.id.tvTotalCabang)
        tvCabangBuka = findViewById(R.id.tvCabangBuka)
        rvCabang = findViewById(R.id.rvCabang)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        fabTambahCabang = findViewById(R.id.fabTambahCabang)
        progressBar = findViewById(R.id.progressBar)

        // Perbaikan: Cari dari root view activity, bukan dari layoutEmptyState
        tvTotalCabangLabel = findViewById(R.id.tvTotalCabangLabel)
        tvCabangBukaLabel = findViewById(R.id.tvCabangBukaLabel)
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle)
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle)
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

    private fun updateAllTexts() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Update toolbar title
        toolbar.title = texts["title"]

        // Update search hint
        tilSearch.hint = texts["search_hint"]

        // Update FAB content description
        fabTambahCabang.contentDescription = texts["add_branch"]

        // Update stats labels
        tvTotalCabangLabel.text = texts["total_branch"]
        tvCabangBukaLabel.text = texts["currently_open"]

        // Update empty state texts
        tvEmptyTitle.text = texts["empty_title"]
        tvEmptySubtitle.text = texts["empty_subtitle"]
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
                val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
                Toast.makeText(this@DataCabangActivity, "${texts["error"]}: ${error.message}", Toast.LENGTH_SHORT).show()
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
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        AlertDialog.Builder(this)
            .setTitle(texts["confirm_delete_title"])
            .setMessage("${texts["confirm_delete_message"]} '${cabang.namaCabang}'?")
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton(texts["delete"]) { _, _ ->
                hapusCabang(cabang)
            }
            .setNegativeButton(texts["cancel"], null)
            .show()
    }

    private fun hapusCabang(cabang: modelCabang) {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        if (cabang.idCabang.isNotEmpty()) {
            showLoading(true)

            myRef.child(cabang.idCabang).removeValue()
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(this, "Cabang '${cabang.namaCabang}' ${texts["branch_deleted_success"]}", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    showLoading(false)
                    Toast.makeText(this, "${texts["delete_failed"]}: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, texts["invalid_id"], Toast.LENGTH_SHORT).show()
        }
    }

    private fun editCabang(cabang: modelCabang) {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
        Toast.makeText(this, texts["edit_feature_soon"], Toast.LENGTH_SHORT).show()

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

        // Check if language has changed
        val savedLanguage = sharedPref.getString("selected_language", "id") ?: "id"
        if (savedLanguage != currentLanguage) {
            currentLanguage = savedLanguage
            updateAllTexts()
        }

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