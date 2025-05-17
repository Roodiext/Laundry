package com.rudi.laundry.Layanan

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.rudi.laundry.LayananTambahan.TambahLayananTambahan
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelLayanan
import java.text.NumberFormat
import java.util.*

class TambahLayananActivity : AppCompatActivity() {

    private lateinit var etNama: EditText
    private lateinit var etHarga: EditText
    private lateinit var etCabang: EditText
    private lateinit var btSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_layanan)

        etNama = findViewById(R.id.etnama_layanan)
        etHarga = findViewById(R.id.etharga_layanan)
        etCabang = findViewById(R.id.etcabang_layanan)
        btSimpan = findViewById(R.id.bttambah)

        // Format harga otomatis ke Rupiah dengan koma di belakang
        etHarga.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etHarga.removeTextChangedListener(this)

                    val localeID = Locale("in", "ID")
                    val cleanString = s.toString().replace("[Rp,.\\s]".toRegex(), "")

                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble() / 100
                        val formatted = NumberFormat.getCurrencyInstance(localeID).format(parsed)
                        current = formatted
                        etHarga.setText(formatted)
                        etHarga.setSelection(formatted.length)
                    }

                    etHarga.addTextChangedListener(this)
                }
            }
        })

        btSimpan.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val harga = etHarga.text.toString()
                .replace("Rp", "")
                .replace(".", "")
                .replace(",", ".")
                .trim()
            val cabang = etCabang.text.toString().trim()

            if (nama.isEmpty() || harga.isEmpty() || cabang.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference("layanan").push()
            val id = ref.key ?: ""

            val data = modelLayanan(id, nama, harga, cabang)
            ref.setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Layanan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menyimpan data!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun LayananTambahan(view: View) {
        val intent = Intent(this@TambahLayananActivity, TambahLayananTambahan::class.java)
        startActivity(intent)
    }

}
