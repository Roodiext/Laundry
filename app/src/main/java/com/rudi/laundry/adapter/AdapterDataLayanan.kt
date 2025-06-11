package com.rudi.laundry.adapter

import android.app.Dialog
import android.content.Context
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
import com.rudi.laundry.Layanan.EditLayananActivity
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelLayanan
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class AdapterDataLayanan(
    private val listLayanan: ArrayList<modelLayanan>,
    private val onDeleteSuccess: (() -> Unit)? = null // Callback untuk refresh data
) : RecyclerView.Adapter<AdapterDataLayanan.ViewHolder>() {

    private val database = FirebaseDatabase.getInstance()
    private val layananRef = database.getReference("layanan")

    // Language texts mapping
    private val languageTexts = mapOf(
        "id" to mapOf(
            "service_name" to "Nama Layanan",
            "price" to "Harga",
            "branch_name" to "Nama Cabang",
            "view_detail" to "Lihat Detail",
            "contact" to "Hubungi",
            "no_name" to "Tidak Ada Nama",
            "no_price" to "Tidak Ada Harga",
            "invalid_price" to "Harga Tidak Valid",
            "no_branch" to "Tidak Ada Cabang",
            "no_id" to "Tidak Ada ID",
            "no_type" to "Tidak Ada Jenis",
            "service_id" to "ID Layanan",
            "service_type" to "Jenis Layanan",
            "edit" to "Edit",
            "delete" to "Hapus",
            "delete_confirmation" to "Konfirmasi Hapus",
            "delete_message" to "Apakah Anda yakin ingin menghapus layanan",
            "delete_warning" to "Tindakan ini tidak dapat dibatalkan.",
            "cancel" to "Batal",
            "deleting" to "Menghapus layanan...",
            "delete_success" to "berhasil dihapus",
            "delete_failed" to "Gagal menghapus layanan",
            "invalid_id" to "ID layanan tidak valid",
            // Dialog texts
            "dialog_title" to "Detail Layanan",
            "dialog_subtitle" to "Informasi lengkap layanan laundry",
            "service_price" to "Harga Layanan",
            "branch" to "Cabang"
        ),
        "en" to mapOf(
            "service_name" to "Service Name",
            "price" to "Price",
            "branch_name" to "Branch Name",
            "view_detail" to "View Detail",
            "contact" to "Contact",
            "no_name" to "No Name",
            "no_price" to "No Price",
            "invalid_price" to "Invalid Price",
            "no_branch" to "No Branch",
            "no_id" to "No ID",
            "no_type" to "No Type",
            "service_id" to "Service ID",
            "service_type" to "Service Type",
            "edit" to "Edit",
            "delete" to "Delete",
            "delete_confirmation" to "Delete Confirmation",
            "delete_message" to "Are you sure you want to delete service",
            "delete_warning" to "This action cannot be undone.",
            "cancel" to "Cancel",
            "deleting" to "Deleting service...",
            "delete_success" to "successfully deleted",
            "delete_failed" to "Failed to delete service",
            "invalid_id" to "Invalid service ID",
            // Dialog texts
            "dialog_title" to "Service Details",
            "dialog_subtitle" to "Complete laundry service information",
            "service_price" to "Service Price",
            "branch" to "Branch"
        )
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLayanan[position]
        val context = holder.itemView.context
        val currentLanguage = getCurrentLanguage(context)
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Set labels based on language
        holder.tvServiceNameLabel.text = texts["service_name"]
        holder.tvPriceLabel.text = texts["price"]
        holder.tvBranchLabel.text = texts["branch_name"]

        // Set data
        holder.tvNamaLayanan.text = item.namaLayanan ?: texts["no_name"]

        // Format harga dengan format rupiah yang konsisten
        val harga = item.hargaLayanan
        val formattedHarga = if (!harga.isNullOrEmpty()) {
            val cleanHarga = harga.replace("[^\\d]".toRegex(), "")
            if (cleanHarga.isNotEmpty()) {
                val amount = cleanHarga.toLongOrNull()
                if (amount != null) {
                    formatRupiah(amount)
                } else {
                    texts["invalid_price"]!!
                }
            } else {
                texts["no_price"]!!
            }
        } else {
            texts["no_price"]!!
        }
        holder.tvHargaLayanan.text = formattedHarga

        holder.tvcabang.text = item.cabang.ifEmpty { texts["no_branch"]!! }

        // Update button text based on language
        holder.btLihatDetail.text = texts["view_detail"]
        holder.btHubungi.text = texts["contact"]

        holder.cvCARD.setOnClickListener {
            // Optional: Tambahkan aksi jika perlu
        }

        // Set OnClickListener untuk button "Lihat Detail"
        holder.btLihatDetail.setOnClickListener {
            showDialogModLayanan(holder.itemView, item, position)
        }

        // Set OnClickListener untuk button "Hubungi"
        holder.btHubungi.setOnClickListener {
            // Implementasi aksi hubungi
            // Misalnya: membuka WhatsApp, email, atau telepon
            // Untuk saat ini hanya menampilkan toast
            Toast.makeText(context, "${texts["contact"]}: ${item.namaLayanan}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentLanguage(context: Context): String {
        val sharedPref = context.getSharedPreferences("language_pref", Context.MODE_PRIVATE)
        return sharedPref.getString("selected_language", "id") ?: "id"
    }

    private fun formatRupiah(amount: Long): String {
        val symbols = DecimalFormatSymbols(Locale("id", "ID"))
        symbols.groupingSeparator = '.'
        val decimalFormat = DecimalFormat("#,###", symbols)
        return "Rp ${decimalFormat.format(amount)}"
    }

    private fun showDialogModLayanan(view: View, layanan: modelLayanan, position: Int) {
        val context = view.context
        val currentLanguage = getCurrentLanguage(context)
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_mod_layanan)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Inisialisasi komponen dialog
        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_judul)
        val tvDialogSubtitle = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_subtitle)

        // Labels
        val tvIdLayananLabel = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_ID_label)
        val tvNamaLayananLabel = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_Nama_label)
        val tvHargaLayananLabel = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_Harga_label)
        val tvCabangLabel = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_Cabang_label)
        val tvJenisLayananLabel = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_Jenis_label)

        // Values
        val tvIdLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_ID_value)
        val tvNamaLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_Nama_value)
        val tvHargaLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_Harga_value)
        val tvCabang = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_Cabang_value)
        val tvJenisLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_Jenis_value)

        // Buttons
        val btEdit = dialog.findViewById<MaterialButton>(R.id.btDIALOG_MOD_LAYANAN_Edit)
        val btHapus = dialog.findViewById<MaterialButton>(R.id.btDIALOG_MOD_LAYANAN_Hapus)

        // Update dialog texts based on language
        tvDialogTitle.text = texts["dialog_title"]
        tvDialogSubtitle.text = texts["dialog_subtitle"]

        // Update labels based on language
        tvIdLayananLabel.text = texts["service_id"]
        tvNamaLayananLabel.text = texts["service_name"]
        tvHargaLayananLabel.text = texts["service_price"]
        tvCabangLabel.text = texts["branch"]
        tvJenisLayananLabel.text = texts["service_type"]

        // Update button text based on language
        btEdit.text = texts["edit"]
        btHapus.text = texts["delete"]

        // Set data ke dialog
        tvIdLayanan.text = layanan.idLayanan.ifEmpty { texts["no_id"]!! }
        tvNamaLayanan.text = layanan.namaLayanan ?: texts["no_name"]!!

        // Format harga untuk dialog
        val harga = layanan.hargaLayanan
        val formattedHarga = if (!harga.isNullOrEmpty()) {
            val cleanHarga = harga.replace("[^\\d]".toRegex(), "")
            if (cleanHarga.isNotEmpty()) {
                val amount = cleanHarga.toLongOrNull()
                if (amount != null) {
                    formatRupiah(amount)
                } else {
                    texts["invalid_price"]!!
                }
            } else {
                texts["no_price"]!!
            }
        } else {
            texts["no_price"]!!
        }
        tvHargaLayanan.text = formattedHarga

        tvCabang.text = layanan.cabang.ifEmpty { texts["no_branch"]!! }
        tvJenisLayanan.text = layanan.jenisLayanan.ifEmpty { texts["no_type"]!! }

        // Set OnClickListener untuk button Edit
        btEdit.setOnClickListener {
            dialog.dismiss()

            // Buat intent untuk membuka EditLayananActivity
            val intent = Intent(context, EditLayananActivity::class.java)

            // Kirim data layanan sebagai extra
            intent.putExtra("layanan_data", layanan)

            // Start activity dengan request code untuk menangani result
            if (context is androidx.fragment.app.FragmentActivity) {
                (context as androidx.fragment.app.FragmentActivity).startActivityForResult(intent, EDIT_REQUEST_CODE)
            } else if (context is androidx.appcompat.app.AppCompatActivity) {
                (context as androidx.appcompat.app.AppCompatActivity).startActivityForResult(intent, EDIT_REQUEST_CODE)
            } else {
                // Fallback jika context bukan activity
                context.startActivity(intent)
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
        val context = view.context
        val currentLanguage = getCurrentLanguage(context)
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        AlertDialog.Builder(context)
            .setTitle(texts["delete_confirmation"])
            .setMessage("${texts["delete_message"]} '${layanan.namaLayanan}'?\n\n${texts["delete_warning"]}")
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton(texts["delete"]) { _, _ ->
                deleteLayanan(view, layanan, position)
            }
            .setNegativeButton(texts["cancel"], null)
            .show()
    }

    private fun deleteLayanan(view: View, layanan: modelLayanan, position: Int) {
        val context = view.context
        val currentLanguage = getCurrentLanguage(context)
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!

        // Validasi ID layanan
        if (layanan.idLayanan.isEmpty()) {
            Toast.makeText(context, texts["invalid_id"], Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan loading toast
        Toast.makeText(context, texts["deleting"], Toast.LENGTH_SHORT).show()

        // Hapus dari Firebase
        layananRef.child(layanan.idLayanan).removeValue()
            .addOnSuccessListener {
                // Hapus dari list dan update adapter
                listLayanan.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, listLayanan.size)

                Toast.makeText(context,
                    "${texts["delete_message"]} '${layanan.namaLayanan}' ${texts["delete_success"]}",
                    Toast.LENGTH_SHORT).show()

                // Panggil callback jika ada
                onDeleteSuccess?.invoke()
            }
            .addOnFailureListener { error ->
                Toast.makeText(context,
                    "${texts["delete_failed"]}: ${error.message}",
                    Toast.LENGTH_LONG).show()

                android.util.Log.e("AdapterDataLayanan", "Delete error: ${error.message}")
            }
    }

    // Method untuk update data dari luar (jika diperlukan)
    fun updateData(newList: ArrayList<modelLayanan>) {
        listLayanan.clear()
        listLayanan.addAll(newList)
        notifyDataSetChanged()
    }

    // Method untuk update item tertentu setelah edit
    fun updateItem(position: Int, updatedLayanan: modelLayanan) {
        if (position >= 0 && position < listLayanan.size) {
            listLayanan[position] = updatedLayanan
            notifyItemChanged(position)
        }
    }

    // Method untuk refresh tampilan ketika bahasa berubah
    fun refreshLanguage() {
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listLayanan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaLayanan: TextView = itemView.findViewById(R.id.tvNamaPelanggan)
        val tvHargaLayanan: TextView = itemView.findViewById(R.id.tvAlamatPelanggan)
        val tvcabang: TextView = itemView.findViewById(R.id.tvNoHPPelanggan)
        val tvServiceNameLabel: TextView = itemView.findViewById(R.id.tvServiceNameLabel)
        val tvPriceLabel: TextView = itemView.findViewById(R.id.tvPriceLabel)
        val tvBranchLabel: TextView = itemView.findViewById(R.id.tvBranchLabel)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_layanan)
        val btLihatDetail: MaterialButton = itemView.findViewById(R.id.btSimpan)
        val btHubungi: MaterialButton = itemView.findViewById(R.id.btLihat)
    }

    companion object {
        const val EDIT_REQUEST_CODE = 100
    }
}