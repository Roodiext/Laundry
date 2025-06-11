package com.rudi.laundry.LayananTambahan

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
import com.rudi.laundry.adapter.AdapterLayananTambahan
import com.rudi.laundry.modeldata.modelLayanan

class PilihLayananTambahan : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: AdapterLayananTambahan

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("layanan_tambahan")
    private val listLayanan = arrayListOf<modelLayanan>()
    private val listLayananFull = arrayListOf<modelLayanan>()

    // Language system
    private var currentLanguage = "id" // Default bahasa Indonesia

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "search_hint" to "Cari Data Layanan",
            "no_name" to "Tidak Ada Nama",
            "no_price" to "Tidak Ada Harga",
            "error_message" to "Terjadi kesalahan: "
        ),
        "en" to mapOf(
            "search_hint" to "Search Service Data",
            "no_name" to "No Name",
            "no_price" to "No Price",
            "error_message" to "An error occurred: "
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_layanan)

        // Load saved language preference
        loadLanguagePreference()

        recyclerView = findViewById(R.id.rvLayanan)
        searchView = findViewById(R.id.searchView)

        // Setup RecyclerView terlebih dahulu
        setupRecyclerView()

        // Konfigurasikan SearchView
        setupSearchView()

        // Update UI texts based on language
        updateUITexts()

        // Terakhir, muat data dari Firebase
        loadDataFirebase()

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

    private fun getCurrentTexts(): Map<String, String> {
        return languageTexts[currentLanguage] ?: languageTexts["id"]!!
    }

    private fun updateUITexts() {
        val texts = getCurrentTexts()

        // Update SearchView hint
        searchView.queryHint = texts["search_hint"]

        // Update adapter language if it's already initialized
        if (::adapter.isInitialized) {
            adapter.updateLanguage(currentLanguage)
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdapterLayananTambahan(listLayanan, currentLanguage) { selectedLayanan ->
            val intent = Intent()
            intent.putExtra("namaTambahan", selectedLayanan.namaLayanan)
            intent.putExtra("hargaTambahan", selectedLayanan.hargaLayanan)
            setResult(RESULT_OK, intent)
            finish()
        }
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
            listLayananFull.filter {
                it.namaLayanan?.contains(query, ignoreCase = true) == true
            }
        } else listLayananFull

        adapter.updateList(ArrayList(filteredList))
    }

    private fun loadDataFirebase() {
        myRef.orderByChild("namaLayanan").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Bersihkan data lama
                listLayanan.clear()
                listLayananFull.clear()

                // Proses data baru
                for (data in snapshot.children) {
                    val layanan = data.getValue(modelLayanan::class.java)
                    if (layanan != null) {
                        listLayanan.add(layanan)
                        listLayananFull.add(layanan)
                    }
                }

                // Update RecyclerView dengan semua data
                if (listLayananFull.isNotEmpty()) {
                    adapter.updateList(listLayananFull)
                }

                // Pastikan SearchView tidak mendapat fokus setelah data dimuat
                searchView.clearFocus()
            }

            override fun onCancelled(error: DatabaseError) {
                val texts = getCurrentTexts()
                Toast.makeText(this@PilihLayananTambahan,
                    texts["error_message"] + error.message,
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        // Check if language preference has changed
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        val newLanguage = sharedPref.getString("selected_language", "id") ?: "id"

        if (newLanguage != currentLanguage) {
            currentLanguage = newLanguage
            updateUITexts()
        }

        // Pastikan data ditampilkan saat activity kembali aktif
        // dan reset filter agar semua data ditampilkan
        if (listLayananFull.isNotEmpty()) {
            adapter.updateList(ArrayList(listLayananFull))
        }
        // Reset search query dan clear focus
        searchView.setQuery("", false)
        searchView.clearFocus()
    }
}