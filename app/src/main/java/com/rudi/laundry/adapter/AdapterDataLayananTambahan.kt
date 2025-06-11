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

    // Language texts
    private val languageTexts = mapOf(
        "id" to mapOf(
            "service_name" to "Nama Layanan",
            "price" to "Harga",
            "branch_name" to "Nama Cabang",
            "view_details" to "Lihat Detail",
            "contact" to "Hubungi",
            "no_name" to "Tidak Ada Nama",
            "no_price" to "Tidak Ada Harga",
            "invalid_price" to "Harga Tidak Valid",
            "no_branch" to "Tidak Ada Cabang",
            "no_id" to "Tidak Ada ID",
            "additional" to "Tambahan",
            "contact_feature" to "Fitur hubungi untuk",
            "confirm_delete" to "Konfirmasi Hapus",
            "delete_message" to "Apakah Anda yakin ingin menghapus layanan tambahan",
            "action_irreversible" to "Tindakan ini tidak dapat dibatalkan.",
            "delete" to "Hapus",
            "cancel" to "Batal",
            "invalid_id" to "ID layanan tambahan tidak valid",
            "deleting" to "Menghapus layanan tambahan...",
            "delete_success" to "berhasil dihapus",
            "delete_failed" to "Gagal menghapus layanan tambahan:",
            "service_id" to "ID Layanan",
            "service_type" to "Jenis Layanan",
            "edit" to "Edit",
            "additional_service" to "Layanan Tambahan",
            // Dialog specific texts
            "dialog_title" to "Detail Layanan Tambahan",
            "dialog_subtitle" to "Informasi lengkap layanan tambahan laundry",
            "service_id_label" to "ID Layanan Tambahan",
            "service_name_label" to "Nama Layanan Tambahan",
            "service_price_label" to "Harga Layanan Tambahan",
            "branch_label" to "Cabang",
            "service_type_label" to "Jenis Layanan"
        ),
        "en" to mapOf(
            "service_name" to "Service Name",
            "price" to "Price",
            "branch_name" to "Branch Name",
            "view_details" to "View Details",
            "contact" to "Contact",
            "no_name" to "No Name",
            "no_price" to "No Price",
            "invalid_price" to "Invalid Price",
            "no_branch" to "No Branch",
            "no_id" to "No ID",
            "additional" to "Additional",
            "contact_feature" to "Contact feature for",
            "confirm_delete" to "Confirm Delete",
            "delete_message" to "Are you sure you want to delete the additional service",
            "action_irreversible" to "This action cannot be undone.",
            "delete" to "Delete",
            "cancel" to "Cancel",
            "invalid_id" to "Invalid additional service ID",
            "deleting" to "Deleting additional service...",
            "delete_success" to "successfully deleted",
            "delete_failed" to "Failed to delete additional service:",
            "service_id" to "Service ID",
            "service_type" to "Service Type",
            "edit" to "Edit",
            "additional_service" to "Additional Service",
            // Dialog specific texts
            "dialog_title" to "Additional Service Details",
            "dialog_subtitle" to "Complete information about additional laundry service",
            "service_id_label" to "Additional Service ID",
            "service_name_label" to "Additional Service Name",
            "service_price_label" to "Additional Service Price",
            "branch_label" to "Branch",
            "service_type_label" to "Service Type"
        )
    )

    private fun getCurrentLanguage(context: Context): String {
        val sharedPref = context.getSharedPreferences("language_pref", Context.MODE_PRIVATE)
        return sharedPref.getString("selected_language", "id") ?: "id"
    }

    private fun getText(context: Context, key: String): String {
        val currentLanguage = getCurrentLanguage(context)
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
        return texts[key] ?: key
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_layanan_tambahan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLayananTambahan[position]
        val context = holder.itemView.context

        // Update header title
        holder.tvHeaderTitle.text = getText(context, "additional_service")

        // Update label texts berdasarkan bahasa saat ini
        holder.tvLabelNamaLayanan.text = getText(context, "service_name")
        holder.tvLabelHarga.text = getText(context, "price")
        holder.tvLabelCabang.text = getText(context, "branch_name")

        // Set data values
        holder.tvNamaLayanan.text = item.namaLayanan ?: getText(context, "no_name")

        // Format harga dengan format rupiah yang konsisten
        val harga = item.hargaLayanan
        val formattedHarga = if (!harga.isNullOrEmpty()) {
            val cleanHarga = harga.replace("[^\\d]".toRegex(), "")
            if (cleanHarga.isNotEmpty()) {
                val amount = cleanHarga.toLongOrNull()
                if (amount != null) {
                    formatRupiah(amount)
                } else {
                    getText(context, "invalid_price")
                }
            } else {
                getText(context, "no_price")
            }
        } else {
            getText(context, "no_price")
        }
        holder.tvHargaLayanan.text = formattedHarga

        holder.tvCabang.text = item.cabang.ifEmpty { getText(context, "no_branch") }

        // Update button texts
        holder.btLihatDetail.text = getText(context, "view_details")
        holder.btHubungi.text = getText(context, "contact")

        holder.cvCard.setOnClickListener {
            // Optional: Tambahkan aksi jika perlu
        }

        // Set OnClickListener untuk button "Lihat Detail"
        holder.btLihatDetail.setOnClickListener {
            showDialogModLayananTambahan(holder.itemView, item, position)
        }

        // Set OnClickListener untuk button "Hubungi"
        holder.btHubungi.setOnClickListener {
            val contactText = "${getText(context, "contact_feature")} ${item.namaLayanan}"
            Toast.makeText(context, contactText, Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatRupiah(amount: Long): String {
        val symbols = DecimalFormatSymbols(Locale("id", "ID"))
        symbols.groupingSeparator = '.'
        val decimalFormat = DecimalFormat("#,###", symbols)
        return "Rp ${decimalFormat.format(amount)}"
    }

    private fun showDialogModLayananTambahan(view: View, layanan: modelLayanan, position: Int) {
        val context = view.context
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_mod_layanan_tambahan)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Inisialisasi komponen dialog
        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_judul)
        val tvDialogSubtitle = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_subtitle)

        // Labels
        val tvLabelIdLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_ID_label)
        val tvLabelNamaLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_Nama_label)
        val tvLabelHargaLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_Harga_label)
        val tvLabelCabang = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_Cabang_label)
        val tvLabelJenisLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_Jenis_label)

        // Values
        val tvIdLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_ID_value)
        val tvNamaLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_Nama_value)
        val tvHargaLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_Harga_value)
        val tvCabang = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_Cabang_value)
        val tvJenisLayanan = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_LAYANAN_TAMBAHAN_Jenis_value)

        // Buttons
        val btEdit = dialog.findViewById<MaterialButton>(R.id.btDIALOG_MOD_LAYANAN_TAMBAHAN_Edit)
        val btHapus = dialog.findViewById<MaterialButton>(R.id.btDIALOG_MOD_LAYANAN_TAMBAHAN_Hapus)

        // Update dialog texts based on current language
        tvDialogTitle?.text = getText(context, "dialog_title")
        tvDialogSubtitle?.text = getText(context, "dialog_subtitle")

        // Update label texts
        tvLabelIdLayanan?.text = getText(context, "service_id_label")
        tvLabelNamaLayanan?.text = getText(context, "service_name_label")
        tvLabelHargaLayanan?.text = getText(context, "service_price_label")
        tvLabelCabang?.text = getText(context, "branch_label")
        tvLabelJenisLayanan?.text = getText(context, "service_type_label")

        // Update button texts in dialog
        btEdit.text = getText(context, "edit")
        btHapus.text = getText(context, "delete")

        // Set data ke dialog
        tvIdLayanan.text = layanan.idLayanan.ifEmpty { getText(context, "no_id") }
        tvNamaLayanan.text = layanan.namaLayanan ?: getText(context, "no_name")

        // Format harga untuk dialog
        val harga = layanan.hargaLayanan
        val formattedHarga = if (!harga.isNullOrEmpty()) {
            val cleanHarga = harga.replace("[^\\d]".toRegex(), "")
            if (cleanHarga.isNotEmpty()) {
                val amount = cleanHarga.toLongOrNull()
                if (amount != null) {
                    formatRupiah(amount)
                } else {
                    getText(context, "invalid_price")
                }
            } else {
                getText(context, "no_price")
            }
        } else {
            getText(context, "no_price")
        }
        tvHargaLayanan.text = formattedHarga

        tvCabang.text = layanan.cabang.ifEmpty { getText(context, "no_branch") }
        tvJenisLayanan.text = layanan.jenisLayanan.ifEmpty { getText(context, "additional") }

        // Set OnClickListener untuk button Edit
        btEdit.setOnClickListener {
            dialog.dismiss()

            // Buat intent untuk membuka EditLayananTambahanActivity
            val intent = Intent(context, EditLayananTambahanActivity::class.java)

            // Kirim data layanan sebagai extra
            intent.putExtra("layanan_tambahan_data", layanan)

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
        val deleteMessage = "${getText(context, "delete_message")} '${layanan.namaLayanan}'?\n\n${getText(context, "action_irreversible")}"

        AlertDialog.Builder(context)
            .setTitle(getText(context, "confirm_delete"))
            .setMessage(deleteMessage)
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton(getText(context, "delete")) { _, _ ->
                deleteLayananTambahan(view, layanan, position)
            }
            .setNegativeButton(getText(context, "cancel"), null)
            .show()
    }

    private fun deleteLayananTambahan(view: View, layanan: modelLayanan, position: Int) {
        val context = view.context

        // Validasi ID layanan
        if (layanan.idLayanan.isEmpty()) {
            Toast.makeText(context, getText(context, "invalid_id"), Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan loading toast
        Toast.makeText(context, getText(context, "deleting"), Toast.LENGTH_SHORT).show()

        // Hapus dari Firebase
        layananTambahanRef.child(layanan.idLayanan).removeValue()
            .addOnSuccessListener {
                // Hapus dari list dan update adapter
                listLayananTambahan.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, listLayananTambahan.size)

                val successMessage = "Layanan tambahan '${layanan.namaLayanan}' ${getText(context, "delete_success")}"
                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()

                // Panggil callback jika ada
                onDeleteSuccess?.invoke()
            }
            .addOnFailureListener { error ->
                val errorMessage = "${getText(context, "delete_failed")} ${error.message}"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

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

    // Method untuk refresh language tanpa mengubah data
    fun refreshLanguage() {
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listLayananTambahan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHeaderTitle: TextView = itemView.findViewById(R.id.tvHeaderTitle)
        val tvLabelNamaLayanan: TextView = itemView.findViewById(R.id.tvLabelNamaLayanan)
        val tvLabelHarga: TextView = itemView.findViewById(R.id.tvLabelHarga)
        val tvLabelCabang: TextView = itemView.findViewById(R.id.tvLabelCabang)
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