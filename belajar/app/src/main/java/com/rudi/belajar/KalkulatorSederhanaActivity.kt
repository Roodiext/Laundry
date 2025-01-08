package com.rudi.belajar

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowI   nsetsCompat

class KalkulatorSederhanaActivity : AppCompatActivity() {
    //langakh 1
    //inisialisasi variabel
    //variabel dari komponen yang diperlukan
    lateinit var bilangan1:EditText
    lateinit var bilangan2:EditText
    lateinit var operasi:Spinner
    lateinit var hitung:Button
    //langkah5
    //membuat variabel penampung bilangan/nilai
    var angka1:Double = 0.0
    var angka2:Double = 0.0
    var hasil:Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kalkulator_sederhana)
        //langkah4
        //memanggil fun init
        init()

        //langkah7
        //button hitung di klik
        //memanggil fun hitung()
        hitung.setOnClickListener{
            //memanggil fun hitung
            hitung()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    //langkah2
    //membuat function
    //untuk sinkron komponen
    fun init(){
        //langkah3
        //mencocokan variabel dengan komponen
        bilangan1=findViewById(R.id.ETbilangan1)
        bilangan2=findViewById(R.id.ETbilangan2)
        operasi=findViewById(R.id.tvOperasiAritmatika)
        hitung=findViewById(R.id.BThitung)

    }

    //langkah 6
    //menghitung operasi aritmatika
    //membuat fun hitung
    fun hitung() {
        //tambah = posisi 0
        //kurang = posisi 1
        //kali = posisi 2
        //bagi = posisi 3

        angka1=bilangan1.text.toString().toDouble()
        angka2=bilangan2.text.toString().toDouble()     
        if (operasi.selectedItemPosition == 0) {
            //operasi penjumlahan
            hasil = angka1+angka2
        } else if (operasi.selectedItemPosition == 1) {
            //operasi pengurangan
            hasil = angka1-angka2
        }else if (operasi.selectedItemPosition == 2){
            //operasi perkalian
            hasil = angka1*angka2
        }else if (operasi.selectedItemPosition == 3){
            //operasi pembagian
            hasil=angka1/angka2
        }else{
            hasil=0.0
        }
        //toast
        //snackbar
        Toast.makeText(this@KalkulatorSederhana