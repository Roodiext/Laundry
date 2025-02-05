package com.rudi.laundry.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.database.core.view.View
import androidx.core.view.WindowInsetsCompat
import com.rudi.laundry.R
import androidx.core.view.ViewCompat
import com.rudi.laundry.modeldata.modelPelanggan

class AdapterDataPelanggan (
    private val listPelanggan: ArrayList<modelPelanggan>)
    : RecyclerView.Adapter<AdapterDataPelanggan.ViewHolder>()  {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val  view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pelanggan,parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdapterDataPelanggan.ViewHolder, position: Int) {
        val item = listPelanggan[position]
        holder.tvID.text = item.idPelanggan
        holder.tvnama.text = item.namaPelanggan
        holder.tvAlamat.text = item.alamatPelanggan
        holder.tvBoHP.text = item.noHPPelanggan
        holder.tvTerdaftar.text = item.terdaftar
        holder.cvCARD.setOnClick
    }

    override fun getItemCount(): Int {
        return listPelanggan.size
    }

    class  ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val  cvCARD= itemView.findviewByid<View>(R.id.cvCard_PELANGGAN)
    }
    }


