package com.rudi.laundry.pelanggan

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
import com.rudi.laundry.adapter.AdapterDataPelanggan
import com.rudi.laundry.modeldata.modelPelanggan

class PilihPelangganActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: AdapterDataPelanggan
    private val listPelanggan = arrayListOf<modelPelanggan>()

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("pelanggan")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_pelanggan)

        recyclerView = findViewById(R.id.rvPelanggan)
        searchView = findViewById(R.id.searchView)

        setupRecyclerView()
        loadDataFirebase()
        setupSearchListener()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdapterDataPelanggan(listPelanggan)
        recyclerView.adapter = adapter
    }

    private fun loadDataFirebase() {
        myRef.orderByChild("nama").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listPelanggan.clear()
                for (data in snapshot.children) {
                    val pelanggan = data.getValue(modelPelanggan::class.java)
                    if (pelanggan != null) {
                        listPelanggan.add(pelanggan)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PilihPelangganActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSearchListener() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (!newText.isNullOrBlank()) {
                    listPelanggan.filter {
                        it.namaPelanggan?.contains(newText, ignoreCase = true) == true
                    }
                } else listPelanggan
                adapter.updateList(filteredList)
                return true
            }
        })
    }
}
