package com.rudi.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelLayanan

class AdapterDataLayanan (
    private val listLayanan: ArrayList<modelLayanan>
) : RecyclerView.Adapter<AdapterDataLayanan.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLayanan[position]

        holder.tvNamaLayanan.text = item.namaLayanan ?: "Tidak Ada Nama"
        holder.tvHargaLayanan.text = item.hargaLayanan ?: "Tidak Ada Alamat"
        holder.tvcabang.text = item.cabang ?: "Tidak Ada No HP"

        holder.cvCARD.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return listLayanan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaLayanan: TextView = itemView.findViewById(R.id.tvNamaPelanggan)  // Sesuai XML
        val tvHargaLayanan: TextView = itemView.findViewById(R.id.tvAlamatPelanggan)  // Sesuai XML
        val tvcabang: TextView = itemView.findViewById(R.id.tvNoHPPelanggan)  // Sesuai XML
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_layanan)
    }

}