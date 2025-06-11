package com.rudi.laundry.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelCabang

class AdapterDataCabang(
    private var listCabang: ArrayList<modelCabang>,
    private val onDeleteClick: (modelCabang) -> Unit,
    private val onEditClick: (modelCabang) -> Unit
) : RecyclerView.Adapter<AdapterDataCabang.ViewHolder>() {

    // Language texts mapping
    private val languageTexts = mapOf(
        "id" to mapOf(
            "store_name_na" to "Nama Toko Tidak Tersedia",
            "branch_name_na" to "Nama Cabang Tidak Tersedia",
            "address_na" to "Alamat tidak tersedia",
            "phone_na" to "Telepon tidak tersedia",
            "operational_hours_na" to "Info jam operasional tidak tersedia",
            "status_na" to "Status tidak tersedia",
            "open" to "BUKA",
            "closed" to "TUTUP",
            "address_label" to "Alamat",
            "phone_label" to "Telepon",
            "directions_btn" to "PETUNJUK",
            "edit_btn" to "EDIT",
            "delete_btn" to "HAPUS",
            "everyday" to "Setiap Hari",
            "monday" to "Senin",
            "tuesday" to "Selasa",
            "wednesday" to "Rabu",
            "thursday" to "Kamis",
            "friday" to "Jumat",
            "saturday" to "Sabtu",
            "sunday" to "Minggu"
        ),
        "en" to mapOf(
            "store_name_na" to "Store Name Not Available",
            "branch_name_na" to "Branch Name Not Available",
            "address_na" to "Address not available",
            "phone_na" to "Phone not available",
            "operational_hours_na" to "Operational hours info not available",
            "status_na" to "Status not available",
            "open" to "OPEN",
            "closed" to "CLOSED",
            "address_label" to "Address",
            "phone_label" to "Phone",
            "directions_btn" to "DIRECTIONS",
            "edit_btn" to "EDIT",
            "delete_btn" to "DELETE",
            "everyday" to "Everyday",
            "monday" to "Monday",
            "tuesday" to "Tuesday",
            "wednesday" to "Wednesday",
            "thursday" to "Thursday",
            "friday" to "Friday",
            "saturday" to "Saturday",
            "sunday" to "Sunday"
        )
    )

    private var currentLanguage = "id"
    private var context: Context? = null

    // Function to set context and initialize language
    fun setContext(context: Context) {
        this.context = context
        loadLanguagePreference()
    }

    private fun loadLanguagePreference() {
        context?.let { ctx ->
            val sharedPref = ctx.getSharedPreferences("language_pref", Context.MODE_PRIVATE)
            val savedLanguage = sharedPref.getString("selected_language", "id") ?: "id"
            if (currentLanguage != savedLanguage) {
                currentLanguage = savedLanguage
                android.util.Log.d("AdapterDebug", "Language loaded: $currentLanguage")
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStoreName: TextView = itemView.findViewById(R.id.tvStoreName)
        val tvBranchName: TextView = itemView.findViewById(R.id.tvBranchName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        val tvOperationalHours: TextView = itemView.findViewById(R.id.tvOperationalHours)
        val btnCall: ImageButton = itemView.findViewById(R.id.btnCall)
        val btnDirections: Button = itemView.findViewById(R.id.btnDirections)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        // Additional labels that need translation
        val tvAddressLabel: TextView? = itemView.findViewById(R.id.tvAddressLabel)
        val tvPhoneLabel: TextView? = itemView.findViewById(R.id.tvPhoneLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Set context if not already set
        if (context == null) {
            setContext(parent.context)
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_cabang, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cabang = listCabang[position]
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Refresh language preference before binding
        loadLanguagePreference()

        // Debug log untuk melihat data yang sedang di-bind
        android.util.Log.d("AdapterDebug", "Binding position $position:")
        android.util.Log.d("AdapterDebug", "  - namaToko: '${cabang.namaToko}'")
        android.util.Log.d("AdapterDebug", "  - namaCabang: '${cabang.namaCabang}'")
        android.util.Log.d("AdapterDebug", "  - currentLanguage: '$currentLanguage'")

        // Set data ke views dengan null safety dan trim whitespace
        holder.tvStoreName.text = if (cabang.namaToko.trim().isNotEmpty()) {
            cabang.namaToko.trim()
        } else {
            texts["store_name_na"]!!
        }

        holder.tvBranchName.text = if (cabang.namaCabang.trim().isNotEmpty()) {
            cabang.namaCabang.trim()
        } else {
            texts["branch_name_na"]!!
        }

        holder.tvAddress.text = if (cabang.alamatCabang.trim().isNotEmpty()) {
            cabang.alamatCabang.trim()
        } else {
            texts["address_na"]!!
        }

        holder.tvPhone.text = if (cabang.noTelpCabang.trim().isNotEmpty()) {
            cabang.noTelpCabang.trim()
        } else {
            texts["phone_na"]!!
        }

        // Set jam operasional info dengan terjemahan
        try {
            holder.tvOperationalHours.text = getTranslatedOperationalHours(cabang, texts)
        } catch (e: Exception) {
            holder.tvOperationalHours.text = texts["operational_hours_na"]!!
            android.util.Log.e("AdapterDebug", "Error getting operational hours: ${e.message}")
        }

        // Set status real-time dengan warna yang sesuai dan terjemahan
        try {
            val realTimeStatus = cabang.getRealTimeStatus()
            val translatedStatus = when (realTimeStatus) {
                "BUKA" -> {
                    holder.tvStatus.setBackgroundResource(R.drawable.status_badge_open)
                    texts["open"]!!
                }
                "TUTUP" -> {
                    holder.tvStatus.setBackgroundResource(R.drawable.status_badge_closed)
                    texts["closed"]!!
                }
                else -> {
                    holder.tvStatus.setBackgroundResource(R.drawable.status_badge_background)
                    if (realTimeStatus == "OPEN") texts["open"]!! else if (realTimeStatus == "CLOSED") texts["closed"]!! else realTimeStatus
                }
            }
            holder.tvStatus.text = translatedStatus
        } catch (e: Exception) {
            holder.tvStatus.text = texts["status_na"]!!
            holder.tvStatus.setBackgroundResource(R.drawable.status_badge_background)
            android.util.Log.e("AdapterDebug", "Error getting real-time status: ${e.message}")
        }

        // Update button text with translation
        holder.btnDirections.text = texts["directions_btn"]!!
        holder.btnEdit.text = texts["edit_btn"]!!
        holder.btnDelete.text = texts["delete_btn"]!!

        // Update labels if they exist in the layout
        holder.tvAddressLabel?.text = texts["address_label"]!!.uppercase()
        holder.tvPhoneLabel?.text = texts["phone_label"]!!.uppercase()

        // Click listeners
        holder.btnCall.setOnClickListener {
            val phoneNumber = cabang.noTelpCabang.trim()
            if (phoneNumber.isNotEmpty()) {
                try {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phoneNumber")
                    }
                    holder.itemView.context.startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("AdapterDebug", "Error starting call intent: ${e.message}")
                }
            }
        }

        holder.btnDirections.setOnClickListener {
            val address = cabang.alamatCabang.trim()
            if (address.isNotEmpty()) {
                try {
                    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                        setPackage("com.google.android.apps.maps")
                    }

                    // Cek apakah Google Maps tersedia
                    if (mapIntent.resolveActivity(holder.itemView.context.packageManager) != null) {
                        holder.itemView.context.startActivity(mapIntent)
                    } else {
                        // Fallback ke web browser
                        val webIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}")
                        }
                        holder.itemView.context.startActivity(webIntent)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AdapterDebug", "Error starting maps intent: ${e.message}")
                }
            }
        }

        holder.btnEdit.setOnClickListener {
            try {
                onEditClick(cabang)
            } catch (e: Exception) {
                android.util.Log.e("AdapterDebug", "Error in edit click: ${e.message}")
            }
        }

        holder.btnDelete.setOnClickListener {
            try {
                onDeleteClick(cabang)
            } catch (e: Exception) {
                android.util.Log.e("AdapterDebug", "Error in delete click: ${e.message}")
            }
        }

        // Set click listener untuk seluruh card (optional)
        holder.itemView.setOnClickListener {
            android.util.Log.d("AdapterDebug", "Card clicked for: ${cabang.namaCabang}")
        }
    }

    private fun getTranslatedOperationalHours(cabang: modelCabang, texts: Map<String, String>): String {
        return try {
            val originalHours = cabang.getOperationalHoursInfo()

            // Translate day names
            var translatedHours = originalHours

            // Replace day names based on current language
            when (currentLanguage) {
                "en" -> {
                    translatedHours = translatedHours
                        .replace("Setiap Hari", texts["everyday"]!!, ignoreCase = true)
                        .replace("Senin", texts["monday"]!!, ignoreCase = true)
                        .replace("Selasa", texts["tuesday"]!!, ignoreCase = true)
                        .replace("Rabu", texts["wednesday"]!!, ignoreCase = true)
                        .replace("Kamis", texts["thursday"]!!, ignoreCase = true)
                        .replace("Jumat", texts["friday"]!!, ignoreCase = true)
                        .replace("Sabtu", texts["saturday"]!!, ignoreCase = true)
                        .replace("Minggu", texts["sunday"]!!, ignoreCase = true)
                }
                "id" -> {
                    // Convert from English to Indonesian if needed
                    translatedHours = translatedHours
                        .replace("Everyday", texts["everyday"]!!, ignoreCase = true)
                        .replace("Monday", texts["monday"]!!, ignoreCase = true)
                        .replace("Tuesday", texts["tuesday"]!!, ignoreCase = true)
                        .replace("Wednesday", texts["wednesday"]!!, ignoreCase = true)
                        .replace("Thursday", texts["thursday"]!!, ignoreCase = true)
                        .replace("Friday", texts["friday"]!!, ignoreCase = true)
                        .replace("Saturday", texts["saturday"]!!, ignoreCase = true)
                        .replace("Sunday", texts["sunday"]!!, ignoreCase = true)
                }
            }

            translatedHours
        } catch (e: Exception) {
            android.util.Log.e("AdapterDebug", "Error translating operational hours: ${e.message}")
            texts["operational_hours_na"]!!
        }
    }

    override fun getItemCount(): Int {
        android.util.Log.d("AdapterDebug", "getItemCount: ${listCabang.size}")
        return listCabang.size
    }

    // Method untuk update bahasa
    fun updateLanguage(newLanguage: String) {
        if (currentLanguage != newLanguage) {
            val oldLanguage = currentLanguage
            currentLanguage = newLanguage
            android.util.Log.d("AdapterDebug", "Language updated from $oldLanguage to: $currentLanguage")
            notifyDataSetChanged()
        }
    }

    // Method untuk mendapatkan bahasa saat ini
    fun getCurrentLanguage(): String {
        return currentLanguage
    }

    // Method untuk refresh language dari SharedPreferences
    fun refreshLanguageFromPreferences() {
        context?.let { ctx ->
            val sharedPref = ctx.getSharedPreferences("language_pref", Context.MODE_PRIVATE)
            val savedLanguage = sharedPref.getString("selected_language", "id") ?: "id"
            android.util.Log.d("AdapterDebug", "Refreshing language from preferences: $savedLanguage")
            updateLanguage(savedLanguage)
        }
    }

    // Method untuk update data dengan logging yang lebih detail
    fun updateData(newList: ArrayList<modelCabang>) {
        android.util.Log.d("AdapterDebug", "=== updateData called ===")
        android.util.Log.d("AdapterDebug", "Previous size: ${listCabang.size}")
        android.util.Log.d("AdapterDebug", "New size: ${newList.size}")

        // Clear existing data
        listCabang.clear()
        android.util.Log.d("AdapterDebug", "Existing data cleared")

        // Add new data
        listCabang.addAll(newList)
        android.util.Log.d("AdapterDebug", "New data added, current size: ${listCabang.size}")

        // Refresh language and notify adapter
        refreshLanguageFromPreferences()
        notifyDataSetChanged()
        android.util.Log.d("AdapterDebug", "notifyDataSetChanged() called")
        android.util.Log.d("AdapterDebug", "=== updateData finished ===")
    }

    // Method untuk menghitung statistik berdasarkan status real-time
    fun getStats(): Pair<Int, Int> {
        val total = listCabang.size
        var buka = 0

        try {
            buka = listCabang.count {
                val status = it.getRealTimeStatus()
                status == "BUKA" || status == "OPEN"
            }
        } catch (e: Exception) {
            android.util.Log.e("AdapterDebug", "Error calculating stats: ${e.message}")
        }

        android.util.Log.d("AdapterDebug", "Stats - Total: $total, Buka: $buka")
        return Pair(total, buka)
    }

    // Method untuk refresh status real-time
    fun refreshRealTimeStatus() {
        android.util.Log.d("AdapterDebug", "Refreshing real-time status for ${listCabang.size} items")
        refreshLanguageFromPreferences()
        notifyDataSetChanged()
    }

    // Method untuk mendapatkan data cabang berdasarkan posisi
    fun getCabangAt(position: Int): modelCabang? {
        return if (position >= 0 && position < listCabang.size) {
            listCabang[position]
        } else {
            android.util.Log.w("AdapterDebug", "Invalid position: $position, size: ${listCabang.size}")
            null
        }
    }

    // Method untuk mendapatkan semua data
    fun getAllCabang(): ArrayList<modelCabang> {
        return ArrayList(listCabang)
    }

    // Method untuk menghapus item berdasarkan posisi
    fun removeAt(position: Int) {
        if (position >= 0 && position < listCabang.size) {
            listCabang.removeAt(position)
            notifyItemRemoved(position)
            android.util.Log.d("AdapterDebug", "Item removed at position $position")
        } else {
            android.util.Log.w("AdapterDebug", "Cannot remove, invalid position: $position")
        }
    }

    // Method untuk menambah item
    fun addItem(cabang: modelCabang) {
        listCabang.add(cabang)
        notifyItemInserted(listCabang.size - 1)
        android.util.Log.d("AdapterDebug", "Item added: ${cabang.namaCabang}")
    }

    // Method untuk update item berdasarkan posisi
    fun updateItem(position: Int, cabang: modelCabang) {
        if (position >= 0 && position < listCabang.size) {
            listCabang[position] = cabang
            notifyItemChanged(position)
            android.util.Log.d("AdapterDebug", "Item updated at position $position: ${cabang.namaCabang}")
        } else {
            android.util.Log.w("AdapterDebug", "Cannot update, invalid position: $position")
        }
    }

    // Method untuk clear semua data
    fun clearAll() {
        val size = listCabang.size
        listCabang.clear()
        notifyItemRangeRemoved(0, size)
        android.util.Log.d("AdapterDebug", "All items cleared, previous size: $size")
    }
}