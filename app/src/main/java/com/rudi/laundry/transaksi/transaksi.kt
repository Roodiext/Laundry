    package com.rudi.laundry.transaksi

    import android.content.Intent
    import android.os.Bundle
    import android.view.View
    import android.widget.Button
    import android.widget.TextView
    import android.widget.Toast
    import androidx.activity.enableEdgeToEdge
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.rudi.laundry.R
    import com.rudi.laundry.Layanan.PilihLayananActivity
    import com.rudi.laundry.LayananTambahan.PilihLayananTambahan
    import com.rudi.laundry.adapter.AdapterLayananTransaksi
    import com.rudi.laundry.modeldata.modelLayanan
    import com.rudi.laundry.pelanggan.PilihPelangganActivity
    import java.io.Serializable

    class transaksi : AppCompatActivity() {

        private lateinit var namaPelangganText: TextView
        private lateinit var noHPPelangganText: TextView
        private lateinit var namaLayananText: TextView
        private lateinit var hargaLayananText: TextView
        private lateinit var rvLayananTambahan: RecyclerView
        private lateinit var buttonProses: Button // Tambahkan tombol proses
        private lateinit var adapterTambahan: AdapterLayananTransaksi
        private val listTambahan = mutableListOf<modelLayanan>()

        // Untuk menyimpan data pelanggan dan layanan
        private var namaPelanggan: String = ""
        private var nomorHP: String = ""
        private var namaLayanan: String = ""
        private var hargaLayanan: String = ""

        private val pilihPelangganLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                namaPelanggan = data?.getStringExtra("nama") ?: ""
                nomorHP = data?.getStringExtra("hp") ?: ""
                namaPelangganText.text = "Nama Pelanggan: $namaPelanggan"
                noHPPelangganText.text = "No HP: $nomorHP"
            }
        }

        private val pilihLayananLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                namaLayanan = data?.getStringExtra("nama") ?: ""
                hargaLayanan = data?.getStringExtra("harga") ?: ""
                namaLayananText.text = "Layanan: $namaLayanan"
                hargaLayananText.text = "Harga: $hargaLayanan"
            }
        }

        private val pilihTambahanLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val nama = data?.getStringExtra("namaTambahan")
                val harga = data?.getStringExtra("hargaTambahan")

                if (nama != null && harga != null) {
                    val layanan = modelLayanan(namaLayanan = nama, hargaLayanan = harga)
                    listTambahan.add(layanan)
                    adapterTambahan.notifyDataSetChanged()

                    Toast.makeText(this, "Menambahkan: $nama - $harga", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Data layanan tambahan tidak valid", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_transaksi)

            namaPelangganText = findViewById(R.id.label_nama)
            noHPPelangganText = findViewById(R.id.label_hp)
            namaLayananText = findViewById(R.id.label_layanan)
            hargaLayananText = findViewById(R.id.label_harga)
            rvLayananTambahan = findViewById(R.id.rvLayananTambahan)
            buttonProses = findViewById(R.id.btn_proses)

            setupRecyclerView()

            // Setup tombol proses
            buttonProses.setOnClickListener {
                if (validateData()) {
                    // Kirim data ke KonfirmasiDataTransaksi
                    val intent = Intent(this@transaksi, KonfirmasiDataTransaksi::class.java)
                    intent.putExtra("nama_pelanggan", namaPelanggan)
                    intent.putExtra("nomor_hp", nomorHP)
                    intent.putExtra("nama_layanan", namaLayanan)
                    intent.putExtra("harga_layanan", hargaLayanan)
                    intent.putExtra("layanan_tambahan", ArrayList(listTambahan) as Serializable)
                    startActivity(intent)
                }
            }

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transaksi)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        private fun validateData(): Boolean {
            if (namaPelanggan.isEmpty() || nomorHP.isEmpty()) {
                Toast.makeText(this, "Silakan pilih pelanggan terlebih dahulu", Toast.LENGTH_SHORT).show()
                return false
            }

            if (namaLayanan.isEmpty() || hargaLayanan.isEmpty()) {
                Toast.makeText(this, "Silakan pilih layanan utama terlebih dahulu", Toast.LENGTH_SHORT).show()
                return false
            }

            return true
        }

        private fun setupRecyclerView() {
            adapterTambahan = AdapterLayananTransaksi(listTambahan) { layanan ->
                listTambahan.remove(layanan)
                adapterTambahan.notifyDataSetChanged()
                Toast.makeText(this, "${layanan.namaLayanan} dihapus", Toast.LENGTH_SHORT).show()
            }

            rvLayananTambahan.layoutManager = LinearLayoutManager(this)
            rvLayananTambahan.adapter = adapterTambahan
        }

        fun PilihPelanggan(view: View?) {
            val intent = Intent(this@transaksi, PilihPelangganActivity::class.java)
            pilihPelangganLauncher.launch(intent)
        }

        fun PilihLayanan(view: View?) {
            val intent = Intent(this@transaksi, PilihLayananActivity::class.java)
            pilihLayananLauncher.launch(intent)
        }

        fun DataLayananTambahan(view: View?) {
            val intent = Intent(this@transaksi, PilihLayananTambahan::class.java)
            pilihTambahanLauncher.launch(intent)
        }
    }