package com.rudi.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelLayanan
import java.text.NumberFormat
import java.util.*

class AdapterPilihLayanan(
    private val listLayanan: MutableList<modelLayanan>,
    private var currentLanguage: String = "id",
    private var languageTexts: Map<String, String> = mapOf(),
    private val onItemClick: (modelLayanan) -> Unit
) : RecyclerView.Adapter<AdapterPilihLayanan.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_pilih_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLayanan[position]

        // Set nama layanan dengan fallback text
        holder.tvNamaLayanan.text = item.namaLayanan ?: (languageTexts["no_service_name"] ?: "Tidak Ada Nama")

        // Format harga dengan currency sesuai bahasa
        val harga = item.hargaLayanan?.toDoubleOrNull()
        val formattedHarga = if (harga != null) {
            formatCurrency(harga, currentLanguage)
        } else {
            languageTexts["no_price"] ?: "Tidak Ada Harga"
        }
        holder.tvHargaLayanan.text = formattedHarga

        // Set click listener
        holder.cvCARD.setOnClickListener {
            onItemClick(item)
        }

        // Update static texts in card jika ada akses ke view lainnya
        updateCardTexts(holder)
    }

    override fun getItemCount(): Int = listLayanan.size

    private fun formatCurrency(amount: Double, language: String): String {
        return when (language) {
            "en" -> {
                // Format dalam IDR untuk English
                val formatter = NumberFormat.getInstance(Locale.US)
                "IDR ${formatter.format(amount)}"
            }
            else -> {
                // Format dalam Rupiah untuk Indonesia
                val localeID = Locale("in", "ID")
                val formatter = NumberFormat.getCurrencyInstance(localeID)
                formatter.format(amount)
            }
        }
    }

    private fun updateCardTexts(holder: ViewHolder) {
        // Update text-text statis di card berdasarkan bahasa
        holder.tvPremiumService?.text = languageTexts["premium_service"] ?: "Premium Service"
        holder.tvLabelNamaLayanan?.text = languageTexts["service_name"] ?: "Nama Layanan"
        holder.tvLabelHargaLayanan?.text = languageTexts["service_price"] ?: "Harga Layanan"
        holder.tvAffordable?.text = languageTexts["affordable"] ?: "Terjangkau"
        holder.tvPilihLayanan?.text = languageTexts["select_service"] ?: "Pilih Layanan Ini"
    }

    fun updateList(newList: List<modelLayanan>) {
        listLayanan.clear()
        listLayanan.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateLanguage(language: String, texts: Map<String, String>) {
        currentLanguage = language
        languageTexts = texts
        notifyDataSetChanged() // Refresh semua item dengan bahasa baru
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaLayanan: TextView = itemView.findViewById(R.id.tvNamaLayanan)
        val tvHargaLayanan: TextView = itemView.findViewById(R.id.tvharga_layanan)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_layanan)

        // Reference ke TextView yang bisa diubah bahasanya
        val tvPremiumService: TextView? = itemView.findViewById(R.id.tvPremiumService)
        val tvLabelNamaLayanan: TextView? = itemView.findViewById(R.id.tvLabelNamaLayanan)
        val tvLabelHargaLayanan: TextView? = itemView.findViewById(R.id.tvLabelHargaLayanan)
        val tvAffordable: TextView? = itemView.findViewById(R.id.tvAffordable)
        val tvPilihLayanan: TextView? = itemView.findViewById(R.id.tvPilihLayanan)
    }
}