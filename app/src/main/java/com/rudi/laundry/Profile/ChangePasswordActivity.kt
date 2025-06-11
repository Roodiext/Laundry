package com.rudi.laundry.Profile

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.rudi.laundry.R

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etCurrentPassword: TextInputEditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnBack: ImageView
    private lateinit var btnUpdatePassword: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        initViews()

        // Setup click listeners
        setupClickListeners()
    }

    private fun initViews() {
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnBack = findViewById(R.id.btnBack)
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword)
    }

    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            onBackPressed()
        }

        // Update password button
        btnUpdatePassword.setOnClickListener {
            updatePassword()
        }
    }

    private fun updatePassword() {
        val currentPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Validate input
        if (!validateInput(currentPassword, newPassword, confirmPassword)) {
            return
        }

        // Show loading state
        btnUpdatePassword.isEnabled = false
        showLoadingToast("Memperbarui password...")

        val user = auth.currentUser
        if (user != null && user.email != null) {
            // Re-authenticate user with current password
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // Update password
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                btnUpdatePassword.isEnabled = true

                                if (updateTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Password berhasil diperbarui",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    finish()
                                } else {
                                    handlePasswordUpdateError(updateTask.exception)
                                }
                            }
                    } else {
                        btnUpdatePassword.isEnabled = true
                        handleReauthenticationError(reauthTask.exception)
                    }
                }
        } else {
            btnUpdatePassword.isEnabled = true
            Toast.makeText(this, "User tidak ditemukan. Silakan login ulang.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun validateInput(currentPassword: String, newPassword: String, confirmPassword: String): Boolean {
        var isValid = true

        // Check current password
        if (currentPassword.isEmpty()) {
            etCurrentPassword.error = "Password saat ini tidak boleh kosong"
            etCurrentPassword.requestFocus()
            isValid = false
        }

        // Check new password
        if (newPassword.isEmpty()) {
            etNewPassword.error = "Password baru tidak boleh kosong"
            if (isValid) etNewPassword.requestFocus()
            isValid = false
        } else if (newPassword.length < 6) {
            etNewPassword.error = "Password minimal 6 karakter"
            if (isValid) etNewPassword.requestFocus()
            isValid = false
        }

        // Check confirm password
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Konfirmasi password tidak boleh kosong"
            if (isValid) etConfirmPassword.requestFocus()
            isValid = false
        } else if (newPassword != confirmPassword) {
            etConfirmPassword.error = "Password tidak sama"
            if (isValid) etConfirmPassword.requestFocus()
            isValid = false
        }

        // Check if new password is same as current password
        if (isValid && currentPassword == newPassword) {
            etNewPassword.error = "Password baru harus berbeda dari password saat ini"
            etNewPassword.requestFocus()
            isValid = false
        }

        return isValid
    }

    private fun handleReauthenticationError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                etCurrentPassword.error = "Password saat ini salah"
                etCurrentPassword.requestFocus()
                Toast.makeText(this, "Password saat ini tidak benar", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(
                    this,
                    "Gagal memverifikasi password: ${exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handlePasswordUpdateError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                etNewPassword.error = "Password terlalu lemah"
                etNewPassword.requestFocus()
                Toast.makeText(this, "Password terlalu lemah. Gunakan kombinasi huruf dan angka.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(
                    this,
                    "Gagal memperbarui password: ${exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showLoadingToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        // Check if user has entered any data
        val hasInput = etCurrentPassword.text.toString().isNotEmpty() ||
                etNewPassword.text.toString().isNotEmpty() ||
                etConfirmPassword.text.toString().isNotEmpty()

        if (hasInput) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Batalkan Perubahan?")
                .setMessage("Data yang telah dimasukkan akan hilang. Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("Tidak") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            super.onBackPressed()
        }
    }
}