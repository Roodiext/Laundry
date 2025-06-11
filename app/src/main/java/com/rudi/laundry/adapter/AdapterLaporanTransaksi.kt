package com.rudi.laundry.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelTransaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdapterlaporanTransaksi(
    private val listTransaksi: List<modelTransaksi>,
    private val onItemClick: (modelTransaksi) -> Unit,
    private val onButtonClick: (modelTransaksi) -> Unit,
    private val context: Context
) : RecyclerView.Adapter<AdapterlaporanTransaksi.TransaksiViewHolder>() {

    // Variabel untuk menyimpan bahasa saat ini
    private var currentLanguage: String = "id"

    init {
        // Load bahasa dari SharedPreferences
        loadLanguagePreference()
    }

    private fun loadLanguagePreference() {
        val sharedPref = context.getSharedPreferences("language_pref", Context.MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"
    }

    // Function untuk update bahasa dari luar adapter
    fun updateLanguage(language: String) {
        currentLanguage = language
        notifyDataSetChanged()
    }

    inner class TransaksiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNama: TextView = itemView.findViewById(R.id.txtNama)
        val txtTanggal: TextView = itemView.findViewById(R.id.txtTanggal)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val txtLayanan: TextView = itemView.findViewById(R.id.txtLayanan)
        val txtTambahan: TextView = itemView.findViewById(R.id.txtTambahan)
        val txtTotal: TextView = itemView.findViewById(R.id.txtTotal)
        val txtTotalBayarLabel: TextView? = itemView.findViewById(R.id.txtTotalBayarLabel)
        val btnAksi: Button? = itemView.findViewById(R.id.btnAksi)
        val layoutPengambilan: LinearLayout? = itemView.findViewById(R.id.layoutPengambilan)
        val txtTanggalPengambilan: TextView? = itemView.findViewById(R.id.txtTanggalPengambilan)

        fun bind(transaksi: modelTransaksi, position: Int) {
            txtNama.text = transaksi.namaPelanggan
            txtTanggal.text = transaksi.tanggalTransaksi

            // Menggunakan bahasa saat ini untuk display status
            txtStatus.text = transaksi.getDisplayStatus(currentLanguage)
            txtLayanan.text = transaksi.namaLayanan

            // Set jumlah layanan tambahan dengan bahasa yang sesuai
            val jumlahTambahan = transaksi.layananTambahan.size
            if (jumlahTambahan > 0) {
                val tambahanText = transaksi.getLayananTambahanText(currentLanguage)
                txtTambahan.text = "+$jumlahTambahan $tambahanText"
                txtTambahan.visibility = View.VISIBLE
            } else {
                txtTambahan.visibility = View.GONE
            }

            // Format total bayar berdasarkan bahasa
            txtTotal.text = formatRupiah(transaksi.totalBayar, currentLanguage)

            // Set label "Total Bayar" dengan bahasa yang sesuai
            txtTotalBayarLabel?.text = transaksi.getTotalBayarText(currentLanguage)

            txtStatus.setTextColor(Color.parseColor(transaksi.getStatusColor()))

            // Set background drawable berdasarkan status
            val backgroundRes = when (transaksi.getStatusBackground()) {
                "bg_status_merah" -> R.drawable.bg_status_merah
                "bg_status_kuning" -> R.drawable.bg_status_kuning
                "bg_status_hijau" -> R.drawable.bg_status_hijau
                else -> R.drawable.bg_status_merah // default
            }
            txtStatus.background = ContextCompat.getDrawable(itemView.context, backgroundRes)

            // Handle button aksi dengan bahasa yang sesuai
            if (transaksi.shouldShowButton() && btnAksi != null) {
                btnAksi.visibility = View.VISIBLE
                btnAksi.text = transaksi.getButtonText(currentLanguage)
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

                // Format tanggal pengambilan sesuai bahasa
                val formattedDate = formatDateForDisplay(transaksi.tanggalPengambilan, currentLanguage)
                txtTanggalPengambilan?.text = formattedDate
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

        private fun formatRupiah(amount: Double, language: String): String {
            return when (language) {
                "en" -> {
                    // Format untuk bahasa Inggris dengan USD (atau bisa disesuaikan)
                    val localeUS = Locale.US
                    "IDR ${NumberFormat.getNumberInstance(localeUS).format(amount)}"
                }
                else -> {
                    // Format untuk bahasa Indonesia
                    val localeID = Locale("in", "ID")
                    NumberFormat.getCurrencyInstance(localeID).format(amount)
                }
            }
        }

        private fun formatDateForDisplay(dateString: String, language: String): String {
            if (dateString.isEmpty()) return ""

            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(dateString)

                val outputFormat = when (language) {
                    "en" -> SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.ENGLISH)
                    else -> SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))
                }

                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                dateString
            }
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
        // Reload bahasa setiap kali bind untuk memastikan konsistensi
        loadLanguagePreference()
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