package com.rudi.laundry.adapter

import android.app.Dialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import com.rudi.laundry.LayananTambahan.EditLayananTambahanActivity
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelLayanan
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class AdapterDataLayananTambahan(
    private val listLayananTambahan: ArrayList<modelLayanan>,
    private val onDeleteSuccess: (() -> Unit)? = null // Callback untuk refresh data
) : RecyclerView.Adapter<AdapterDataLayananTambahan.ViewHolder>() {

    private val database = FirebaseDatabase.getInstance()
    private val layananTambahanRef = database.getReference("layanan_tambahan")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_layanan_tambahan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLayananTambahan[position]

        holder.tvNamaLayanan.text = item.namaLayanan ?: "Tidak Ada Nama"

        // Format harga dengan format rupiah yang konsisten
        val harga = item.hargaLayanan
        val formattedHarga = if (!harga.isNullOrEmpty()) {
            val cleanHarga = harga.replace("[^\\d]".toRegex(), "")
            if (cleanHarga.isNotEmpty()) {
                val amount = cleanHarga.toLongOrNull()
                if (amount != null) {
                    formatRupiah(amount)
                } else {
                    "Harga Tidak Valid"
                }
            } else {
                "Tidak Ada Harga"
            }
        } else {
            "Tidak Ada Harga"
        }
        holder.tvHargaLayanan.text = formattedHarga

        holder.tvCabang.text = item.cabang.ifEmpty { "Tidak Ada Cabang" }

        holder.cvCard.setOnClickListener {
            // Optional: Tambahkan aksi jika perlu
        }

        // Set OnClickListener untuk button "Lihat Detail"
        holder.btLihatDetail.setOnClickListener {
            showDialogModLayananTambahan(holder.itemView, item, position)
        }

        // Set OnClickListener untuk button "Hubungi"
        holder.btHubungi.setOnClickListener {
            // Implementasi hubungi jika diperlukan
            Toast.makeText(holder.itemView.context,
                "Fitur hubungi untuk ${item.namaLayanan}",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatRupiah(amount: Long): String {
        val symbols = DecimalFormatSymbols(Locale("id", "ID"))
        symbols.groupingSeparator = '.'
        val decimalFormat = DecimalFormat("#,###", symbols)
        return "Rp ${decimalFormat.format(amount)}"
    }

    private fun showDialogModLayananTambahan(view: View, layanan: modelLayanan, position: Int) {
        val dialog = Dialog(view.context)
        dialog.setContentView(R.layout.dialog_mod_layanan_tambahan)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Inisialisasi komponen dialog
        val tvIdLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_ID_value)
        val tvNamaLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_Nama_value)
        val tvHargaLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_Harga_value)
        val tvCabang = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_Cabang_value)
        val tvJenisLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_Jenis_value)
        val btEdit = dialog.findViewById<MaterialButton>(R.id.btDIALOG_MOD_LAYANAN_TAMBAHAN_Edit)
        val btHapus = dialog.findViewById<MaterialButton>(R.id.btDIALOG_MOD_LAYANAN_TAMBAHAN_Hapus)

        // Set data ke dialog
        tvIdLayanan.text = layanan.idLayanan.ifEmpty { "Tidak Ada ID" }
        tvNamaLayanan.text = layanan.namaLayanan ?: "Tidak Ada Nama"

        // Format harga untuk dialog
        val harga = layanan.hargaLayanan
        val formattedHarga = if (!harga.isNullOrEmpty()) {
            val cleanHarga = harga.replace("[^\\d]".toRegex(), "")
            if (cleanHarga.isNotEmpty()) {
                val amount = cleanHarga.toLongOrNull()
                if (amount != null) {
                    formatRupiah(amount)
                } else {
                    "Harga Tidak Valid"
                }
            } else {
                "Tidak Ada Harga"
            }
        } else {
            "Tidak Ada Harga"
        }
        tvHargaLayanan.text = formattedHarga

        tvCabang.text = layanan.cabang.ifEmpty { "Tidak Ada Cabang" }
        tvJenisLayanan.text = layanan.jenisLayanan.ifEmpty { "Tambahan" }

        // Set OnClickListener untuk button Edit
        btEdit.setOnClickListener {
            dialog.dismiss()

            // Buat intent untuk membuka EditLayananTambahanActivity
            val intent = Intent(view.context, EditLayananTambahanActivity::class.java)

            // Kirim data layanan sebagai extra
            intent.putExtra("layanan_tambahan_data", layanan)

            // Start activity dengan request code untuk menangani result
            if (view.context is androidx.fragment.app.FragmentActivity) {
                (view.context as androidx.fragment.app.FragmentActivity).startActivityForResult(intent, EDIT_REQUEST_CODE)
            } else if (view.context is androidx.appcompat.app.AppCompatActivity) {
                (view.context as androidx.appcompat.app.AppCompatActivity).startActivityForResult(intent, EDIT_REQUEST_CODE)
            } else {
                // Fallback jika context bukan activity
                view.context.startActivity(intent)
            }
        }

        // Set OnClickListener untuk button Hapus
        btHapus.setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmationDialog(view, layanan, position)
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(view: View, layanan: modelLayanan, position: Int) {
        AlertDialog.Builder(view.context)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus layanan tambahan '${layanan.namaLayanan}'?\n\nTindakan ini tidak dapat dibatalkan.")
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Hapus") { _, _ ->
                deleteLayananTambahan(view, layanan, position)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteLayananTambahan(view: View, layanan: modelLayanan, position: Int) {
        // Validasi ID layanan
        if (layanan.idLayanan.isEmpty()) {
            Toast.makeText(view.context, "ID layanan tambahan tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan loading toast
        Toast.makeText(view.context, "Menghapus layanan tambahan...", Toast.LENGTH_SHORT).show()

        // Hapus dari Firebase
        layananTambahanRef.child(layanan.idLayanan).removeValue()
            .addOnSuccessListener {
                // Hapus dari list dan update adapter
                listLayananTambahan.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, listLayananTambahan.size)

                Toast.makeText(view.context,
                    "Layanan tambahan '${layanan.namaLayanan}' berhasil dihapus",
                    Toast.LENGTH_SHORT).show()

                // Panggil callback jika ada
                onDeleteSuccess?.invoke()
            }
            .addOnFailureListener { error ->
                Toast.makeText(view.context,
                    "Gagal menghapus layanan tambahan: ${error.message}",
                    Toast.LENGTH_LONG).show()

                android.util.Log.e("AdapterDataLayananTambahan", "Delete error: ${error.message}")
            }
    }

    // Method untuk update data dari luar (jika diperlukan)
    fun updateData(newList: ArrayList<modelLayanan>) {
        listLayananTambahan.clear()
        listLayananTambahan.addAll(newList)
        notifyDataSetChanged()
    }

    // Method untuk update item tertentu setelah edit
    fun updateItem(position: Int, updatedLayanan: modelLayanan) {
        if (position >= 0 && position < listLayananTambahan.size) {
            listLayananTambahan[position] = updatedLayanan
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return listLayananTambahan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaLayanan: TextView = itemView.findViewById(R.id.tvNamaPelanggan)
        val tvHargaLayanan: TextView = itemView.findViewById(R.id.tvAlamatPelanggan)
        val tvCabang: TextView = itemView.findViewById(R.id.tvNoHPPelanggan)
        val cvCard: CardView = itemView.findViewById(R.id.cvCARD_layanan_tambahan)
        val btLihatDetail: MaterialButton = itemView.findViewById(R.id.btSimpan)
        val btHubungi: MaterialButton = itemView.findViewById(R.id.btLihat)
    }

    companion object {
        const val EDIT_REQUEST_CODE = 101
    }
}