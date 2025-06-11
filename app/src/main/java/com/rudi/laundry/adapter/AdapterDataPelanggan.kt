package com.rudi.laundry.adapter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelPelanggan

class AdapterDataPelanggan(
    private val listPelanggan: ArrayList<modelPelanggan>,
    private val onDeleteClick: ((modelPelanggan) -> Unit)? = null,
    private val onEditClick: ((modelPelanggan) -> Unit)? = null
) : RecyclerView.Adapter<AdapterDataPelanggan.ViewHolder>() {

    // Language texts untuk adapter
    private val languageTexts = mapOf(
        "id" to mapOf(
            "no_name" to "Tidak Ada Nama",
            "no_address" to "Tidak Ada Alamat",
            "no_phone" to "Tidak Ada No HP",
            "not_registered" to "Belum Terdaftar",
            "view" to "Lihat",
            "contact" to "Hubungi",
            "customer_detail" to "Detail Pelanggan",
            "customer_info_subtitle" to "Informasi lengkap pelanggan",
            "customer_id_label" to "ID Pelanggan",
            "full_name_label" to "Nama Lengkap",
            "phone_number_label" to "Nomor Telepon",
            "branch_label" to "Cabang",
            "registration_date_label" to "Tanggal Terdaftar",
            "no_id" to "Tidak ada ID",
            "no_name_detail" to "Tidak ada nama",
            "no_phone_detail" to "Tidak ada nomor",
            "no_branch" to "Tidak ada cabang",
            "no_info" to "Tidak diketahui",
            "edit" to "Sunting",
            "delete" to "Hapus",
            "opening_edit" to "Membuka halaman edit...",
            "delete_confirm_title" to "Konfirmasi Hapus",
            "delete_confirm_message" to "Apakah Anda yakin ingin menghapus pelanggan",
            "yes" to "Ya",
            "no" to "Tidak",
            "phone_not_available" to "Nomor HP tidak tersedia",
            "invalid_phone" to "Nomor HP tidak valid",
            "opening_whatsapp" to "Membuka WhatsApp...",
            "opening_whatsapp_web" to "Membuka WhatsApp Web...",
            "opening_phone" to "Membuka aplikasi telepon...",
            "cannot_open_phone" to "Tidak dapat membuka aplikasi telepon"
        ),
        "en" to mapOf(
            "no_name" to "No Name",
            "no_address" to "No Address",
            "no_phone" to "No Phone Number",
            "not_registered" to "Not Registered",
            "view" to "View",
            "contact" to "Contact",
            "customer_detail" to "Customer Detail",
            "customer_info_subtitle" to "Complete customer information",
            "customer_id_label" to "Customer ID",
            "full_name_label" to "Full Name",
            "phone_number_label" to "Phone Number",
            "branch_label" to "Branch",
            "registration_date_label" to "Registration Date",
            "no_id" to "No ID",
            "no_name_detail" to "No name",
            "no_phone_detail" to "No phone number",
            "no_branch" to "No branch",
            "no_info" to "Unknown",
            "edit" to "Edit",
            "delete" to "Delete",
            "opening_edit" to "Opening edit page...",
            "delete_confirm_title" to "Delete Confirmation",
            "delete_confirm_message" to "Are you sure you want to delete customer",
            "yes" to "Yes",
            "no" to "No",
            "phone_not_available" to "Phone number not available",
            "invalid_phone" to "Invalid phone number",
            "opening_whatsapp" to "Opening WhatsApp...",
            "opening_whatsapp_web" to "Opening WhatsApp Web...",
            "opening_phone" to "Opening phone app...",
            "cannot_open_phone" to "Cannot open phone app"
        )
    )

    private fun getCurrentLanguage(context: Context): String {
        val sharedPref = context.getSharedPreferences("language_pref", Context.MODE_PRIVATE)
        return sharedPref.getString("selected_language", "id") ?: "id"
    }

    private fun getTranslation(context: Context, key: String): String {
        val currentLanguage = getCurrentLanguage(context)
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
        return texts[key] ?: key
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pelanggan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPelanggan[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = listPelanggan.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNama: TextView = itemView.findViewById(R.id.tvNamaPelanggan)
        private val tvAlamat: TextView = itemView.findViewById(R.id.tvAlamatPelanggan)
        private val tvNoHP: TextView = itemView.findViewById(R.id.tvNoHPPelanggan)
        private val tvTerdaftar: TextView = itemView.findViewById(R.id.tvTerdaftarPelanggan)
        private val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_pelanggan)
        private val btLihat: MaterialButton = itemView.findViewById(R.id.btLihat)
        private val btHubungi: MaterialButton = itemView.findViewById(R.id.btHubungi)

        fun bind(pelanggan: modelPelanggan) {
            val context = itemView.context

            // Set data ke views dengan terjemahan
            tvNama.text = pelanggan.namaPelanggan ?: getTranslation(context, "no_name")
            tvAlamat.text = pelanggan.alamatPelanggan ?: getTranslation(context, "no_address")
            tvNoHP.text = pelanggan.noHPPelanggan ?: getTranslation(context, "no_phone")
            tvTerdaftar.text = if (pelanggan.terdaftar.isNotEmpty()) pelanggan.terdaftar else getTranslation(context, "not_registered")

            // Set text tombol dengan terjemahan
            btLihat.text = getTranslation(context, "view")
            btHubungi.text = getTranslation(context, "contact")

            // Tombol Lihat - Menampilkan Dialog Detail
            btLihat.setOnClickListener {
                showDetailDialog(context, pelanggan)
            }

            // Tombol Hubungi - Membuka WhatsApp
            btHubungi.setOnClickListener {
                hubungiPelanggan(context, pelanggan.noHPPelanggan)
            }

            // Klik pada card juga bisa membuka dialog
            cvCARD.setOnClickListener {
                showDetailDialog(context, pelanggan)
            }
        }

        private fun showDetailDialog(context: Context, pelanggan: modelPelanggan) {
            // Buat dialog dengan style custom
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_mod_pelanggan)

            // Set dialog properties untuk menghilangkan background putih
            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setGravity(Gravity.CENTER)
                // Menambahkan sedikit transparansi pada background
                setDimAmount(0.7f)
            }

            dialog.setCancelable(true)

            // Initialize dialog views
            val tvJudul: TextView = dialog.findViewById(R.id.tvDIALOG_MOD_PELANGGAN_judul)
            val tvSubtitle: TextView = dialog.findViewById(R.id.tvDIALOG_MOD_PELANGGAN_subtitle)
            val tvIdLabel: TextView = dialog.findViewById(R.id.tvDIALOG_MOD_PELANGGAN_ID_label)
            val tvNamaLabel: TextView = dialog.findViewById(R.id.tvDIALOG_MOD_PELANGGAN_Nama_label)
            val tvNoHPLabel: TextView = dialog.findViewById(R.id.tvDIALOG_MOD_PELANGGAN_NoHP_label)
            val tvCabangLabel: TextView = dialog.findViewById(R.id.tvDIALOG_MOD_PELANGGAN_Cabang_label)
            val tvTerdaftarLabel: TextView = dialog.findViewById(R.id.tvDIALOG_MOD_PELANGGAN_Terdaftar_label)
            val btEdit: MaterialButton = dialog.findViewById(R.id.btDIALOG_MOD_PELANGGAN_Edit)
            val btHapus: MaterialButton = dialog.findViewById(R.id.btDIALOG_MOD_PELANGGAN_Hapus)

            // Set semua teks dengan terjemahan
            tvJudul.text = getTranslation(context, "customer_detail")
            tvSubtitle.text = getTranslation(context, "customer_info_subtitle")

            // Set labels dengan terjemahan
            tvIdLabel.text = getTranslation(context, "customer_id_label")
            tvNamaLabel.text = getTranslation(context, "full_name_label")
            tvNoHPLabel.text = getTranslation(context, "phone_number_label")
            tvCabangLabel.text = getTranslation(context, "branch_label")
            tvTerdaftarLabel.text = getTranslation(context, "registration_date_label")


            // Set text tombol dengan terjemahan
            btEdit.text = getTranslation(context, "edit")
            btHapus.text = getTranslation(context, "delete")

            // Set button listeners dengan animasi dan feedback yang lebih baik
            setupEditButtonAnimation(btEdit, pelanggan, context, dialog)

            btHapus.setOnClickListener {
                dialog.dismiss()
                showDeleteConfirmation(context, pelanggan)
            }

            dialog.show()
        }

        // Fungsi untuk setup animasi tombol edit dengan feedback yang lebih baik
        private fun setupEditButtonAnimation(button: MaterialButton, pelanggan: modelPelanggan, context: Context, dialog: Dialog) {
            button.setOnClickListener { view ->
                // Animasi click dengan scale effect
                view.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction {
                        view.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .withEndAction {
                                // Setelah animasi selesai, tutup dialog dan panggil callback
                                dialog.dismiss()

                                // Tampilkan toast untuk feedback user dengan terjemahan
                                Toast.makeText(context, getTranslation(context, "opening_edit"), Toast.LENGTH_SHORT).show()

                                // Panggil callback untuk edit pelanggan
                                onEditClick?.invoke(pelanggan)
                            }
                            .start()
                    }
                    .start()
            }
        }

        private fun showDeleteConfirmation(context: Context, pelanggan: modelPelanggan) {
            AlertDialog.Builder(context)
                .setTitle(getTranslation(context, "delete_confirm_title"))
                .setMessage("${getTranslation(context, "delete_confirm_message")} ${pelanggan.namaPelanggan}?")
                .setPositiveButton(getTranslation(context, "yes")) { _, _ ->
                    onDeleteClick?.invoke(pelanggan)
                }
                .setNegativeButton(getTranslation(context, "no"), null)
                .show()
        }

        private fun hubungiPelanggan(context: Context, noHP: String?) {
            if (noHP.isNullOrEmpty() || noHP == getTranslation(context, "no_phone")) {
                Toast.makeText(context, getTranslation(context, "phone_not_available"), Toast.LENGTH_SHORT).show()
                return
            }

            try {
                // Format nomor HP untuk WhatsApp (hilangkan karakter non-digit)
                val cleanNumber = noHP.replace(Regex("[^0-9+]"), "")

                if (cleanNumber.isEmpty()) {
                    Toast.makeText(context, getTranslation(context, "invalid_phone"), Toast.LENGTH_SHORT).show()
                    return
                }

                // Jika nomor dimulai dengan 0, ganti dengan 62 (kode negara Indonesia)
                val formattedNumber = when {
                    cleanNumber.startsWith("0") -> "62${cleanNumber.substring(1)}"
                    cleanNumber.startsWith("62") -> cleanNumber
                    cleanNumber.startsWith("+62") -> cleanNumber.substring(1)
                    else -> "62$cleanNumber"
                }

                // Method 1: Coba dengan Intent langsung tanpa package (lebih universal)
                val whatsappIntent = Intent(Intent.ACTION_VIEW)
                whatsappIntent.data = Uri.parse("https://wa.me/$formattedNumber")

                try {
                    context.startActivity(whatsappIntent)
                    Toast.makeText(context, getTranslation(context, "opening_whatsapp"), Toast.LENGTH_SHORT).show()
                    return
                } catch (e: Exception) {
                    // Method 2: Coba dengan scheme whatsapp://
                    try {
                        val whatsappSchemeIntent = Intent(Intent.ACTION_VIEW)
                        whatsappSchemeIntent.data = Uri.parse("whatsapp://send?phone=$formattedNumber")
                        context.startActivity(whatsappSchemeIntent)
                        Toast.makeText(context, getTranslation(context, "opening_whatsapp"), Toast.LENGTH_SHORT).show()
                        return
                    } catch (e2: Exception) {
                        // Method 3: Coba dengan Intent.ACTION_SEND
                        try {
                            val sendIntent = Intent()
                            sendIntent.action = Intent.ACTION_SEND
                            sendIntent.putExtra(Intent.EXTRA_TEXT, "")
                            sendIntent.putExtra("jid", "$formattedNumber@s.whatsapp.net")
                            sendIntent.`package` = "com.whatsapp"
                            sendIntent.type = "text/plain"
                            context.startActivity(sendIntent)
                            Toast.makeText(context, getTranslation(context, "opening_whatsapp"), Toast.LENGTH_SHORT).show()
                            return
                        } catch (e3: Exception) {
                            // Method 4: Fallback ke browser untuk wa.me
                            try {
                                val browserIntent = Intent(Intent.ACTION_VIEW)
                                browserIntent.data = Uri.parse("https://wa.me/$formattedNumber")
                                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(browserIntent)
                                Toast.makeText(context, getTranslation(context, "opening_whatsapp_web"), Toast.LENGTH_SHORT).show()
                                return
                            } catch (e4: Exception) {
                                // Terakhir fallback ke telepon
                                openPhoneDialer(context, noHP)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                openPhoneDialer(context, noHP)
            }
        }

        private fun openPhoneDialer(context: Context, noHP: String) {
            try {
                val phoneIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$noHP")
                }
                context.startActivity(phoneIntent)
                Toast.makeText(context, getTranslation(context, "opening_phone"), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, getTranslation(context, "cannot_open_phone"), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function untuk update list data dan refresh tampilan
    fun updateList(newList: List<modelPelanggan>) {
        listPelanggan.clear()
        listPelanggan.addAll(newList)
        notifyDataSetChanged()

        // Debug log untuk memastikan data terupdate
        android.util.Log.d("AdapterDataPelanggan", "Data updated. Size: ${listPelanggan.size}")
    }

    // Function untuk refresh bahasa ketika bahasa berubah
    fun refreshLanguage() {
        notifyDataSetChanged()
    }

    // Function untuk menambah item baru
    fun addItem(pelanggan: modelPelanggan) {
        listPelanggan.add(pelanggan)
        notifyItemInserted(listPelanggan.size - 1)
    }

    // Function untuk menghapus item
    fun removeItem(position: Int): modelPelanggan? {
        return if (position in 0 until listPelanggan.size) {
            val removedItem = listPelanggan.removeAt(position)
            notifyItemRemoved(position)
            removedItem
        } else {
            null
        }
    }

    // Function untuk mencari pelanggan berdasarkan ID dan menghapusnya
    fun removeItemById(id: String): Boolean {
        val position = listPelanggan.indexOfFirst { it.idPelanggan == id }
        return if (position != -1) {
            listPelanggan.removeAt(position)
            notifyItemRemoved(position)
            true
        } else {
            false
        }
    }

    // Function untuk update item tertentu
    fun updateItem(position: Int, pelanggan: modelPelanggan) {
        if (position in 0 until listPelanggan.size) {
            listPelanggan[position] = pelanggan
            notifyItemChanged(position)
        }
    }
}