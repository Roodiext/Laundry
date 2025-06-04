package com.rudi.laundry.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelCabang

class AdapterDataCabang(
    private var listCabang: ArrayList<modelCabang>,
    private val onDeleteClick: (modelCabang) -> Unit,
    private val onEditClick: (modelCabang) -> Unit
) : RecyclerView.Adapter<AdapterDataCabang.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStoreName: TextView = itemView.findViewById(R.id.tvStoreName)
        val tvBranchName: TextView = itemView.findViewById(R.id.tvBranchName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        val tvOperationalHours: TextView = itemView.findViewById(R.id.tvOperationalHours)
        val btnCall: ImageButton = itemView.findViewById(R.id.btnCall)
        val btnDirections: Button = itemView.findViewById(R.id.btnDirections)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_cabang, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cabang = listCabang[position]

        // Debug log untuk melihat data yang sedang di-bind
        android.util.Log.d("AdapterDebug", "Binding position $position:")
        android.util.Log.d("AdapterDebug", "  - namaToko: '${cabang.namaToko}'")
        android.util.Log.d("AdapterDebug", "  - namaCabang: '${cabang.namaCabang}'")
        android.util.Log.d("AdapterDebug", "  - alamatCabang: '${cabang.alamatCabang}'")
        android.util.Log.d("AdapterDebug", "  - noTelpCabang: '${cabang.noTelpCabang}'")

        // Set data ke views dengan null safety dan trim whitespace
        holder.tvStoreName.text = if (cabang.namaToko.trim().isNotEmpty()) {
            cabang.namaToko.trim()
        } else {
            "Nama Toko Tidak Tersedia"
        }

        holder.tvBranchName.text = if (cabang.namaCabang.trim().isNotEmpty()) {
            cabang.namaCabang.trim()
        } else {
            "Nama Cabang Tidak Tersedia"
        }

        holder.tvAddress.text = if (cabang.alamatCabang.trim().isNotEmpty()) {
            cabang.alamatCabang.trim()
        } else {
            "Alamat tidak tersedia"
        }

        holder.tvPhone.text = if (cabang.noTelpCabang.trim().isNotEmpty()) {
            cabang.noTelpCabang.trim()
        } else {
            "Telepon tidak tersedia"
        }

        // Set jam operasional info
        try {
            holder.tvOperationalHours.text = cabang.getOperationalHoursInfo()
        } catch (e: Exception) {
            holder.tvOperationalHours.text = "Info jam operasional tidak tersedia"
            android.util.Log.e("AdapterDebug", "Error getting operational hours: ${e.message}")
        }

        // Set status real-time dengan warna yang sesuai
        try {
            val realTimeStatus = cabang.getRealTimeStatus()
            holder.tvStatus.text = realTimeStatus

            when (realTimeStatus) {
                "BUKA" -> {
                    holder.tvStatus.setBackgroundResource(R.drawable.status_badge_open)
                    holder.tvStatus.text = "BUKA"
                }
                "TUTUP" -> {
                    holder.tvStatus.setBackgroundResource(R.drawable.status_badge_closed)
                    holder.tvStatus.text = "TUTUP"
                }
                else -> {
                    holder.tvStatus.setBackgroundResource(R.drawable.status_badge_background)
                    holder.tvStatus.text = realTimeStatus
                }
            }
        } catch (e: Exception) {
            holder.tvStatus.text = "Status tidak tersedia"
            holder.tvStatus.setBackgroundResource(R.drawable.status_badge_background)
            android.util.Log.e("AdapterDebug", "Error getting real-time status: ${e.message}")
        }

        // Click listeners
        holder.btnCall.setOnClickListener {
            val phoneNumber = cabang.noTelpCabang.trim()
            if (phoneNumber.isNotEmpty()) {
                try {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phoneNumber")
                    }
                    holder.itemView.context.startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("AdapterDebug", "Error starting call intent: ${e.message}")
                }
            }
        }

        holder.btnDirections.setOnClickListener {
            val address = cabang.alamatCabang.trim()
            if (address.isNotEmpty()) {
                try {
                    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                        setPackage("com.google.android.apps.maps")
                    }

                    // Cek apakah Google Maps tersedia
                    if (mapIntent.resolveActivity(holder.itemView.context.packageManager) != null) {
                        holder.itemView.context.startActivity(mapIntent)
                    } else {
                        // Fallback ke web browser
                        val webIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}")
                        }
                        holder.itemView.context.startActivity(webIntent)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AdapterDebug", "Error starting maps intent: ${e.message}")
                }
            }
        }

        holder.btnEdit.setOnClickListener {
            try {
                onEditClick(cabang)
            } catch (e: Exception) {
                android.util.Log.e("AdapterDebug", "Error in edit click: ${e.message}")
            }
        }

        holder.btnDelete.setOnClickListener {
            try {
                onDeleteClick(cabang)
            } catch (e: Exception) {
                android.util.Log.e("AdapterDebug", "Error in delete click: ${e.message}")
            }
        }

        // Set click listener untuk seluruh card (optional)
        holder.itemView.setOnClickListener {
            // Bisa ditambahkan aksi ketika card diklik
            // Misalnya membuka detail cabang
            android.util.Log.d("AdapterDebug", "Card clicked for: ${cabang.namaCabang}")
        }
    }

    override fun getItemCount(): Int {
        android.util.Log.d("AdapterDebug", "getItemCount: ${listCabang.size}")
        return listCabang.size
    }

    // Method untuk update data dengan logging yang lebih detail
    fun updateData(newList: ArrayList<modelCabang>) {
        android.util.Log.d("AdapterDebug", "=== updateData called ===")
        android.util.Log.d("AdapterDebug", "Previous size: ${listCabang.size}")
        android.util.Log.d("AdapterDebug", "New size: ${newList.size}")

        // Log data yang akan di-update
        newList.forEachIndexed { index, cabang ->
            android.util.Log.d("AdapterDebug", "Item $index:")
            android.util.Log.d("AdapterDebug", "  - namaToko: '${cabang.namaToko}'")
            android.util.Log.d("AdapterDebug", "  - namaCabang: '${cabang.namaCabang}'")
            android.util.Log.d("AdapterDebug", "  - alamatCabang: '${cabang.alamatCabang}'")
        }

        // Clear existing data
        listCabang.clear()
        android.util.Log.d("AdapterDebug", "Existing data cleared")

        // Add new data
        listCabang.addAll(newList)
        android.util.Log.d("AdapterDebug", "New data added, current size: ${listCabang.size}")

        // Notify adapter
        notifyDataSetChanged()
        android.util.Log.d("AdapterDebug", "notifyDataSetChanged() called")
        android.util.Log.d("AdapterDebug", "=== updateData finished ===")
    }

    // Method untuk menghitung statistik berdasarkan status real-time
    fun getStats(): Pair<Int, Int> {
        val total = listCabang.size
        var buka = 0

        try {
            buka = listCabang.count { it.getRealTimeStatus() == "BUKA" }
        } catch (e: Exception) {
            android.util.Log.e("AdapterDebug", "Error calculating stats: ${e.message}")
        }

        android.util.Log.d("AdapterDebug", "Stats - Total: $total, Buka: $buka")
        return Pair(total, buka)
    }

    // Method untuk refresh status real-time
    fun refreshRealTimeStatus() {
        android.util.Log.d("AdapterDebug", "Refreshing real-time status for ${listCabang.size} items")
        notifyDataSetChanged()
    }

    // Method untuk mendapatkan data cabang berdasarkan posisi
    fun getCabangAt(position: Int): modelCabang? {
        return if (position >= 0 && position < listCabang.size) {
            listCabang[position]
        } else {
            android.util.Log.w("AdapterDebug", "Invalid position: $position, size: ${listCabang.size}")
            null
        }
    }

    // Method untuk mendapatkan semua data
    fun getAllCabang(): ArrayList<modelCabang> {
        return ArrayList(listCabang)
    }

    // Method untuk menghapus item berdasarkan posisi
    fun removeAt(position: Int) {
        if (position >= 0 && position < listCabang.size) {
            listCabang.removeAt(position)
            notifyItemRemoved(position)
            android.util.Log.d("AdapterDebug", "Item removed at position $position")
        } else {
            android.util.Log.w("AdapterDebug", "Cannot remove, invalid position: $position")
        }
    }

    // Method untuk menambah item
    fun addItem(cabang: modelCabang) {
        listCabang.add(cabang)
        notifyItemInserted(listCabang.size - 1)
        android.util.Log.d("AdapterDebug", "Item added: ${cabang.namaCabang}")
    }

    // Method untuk update item berdasarkan posisi
    fun updateItem(position: Int, cabang: modelCabang) {
        if (position >= 0 && position < listCabang.size) {
            listCabang[position] = cabang
            notifyItemChanged(position)
            android.util.Log.d("AdapterDebug", "Item updated at position $position: ${cabang.namaCabang}")
        } else {
            android.util.Log.w("AdapterDebug", "Cannot update, invalid position: $position")
        }
    }

    // Method untuk clear semua data
    fun clearAll() {
        val size = listCabang.size
        listCabang.clear()
        notifyItemRangeRemoved(0, size)
        android.util.Log.d("AdapterDebug", "All items cleared, previous size: $size")
    }
}