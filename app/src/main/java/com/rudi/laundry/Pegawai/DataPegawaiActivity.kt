package com.rudi.laundry.Pegawai

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.rudi.laundry.R
import com.rudi.laundry.adapter.AdapterDataPegawai
import com.rudi.laundry.modeldata.modelPegawai

class DataPegawaiActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("pegawai")

    private lateinit var rvDataPegawai: RecyclerView
    private lateinit var fabTambahPegawai: FloatingActionButton
    private var listPegawai = arrayListOf<modelPegawai>()
    private lateinit var adapter: AdapterDataPegawai

    // Language support
    private var currentLanguage = "id"

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "add_employee" to "Tambah Pegawai",
            "data_loaded" to "Data dimuat",
            "employees" to "pegawai",
            "no_employee_data" to "Belum ada data pegawai",
            "error_loading_data" to "Error memuat data"
        ),
        "en" to mapOf(
            "add_employee" to "Add Employee",
            "data_loaded" to "Data loaded",
            "employees" to "employees",
            "no_employee_data" to "No employee data yet",
            "error_loading_data" to "Error loading data"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_pegawai)

        loadCurrentLanguage()
        initViews()
        setupRecyclerView()
        setupListeners()
        getData()
    }

    private fun loadCurrentLanguage() {
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"
    }

    private fun getText(key: String): String {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
        return texts[key] ?: key
    }

    private fun initViews() {
        rvDataPegawai = findViewById(R.id.rvPegawai)
        fabTambahPegawai = findViewById(R.id.fab_tambah_pegawai)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDataPegawai.layoutManager = layoutManager
        rvDataPegawai.setHasFixedSize(true)

        // Inisialisasi adapter
        adapter = AdapterDataPegawai(listPegawai, this)
        rvDataPegawai.adapter = adapter
    }

    private fun setupListeners() {
        fabTambahPegawai.setOnClickListener {
            val intent = Intent(this, TambahPegawaiActivity::class.java)
            // Tambahkan extra untuk menandai mode tambah dengan bahasa yang sesuai
            intent.putExtra("judul", getText("add_employee"))
            startActivity(intent)
        }
    }

    private fun getData() {
        // Menggunakan ValueEventListener untuk real-time updates
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear list terlebih dahulu
                listPegawai.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val pegawai = dataSnapshot.getValue(modelPegawai::class.java)

                        // Validasi data pegawai
                        if (pegawai != null && !pegawai.idPegawai.isNullOrEmpty()) {
                            listPegawai.add(pegawai)
                        }
                    }

                    // Urutkan berdasarkan nama atau ID (opsional)
                    listPegawai.sortBy { it.namaPegawai }

                    // Update adapter dengan data baru
                    adapter.notifyDataSetChanged()

                    // Log untuk debug dengan bahasa yang sesuai
                    Toast.makeText(this@DataPegawaiActivity,
                        "${getText("data_loaded")}: ${listPegawai.size} ${getText("employees")}", Toast.LENGTH_SHORT).show()
                } else {
                    // Jika tidak ada data
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this@DataPegawaiActivity,
                        getText("no_employee_data"), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataPegawaiActivity,
                    "${getText("error_loading_data")}: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Check jika bahasa berubah
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        val newLanguage = sharedPref.getString("selected_language", "id") ?: "id"

        if (newLanguage != currentLanguage) {
            currentLanguage = newLanguage
            // Update adapter untuk refresh bahasa
            adapter.updateLanguage()
        }

        // Firebase listener sudah handle real-time updates
        // Tidak perlu refresh manual
    }
}