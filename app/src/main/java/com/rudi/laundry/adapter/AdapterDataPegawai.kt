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

    // Language support - ambil dari SharedPreferences
    private var currentLanguage = "id"

    // Language texts untuk adapter - DITAMBAHKAN TEXT UNTUK DIALOG
    private val languageTexts = mapOf(
        "id" to mapOf(
            "no_name" to "Tidak Ada Nama",
            "no_address" to "Tidak Ada Alamat",
            "no_phone" to "Tidak Ada No HP",
            "not_registered" to "Belum Terdaftar",
            "branch_not_found" to "Cabang Tidak Ditemukan",
            "employee_data" to "Data Pegawai",
            "employee_name" to "Nama Pegawai",
            "address" to "Alamat: ",
            "phone" to "No HP: ",
            "branch" to "Cabang: ",
            "contact" to "Hubungi",
            "view" to "Lihat",
            "edit" to "Edit",
            "delete" to "Hapus",
            "confirm_delete" to "Konfirmasi Hapus",
            "delete_confirmation" to "Apakah Anda yakin ingin menghapus data pegawai",
            "yes" to "Ya",
            "no" to "Tidak",
            "invalid_id" to "ID Pegawai tidak valid",
            "delete_success" to "Data pegawai berhasil dihapus",
            "delete_failed" to "Gagal menghapus data",
            // Dialog texts
            "employee_detail" to "Detail Pegawai",
            "employee_complete_info" to "Informasi lengkap pegawai",
            "employee_id" to "ID Pegawai",
            "employee_name_label" to "Nama Pegawai",
            "address_label" to "Alamat",
            "phone_label" to "Nomor Telepon",
            "branch_label" to "Cabang"
        ),
        "en" to mapOf(
            "no_name" to "No Name",
            "no_address" to "No Address",
            "no_phone" to "No Phone Number",
            "not_registered" to "Not Registered",
            "branch_not_found" to "Branch Not Found",
            "employee_data" to "Employee Data",
            "employee_name" to "Employee Name",
            "address" to "Address: ",
            "phone" to "Phone: ",
            "branch" to "Branch: ",
            "contact" to "Contact",
            "view" to "View",
            "edit" to "Edit",
            "delete" to "Delete",
            "confirm_delete" to "Confirm Delete",
            "delete_confirmation" to "Are you sure you want to delete employee data",
            "yes" to "Yes",
            "no" to "No",
            "invalid_id" to "Invalid Employee ID",
            "delete_success" to "Employee data successfully deleted",
            "delete_failed" to "Failed to delete data",
            // Dialog texts
            "employee_detail" to "Employee Detail",
            "employee_complete_info" to "Complete employee information",
            "employee_id" to "Employee ID",
            "employee_name_label" to "Employee Name",
            "address_label" to "Address",
            "phone_label" to "Phone Number",
            "branch_label" to "Branch"
        )
    )

    init {
        // Load semua data cabang ke cache saat adapter dibuat
        loadCabangCache()
        // Load current language dari SharedPreferences
        loadCurrentLanguage()
    }

    private fun loadCurrentLanguage() {
        val sharedPref = context.getSharedPreferences("language_pref", Context.MODE_PRIVATE)
        currentLanguage = sharedPref.getString("selected_language", "id") ?: "id"
    }

    private fun getText(key: String): String {
        val texts = languageTexts[currentLanguage] ?: languageTexts["id"]!!
        return texts[key] ?: key
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pegawai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPegawai[position]

        // Update language setiap kali bind (untuk handle perubahan bahasa real-time)
        loadCurrentLanguage()

        // Set data pegawai
        holder.tvNama.text = item.namaPegawai ?: getText("no_name")
        holder.tvAlamat.text = item.alamatPegawai ?: getText("no_address")
        holder.tvNoHP.text = item.noHPPegawai ?: getText("no_phone")

        // Tampilkan nama cabang berdasarkan ID cabang
        val cabangDisplay = getCabangDisplayName(item.cabang)
        holder.tvCabang.text = cabangDisplay

        // Update label text dengan bahasa yang sesuai
        holder.tvHeaderPegawai.text = getText("employee_data")
        holder.tvLabelNamaPegawai.text = getText("employee_name")
        holder.tvLabelAlamatPegawai.text = getText("address")
        holder.tvLabelNoHPPegawai.text = getText("phone")
        holder.tvLabelCabangPegawai.text = getText("branch")

        // Update button text dengan bahasa yang sesuai
        holder.btnLihat.text = getText("view")
        holder.btHubungiPegawai.text = getText("contact")

        // Tambahkan click listener untuk button Lihat
        holder.btnLihat.setOnClickListener {
            showDetailDialog(item)
        }

        // Tambahkan click listener untuk button Hubungi (optional)
        holder.btHubungiPegawai.setOnClickListener {
            // Implementasi untuk menghubungi pegawai (bisa buka dialer dengan nomor HP)
            val phoneNumber = item.noHPPegawai
            if (!phoneNumber.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = android.net.Uri.parse("tel:$phoneNumber")
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open dialer", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, getText("no_phone"), Toast.LENGTH_SHORT).show()
            }
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
            return getText("not_registered")
        }

        val cabang = cabangCache[cabangId]

        return when {
            cabang == null -> getText("branch_not_found")
            cabang.namaToko.isNotEmpty() && cabang.namaCabang.isNotEmpty() ->
                "${cabang.namaToko} - ${cabang.namaCabang}"
            cabang.namaToko.isNotEmpty() -> cabang.namaToko
            cabang.namaCabang.isNotEmpty() -> cabang.namaCabang
            else -> getText("not_registered")
        }
    }

    private fun showDetailDialog(pegawai: modelPegawai) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_mod_pegawai)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // BAGIAN BARU: Update language sebelum set UI
        loadCurrentLanguage()

        // Bind data to dialog views
        val tvJudul = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_judul)
        val tvIDValue = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_ID_value)
        val tvNamaValue = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_Nama_value)
        val tvAlamatValue = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_Alamat_value)
        val tvNoHPValue = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_NoHP_value)
        val tvCabangValue = dialog.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_Cabang_value)

        val btEdit = dialog.findViewById<MaterialButton>(R.id.btDIALOG_MOD_PEGAWAI_Edit)
        val btHapus = dialog.findViewById<MaterialButton>(R.id.btDIALOG_MOD_PEGAWAI_Hapus)

        // BAGIAN BARU: Set title dialog dengan bahasa yang sesuai
        tvJudul.text = getText("employee_detail")

        // Set data to views dengan bahasa yang sesuai
        tvIDValue.text = pegawai.idPegawai
        tvNamaValue.text = pegawai.namaPegawai ?: getText("no_name")
        tvAlamatValue.text = pegawai.alamatPegawai ?: getText("no_address")
        tvNoHPValue.text = pegawai.noHPPegawai ?: getText("no_phone")

        // Tampilkan nama cabang yang proper
        val cabangDisplay = getCabangDisplayName(pegawai.cabang)
        tvCabangValue.text = cabangDisplay

        // Update button text dengan bahasa yang sesuai
        btEdit.text = getText("edit")
        btHapus.text = getText("delete")

        // Edit button click listener
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
        confirmDialog.setTitle(getText("confirm_delete"))
        confirmDialog.setMessage("${getText("delete_confirmation")} ${pegawai.namaPegawai}?")
        confirmDialog.setIcon(android.R.drawable.ic_dialog_alert)
        confirmDialog.setPositiveButton(getText("yes")) { _, _ ->
            deletePegawai(pegawai, parentDialog)
        }
        confirmDialog.setNegativeButton(getText("no")) { dialog, _ ->
            dialog.dismiss()
        }
        confirmDialog.show()
    }

    private fun deletePegawai(pegawai: modelPegawai, parentDialog: Dialog) {
        // Validasi ID pegawai tidak null atau kosong
        if (pegawai.idPegawai.isNullOrEmpty()) {
            Toast.makeText(context, getText("invalid_id"), Toast.LENGTH_SHORT).show()
            return
        }

        myRef.child(pegawai.idPegawai).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, getText("delete_success"), Toast.LENGTH_SHORT).show()
                parentDialog.dismiss()

                // PENTING: Jangan manipulasi list secara manual di adapter
                // Biarkan Firebase ValueEventListener di DataPegawaiActivity yang menangani update data
                // Karena Firebase akan otomatis trigger onDataChange() setelah data dihapus
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "${getText("delete_failed")}: ${error.message}", Toast.LENGTH_SHORT).show()
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

    // Method untuk update bahasa (dipanggil ketika bahasa berubah)
    fun updateLanguage() {
        loadCurrentLanguage()
        notifyDataSetChanged()
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

        // Tambahan untuk dynamic text labels
        val tvHeaderPegawai: TextView = itemView.findViewById(R.id.tvHeaderPegawai)
        val tvLabelNamaPegawai: TextView = itemView.findViewById(R.id.tvLabelNamaPegawai)
        val tvLabelAlamatPegawai: TextView = itemView.findViewById(R.id.tvLabelAlamatPegawai)
        val tvLabelNoHPPegawai: TextView = itemView.findViewById(R.id.tvLabelNoHPPegawai)
        val tvLabelCabangPegawai: TextView = itemView.findViewById(R.id.tvLabelCabangPegawai)
        val btHubungiPegawai: MaterialButton = itemView.findViewById(R.id.btHubungiPegawai)
    }
}