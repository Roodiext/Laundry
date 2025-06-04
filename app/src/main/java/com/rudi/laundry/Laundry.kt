package com.rudi.laundry

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rudi.laundry.Layanan.DataLayananActivity
import com.rudi.laundry.LayananTambahan.DataLayananTambahanActivity
import com.rudi.laundry.Pegawai.DataPegawaiActivity
import com.rudi.laundry.cabang.DataCabangActivity
import com.rudi.laundry.pelanggan.DataPelangganActivity
import com.rudi.laundry.transaksi.transaksi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Laundry : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laundry)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val greetingTextView = findViewById<TextView>(R.id.name)
        val dateTextView = findViewById<TextView>(R.id.date)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val nama = prefs.getString("nama", "Pengguna")

        val greeting = when (hour) {
            in 4..10 -> "Selamat Pagi, $nama"
            in 11..15 -> "Selamat Siang, $nama"
            in 16..18 -> "Selamat Sore, $nama"
            else -> "Selamat Malam, $nama"
        }

        greetingTextView.text = greeting

        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        dateTextView.text = currentDate
    }

    fun account(view: View?) {
        val intent: Intent = Intent(
            this@Laundry,
            profile::class.java
        )
        startActivity(intent)
    }

    fun Cabang(view: View?) {
        val intent: Intent = Intent(
            this@Laundry,
            DataCabangActivity::class.java
        )
        startActivity(intent)
    }

    fun pelanggan(view: View?) {
        val intent: Intent = Intent(
            this@Laundry,
            DataPelangganActivity::class.java
        )
        startActivity(intent)
    }

    fun pegawai(view: View?) {
        val intent: Intent = Intent(
            this@Laundry,
            DataPegawaiActivity::class.java
        )
        startActivity(intent)
    }

    fun layanan(view: View?) {
        val intent: Intent = Intent(
            this@Laundry,
            DataLayananActivity::class.java
        )
        startActivity(intent)
    }

    fun layanantambahan(view: View) {
        val intent: Intent = Intent(
            this@Laundry,
            DataLayananTambahanActivity::class.java
        )
        startActivity(intent)
    }

    fun transaksi(view: View?) {
        val intent: Intent = Intent(
            this@Laundry,
            transaksi::class.java
        )
        startActivity(intent)
    }
}
