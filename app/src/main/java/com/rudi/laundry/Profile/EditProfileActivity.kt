package com.rudi.laundry.Profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.rudi.laundry.R

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etName: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var profileImage: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var btnSave: View
    private lateinit var btnChangePhoto: View

    private var selectedImageUri: Uri? = null

    // Activity result launcher for image selection
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                profileImage.setImageURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        initViews()

        // Load current user data
        loadUserData()

        // Setup click listeners
        setupClickListeners()
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        etAddress = findViewById(R.id.etAddress)
        profileImage = findViewById(R.id.profileImage)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val nama = prefs.getString("nama", "") ?: ""
        val nohp = prefs.getString("nohp", "") ?: ""
        val email = prefs.getString("email", "") ?: ""
        val alamat = prefs.getString("alamat", "") ?: ""

        etName.setText(nama)
        etPhone.setText(nohp)
        etEmail.setText(email)
        etAddress.setText(alamat)

        // Load Firebase user email if available
        auth.currentUser?.email?.let { firebaseEmail ->
            if (email.isEmpty()) {
                etEmail.setText(firebaseEmail)
            }
        }
    }

    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            onBackPressed()
        }

        // Change photo button
        btnChangePhoto.setOnClickListener {
            showImagePickerDialog()
        }

        // Save button
        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Kamera", "Galeri")
        AlertDialog.Builder(this)
            .setTitle("Pilih Foto")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            imagePickerLauncher.launch(intent)
        } else {
            Toast.makeText(this, "Kamera tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun saveProfile() {
        val nama = etName.text.toString().trim()
        val nohp = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val alamat = etAddress.text.toString().trim()

        // Validate input
        if (nama.isEmpty()) {
            etName.error = "Nama tidak boleh kosong"
            etName.requestFocus()
            return
        }

        if (nohp.isEmpty()) {
            etPhone.error = "Nomor telepon tidak boleh kosong"
            etPhone.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Email tidak boleh kosong"
            etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Format email tidak valid"
            etEmail.requestFocus()
            return
        }

        // Show loading
        btnSave.isEnabled = false

        // Update Firebase user profile
        val user = auth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(nama)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update email if different
                    if (user.email != email) {
                        user.updateEmail(email)
                            .addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    saveToSharedPreferences(nama, nohp, email, alamat)
                                } else {
                                    // Save other data even if email update fails
                                    saveToSharedPreferences(nama, nohp, user.email ?: email, alamat)
                                    Toast.makeText(this, "Profil disimpan, namun email gagal diperbarui", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        saveToSharedPreferences(nama, nohp, email, alamat)
                    }
                } else {
                    // Save to SharedPreferences even if Firebase update fails
                    saveToSharedPreferences(nama, nohp, email, alamat)
                    Toast.makeText(this, "Profil disimpan secara lokal", Toast.LENGTH_SHORT).show()
                }
                btnSave.isEnabled = true
            }
    }

    private fun saveToSharedPreferences(nama: String, nohp: String, email: String, alamat: String) {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        prefs.edit()
            .putString("nama", nama)
            .putString("nohp", nohp)
            .putString("email", email)
            .putString("alamat", alamat)
            .apply()

        Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()

        // Return to profile activity
        setResult(RESULT_OK)
        finish()
    }

    override fun onBackPressed() {
        // Check if there are unsaved changes
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val currentNama = prefs.getString("nama", "") ?: ""
        val currentNohp = prefs.getString("nohp", "") ?: ""
        val currentEmail = prefs.getString("email", "") ?: ""
        val currentAlamat = prefs.getString("alamat", "") ?: ""

        val hasChanges = etName.text.toString().trim() != currentNama ||
                etPhone.text.toString().trim() != currentNohp ||
                etEmail.text.toString().trim() != currentEmail ||
                etAddress.text.toString().trim() != currentAlamat ||
                selectedImageUri != null

        if (hasChanges) {
            AlertDialog.Builder(this)
                .setTitle("Perubahan Belum Disimpan")
                .setMessage("Anda memiliki perubahan yang belum disimpan. Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Keluar") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            super.onBackPressed()
        }
    }
}