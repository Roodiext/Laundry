package com.rudi.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelLayanan

class AdapterLayananTambahan(
    private val listLayanan: MutableList<modelLayanan>,
    private var currentLanguage: String = "id",
    private val onItemClick: (modelLayanan) -> Unit
) : RecyclerView.Adapter<AdapterLayananTambahan.ViewHolder>() {

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "no_name" to "Tidak Ada Nama",
            "no_price" to "Tidak Ada Harga",
            "header_title" to "Layanan Tambahan",
            "service_label" to "Layanan Tambahan",
            "price_label" to "Harga Tambahan",
            "optional_badge" to "Opsional",
            "add_button" to "Tambah Layanan Ini"
        ),
        "en" to mapOf(
            "no_name" to "No Name",
            "no_price" to "No Price",
            "header_title" to "Additional Service",
            "service_label" to "Additional Service",
            "price_label" to "Additional Price",
            "optional_badge" to "Optional",
            "add_button" to "Add This Service"
        )
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_pilih_layanan_tambahan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLayanan[position]
        val texts = getCurrentTexts()

        // Set data layanan
        holder.tvNamaLayanan.text = item.namaLayanan ?: texts["no_name"]
        holder.tvHargaLayanan.text = item.hargaLayanan ?: texts["no_price"]

        // Update teks statis berdasarkan bahasa
        holder.tvHeaderTitle.text = texts["header_title"]
        holder.tvServiceLabel.text = texts["service_label"]
        holder.tvPriceLabel.text = texts["price_label"]
        holder.tvOptionalBadge.text = texts["optional_badge"]
        holder.tvAddButton.text = texts["add_button"]

        holder.cvCARD.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = listLayanan.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaLayanan: TextView = itemView.findViewById(R.id.tvNamaLayanan)
        val tvHargaLayanan: TextView = itemView.findViewById(R.id.tvharga_layanan)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_layanan_tambahan)

        // TextView untuk teks statis yang perlu diubah bahasanya
        val tvHeaderTitle: TextView = itemView.findViewById(R.id.tvHeaderTitle)
        val tvServiceLabel: TextView = itemView.findViewById(R.id.tvServiceLabel)
        val tvPriceLabel: TextView = itemView.findViewById(R.id.tvPriceLabel)
        val tvOptionalBadge: TextView = itemView.findViewById(R.id.tvOptionalBadge)
        val tvAddButton: TextView = itemView.findViewById(R.id.tvAddButton)
    }

    fun updateList(newList: List<modelLayanan>) {
        listLayanan.clear()
        listLayanan.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateLanguage(newLanguage: String) {
        if (currentLanguage != newLanguage) {
            currentLanguage = newLanguage
            notifyDataSetChanged() // Refresh all items to apply new language
        }
    }

    private fun getCurrentTexts(): Map<String, String> {
        return languageTexts[currentLanguage] ?: languageTexts["id"]!!
    }
}