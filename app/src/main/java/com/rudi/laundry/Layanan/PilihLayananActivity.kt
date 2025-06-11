package com.rudi.laundry.Layanan

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
import com.rudi.laundry.adapter.AdapterPilihLayanan
import com.rudi.laundry.modeldata.modelLayanan

class PilihLayananActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: AdapterPilihLayanan

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("layanan")
    private val listLayanan = arrayListOf<modelLayanan>()
    private val listLayananFull = arrayListOf<modelLayanan>()

    // Language support variables
    private var currentLanguage = "id" // Default bahasa Indonesia

    // Language texts untuk activity ini
    private val languageTexts = mapOf(
        "id" to mapOf(
            "search_hint" to "Cari layanan...",
            "no_service_name" to "Tidak Ada Nama",
            "no_price" to "Tidak Ada Harga",
            "loading_error" to "Gagal memuat data layanan",
            "affordable" to "Terjangkau",
            "premium_service" to "Layanan Premium",
            "service_name" to "Nama Layanan",
            "service_price" to "Harga Layanan",
            "select_service" to "Pilih Layanan Ini"
        ),
        "en" to mapOf(
            "search_hint" to "Search services...",
            "no_service_name" to "No Name Available",
            "no_price" to "No Price Available",
            "loading_error" to "Failed to load service data",
            "affordable" to "Affordable",
            "premium_service" to "Premium Service",
            "service_name" to "Service Name",
            "service_price" to "Service Price",
            "select_service" to "Select This Service"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_layanan)

        // Load language preference
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

    private fun updateUITexts() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Update SearchView hint
        searchView.queryHint = texts["search_hint"]

        // Update adapter dengan language context
        if (::adapter.isInitialized) {
            adapter.updateLanguage(currentLanguage, texts)
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        adapter = AdapterPilihLayanan(listLayanan, currentLanguage, texts) { selectedLayanan ->
            val intent = Intent()
            intent.putExtra("nama", selectedLayanan.namaLayanan)
            intent.putExtra("harga", selectedLayanan.hargaLayanan)
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
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        myRef.orderByChild("nama").addValueEventListener(object : ValueEventListener {
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
                Toast.makeText(this@PilihLayananActivity, texts["loading_error"], Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        // Check if language has changed
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        val newLanguage = sharedPref.getString("selected_language", "id") ?: "id"

        if (newLanguage != currentLanguage) {
            currentLanguage = newLanguage
            updateUITexts()
        }

        // Pastikan data ditampilkan saat activity kembali aktif
        if (listLayananFull.isNotEmpty()) {
            adapter.updateList(ArrayList(listLayananFull))
        }
        // Reset search query dan clear focus
        searchView.setQuery("", false)
        searchView.clearFocus()
    }
}