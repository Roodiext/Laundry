package com.rudi.laundry.adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rudi.laundry.Pegawai.EditPegawaiActivity
import com.rudi.laundry.R
import com.rudi.laundry.modeldata.modelPegawai
import com.rudi.laundry.modeldata.modelCabang

class AdapterDataPegawai(
    private val listPegawai: ArrayList<modelPegawai>,
    private val context: Context
) : RecyclerView.Adapter<AdapterDataPegawai.ViewHolder>() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("pegawai")
    private val cabangRef: DatabaseReference = database.getReference("cabang")

    // Cache untuk menyimpan data cabang agar tidak perlu query berulang
    private val cabangCache = mutableMapOf<String, modelCabang>()

    init {
        // Load semua data cabang ke cache saat adapter dibuat
        loadCabangCache()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pegawai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPegawai[position]

        holder.tvNama.text = item.namaPegawai ?: "Tidak Ada Nama"
        holder.tvAlamat.text = item.alamatPegawai ?: "Tidak Ada Alamat"
        holder.tvNoHP.text = item.noHPPegawai ?: "Tidak Ada No HP"

        // Tampilkan nama cabang berdasarkan ID cabang
        val cabangDisplay = getCabangDisplayName(item.cabang)
        holder.tvCabang.text = cabangDisplay

        // Tambahkan click listener untuk button Lihat
        holder.btnLihat.setOnClickListener {
            showDetailDialog(item)
        }
    }

    private fun loadCabangCache() {
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cabangCache.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        try {
                            val cabang = dataSnapshot.getValue(modelCabang::class.java)

                            if (cabang != null) {
                                // Pastikan ID cabang ter-set dari key Firebase
                                if (cabang.idCabang.isEmpty()) {
                                    cabang.idCabang = dataSnapshot.key ?: ""
                                }

                                cabangCache[cabang.idCabang] = cabang
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("AdapterPegawai", "Error loading cabang: ${e.message}")
                        }
                    }
                }

                // Refresh tampilan setelah cache cabang ter-update
                notifyDataSetChanged()

                android.util.Log.d("AdapterPegawai", "Cabang cache loaded: ${cabangCache.size} items")
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("AdapterPegawai", "Failed to load cabang cache: ${error.message}")
            }
        })
    }

    private fun getCabangDisplayName(cabangId: String): String {
        if (cabangId.isEmpty()) {
            return "Belum Terdaftar"
        }

        val cabang = cabangCache[cabangId]

        return when {
            cabang == null -> "Cabang Tidak Ditemukan"
            cabang.namaToko.isNotEmpty() && cabang.namaCabang.isNotEmpty() ->
                "${cabang.namaToko} - ${cabang.namaCabang}"
            cabang.namaToko.isNotEmpty() -> cabang.namaToko
            cabang.namaCabang.isNotEmpty() -> cabang.namaCabang
            else -> "Belum Terdaftar"
        }
    }

    private fun showDetailDialog(pegawai: modelPegawai) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_mod_pegawai)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Bind data to dialog views
        val tvIDValue = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_ID_value)
        val tvNamaValue = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_Nama_value)
        val tvAlamatValue = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_Alamat_value)
        val tvNoHPValue = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_NoHP_value)
        val tvCabangValue = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_Cabang_value)

        val btEdit = dialog.findViewById<MaterialButton>(R.id.btDIALOG_MOD_PEGAWAI_Edit)
        val btHapus = dialog.findViewById<MaterialButton>(R.id.btDIALOG_MOD_PEGAWAI_Hapus)

        // Set data to views
        tvIDValue.text = pegawai.idPegawai
        tvNamaValue.text = pegawai.namaPegawai ?: "Tidak Ada Nama"
        tvAlamatValue.text = pegawai.alamatPegawai ?: "Tidak Ada Alamat"
        tvNoHPValue.text = pegawai.noHPPegawai ?: "Tidak Ada No HP"

        // Tampilkan nama cabang yang proper
        val cabangDisplay = getCabangDisplayName(pegawai.cabang)
        tvCabangValue.text = cabangDisplay

        // Edit button click listener - UPDATED to use EditPegawaiActivity
        btEdit.setOnClickListener {
            val intent = Intent(context, EditPegawaiActivity::class.java)
            intent.putExtra("ACTION_TYPE", "EDIT")
            intent.putExtra("idPegawai", pegawai.idPegawai)
            intent.putExtra("namaPegawai", pegawai.namaPegawai)
            intent.putExtra("alamatPegawai", pegawai.alamatPegawai)
            intent.putExtra("noHPPegawai", pegawai.noHPPegawai)
            intent.putExtra("idCabang", pegawai.cabang)
            context.startActivity(intent)
            dialog.dismiss()
        }

        // Delete button click listener
        btHapus.setOnClickListener {
            showDeleteConfirmationDialog(pegawai, dialog)
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(pegawai: modelPegawai, parentDialog: Dialog) {
        val confirmDialog = androidx.appcompat.app.AlertDialog.Builder(context)
        confirmDialog.setTitle("Konfirmasi Hapus")
        confirmDialog.setMessage("Apakah Anda yakin ingin menghapus data pegawai ${pegawai.namaPegawai}?")
        confirmDialog.setIcon(android.R.drawable.ic_dialog_alert)
        confirmDialog.setPositiveButton("Ya") { _, _ ->
            deletePegawai(pegawai, parentDialog)
        }
        confirmDialog.setNegativeButton("Tidak") { dialog, _ ->
            dialog.dismiss()
        }
        confirmDialog.show()
    }

    private fun deletePegawai(pegawai: modelPegawai, parentDialog: Dialog) {
        // Validasi ID pegawai tidak null atau kosong
        if (pegawai.idPegawai.isNullOrEmpty()) {
            Toast.makeText(context, "ID Pegawai tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        myRef.child(pegawai.idPegawai).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Data pegawai berhasil dihapus", Toast.LENGTH_SHORT).show()
                parentDialog.dismiss()

                // PENTING: Jangan manipulasi list secara manual di adapter
                // Biarkan Firebase ValueEventListener di DataPegawaiActivity yang menangani update data
                // Karena Firebase akan otomatis trigger onDataChange() setelah data dihapus
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Gagal menghapus data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Method untuk update data adapter (dipanggil dari activity)
    fun updateData(newList: ArrayList<modelPegawai>) {
        listPegawai.clear()
        listPegawai.addAll(newList)
        notifyDataSetChanged()
    }

    // Method untuk refresh cache cabang jika diperlukan
    fun refreshCabangCache() {
        loadCabangCache()
    }

    override fun getItemCount(): Int {
        return listPegawai.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaPegawai)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvAlamatPegawai)
        val tvNoHP: TextView = itemView.findViewById(R.id.tvNoHPPegawai)
        val tvCabang: TextView = itemView.findViewById(R.id.tvCabangPegawai)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_pegawai)
        val btnLihat: MaterialButton = itemView.findViewById(R.id.btnLihatPegawai)
    }
}