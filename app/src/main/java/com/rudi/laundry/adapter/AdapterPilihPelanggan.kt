package com.rudi.laundry.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelPelanggan

class AdapterPilihPelanggan(
    private val context: Context,
    private val listPelanggan: MutableList<modelPelanggan>,
    private val onItemClick: (modelPelanggan) -> Unit
) : RecyclerView.Adapter<AdapterPilihPelanggan.ViewHolder>() {

    private var currentLanguage = "id" // Default bahasa Indonesia

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "no_name" to "Tidak Ada Nama",
            "no_address" to "Tidak Ada Alamat",
            "no_phone" to "Tidak Ada No HP",
            "address_label" to "Alamat: ",
            "phone_label" to "No HP: "
        ),
        "en" to mapOf(
            "no_name" to "No Name",
            "no_address" to "No Address",
            "no_phone" to "No Phone Number",
            "address_label" to "Address: ",
            "phone_label" to "Phone: "
        )
    )

    init {
        // Load saved language preference
        val sharedPref = context.getSharedPreferences("language_pref", Context.MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_pilih_pelanggan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPelanggan[position]
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Update language untuk holder ini
        holder.updateLanguage(currentLanguage)

        // Set data dengan fallback text yang sudah ditranslate
        holder.tvNama.text = item.namaPelanggan ?: texts["no_name"]
        holder.tvAlamat.text = item.alamatPelanggan ?: texts["no_address"]
        holder.tvNoHP.text = item.noHPPelanggan ?: texts["no_phone"]

        // Update label text
        holder.tvAlamatLabel.text = texts["address_label"]
        holder.tvNoHPLabel.text = texts["phone_label"]

        holder.cvCARD.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = listPelanggan.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaPelanggan)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvAlamatPelanggan)
        val tvNoHP: TextView = itemView.findViewById(R.id.tvNoHPPelanggan)
        val tvAlamatLabel: TextView = itemView.findViewById(R.id.tvAlamatLabel)
        val tvNoHPLabel: TextView = itemView.findViewById(R.id.tvNoHPLabel)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_pelanggan)

        private var currentLanguage = "id"

        fun updateLanguage(language: String) {
            this.currentLanguage = language
        }
    }

    fun updateList(newList: List<modelPelanggan>) {
        listPelanggan.clear()
        listPelanggan.addAll(newList)
        notifyDataSetChanged()
    }

    // Fungsi untuk update bahasa dari luar
    fun updateLanguage() {
        val sharedPref = context.getSharedPreferences("language_pref", Context.MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"
        notifyDataSetChanged()
    }
}