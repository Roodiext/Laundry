    package com.rudi.laundry

    import android.content.Intent
    import android.os.Bundle
    import android.text.InputType
    import android.view.View
    import android.widget.EditText
    import android.widget.ImageView
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat

    class Login : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            enableEdgeToEdge()
            setContentView(R.layout.activity_login) // HARUS DI SINI DULU

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            val passwordEditText = findViewById<EditText>(R.id.editPassword)
            val eyeIcon = findViewById<ImageView>(R.id.eyeIcon)

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
        }

        fun Laundry(view: View?) {
            val intent = Intent(this@Login, Laundry::class.java)
            startActivity(intent)
        }
        fun Registrasi(view: View?) {
            val intent = Intent(this@Login, RegisterActivity::class.java)
            startActivity(intent)
        }

    }
