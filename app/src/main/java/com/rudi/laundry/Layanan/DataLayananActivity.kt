package com.rudi.laundry.Layanan

import android.content.Intent
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

class DataLayananActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("layanan")

    private lateinit var rvDataLayanan: RecyclerView
    private lateinit var fabTambahLayanan: FloatingActionButton
    private var listLayanan = arrayListOf<modelLayanan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_layanan)

        initViews()
        setupRecyclerView()
        setupListeners()
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
                    val adapter = AdapterDataLayanan(listLayanan)
                    rvDataLayanan.adapter = adapter
                    adapter.notifyDataSetChanged()
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
}