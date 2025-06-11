package com.rudi.laundry

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var namaEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // Initialize views
        namaEditText = findViewById(R.id.editnama)
        phoneEditText = findViewById(R.id.editPhone)
        passwordEditText = findViewById(R.id.editPassword)
        registerButton = findViewById(R.id.btnLogin) // ID masih btnLogin di XML
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

        // Setup register button click listener
        registerButton.setOnClickListener {
            performRegister()
        }

        // Setup back to login click listener
        val backToLoginText = findViewById<TextView>(R.id.tvBackToLogin)
        backToLoginText.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    private fun performRegister() {
        val namaStr = namaEditText.text.toString().trim()
        val phoneStr = phoneEditText.text.toString().trim()
        val passwordStr = passwordEditText.text.toString().trim()

        // Validasi input kosong dan format
        if (namaStr.isEmpty()) {
            namaEditText.error = "Nama lengkap harus diisi"
            namaEditText.requestFocus()
            Toast.makeText(this, "Silakan masukkan nama lengkap", Toast.LENGTH_SHORT).show()
            return
        }

        if (namaStr.length < 2) {
            namaEditText.error = "Nama minimal 2 karakter"
            namaEditText.requestFocus()
            Toast.makeText(this, "Nama minimal 2 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        if (phoneStr.isEmpty()) {
            phoneEditText.error = "No HP harus diisi"
            phoneEditText.requestFocus()
            Toast.makeText(this, "Silakan masukkan nomor HP", Toast.LENGTH_SHORT).show()
            return
        }

        if (phoneStr.length < 10 || phoneStr.length > 15) {
            phoneEditText.error = "No HP tidak valid (10-15 digit)"
            phoneEditText.requestFocus()
            Toast.makeText(this, "Nomor HP harus 10-15 digit", Toast.LENGTH_SHORT).show()
            return
        }

        if (!phoneStr.matches(Regex("^[0-9]+$"))) {
            phoneEditText.error = "No HP hanya boleh angka"
            phoneEditText.requestFocus()
            Toast.makeText(this, "Nomor HP hanya boleh berisi angka", Toast.LENGTH_SHORT).show()
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
        namaEditText.error = null
        phoneEditText.error = null
        passwordEditText.error = null

        // Show loading state
        registerButton.isEnabled = false
        registerButton.text = "Mendaftar..."

        // Create dummy email from phone number
        val email = "$phoneStr@laundry.com"

        // Register with Firebase
        auth.createUserWithEmailAndPassword(email, passwordStr)
            .addOnCompleteListener { task ->
                registerButton.isEnabled = true
                registerButton.text = "Registrasi"

                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val userData = mapOf(
                        "nama" to namaStr,
                        "nohp" to phoneStr,
                        "createdAt" to System.currentTimeMillis()
                    )

                    FirebaseDatabase.getInstance().getReference("users")
                        .child(uid).setValue(userData)
                        .addOnSuccessListener {
                            // Simpan session
                            val prefs = getSharedPreferences("session", MODE_PRIVATE)
                            prefs.edit()
                                .putString("nama", namaStr)
                                .putString("nohp", phoneStr)
                                .apply()

                            Toast.makeText(this, "Registrasi berhasil! Selamat datang $namaStr", Toast.LENGTH_SHORT).show()

                            // Kembali ke login
                            startActivity(Intent(this, Login::class.java))
                            finish()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Gagal menyimpan data: ${exception.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    // Registration gagal
                    val errorMessage = when {
                        task.exception?.message?.contains("already in use") == true -> "Nomor HP sudah terdaftar"
                        task.exception?.message?.contains("weak-password") == true -> "Password terlalu lemah"
                        task.exception?.message?.contains("network") == true -> "Periksa koneksi internet Anda"
                        else -> "Registrasi gagal: ${task.exception?.message}"
                    }

                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()

                    // Highlight field yang bermasalah
                    if (errorMessage.contains("sudah terdaftar")) {
                        phoneEditText.error = "Nomor HP sudah terdaftar"
                        phoneEditText.requestFocus()
                    } else if (errorMessage.contains("Password")) {
                        passwordEditText.error = "Password terlalu lemah"
                        passwordEditText.requestFocus()
                    }
                }
            }
    }
}