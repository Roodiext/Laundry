package com.rudi.laundry.Pegawai

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.rudi.laundry.R
import com.rudi.laundry.adapter.AdapterDataPegawai
import com.rudi.laundry.modeldata.modelPegawai

class DataPegawaiActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("pegawai")

    private lateinit var rvDataPegawai: RecyclerView
    private lateinit var fabTambahPegawai: FloatingActionButton
    private var listPegawai = arrayListOf<modelPegawai>()
    private lateinit var adapter: AdapterDataPegawai

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_pegawai)

        initViews()
        setupRecyclerView()
        setupListeners()
        getData()
    }

    private fun initViews() {
        rvDataPegawai = findViewById(R.id.rvPegawai)
        fabTambahPegawai = findViewById(R.id.fab_tambah_pegawai)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDataPegawai.layoutManager = layoutManager
        rvDataPegawai.setHasFixedSize(true)

        // Inisialisasi adapter
        adapter = AdapterDataPegawai(listPegawai, this)
        rvDataPegawai.adapter = adapter
    }

    private fun setupListeners() {
        fabTambahPegawai.setOnClickListener {
            val intent = Intent(this, TambahPegawaiActivity::class.java)
            // Tambahkan extra untuk menandai mode tambah
            intent.putExtra("judul", "Tambah Pegawai")
            startActivity(intent)
        }
    }

    private fun getData() {
        // Menggunakan ValueEventListener untuk real-time updates
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear list terlebih dahulu
                listPegawai.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val pegawai = dataSnapshot.getValue(modelPegawai::class.java)

                        // Validasi data pegawai
                        if (pegawai != null && !pegawai.idPegawai.isNullOrEmpty()) {
                            listPegawai.add(pegawai)
                        }
                    }

                    // Urutkan berdasarkan nama atau ID (opsional)
                    listPegawai.sortBy { it.namaPegawai }

                    // Update adapter dengan data baru
                    adapter.notifyDataSetChanged()

                    // Log untuk debug
                    Toast.makeText(this@DataPegawaiActivity,
                        "Data dimuat: ${listPegawai.size} pegawai", Toast.LENGTH_SHORT).show()
                } else {
                    // Jika tidak ada data
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this@DataPegawaiActivity,
                        "Belum ada data pegawai", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataPegawaiActivity,
                    "Error memuat data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Firebase listener sudah handle real-time updates
        // Tidak perlu refresh manual
    }
}