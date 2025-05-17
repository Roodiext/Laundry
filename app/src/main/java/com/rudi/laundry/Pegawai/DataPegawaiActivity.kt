    package com.rudi.laundry.Pegawai

    import android.content.Intent
    import android.os.Bundle
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.google.android.material.floatingactionbutton.FloatingActionButton
    import com.google.firebase.database.*
    import com.rudi.laundry.Pegawai.TambahPegawaiActivity
    import com.rudi.laundry.R
    import com.rudi.laundry.adapter.AdapterDataPegawai
    import com.rudi.laundry.modeldata.modelPegawai

    class DataPegawaiActivity : AppCompatActivity() {

        private val database = FirebaseDatabase.getInstance()
        private val myRef: DatabaseReference = database.getReference("pegawai")

        private lateinit var rvDataPegawai: RecyclerView
        private lateinit var fabTambahPegawai: FloatingActionButton
        private var listPegawai = arrayListOf<modelPegawai>()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_data_pegawai)

            initViews()
            setupRecyclerView()
            setupListeners()
            getData()

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.datapegawai)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        private fun initViews() {
            rvDataPegawai = findViewById(R.id.rvPegawai)
            fabTambahPegawai = findViewById(R.id.fab_tambah_pegawai)
        }

        private fun setupRecyclerView() {
            val layoutManager = LinearLayoutManager(this)
            layoutManager.reverseLayout = true
            layoutManager.stackFromEnd = true
            rvDataPegawai.layoutManager = layoutManager
            rvDataPegawai.setHasFixedSize(true)
        }

        private fun setupListeners() {
            fabTambahPegawai.setOnClickListener {
                val intent = Intent(this, TambahPegawaiActivity::class.java)
                startActivity(intent)
            }
        }

        private fun getData() {
            val query = myRef.orderByChild("idPegawai").limitToLast(100)
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listPegawai.clear()
                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {
                            val pegawai = dataSnapshot.getValue(modelPegawai::class.java) ?: modelPegawai()
                            listPegawai.add(pegawai)
                        }
                        val adapter = AdapterDataPegawai(listPegawai, this@DataPegawaiActivity)
                        rvDataPegawai.adapter = adapter
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DataPegawaiActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
