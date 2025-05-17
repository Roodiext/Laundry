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
    private val onItemClick: (modelLayanan) -> Unit
) : RecyclerView.Adapter<AdapterPilihLayanan.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_pilih_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLayanan[position]

        holder.tvNamaLayanan.text = item.namaLayanan ?: "Tidak Ada Nama"

        val harga = item.hargaLayanan?.toDoubleOrNull()
        val formattedHarga = if (harga != null) {
            NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(harga)
        } else {
            "Tidak Ada Harga"
        }
        holder.tvHargaLayanan.text = formattedHarga

        holder.cvCARD.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = listLayanan.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaLayanan: TextView = itemView.findViewById(R.id.tvNamaLayanan)
        val tvHargaLayanan: TextView = itemView.findViewById(R.id.tvharga_layanan)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_layanan)
    }

    fun updateList(newList: List<modelLayanan>) {
        listLayanan.clear()
        listLayanan.addAll(newList)
        notifyDataSetChanged()
    }
}
