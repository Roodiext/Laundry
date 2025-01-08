package com.rudi.laundry

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Laundry : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_laundry)

        // Mengatur padding untuk edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Menemukan TextView berdasarkan ID di layout
        val greetingTextView = findViewById<TextView>(R.id.name)
        val dateTextView = findViewById<TextView>(R.id.date)

        // Mendapatkan waktu saat ini
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Menentukan teks salam berdasarkan waktu
        val greeting = when (hour) {
            in 4..10 -> "Selamat Pagi, Rudi"
            in 11..15 -> "Selamat Siang, Rudi"
            in 16..18 -> "Selamat Sore, Rudi"
            else -> "Selamat Malam, Rudi"
        }

        // Menampilkan teks salam di TextView
        greetingTextView.text = greeting

        // Menentukan format tanggal (Hari, Bulan, Tahun)
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)

        // Menampilkan tanggal di TextView
        dateTextView.text = currentDate
    }
}
