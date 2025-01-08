package com.rudi.belajarrudi

import android.icu.text.DecimalFormat
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class KalkulatorActivity : AppCompatActivity() {
    lateinit var buttonc: Button
    lateinit var button1: Button
    lateinit var button2: Button
    lateinit var button3: Button
    lateinit var button4: Button
    lateinit var button5: Button
    lateinit var button6: Button
    lateinit var button7: Button
    lateinit var button8: Button
    lateinit var button9: Button
    lateinit var button00: Button
    lateinit var button0: Button
    lateinit var buttonkoma: Button
    lateinit var buttonkali: Button
    lateinit var buttontambah: Button
    lateinit var buttonkurang: Button
    lateinit var buttonbagi: Button
    lateinit var buttonhps: Button
    lateinit var buttonpersen: Button
    lateinit var buttonsama: Button

    var NilaiAwal: Double = 0.0
    var aksi: String = ""
    var koma: Boolean = false
    lateinit var judul: TextView
    lateinit var hasil: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kalkulator)

        init()
        pencet()
        isiData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    // Initialize all button references
    fun init() {
        buttonc = findViewById(R.id.buttonc)
        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        button3 = findViewById(R.id.button3)
        button4 = findViewById(R.id.button4)
        button5 = findViewById(R.id.button5)
        button6 = findViewById(R.id.button6)
        button7 = findViewById(R.id.button7)
        button8 = findViewById(R.id.button8)
        button9 = findViewById(R.id.button9)
        button00 = findViewById(R.id.button00)
        button0 = findViewById(R.id.button0)
        buttonpersen = findViewById(R.id.buttonpersen)
        buttonkali = findViewById(R.id.buttonkali)
        buttonbagi = findViewById(R.id.buttonbagi)
        buttonkurang = findViewById(R.id.buttonkurang)
        buttontambah = findViewById(R.id.buttontambah)
        buttonsama = findViewById(R.id.buttonsama)
        buttonhps = findViewById(R.id.buttonhps)
        buttonkoma = findViewById(R.id.buttonkoma)
        hasil = findViewById(R.id.hasil)
        judul = findViewById(R.id.judul)
    }

    // Reset NilaiAwal and the result text
    private fun isiData() {
        if (hasil.text.isNotEmpty()) {
            NilaiAwal = hasil.text.toString().toDouble()
            hasil.text.clear()
        } else {
            NilaiAwal = 0.0
        }
    }



    // Handle button clicks
    private fun pencet() {
        buttonc.setOnClickListener {
            koma = false
            aksi = ""
           hasil.text.clear()
        }


        // Number buttons
        button1.setOnClickListener { appendNumber("1") }
        button2.setOnClickListener { appendNumber("2") }
        button3.setOnClickListener { appendNumber("3") }
        button4.setOnClickListener { appendNumber("4") }
        button5.setOnClickListener { appendNumber("5") }
        button6.setOnClickListener { appendNumber("6") }
        button7.setOnClickListener { appendNumber("7") }
        button8.setOnClickListener { appendNumber("8") }
        button9.setOnClickListener { appendNumber("9") }
        button0.setOnClickListener { appendNumber("0") }
        button00.setOnClickListener { appendNumber("00") }

        // Decimal (comma) button
        buttonkoma.setOnClickListener {
            if (hasil.text.isNotEmpty() && !koma) {
                hasil.setText("${hasil.text},")  // Append a comma
                koma = true  // Prevent adding multiple commas
            }
        }

        buttonhps.setOnClickListener {
            val currentText = hasil.text.toString()
            if (currentText.isNotEmpty()) {

                hasil.setText(currentText.substring(0, currentText.length - 1))
            }
        }

        button1.setOnClickListener {
            if (koma) {
                hasil.setText("0.1")
            } else {
                hasil.setText("${hasil.text}1")
            }
        }

        button2.setOnClickListener {
            if (koma) {
                hasil.setText("0.2")
            } else {
               hasil.setText("${hasil.text}2")
            }
        }

        button3.setOnClickListener {
            if (koma) {
                hasil.setText("0.3")
            } else {
                hasil.setText("${hasil.text}3")
            }
        }

        button4.setOnClickListener {
            if (koma) {
                hasil.setText("0.4")
            } else {
                hasil.setText("${hasil.text}4")
            }
        }

        button5.setOnClickListener {
            if (koma) {
               hasil.setText("0.5")
            } else {
                hasil.setText("${hasil.text}5")
            }
        }

        button6.setOnClickListener {
            if (koma) {
                hasil.setText("0.6")
            } else {
                hasil.setText("${hasil.text}6")
            }
        }

        button7.setOnClickListener {
            if (koma) {
                hasil.setText("0.7")
            } else {
                hasil.setText("${hasil.text}7")
            }
        }

        button8.setOnClickListener {
            if (koma) {
                hasil.setText("0.8")
            } else {
                hasil.setText("${hasil.text}8")
            }
        }

        button9.setOnClickListener {
            if (koma) {
                hasil.setText("0.9")
            } else {
                hasil.setText("${hasil.text}9")
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
                hasil.setText("${hasil.text}.")
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
            aksi = "bagi"
        }
        buttonpersen.setOnClickListener {
            isiData()
            aksi = "persen"
        }


        // Equals button (calculate the result)
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

                    if (hasil.text.isNotEmpty()) {
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
                }  else if (aksi.equals("persen")) {
                    if (hasil.text.isNotEmpty()) {
                        val hasilPerhitungan = NilaiAwal * hasil.text.toString().toDouble() / 100
                        val hasilTerformat = decimalFormat.format(hasilPerhitungan)
                        hasil.setText(hasilTerformat)
                    }
                }
            }
        }
    }


    // Helper function to append numbers to the result field
    private fun appendNumber(number: String) {
        hasil.setText("${hasil.text}$number")
    }
}
