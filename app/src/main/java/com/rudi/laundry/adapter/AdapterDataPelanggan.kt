package com.rudi.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelPelanggan

class AdapterDataPelanggan(
    private val listPelanggan: ArrayList<modelPelanggan>
) : RecyclerView.Adapter<AdapterDataPelanggan.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pelanggan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPelanggan[position]


        holder.tvNama.text = item.namaPelanggan ?: "Tidak Ada Nama"
        holder.tvAlamat.text = item.alamatPelanggan ?: "Tidak Ada Alamat"
        holder.tvNoHP.text = item.noHPPelanggan ?: "Tidak Ada No HP"
        holder.tvTerdaftar.text = if (item.terdaftar.isNotEmpty()) item.terdaftar else "Belum Terdaftar"

        holder.cvCARD.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return listPelanggan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaPelanggan)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvAlamatPelanggan)
        val tvNoHP: TextView = itemView.findViewById(R.id.tvNoHPPelanggan)
        val tvTerdaftar: TextView = itemView.findViewById(R.id.tvTerdaftarPelanggan)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_pelanggan)
    }

    fun updateList(newList: List<modelPelanggan>) {
        listPelanggan.clear()
        listPelanggan.addAll(newList)
        notifyDataSetChanged()
    }

}
