package com.rudi.laundry

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val nama = findViewById<EditText>(R.id.editnama)
        val nohp = findViewById<EditText>(R.id.editPhone)
        val pass = findViewById<EditText>(R.id.editPassword)
        val registerBtn = findViewById<Button>(R.id.btnLogin)

        registerBtn.setOnClickListener {
            val namaStr = nama.text.toString().trim()
            val nohpStr = nohp.text.toString().trim()
            val passwordStr = pass.text.toString().trim()

            if (namaStr.isEmpty() || nohpStr.isEmpty() || passwordStr.length < 6) {
                Toast.makeText(this, "Lengkapi data dan gunakan password min 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = "$nohpStr@laundry.com" //
            auth.createUserWithEmailAndPassword(email, passwordStr).addOnCompleteListener {
                if (it.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val userData = mapOf(
                        "nama" to namaStr,
                        "nohp" to nohpStr
                    )
                    FirebaseDatabase.getInstance().getReference("users").child(uid).setValue(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Register berhasil", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, Login::class.java))
                            finish()
                        }
                } else {
                    Toast.makeText(this, "Register gagal: ${it.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
