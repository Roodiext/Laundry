package com.rudi.laundry

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.rudi.laundry.Profile.ChangePasswordActivity
import com.rudi.laundry.Profile.EditProfileActivity
import java.text.SimpleDateFormat
import java.util.*

class profile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tvUserName: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfilePhone: TextView
    private lateinit var tvLastLogin: TextView

    // TextViews untuk multilanguage
    private lateinit var tvProfileInfo: TextView
    private lateinit var tvFullNameLabel: TextView
    private lateinit var tvPhoneLabel: TextView
    private lateinit var tvLastLoginLabel: TextView
    private lateinit var tvSettings: TextView
    private lateinit var tvEditProfile: TextView
    private lateinit var tvChangePassword: TextView
    private lateinit var tvLogout: TextView

    private var currentLanguage = "id" // Default bahasa Indonesia

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "profile_info" to "Informasi Profil",
            "full_name" to "Nama Lengkap",
            "phone_number" to "Nomor Telepon",
            "last_login" to "Terakhir Login",
            "settings" to "Pengaturan",
            "edit_profile" to "Edit Profil",
            "change_password" to "Ubah Password",
            "logout" to "Logout",
            "logout_confirmation_title" to "Konfirmasi Logout",
            "logout_confirmation_message" to "Apakah Anda yakin ingin keluar dari aplikasi?",
            "yes" to "Ya",
            "cancel" to "Batal",
            "profile_updated" to "Profil berhasil diperbarui",
            "need_relogin" to "Anda perlu login ulang untuk mengubah password",
            "logout_success" to "Logout berhasil. Sampai jumpa!",
            "logout_failed" to "Gagal logout: ",
            "user_default" to "Pengguna",
            "no_data" to "Tidak tersedia"
        ),
        "en" to mapOf(
            "profile_info" to "Profile Information",
            "full_name" to "Full Name",
            "phone_number" to "Phone Number",
            "last_login" to "Last Login",
            "settings" to "Settings",
            "edit_profile" to "Edit Profile",
            "change_password" to "Change Password",
            "logout" to "Logout",
            "logout_confirmation_title" to "Logout Confirmation",
            "logout_confirmation_message" to "Are you sure you want to exit the application?",
            "yes" to "Yes",
            "cancel" to "Cancel",
            "profile_updated" to "Profile updated successfully",
            "need_relogin" to "You need to login again to change password",
            "logout_success" to "Logout successful. See you later!",
            "logout_failed" to "Logout failed: ",
            "user_default" to "User",
            "no_data" to "Not available"
        )
    )

    // Activity result launcher for edit profile
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Refresh user data when returning from edit profile
            loadUserData()
            val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
            Toast.makeText(this, texts["profile_updated"], Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Load current language
        loadLanguagePreference()

        // Initialize views
        initViews()

        // Load user data from session
        loadUserData()

        // Update all texts based on current language
        updateAllTexts()

        // Setup click listeners
        setupClickListeners()
    }

    private fun loadLanguagePreference() {
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserPhone = findViewById(R.id.tvUserPhone)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfilePhone = findViewById(R.id.tvProfilePhone)
        tvLastLogin = findViewById(R.id.tvLastLogin)

        // Initialize multilanguage TextViews
        tvProfileInfo = findViewById(R.id.tvProfileInfo)
        tvFullNameLabel = findViewById(R.id.tvFullNameLabel)
        tvPhoneLabel = findViewById(R.id.tvPhoneLabel)
        tvLastLoginLabel = findViewById(R.id.tvLastLoginLabel)
        tvSettings = findViewById(R.id.tvSettings)
        tvEditProfile = findViewById(R.id.tvEditProfile)
        tvChangePassword = findViewById(R.id.tvChangePassword)
        tvLogout = findViewById(R.id.tvLogout)
    }

    private fun updateAllTexts() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Update static texts
        tvProfileInfo.text = texts["profile_info"]
        tvFullNameLabel.text = texts["full_name"]
        tvPhoneLabel.text = texts["phone_label"]
        tvLastLoginLabel.text = texts["last_login"]
        tvSettings.text = texts["settings"]
        tvEditProfile.text = texts["edit_profile"]
        tvChangePassword.text = texts["change_password"]
        tvLogout.text = texts["logout"]
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        val nama = prefs.getString("nama", texts["user_default"]) ?: texts["user_default"]!!
        val nohp = prefs.getString("nohp", texts["no_data"]) ?: texts["no_data"]!!

        // Set data to header views
        tvUserName.text = nama
        tvUserPhone.text = nohp

        // Set data to profile info views
        tvProfileName.text = nama
        tvProfilePhone.text = nohp

        // Set last login time (current time for now)
        val locale = if (currentLanguage == "en") Locale.ENGLISH else Locale("id", "ID")
        val currentTime = SimpleDateFormat("dd MMM yyyy, HH:mm", locale)
            .format(Date())
        tvLastLogin.text = currentTime
    }

    private fun setupClickListeners() {
        // Edit Profile click listener
        findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            editProfileLauncher.launch(intent)
        }

        // Change Password click listener
        findViewById<View>(R.id.btnChangePassword).setOnClickListener {
            val user = auth.currentUser
            if (user != null) {
                val intent = Intent(this, ChangePasswordActivity::class.java)
                startActivity(intent)
            } else {
                val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
                Toast.makeText(this, texts["need_relogin"], Toast.LENGTH_SHORT).show()
                // Redirect to login
                performLogout()
            }
        }
    }

    // Logout function called from XML onClick
    fun logout(view: View?) {
        showLogoutConfirmationDialog()
    }

    private fun showLogoutConfirmationDialog() {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        AlertDialog.Builder(this)
            .setTitle(texts["logout_confirmation_title"])
            .setMessage(texts["logout_confirmation_message"])
            .setPositiveButton(texts["yes"]) { _, _ ->
                performLogout()
            }
            .setNegativeButton(texts["cancel"]) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun performLogout() {
        try {
            val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

            // Sign out from Firebase
            auth.signOut()

            // Clear session data
            val prefs = getSharedPreferences("session", MODE_PRIVATE)
            prefs.edit()
                .clear()
                .putBoolean("isLoggedIn", false)
                .apply()

            // Show logout success message
            Toast.makeText(this, texts["logout_success"], Toast.LENGTH_SHORT).show()

            // Navigate to Login activity
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
            Toast.makeText(this, texts["logout_failed"] + "${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if language has changed
        val sharedPref = getSharedPreferences("language_pref", MODE_PRIVATE)
        val newLanguage = sharedPref.getString("selected_language", "id") ?: "id"

        if (newLanguage != currentLanguage) {
            currentLanguage = newLanguage
            updateAllTexts()
        }

        // Refresh user data when returning to this activity
        loadUserData()
    }

    // Function to handle back button - return to main activity
    override fun onBackPressed() {
        val intent = Intent(this, Laundry::class.java)
        startActivity(intent)
        finish()
        super.onBackPressed()
    }
}