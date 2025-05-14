package com.rudi.laundry.adapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.modeldata.modelPelanggan
import com.rudi.laundry.transaksi.transaksi
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.rudi.laundry.R

val tvID: TextView = view.findViewById(R.id.tvID)
val tvNama: TextView = view.findViewById(R.id.tvNama)
val tvAlamat: TextView = view.findViewById(R.id.tvAlamat)
val tvNoHP: TextView = view.findViewById(R.id.tvNoHP)
val cvCARD: CardView = view.findViewById(R.id.cvCARD)
class AdapterPilihPelanggan {
}