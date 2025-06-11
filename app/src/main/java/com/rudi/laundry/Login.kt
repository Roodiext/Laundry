package com.rudi.laundry

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        phoneEditText = findViewById(R.id.editPhone)
        passwordEditText = findViewById(R.id.editPassword)
        loginButton = findViewById(R.id.btnLogin)
        val eyeIcon = findViewById<ImageView>(R.id.eyeIcon)

        // Setup password visibility toggle
        var isPasswordVisible = false
        eyeIcon.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                eyeIcon.setImageResource(R.drawable.showeye)
            } else {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeIcon.setImageResource(R.drawable.hiddeneye)
            }
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        // Setup login button click listener
        loginButton.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val phoneStr = phoneEditText.text.toString().trim()
        val passwordStr = passwordEditText.text.toString().trim()

        // Validasi input kosong
        if (phoneStr.isEmpty()) {
            phoneEditText.error = "No HP harus diisi"
            phoneEditText.requestFocus()
            Toast.makeText(this, "Silakan masukkan nomor HP", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordStr.isEmpty()) {
            passwordEditText.error = "Password harus diisi"
            passwordEditText.requestFocus()
            Toast.makeText(this, "Silakan masukkan password", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordStr.length < 6) {
            passwordEditText.error = "Password minimal 6 karakter"
            passwordEditText.requestFocus()
            Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        // Clear previous errors
        phoneEditText.error = null
        passwordEditText.error = null

        // Show loading state
        loginButton.isEnabled = false
        loginButton.text = "Login..."

        // Create dummy email from phone number
        val email = "$phoneStr@laundry.com"

        // Authenticate with Firebase
        auth.signInWithEmailAndPassword(email, passwordStr)
            .addOnCompleteListener { task ->
                loginButton.isEnabled = true
                loginButton.text = "Login"

                if (task.isSuccessful) {
                    // Login berhasil, ambil data user dari database
                    val uid = auth.currentUser?.uid ?: ""

                    FirebaseDatabase.getInstance().getReference("users")
                        .child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val nama = snapshot.child("nama").getValue(String::class.java) ?: ""

                                // Simpan session
                                val prefs = getSharedPreferences("session", MODE_PRIVATE)
                                prefs.edit()
                                    .putString("nama", nama)
                                    .putString("nohp", phoneStr)
                                    .putBoolean("isLoggedIn", true)
                                    .apply()

                                Toast.makeText(this@Login, "Login berhasil! Selamat datang $nama", Toast.LENGTH_SHORT).show()

                                // Pindah ke activity utama
                                startActivity(Intent(this@Login, Laundry::class.java))
                                finish()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@Login, "Gagal mengambil data user", Toast.LENGTH_SHORT).show()
                            }
                        })
                } else {
                    // Login gagal
                    val errorMessage = when {
                        task.exception?.message?.contains("password") == true -> "Password salah"
                        task.exception?.message?.contains("user") == true -> "Akun tidak ditemukan. Silakan daftar terlebih dahulu"
                        task.exception?.message?.contains("network") == true -> "Periksa koneksi internet Anda"
                        else -> "Login gagal: ${task.exception?.message}"
                    }

                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()

                    // Highlight field yang bermasalah
                    if (errorMessage.contains("Password")) {
                        passwordEditText.error = "Password salah"
                        passwordEditText.requestFocus()
                    } else if (errorMessage.contains("Akun tidak ditemukan")) {
                        phoneEditText.error = "Akun tidak terdaftar"
                        phoneEditText.requestFocus()
                    }
                }
            }
    }

    // Method untuk navigasi ke registrasi (dipanggil dari XML)
    fun Registrasi(view: View?) {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    // Method lama untuk kompatibilitas (bisa dihapus jika tidak digunakan)
    fun Laundry(view: View?) {
        performLogin()
    }
}