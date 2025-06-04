package com.rudi.laundry.pelanggan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    private var adapter: AdapterDataPelanggan? = null

    // Activity Result Launcher untuk menangani hasil dari EditPelangganActivity
    private val editPelangganLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Refresh data setelah edit berhasil
            Toast.makeText(this, "Data pelanggan berhasil diperbarui", Toast.LENGTH_SHORT).show()
            // Data akan otomatis terupdate melalui Firebase listener
        }
    }

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
                }

                // Buat adapter baru untuk memastikan data terupdate
                val newAdapter = AdapterDataPelanggan(
                    listPelanggan,
                    onDeleteClick = { pelanggan ->
                        hapusPelanggan(pelanggan)
                    },
                    onEditClick = { pelanggan ->
                        editPelanggan(pelanggan)
                    }
                )

                // Set adapter dan simpan referensi
                adapter = newAdapter
                rvDataPelanggan.adapter = newAdapter
                newAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataPelangganActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hapusPelanggan(pelanggan: modelPelanggan) {
        if (pelanggan.idPelanggan.isNotEmpty()) {
            // Hapus dari Firebase berdasarkan ID pelanggan
            myRef.child(pelanggan.idPelanggan).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Pelanggan ${pelanggan.namaPelanggan} berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Gagal menghapus pelanggan: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "ID pelanggan tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editPelanggan(pelanggan: modelPelanggan) {
        // Buka EditPelangganActivity dengan data pelanggan
        val intent = Intent(this, EditPelangganActivity::class.java).apply {
            putExtra(EditPelangganActivity.EXTRA_ID_PELANGGAN, pelanggan.idPelanggan)
            putExtra(EditPelangganActivity.EXTRA_NAMA_PELANGGAN, pelanggan.namaPelanggan)
            putExtra(EditPelangganActivity.EXTRA_ALAMAT_PELANGGAN, pelanggan.alamatPelanggan)
            putExtra(EditPelangganActivity.EXTRA_NO_HP_PELANGGAN, pelanggan.noHPPelanggan)
            putExtra(EditPelangganActivity.EXTRA_CABANG_PELANGGAN, pelanggan.cabang)
            putExtra(EditPelangganActivity.EXTRA_TERDAFTAR_PELANGGAN, pelanggan.terdaftar)
        }

        // Gunakan launcher untuk mendapatkan hasil dari EditPelangganActivity
        editPelangganLauncher.launch(intent)
    }
}