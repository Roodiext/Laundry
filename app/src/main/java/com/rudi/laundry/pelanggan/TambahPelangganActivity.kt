package com.rudi.laundry.pelanggan

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelPelanggan

class TambahPelangganActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pelanggan")

    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoHP: EditText
    private lateinit var etCabang: EditText
    private lateinit var btSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pelanggan)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etNama = findViewById(R.id.etnama_pelanggan)
        etAlamat = findViewById(R.id.etalamat_pelanggan)
        etNoHP = findViewById(R.id.etnohp_pelanggan)
        etCabang = findViewById(R.id.etnama_cabang)
        btSimpan = findViewById(R.id.bttambah)
    }

    private fun setupListeners() {
        btSimpan.setOnClickListener {
            val pelangganBaru = myRef.push()
            val pelangganId = pelangganBaru.key ?: "Unknown"
            val data = modelPelanggan(pelangganId, etNama.text.toString(), etAlamat.text.toString(), etNoHP.text.toString(), System.currentTimeMillis().toString(), etCabang.text.toString())

            pelangganBaru.setValue(data)
                .addOnSuccessListener { finish() }
                .addOnFailureListener { Toast.makeText(this, "Gagal!", Toast.LENGTH_SHORT).show() }
        }
    }
}
