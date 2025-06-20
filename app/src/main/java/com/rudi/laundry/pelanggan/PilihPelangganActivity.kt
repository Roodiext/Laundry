package com.rudi.laundry.pelanggan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.rudi.laundry.R
import com.rudi.laundry.adapter.AdapterPilihPelanggan
import com.rudi.laundry.modeldata.modelPelanggan

class PilihPelangganActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: AdapterPilihPelanggan

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("pelanggan")
    private val listPelanggan = arrayListOf<modelPelanggan>()
    private val listPelangganFull = arrayListOf<modelPelanggan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_pelanggan)

        // Inisialisasi views
        recyclerView = findViewById(R.id.rvPelanggan)
        searchView = findViewById(R.id.searchView)

        // Setup RecyclerView terlebih dahulu
        setupRecyclerView()

        // Konfigurasikan SearchView
        setupSearchView()

        // Terakhir, muat data dari Firebase
        loadDataFirebase()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan context untuk akses SharedPreferences
        adapter = AdapterPilihPelanggan(this, listPelanggan) { pelanggan ->
            // Handle item click
            val resultIntent = Intent()
            resultIntent.putExtra("nama", pelanggan.namaPelanggan)
            resultIntent.putExtra("hp", pelanggan.noHPPelanggan)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        // Buat SearchView tidak auto-focus
        searchView.clearFocus()
        searchView.setIconifiedByDefault(false)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterData(newText)
                return true
            }
        })
    }

    private fun filterData(query: String?) {
        val filteredList = if (!query.isNullOrBlank()) {
            listPelangganFull.filter {
                it.namaPelanggan?.contains(query, ignoreCase = true) == true
            }
        } else listPelangganFull

        adapter.updateList(ArrayList(filteredList))
    }

    private fun loadDataFirebase() {
        // Tampilkan indikator loading jika perlu

        myRef.orderByChild("nama").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Bersihkan data lama
                listPelanggan.clear()
                listPelangganFull.clear()

                // Proses data baru
                for (data in snapshot.children) {
                    val pelanggan = data.getValue(modelPelanggan::class.java)
                    if (pelanggan != null) {
                        listPelanggan.add(pelanggan)
                        listPelangganFull.add(pelanggan)
                    }
                }

                // Update RecyclerView dengan semua data
                if (listPelangganFull.isNotEmpty()) {
                    adapter.updateList(listPelangganFull)
                }

                // Sembunyikan indikator loading jika perlu

                // Pastikan SearchView tidak mendapat fokus setelah data dimuat
                searchView.clearFocus()
            }

            override fun onCancelled(error: DatabaseError) {
                // Sembunyikan indikator loading jika perlu
                Toast.makeText(this@PilihPelangganActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Update bahasa ketika kembali ke activity
        adapter.updateLanguage()

        // Pastikan data ditampilkan saat activity kembali aktif
        // dan reset filter agar semua data ditampilkan
        if (listPelangganFull.isNotEmpty()) {
            adapter.updateList(ArrayList(listPelangganFull))
        }
        // Reset search query dan clear focus
        searchView.setQuery("", false)
        searchView.clearFocus()
    }
}