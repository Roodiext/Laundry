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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_layanan)

        recyclerView = findViewById(R.id.rvLayanan)
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
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdapterPilihLayanan(listLayanan) { selectedLayanan ->
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
        // Tampilkan indikator loading jika perlu

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

                // Sembunyikan indikator loading jika perlu

                // Pastikan SearchView tidak mendapat fokus setelah data dimuat
                searchView.clearFocus()
            }

            override fun onCancelled(error: DatabaseError) {
                // Sembunyikan indikator loading jika perlu
                Toast.makeText(this@PilihLayananActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
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