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
        adapter = AdapterPilihLayanan(listLayanan) { selectedLayanan ->
            val intent = Intent()
            intent.putExtra("nama", selectedLayanan.namaLayanan)
            intent.putExtra("harga", selectedLayanan.hargaLayanan)
            setResult(RESULT_OK, intent)
            finish()
        }
        recyclerView.adapter = adapter
    }

    private fun loadDataFirebase() {
        myRef.orderByChild("nama").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listLayanan.clear()
                listLayananFull.clear()
                for (data in snapshot.children) {
                    val layanan = data.getValue(modelLayanan::class.java)
                    if (layanan != null) {
                        listLayanan.add(layanan)
                        listLayananFull.add(layanan)
                    }
                }
                adapter.updateList(listLayanan)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PilihLayananActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSearchListener() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (!newText.isNullOrBlank()) {
                    listLayananFull.filter {
                        it.namaLayanan?.contains(newText, ignoreCase = true) == true
                    }
                } else listLayananFull

                adapter.updateList(ArrayList(filteredList))
                return true
            }
        })
    }
}
