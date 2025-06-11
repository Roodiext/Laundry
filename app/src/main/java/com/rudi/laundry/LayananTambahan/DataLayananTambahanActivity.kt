package com.rudi.laundry.LayananTambahan

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.rudi.laundry.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rudi.laundry.Layanan.TambahLayananActivity
import com.rudi.laundry.adapter.AdapterDataLayananTambahan
import com.rudi.laundry.modeldata.modelLayanan

class DataLayananTambahanActivity : AppCompatActivity() {

    private lateinit var rvDataLayananTambahan: RecyclerView
    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("layanan_tambahan")

    private var listLayananTambahan = arrayListOf<modelLayanan>()
    private lateinit var fabTambahLayananTambahan: FloatingActionButton
    private lateinit var adapter: AdapterDataLayananTambahan
    private lateinit var languagePrefs: SharedPreferences
    private var currentLanguage = "id"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_layanan_tambahan)

        rvDataLayananTambahan = findViewById(R.id.rvLayananTambahan)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.datalayanantambahan)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupLanguageListener()
        setupListeners()
        setupRecyclerView()
        loadData()
    }

    private fun initViews() {
        rvDataLayananTambahan = findViewById(R.id.rvLayananTambahan)
        fabTambahLayananTambahan = findViewById(R.id.fab_tambah_layanan_tambahan)

        // Initialize language preferences
        languagePrefs = getSharedPreferences("language_pref", MODE_PRIVATE)
        currentLanguage = languagePrefs.getString("selected_language", "id") ?: "id"
    }

    private fun setupLanguageListener() {
        // Listener untuk perubahan bahasa
        languagePrefs.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "selected_language") {
                val newLanguage = languagePrefs.getString("selected_language", "id") ?: "id"
                if (newLanguage != currentLanguage) {
                    currentLanguage = newLanguage
                    // Refresh adapter untuk update bahasa
                    if (::adapter.isInitialized) {
                        adapter.refreshLanguage()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        fabTambahLayananTambahan.setOnClickListener {
            val intent = Intent(this, TambahLayananTambahan::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDataLayananTambahan.layoutManager = layoutManager
        rvDataLayananTambahan.setHasFixedSize(true)
    }

    private fun loadData() {
        val query = myRef.orderByChild("idLayanan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listLayananTambahan.clear()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val layanan = dataSnapshot.getValue(modelLayanan::class.java)
                        if (layanan != null) {
                            listLayananTambahan.add(layanan)
                            Log.d("FirebaseDataTambahan", "Data: $layanan")
                        } else {
                            Log.e("FirebaseDataTambahan", "Data null/tidak sesuai format")
                        }
                    }

                    // Initialize adapter jika belum ada
                    if (!::adapter.isInitialized) {
                        adapter = AdapterDataLayananTambahan(listLayananTambahan) {
                            // Callback ketika data berhasil dihapus
                            // Tidak perlu reload karena listener Firebase sudah handle
                        }
                        rvDataLayananTambahan.adapter = adapter
                    } else {
                        // Update data existing adapter
                        adapter.updateData(listLayananTambahan)
                    }
                } else {
                    Log.e("FirebaseDataTambahan", "Snapshot kosong!")
                    // Jika tidak ada data, tetap initialize adapter dengan list kosong
                    if (!::adapter.isInitialized) {
                        adapter = AdapterDataLayananTambahan(listLayananTambahan)
                        rvDataLayananTambahan.adapter = adapter
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataLayananTambahanActivity, error.message, Toast.LENGTH_SHORT).show()
                Log.e("FirebaseDataTambahan", "Database error: ${error.message}")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Check untuk perubahan bahasa ketika kembali ke activity
        val newLanguage = languagePrefs.getString("selected_language", "id") ?: "id"
        if (newLanguage != currentLanguage) {
            currentLanguage = newLanguage
            if (::adapter.isInitialized) {
                adapter.refreshLanguage()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister listener untuk menghindari memory leak
        languagePrefs.unregisterOnSharedPreferenceChangeListener { _, _ -> }
    }
}