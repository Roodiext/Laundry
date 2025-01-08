package com.rudi.belajarrudi

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.DecimalFormat


class KonversiSuhuActivity : AppCompatActivity() {
    lateinit var spSuhuAwal: Spinner
    lateinit var spSuhuAkhir: Spinner
    lateinit var tvHasilSuhuAkhir: TextView
    lateinit var etSuhuAwal: EditText
    lateinit var btKonversi: Button
    lateinit var btBersih: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_konversi_suhu)

        etSuhuAwal = findViewById(R.id.etSuhuAwal)
        spSuhuAwal = findViewById(R.id.spSuhuAwal)
        spSuhuAkhir = findViewById(R.id.spSuhuAkhir)
        tvHasilSuhuAkhir = findViewById(R.id.tvHasilSuhuAkhir)
        btKonversi = findViewById(R.id.btKonversi)
        btBersih = findViewById(R.id.btBersihkan)

        // Menambahkan listener untuk tombol konversi
        btKonversi.setOnClickListener {
            konversiSuhu()
        }

        btBersih.setOnClickListener {
            etSuhuAwal.text.clear()  // Mengosongkan EditText
            spSuhuAwal.setSelection(0)  // Mengatur Spinner ke pilihan pertama
            spSuhuAkhir.setSelection(0)  // Mengatur Spinner ke pilihan pertama
            tvHasilSuhuAkhir.text = "-"  // Mengatur hasil akhir ke default
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    //fungsi konversi suhu
    fun konversiSuhu() {
        val decimalFormat = DecimalFormat("#.###")

        // Cek apakah input kosong
        if (etSuhuAwal.text.isEmpty()) {
            tvHasilSuhuAkhir.text = "Input tidak valid"
            return
        }

        // Mengambil nilai suhu awal
        val suhuAwal = etSuhuAwal.text.toString().toDouble()
        // Mengambil jenis suhu awal
        val suhuAwalString = spSuhuAwal.selectedItem.toString()
        // Mengambil jenis suhu akhir
        val suhuAkhirString = spSuhuAkhir.selectedItem.toString()

        // Logika konversi suhu
        val hasilTerformat = when (suhuAwalString) {
            "Celsius" -> {
                when (suhuAkhirString) {
                    "Celsius" -> decimalFormat.format(suhuAwal)
                    "Fahrenheit" -> decimalFormat.format((suhuAwal * 9/5) + 32)
                    "Kelvin" -> decimalFormat.format(suhuAwal + 273.15)
                    else -> "-"
                }
            }
            "Fahrenheit" -> {
                when (suhuAkhirString) {
                    "Celsius" -> decimalFormat.format((suhuAwal - 32) * 5/9)
                    "Fahrenheit" -> decimalFormat.format(suhuAwal)
                    "Kelvin" -> decimalFormat.format((suhuAwal - 32) * 5/9 + 273.15)
                    else -> "-"
                }
            }
            "Kelvin" -> {
                when (suhuAkhirString) {
                    "Celsius" -> decimalFormat.format(suhuAwal - 273.15)
                    "Fahrenheit" -> decimalFormat.format((suhuAwal - 273.15) * 9/5 + 32)
                    "Kelvin" -> decimalFormat.format(suhuAwal)
                    else -> "-"
                }
            }
            else -> "-"
        }

        // Menampilkan hasil
        tvHasilSuhuAkhir.text = hasilTerformat
    }

}
