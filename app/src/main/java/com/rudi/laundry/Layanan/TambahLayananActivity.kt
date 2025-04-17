package com.rudi.laundry.Layanan

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelLayanan

class TambahLayananActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("layanan")

    private lateinit var etNama: EditText
    private lateinit var etHarga: EditText
    private lateinit var etCabang: EditText
    private lateinit var btSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_layanan)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etNama = findViewById(R.id.etnama_layanan)
        etHarga = findViewById(R.id.etharga_layanan)
        etCabang = findViewById(R.id.etcabang_layanan)
        btSimpan = findViewById(R.id.bttambah)
    }


    private fun setupListeners() {
        btSimpan.setOnClickListener {
            val layananBaru = myRef.push()
            val layananId = layananBaru.key ?: "Unknown"
            val data = modelLayanan(
                layananId,
                etNama.text.toString(),
                etHarga.text.toString(),
                etCabang.text.toString()
            )

            layananBaru.setValue(data)
                .addOnSuccessListener { finish() }
                .addOnFailureListener { Toast.makeText(this, "Gagal!", Toast.LENGTH_SHORT).show() }
        }
    }
}