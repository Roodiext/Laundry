package com.rudi.laundry.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.rudi.laundry.Pegawai.TambahPegawaiActivity
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelPegawai

class AdapterDataPegawai(
    private val listPegawai: ArrayList<modelPegawai>,
    private val context: Context
) : RecyclerView.Adapter<AdapterDataPegawai.ViewHolder>() {

    lateinit var databaseReference: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pegawai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPegawai[position]

        holder.tvNama.text = item.namaPegawai ?: "Tidak Ada Nama"
        holder.tvAlamat.text = item.alamatPegawai ?: "Tidak Ada Alamat"
        holder.tvNoHP.text = item.noHPPegawai ?: "Tidak Ada No HP"
        holder.tvCabang.text = if (item.cabang.isNotEmpty()) item.cabang else "Belum Terdaftar"

        holder.cvCARD.setOnClickListener {
            val intent = Intent(context, TambahPegawaiActivity::class.java)
            intent.putExtra("judul", "Edit Pegawai")
            intent.putExtra("idPegawai", item.idPegawai)
            intent.putExtra("namaPegawai", item.namaPegawai)
            intent.putExtra("noHPPegawai", item.noHPPegawai)
            intent.putExtra("alamatPegawai", item.alamatPegawai)
            intent.putExtra("idCabang", item.cabang)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return listPegawai.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaPegawai)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvAlamatPegawai)
        val tvNoHP: TextView = itemView.findViewById(R.id.tvNoHPPegawai)
        val tvCabang: TextView = itemView.findViewById(R.id.tvCabangPegawai)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_pegawai)
    }
}
