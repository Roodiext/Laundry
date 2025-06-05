package com.rudi.laundry.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelTransaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdapterlaporanTransaksi(
    private val listTransaksi: List<modelTransaksi>,
    private val onItemClick: (modelTransaksi) -> Unit,
    private val onButtonClick: (modelTransaksi) -> Unit
) : RecyclerView.Adapter<AdapterlaporanTransaksi.TransaksiViewHolder>() {

    inner class TransaksiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNama: TextView = itemView.findViewById(R.id.txtNama)
        val txtTanggal: TextView = itemView.findViewById(R.id.txtTanggal)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val txtLayanan: TextView = itemView.findViewById(R.id.txtLayanan)
        val txtTambahan: TextView = itemView.findViewById(R.id.txtTambahan)
        val txtTotal: TextView = itemView.findViewById(R.id.txtTotal)
        val btnAksi: Button? = itemView.findViewById(R.id.btnAksi)
        val layoutPengambilan: LinearLayout? = itemView.findViewById(R.id.layoutPengambilan)
        val txtTanggalPengambilan: TextView? = itemView.findViewById(R.id.txtTanggalPengambilan)

        fun bind(transaksi: modelTransaksi, position: Int) {
            txtNama.text = transaksi.namaPelanggan
            txtTanggal.text = transaksi.tanggalTransaksi
            txtStatus.text = transaksi.getDisplayStatus()
            txtLayanan.text = transaksi.namaLayanan

            // Set jumlah layanan tambahan
            val jumlahTambahan = transaksi.layananTambahan.size
            if (jumlahTambahan > 0) {
                txtTambahan.text = "+$jumlahTambahan Layanan Tambahan"
                txtTambahan.visibility = View.VISIBLE
            } else {
                txtTambahan.visibility = View.GONE
            }

            // Format total bayar
            txtTotal.text = formatRupiah(transaksi.totalBayar)

            // Set status background dan color
            txtStatus.setTextColor(Color.parseColor(transaksi.getStatusColor()))

            // Handle button aksi
            if (transaksi.shouldShowButton() && btnAksi != null) {
                btnAksi.visibility = View.VISIBLE
                btnAksi.text = transaksi.getButtonText()
                btnAksi.setBackgroundColor(Color.parseColor(transaksi.getButtonColor()))
                btnAksi.setOnClickListener {
                    onButtonClick(transaksi)
                }
            } else {
                btnAksi?.visibility = View.GONE
            }

            // Handle layout pengambilan untuk status selesai
            if (transaksi.status == modelTransaksi.STATUS_SELESAI && layoutPengambilan != null) {
                layoutPengambilan.visibility = View.VISIBLE
                txtTanggalPengambilan?.text = transaksi.tanggalPengambilan
            } else {
                layoutPengambilan?.visibility = View.GONE
            }

            // Set nomor urut berdasarkan posisi
            val txtNomor = itemView.findViewById<TextView>(R.id.txtNomor)
            txtNomor?.text = "[${position + 1}]"

            // Set click listener untuk item
            itemView.setOnClickListener {
                onItemClick(transaksi)
            }
        }

        private fun formatRupiah(amount: Double): String {
            val localeID = Locale("in", "ID")
            return NumberFormat.getCurrencyInstance(localeID).format(amount)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaksiViewHolder {
        // Tentukan layout berdasarkan status transaksi
        val layoutId = when (viewType) {
            0 -> R.layout.card_transaksi_belumbayar    // BELUM_BAYAR
            1 -> R.layout.card_transaksi_sudahbayar    // SUDAH_BAYAR
            2 -> R.layout.card_transaksi_selesai       // SELESAI
            else -> R.layout.card_transaksi_belumbayar
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return TransaksiViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransaksiViewHolder, position: Int) {
        holder.bind(listTransaksi[position], position)
    }

    override fun getItemCount(): Int = listTransaksi.size

    override fun getItemViewType(position: Int): Int {
        return when (listTransaksi[position].status) {
            modelTransaksi.STATUS_BELUM_BAYAR -> 0
            modelTransaksi.STATUS_SUDAH_BAYAR -> 1
            modelTransaksi.STATUS_SELESAI -> 2
            else -> 0
        }
    }
}