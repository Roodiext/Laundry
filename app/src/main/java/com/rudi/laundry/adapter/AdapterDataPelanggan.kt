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
            // Set data ke views
            tvNama.text = pelanggan.namaPelanggan ?: "Tidak Ada Nama"
            tvAlamat.text = pelanggan.alamatPelanggan ?: "Tidak Ada Alamat"
            tvNoHP.text = pelanggan.noHPPelanggan ?: "Tidak Ada No HP"
            tvTerdaftar.text = if (pelanggan.terdaftar.isNotEmpty()) pelanggan.terdaftar else "Belum Terdaftar"

            // Tombol Lihat - Menampilkan Dialog Detail
            btLihat.setOnClickListener {
                showDetailDialog(itemView.context, pelanggan)
            }

            // Tombol Hubungi - Membuka WhatsApp
            btHubungi.setOnClickListener {
                hubungiPelanggan(itemView.context, pelanggan.noHPPelanggan)
            }

            // Klik pada card juga bisa membuka dialog
            cvCARD.setOnClickListener {
                showDetailDialog(itemView.context, pelanggan)
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
            val tvIdValue: TextView = dialog.findViewById(R.id.tvDIALOG_MOD_PELANGGAN_ID_value)
            val tvNamaValue: TextView = dialog.findViewById(R.id.tvDIALOG_MOD_PELANGGAN_Nama_value)
            val tvNoHPValue: TextView = dialog.findViewById(R.id.tvDIALOG_MOD_PELANGGAN_NoHP_value)
            val tvCabangValue: TextView = dialog.findViewById(R.id.tvDIALOG_MOD_PELANGGAN_Cabang_value)
            val tvTerdaftarValue: TextView = dialog.findViewById(R.id.tvDIALOG_MOD_PELANGGAN_Terdaftar_value)
            val btEdit: MaterialButton = dialog.findViewById(R.id.btDIALOG_MOD_PELANGGAN_Edit)
            val btHapus: MaterialButton = dialog.findViewById(R.id.btDIALOG_MOD_PELANGGAN_Hapus)

            // Set data to dialog views
            tvJudul.text = "Detail Pelanggan"
            tvIdValue.text = pelanggan.idPelanggan.ifEmpty { "Tidak ada ID" }
            tvNamaValue.text = pelanggan.namaPelanggan ?: "Tidak ada nama"
            tvNoHPValue.text = pelanggan.noHPPelanggan ?: "Tidak ada nomor"
            tvCabangValue.text = pelanggan.cabang.ifEmpty { "Tidak ada cabang" }
            tvTerdaftarValue.text = pelanggan.terdaftar.ifEmpty { "Tidak diketahui" }

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

                                // Tampilkan toast untuk feedback user
                                Toast.makeText(context, "Membuka halaman edit...", Toast.LENGTH_SHORT).show()

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
                .setTitle("Konfirmasi Hapus")
                .setMessage("Apakah Anda yakin ingin menghapus pelanggan ${pelanggan.namaPelanggan}?")
                .setPositiveButton("Ya") { _, _ ->
                    onDeleteClick?.invoke(pelanggan)
                }
                .setNegativeButton("Tidak", null)
                .show()
        }

        private fun hubungiPelanggan(context: Context, noHP: String?) {
            if (noHP.isNullOrEmpty() || noHP == "Tidak Ada No HP") {
                Toast.makeText(context, "Nomor HP tidak tersedia", Toast.LENGTH_SHORT).show()
                return
            }

            try {
                // Format nomor HP untuk WhatsApp (hilangkan karakter non-digit)
                val cleanNumber = noHP.replace(Regex("[^0-9+]"), "")

                if (cleanNumber.isEmpty()) {
                    Toast.makeText(context, "Nomor HP tidak valid", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Membuka WhatsApp...", Toast.LENGTH_SHORT).show()
                    return
                } catch (e: Exception) {
                    // Method 2: Coba dengan scheme whatsapp://
                    try {
                        val whatsappSchemeIntent = Intent(Intent.ACTION_VIEW)
                        whatsappSchemeIntent.data = Uri.parse("whatsapp://send?phone=$formattedNumber")
                        context.startActivity(whatsappSchemeIntent)
                        Toast.makeText(context, "Membuka WhatsApp...", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(context, "Membuka WhatsApp...", Toast.LENGTH_SHORT).show()
                            return
                        } catch (e3: Exception) {
                            // Method 4: Fallback ke browser untuk wa.me
                            try {
                                val browserIntent = Intent(Intent.ACTION_VIEW)
                                browserIntent.data = Uri.parse("https://wa.me/$formattedNumber")
                                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(browserIntent)
                                Toast.makeText(context, "Membuka WhatsApp Web...", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "Membuka aplikasi telepon...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Tidak dapat membuka aplikasi telepon", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function untuk update list data
    fun updateList(newList: List<modelPelanggan>) {
        listPelanggan.clear()
        listPelanggan.addAll(newList)
        notifyDataSetChanged()

        // Debug log untuk memastikan data terupdate
        android.util.Log.d("AdapterDataPelanggan", "Data updated. Size: ${listPelanggan.size}")
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