package com.rudi.belajarrudi

import android.icu.text.DecimalFormat
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class kalkulator1 : AppCompatActivity() {
    lateinit var button1: Button
    lateinit var button2: Button
    lateinit var button3: Button
    lateinit var button4: Button
    lateinit var button5: Button
    lateinit var button6: Button
    lateinit var button7: Button
    lateinit var button8: Button
    lateinit var button9: Button
    lateinit var button0: Button
    lateinit var button00: Button
    lateinit var buttontambah: Button
    lateinit var buttonkurang: Button
    lateinit var buttonkali: Button
    lateinit var buttonbagi: Button
    lateinit var buttonpersen: Button
    lateinit var buttonkoma: Button
    lateinit var buttonc: Button
    lateinit var buttonhps: Button
    lateinit var buttonsama: Button
    var NilaiAwal: Double = 0.0
    var aksi: String = ""
    var koma: Boolean = false
    lateinit var judul: TextView
    lateinit var hasil: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kalkulator1)

        init()
        pencet()
        isiData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun isiData() {
        if (hasil.text.isNotEmpty()) {
            NilaiAwal = hasil.text.toString().toDouble()
            hasil.text.clear()
        } else {
            NilaiAwal = 0.0
        }
    }

    private fun pencet() {
        buttonc.setOnClickListener {
            koma = false
            aksi = ""
            hasil.text.clear()
        }

        buttonhps.setOnClickListener {
            val currentText = hasil.text.toString()
            if (currentText.isNotEmpty()) {

                hasil.setText(currentText.substring(0,currentText.length - 1))
            }
        }

        button1.setOnClickListener {
            if (koma) {
                hasil.setText("'0,1")
            } else{
                hasil.setText("${hasil.text}")
            }
        }

        button2.setOnClickListener {
            if (koma) {
                hasil.setText("'0,2")
            } else{
                hasil.setText("${hasil.text}")
            }
        }

        button3.setOnClickListener {
            if (koma) {
                hasil.setText("'0,3")
            } else{
                hasil.setText("${hasil.text}")
            }
        }

        button4.setOnClickListener {
            if (koma) {
                hasil.setText("'0,4")
            } else{
                hasil.setText("${hasil.text}")
            }
        }

        button5.setOnClickListener {
            if (koma) {
                hasil.setText("'0,5")
            } else{
                hasil.setText("${hasil.text}")
            }
        }

        button7.setOnClickListener {
            if (koma) {
                hasil.setText("'0,7")
            } else{
                hasil.setText("${hasil.text}")
            }
        }

        button8.setOnClickListener {
            if (koma) {
                hasil.setText("'0,8")
            } else{
                hasil.setText("${hasil.text}")
            }
        }

        button9.setOnClickListener {
            if (koma) {
                hasil.setText("'0,9")
            } else{
                hasil.setText("${hasil.text}")
            }
        }

        button0.setOnClickListener {
            if (hasil.text.isNotEmpty()) {
                hasil.setText("${hasil.text}0")
            }
        }

        button00.setOnClickListener {
            if (hasil.text.isNotEmpty()) {
                hasil.setText("${hasil.text}00")
            }
        }

        buttonkoma.setOnClickListener {
            if (hasil.text.isNotEmpty()) {
                hasil.setText("${hasil.text}")
            } else {
                koma = true
            }
        }

        buttontambah.setOnClickListener {
            isiData()
            aksi = "tambah"
        }

        buttonkurang.setOnClickListener {
            isiData()
            aksi = "kurang"
        }

        buttonkali.setOnClickListener {
            isiData()
            aksi = "kali"
        }

        buttonbagi.setOnClickListener {
            isiData()
            aksi = "kali"
        }

        buttonpersen.setOnClickListener {
            isiData()
            aksi = "persen"
        }

        buttonsama.setOnClickListener {
            if (aksi.isNotEmpty()) {
                val decimalFormat = DecimalFormat("#.###")
                if (aksi.equals("tambah")) {

                    if (hasil.text.isNotEmpty()) {
                        val hasilPerhitungan = NilaiAwal + hasil.text.toString().toDouble()
                        val hasilTerformat = decimalFormat.format(hasilPerhitungan)
                        hasil.setText(hasilTerformat)
                    }

                } else if (aksi.equals("kurang")) {

                    if (hasil.text.isNotEmpty()) {
                        val hasilPerhitungan = NilaiAwal - hasil.text.toString().toDouble()
                        val hasilTerformat = decimalFormat.format(hasilPerhitungan)
                        hasil.setText(hasilTerformat)
                    }
                } else if (aksi.equals("kali")) {

                    if ((hasil.text.isNotEmpty())) {
                        val hasilPerhitungan = NilaiAwal * hasil.text.toString().toDouble()
                        val hasilTerformat = decimalFormat.format(hasilPerhitungan)
                        hasil.setText(hasilTerformat)
                    }
                } else if (aksi.equals("bagi")) {

                    if (hasil.text.isNotEmpty()) {
                        val hasilPerhitungan = NilaiAwal / hasil.text.toString().toDouble()
                        val hasilTerformat = decimalFormat.format(hasilPerhitungan)
                        hasil.setText(hasilTerformat)
                    }

                } else if (aksi.equals("persen")) {

                    val hasilPerhitungan = NilaiAwal * hasil.text.toString().toDouble() / 100
                    val hasilTerformat = decimalFormat.format(hasilPerhitungan)
                    hasil.setText(hasilTerformat)
                }
            }
        }
    }

    fun init() {
        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        button3 = findViewById(R.id.button3)
        button4 = findViewById(R.id.button4)
        button5 = findViewById(R.id.button5)
        button6 = findViewById(R.id.button6)
        button7 = findViewById(R.id.button7)
        button8 = findViewById(R.id.button8)
        button9 = findViewById(R.id.button9)
        button0 = findViewById(R.id.button0)
        button00 = findViewById(R.id.button00)
        buttontambah = findViewById(R.id.buttontambah)
        buttonkurang = findViewById(R.id.buttonkurang)
        buttonkali = findViewById(R.id.button1)
        buttonbagi = findViewById(R.id.button1)
        buttonpersen = findViewById(R.id.button1)
        buttonkoma = findViewById(R.id.buttonkoma)
        buttonhps = findViewById(R.id.button1)
        buttonc = findViewById(R.id.button1)
        buttonsama = findViewById(R.id.buttonsama)
        hasil = findViewById(R.id.hasil)
        judul = findViewById(R.id.judul)
    }
}