package com.rudi.laundry.pelanggan

import android.annotation.SuppressLint
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
import com.rudi.laundry.adapter.AdapterDataPelanggan
import com.rudi.laundry.modeldata.modelPelanggan

class DataPelangganActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("pelanggan")

    private lateinit var rvDataPelanggan: RecyclerView
    private lateinit var fabTambahPelanggan: FloatingActionButton
    private var listPelanggan = arrayListOf<modelPelanggan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_pelanggan)

        initViews()
        setupRecyclerView()
        setupListeners()
        getData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.datapelanggan)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        rvDataPelanggan = findViewById(R.id.rvPelanggan)
        fabTambahPelanggan = findViewById(R.id.fab_tambah_pelanggan)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDataPelanggan.layoutManager = layoutManager
        rvDataPelanggan.setHasFixedSize(true)
    }

    private fun setupListeners() {
        fabTambahPelanggan.setOnClickListener {
            val intent = Intent(this, TambahPelangganActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getData() {
        val query = myRef.orderByChild("idPelanggan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listPelanggan.clear()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val pelanggan = dataSnapshot.getValue(modelPelanggan::class.java) ?: modelPelanggan()
                        listPelanggan.add(pelanggan)
                    }
                    val adapter = AdapterDataPelanggan(listPelanggan)
                    rvDataPelanggan.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataPelangganActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
