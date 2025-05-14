package com.rudi.laundry.transaksi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rudi.laundry.R
import com.rudi.laundry.Layanan.PilihLayananActivity
import com.rudi.laundry.pelanggan.PilihPelangganActivity

class transaksi : AppCompatActivity() {

    private lateinit var namaPelangganText: TextView
    private lateinit var noHPPelangganText: TextView
    private lateinit var namaLayananText: TextView
    private lateinit var hargaLayananText: TextView

    private val pilihPelangganLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val nama = data?.getStringExtra("nama")
            val hp = data?.getStringExtra("hp")
            namaPelangganText.text = "Nama Pelanggan: $nama"
            noHPPelangganText.text = "No HP: $hp"
        }
    }

    private val pilihLayananLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val namaLayanan = data?.getStringExtra("nama")
            val harga = data?.getStringExtra("harga")
            namaLayananText.text = "Layanan: $namaLayanan"
            hargaLayananText.text = "Harga: $harga"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaksi)

        namaPelangganText = findViewById(R.id.label_nama)
        noHPPelangganText = findViewById(R.id.label_hp)
        namaLayananText = findViewById(R.id.label_layanan)
        hargaLayananText = findViewById(R.id.label_harga)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transaksi)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun PilihPelanggan(view: View?) {
        val intent = Intent(this@transaksi, PilihPelangganActivity::class.java)
        pilihPelangganLauncher.launch(intent)
    }

    fun PilihLayanan(view: View?) {
        val intent = Intent(this@transaksi, PilihLayananActivity::class.java)
        pilihLayananLauncher.launch(intent)
    }
}
