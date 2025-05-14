package com.rudi.laundry.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelLayanan
import com.rudi.laundry.transaksi.transaksiActivity

class AdapterPilihLayanan(
    private val context: Context,
    private val listLayanan: ArrayList<modelLayanan>
) : RecyclerView.Adapter<AdapterPilihLayanan.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvNamaLayanan: TextView = itemView.findViewById(R.id.tvNamaPelanggan)  // Sesuai XML
        val tvHargaLayanan: TextView = itemView.findViewById(R.id.tvAlamatPelanggan)  // Sesuai XML
        val tvcabang: TextView = itemView.findViewById(R.id.tvNoHPPelanggan)  // Sesuai XML
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_layanan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pilih_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listLayanan.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nomor = position + 1
        val item = listLayanan[position]

        holder.tvID.text = "[$nomor]"
        holder.tvNama.text = item.namaLayanan
        holder.tvAlamat.text = "Deskripsi : ${item.deskripsi}"
        holder.tvNoHP.text = "Harga : Rp ${item.harga}"

        holder.cvCARD.setOnClickListener {
            val intent = Intent(appContext, TransaksiActivity::class.java)
            intent.putExtra("idLayanan", item.idLayanan)
            intent.putExtra("namaLayanan", item.namaLayanan)
            intent.putExtra("harga", item.harga)
            (context as Activity).setResult(Activity.RESULT_OK, intent)
            (appContext as Activity).finish()
        }
    }
}
