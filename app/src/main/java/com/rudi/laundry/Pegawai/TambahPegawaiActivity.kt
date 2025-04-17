package com.rudi.laundry.Pegawai

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelPegawai

class TambahPegawaiActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pegawai")

    private lateinit var tvjudul: TextView
    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoHP: EditText
    private lateinit var etCabang: EditText
    private lateinit var btSimpan: Button

    var idPegawai:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pegawai)

        initViews()
        setupListeners()
        getData()
        update()

        btSimpan.setOnClickListener{
            cekValidasi()
        }
    }

    fun simpan() {
        val pegawaiBaru = myRef.push()
        val pegawaiId = pegawaiBaru.key ?: "Unknown"

        val data = modelPegawai(
            pegawaiId,
            etNama.text.toString(),
            etAlamat.text.toString(),
            etNoHP.text.toString(),
            System.currentTimeMillis().toString(),
            etCabang.text.toString()
        )

        pegawaiBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Pegawai berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menambahkan pegawai", Toast.LENGTH_SHORT).show()
            }
    }


    private fun initViews() {
        tvjudul = findViewById(R.id.tvjudul_tambah_pegawai)
        etNama = findViewById(R.id.etnama_pegawai)
        etAlamat = findViewById(R.id.etalamat_pegawai)
        etNoHP = findViewById(R.id.etnohp_pegawai)
        etCabang = findViewById(R.id.etcabang_pegawai)
        btSimpan = findViewById(R.id.bttambah)
    }

    fun cekValidasi() {
        val nama = etNama.text.toString()
        val alamat = etAlamat.text.toString()
        val noHp = etNoHP.text.toString()
        val cabang = etCabang.text.toString()

        if (nama.isEmpty()) {
            etNama.error = getString(R.string.card_pegawai)
            Toast.makeText(this, getString(R.string.card_pegawai), Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return
        }
        if (alamat.isEmpty()) {
            etAlamat.error = getString(R.string.card_pegawai_Alamat)
            Toast.makeText(this, getString(R.string.card_pegawai_Alamat), Toast.LENGTH_SHORT).show()
            etAlamat.requestFocus()
            return
        }
        if (noHp.isEmpty()) {
            etNoHP.error = getString(R.string.card_pegawai_noHP)
            Toast.makeText(this, getString(R.string.card_pegawai_noHP), Toast.LENGTH_SHORT).show()
            etNoHP.requestFocus()
            return
        }
        if (cabang.isEmpty()) {
            etCabang.error = getString(R.string.card_pegawai_cabang)
            Toast.makeText(this, getString(R.string.card_pegawai_cabang), Toast.LENGTH_SHORT).show()
            etCabang.requestFocus()
            return
        }

        if (btSimpan.text.toString().equals("simpan", ignoreCase = true)) {
            simpan()
        } else if (btSimpan.text.toString().equals("sunting", ignoreCase = true)) {
            hidup()
            etNama.requestFocus()
            btSimpan.text = "Perbarui"
        } else if (btSimpan.text.toString().equals("Perbarui", ignoreCase = true)) {
            update()
        }
    }



    private fun setupListeners() {
        btSimpan.setOnClickListener {
            val pegawaiBaru = myRef.push()
            val pegawaiId = pegawaiBaru.key ?: "Unknown"
            val data = modelPegawai(pegawaiId, etNama.text.toString(), etAlamat.text.toString(), etNoHP.text.toString(), System.currentTimeMillis().toString(), etCabang.text.toString())

            pegawaiBaru.setValue(data)
                .addOnSuccessListener { finish() }
                .addOnFailureListener { Toast.makeText(this, "Gagal!", Toast.LENGTH_SHORT).show() }
        }
    }

    fun getData(){
        idPegawai = intent.getStringExtra("idPegawai").toString()
        val judul = intent.getStringExtra("judul").toString()
        val nama = intent.getStringExtra("namaPegawai").toString()
        val alamat = intent.getStringExtra("alamatPegawai").toString()
        val hp = intent.getStringExtra("noHPPegawai").toString()
        val cabang = intent.getStringExtra("idCabang").toString()
        tvjudul.text = judul
        etNama.setText(nama)
        etAlamat.setText(alamat)
        etNoHP.setText(hp)
        etCabang.setText(cabang)
        if (!tvjudul.text.equals(this.getString(R.string.activity_tambah_pegawai))){
            if (judul.equals("Edit Pegawai")){
                mati()
                btSimpan.text="sunting"
            }
        }else{
            hidup()
            etNama.requestFocus()
            btSimpan.text="Simpan"
        }
    }

    fun mati(){
        etNama.isEnabled=false
        etAlamat.isEnabled=false
        etNoHP.isEnabled=false
        etCabang.isEnabled=false
    }

    fun hidup(){
        etNama.isEnabled=true
        etAlamat.isEnabled=true
        etNoHP.isEnabled=true
        etCabang.isEnabled=true
    }

    fun update(){
        val pegawaiRef = database.getReference("pegawai").child(idPegawai)

        val updateData = mutableMapOf<String, Any>()
        updateData["namaPegawai"] = etNama.text.toString()
        updateData["alamatPegawai"] = etAlamat.text.toString()
        updateData["noHPPegawai"] = etNoHP.text.toString()
        updateData["idCabang"] = etCabang.text.toString()

        pegawaiRef.updateChildren(updateData)
            .addOnSuccessListener {
                Toast.makeText(this@TambahPegawaiActivity, "Data Berhasil Diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this@TambahPegawaiActivity, "Data Pegawai Gagal Diperbarui", Toast.LENGTH_SHORT).show()
            }
    }

}