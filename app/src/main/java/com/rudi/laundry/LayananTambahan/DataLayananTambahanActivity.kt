package com.rudi.laundry.LayananTambahan

import android.content.Intent
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
        setupListeners()
        setupRecyclerView()
        loadData()
    }

    private fun initViews() {
        rvDataLayananTambahan = findViewById(R.id.rvLayananTambahan)
        fabTambahLayananTambahan = findViewById(R.id.fab_tambah_layanan_tambahan)
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
                    val adapter = AdapterDataLayananTambahan(listLayananTambahan)
                    rvDataLayananTambahan.adapter = adapter
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("FirebaseDataTambahan", "Snapshot kosong!")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataLayananTambahanActivity, error.message, Toast.LENGTH_SHORT).show()
                Log.e("FirebaseDataTambahan", "Database error: ${error.message}")
            }
        })
    }

}
