package com.rudi.laundry.LayananTambahan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.rudi.laundry.Layanan.TambahLayananActivity
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelLayanan

class TambahLayananTambahan : AppCompatActivity() {

    private lateinit var etNama: EditText
    private lateinit var etHarga: EditText
    private lateinit var etCabang: EditText
    private lateinit var btSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_layanan_tambahan)

        etNama = findViewById(R.id.etnama_layanan)
        etHarga = findViewById(R.id.etharga_layanan)
        etCabang = findViewById(R.id.etcabang_layanan)
        btSimpan = findViewById(R.id.bttambah)

        btSimpan.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val harga = etHarga.text.toString().trim()
            val cabang = etCabang.text.toString().trim()

            if (nama.isEmpty() || harga.isEmpty() || cabang.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference("layanan_tambahan").push()
            val id = ref.key ?: ""

            val data = modelLayanan(id, nama, harga, cabang)
            ref.setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Layanan Tambahan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menyimpan data!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun layanan(view: View) {
        val intent = Intent(this@TambahLayananTambahan, TambahLayananActivity::class.java)
        startActivity(intent)
    }


}
