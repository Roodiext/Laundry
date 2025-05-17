package com.rudi.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelLayanan

class AdapterLayananTransaksi(
    private val listLayanan: MutableList<modelLayanan>,
    private val onItemClick: (modelLayanan) -> Unit
) : RecyclerView.Adapter<AdapterLayananTransaksi.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_layanan_tambahan_transaksi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLayanan[position]

        // Set nomor urut dengan format [n]
        holder.tvNomor.text = "[${position + 1}]"

        holder.tvNamaLayanan.text = item.namaLayanan ?: "Tidak Ada Nama"
        holder.tvHargaLayanan.text = "Harga: ${item.hargaLayanan ?: "Rp0"}"

        // Set listener untuk tombol hapus
        holder.btnHapus.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = listLayanan.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomor: TextView = itemView.findViewById(R.id.tvNomor)
        val tvNamaLayanan: TextView = itemView.findViewById(R.id.tvNamaLayanan)
        val tvHargaLayanan: TextView = itemView.findViewById(R.id.tvHargaLayanan)
        val btnHapus: ImageView = itemView.findViewById(R.id.btnHapus)
        val cvLayananTambahan: CardView = itemView.findViewById(R.id.cvLayananTambahan)
    }

    fun updateList(newList: List<modelLayanan>) {
        listLayanan.clear()
        listLayanan.addAll(newList)
        notifyDataSetChanged()
    }
}