package com.rudi.laundry.Layanan

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.rudi.laundry.R
import com.rudi.laundry.adapter.AdapterDataLayanan
import com.rudi.laundry.modeldata.modelLayanan
import com.rudi.laundry.Layanan.TambahLayananActivity
import android.util.Log
import com.rudi.laundry.LayananTambahan.DataLayananTambahanActivity

class DataLayananActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("layanan")

    private lateinit var rvDataLayanan: RecyclerView
    private lateinit var fabTambahLayanan: FloatingActionButton
    private var listLayanan = arrayListOf<modelLayanan>()
    private var adapter: AdapterDataLayanan? = null

    // Language preference listener
    private var languagePreferenceListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_layanan)

        initViews()
        setupRecyclerView()
        setupListeners()
        setupLanguageListener()
        getData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.datalayanan)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        rvDataLayanan = findViewById(R.id.rvLayanan)
        fabTambahLayanan = findViewById(R.id.fab_tambah_layanan)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDataLayanan.layoutManager = layoutManager
        rvDataLayanan.setHasFixedSize(true)
    }

    private fun setupListeners() {
        fabTambahLayanan.setOnClickListener {
            val intent = Intent(this, TambahLayananActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupLanguageListener() {
        val sharedPref = getSharedPreferences("language_pref", Context.MODE_PRIVATE)

        languagePreferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "selected_language") {
                // Refresh adapter ketika bahasa berubah
                adapter?.refreshLanguage()
            }
        }

        sharedPref.registerOnSharedPreferenceChangeListener(languagePreferenceListener)
    }

    private fun getData() {
        val query = myRef.orderByChild("idLayanan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listLayanan.clear()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val layanan = dataSnapshot.getValue(modelLayanan::class.java)
                        if (layanan != null) {
                            listLayanan.add(layanan)
                            Log.d("FirebaseData", "Data: $layanan") // Cek apakah data masuk
                        } else {
                            Log.e("FirebaseData", "Data null atau tidak sesuai format!")
                        }
                    }

                    // Create adapter if not exists
                    if (adapter == null) {
                        adapter = AdapterDataLayanan(listLayanan) {
                            // Callback untuk refresh data setelah delete
                            refreshData()
                        }
                        rvDataLayanan.adapter = adapter
                    } else {
                        // Update existing adapter
                        adapter?.updateData(listLayanan)
                    }

                    adapter?.notifyDataSetChanged()
                } else {
                    Log.e("FirebaseData", "Snapshot kosong!")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataLayananActivity, error.message, Toast.LENGTH_SHORT).show()
                Log.e("FirebaseData", "Database error: ${error.message}")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Refresh data saat kembali ke activity ini
        // getData() akan otomatis dipanggil karena ValueEventListener sudah aktif

        // Refresh adapter untuk memastikan bahasa terbaru diterapkan
        adapter?.refreshLanguage()
    }

    // Jika ingin refresh manual, tambahkan method ini:
    private fun refreshData() {
        getData()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister language preference listener
        languagePreferenceListener?.let { listener ->
            val sharedPref = getSharedPreferences("language_pref", Context.MODE_PRIVATE)
            sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}