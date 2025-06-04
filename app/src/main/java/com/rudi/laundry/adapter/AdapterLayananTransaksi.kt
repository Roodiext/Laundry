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
import java.text.NumberFormat
import java.util.*

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

        // Set nomor urut (tanpa kurung siku karena layout sudah circular)
        holder.tvNomor.text = (position + 1).toString()

        // Set nama layanan
        holder.tvNamaLayanan.text = item.namaLayanan ?: "Tidak Ada Nama"

        // Format harga dengan NumberFormat Indonesia
        val harga = item.hargaLayanan?.toDoubleOrNull()
        val formattedHarga = if (harga != null) {
            NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(harga)
        } else {
            "Rp 0"
        }
        holder.tvHargaLayanan.text = formattedHarga

        // Set click listener untuk tombol hapus
        holder.btnHapus.setOnClickListener {
            onItemClick(item)
        }

        // Optional: Set click listener untuk seluruh card
        holder.cvLayananTambahan.setOnClickListener {
            // Bisa digunakan untuk edit atau detail
        }
    }

    override fun getItemCount(): Int = listLayanan.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomor: TextView = itemView.findViewById(R.id.tvNomor)
        val tvNamaLayanan: TextView = itemView.findViewById(R.id.tvNamaLayanan)
        val tvHargaLayanan: TextView = itemView.findViewById(R.id.tvHargaLayanan)
        // PERBAIKAN: btnHapus adalah ImageView, bukan CardView
        val btnHapus: ImageView = itemView.findViewById(R.id.btnHapus)
        val cvLayananTambahan: CardView = itemView.findViewById(R.id.cvLayananTambahan)
    }

    fun updateList(newList: List<modelLayanan>) {
        listLayanan.clear()
        listLayanan.addAll(newList)
        notifyDataSetChanged()
    }

    // Helper function untuk menghapus item berdasarkan position
    fun removeItem(position: Int) {
        if (position >= 0 && position < listLayanan.size) {
            listLayanan.removeAt(position)
            notifyItemRemoved(position)
            // Update nomor urut untuk item setelahnya
            notifyItemRangeChanged(position, listLayanan.size)
        }
    }

    // Helper function untuk menambah item
    fun addItem(layanan: modelLayanan) {
        listLayanan.add(layanan)
        notifyItemInserted(listLayanan.size - 1)
    }
}