package com.rudi.laundry

import com.google.firebase.database.*
import com.rudi.laundry.modeldata.modelTransaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class RevenueCalculator {

    companion object {

        // Interface untuk callback hasil perhitungan
        interface RevenueCallback {
            fun onRevenueCalculated(totalRevenue: Double, todayRevenue: Double, completedTransactions: Int)
            fun onError(error: String)
        }

        // Fungsi untuk menghitung total pendapatan real-time
        fun calculateTotalRevenue(callback: RevenueCallback) {
            val database = FirebaseDatabase.getInstance()
            val transaksiRef = database.getReference("transaksi")

            transaksiRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalRevenue = 0.0
                    var todayRevenue = 0.0
                    var completedTransactions = 0
                    val todayDate = getTodayDateString()

                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {
                            val transaksi = dataSnapshot.getValue(modelTransaksi::class.java)
                            if (transaksi != null && transaksi.status == modelTransaksi.STATUS_SELESAI) {
                                totalRevenue += transaksi.totalBayar
                                completedTransactions++

                                // Hitung pendapatan hari ini
                                val transaksiDate = getDateFromTimestamp(transaksi.tanggalPengambilan)
                                if (transaksiDate == todayDate) {
                                    todayRevenue += transaksi.totalBayar
                                }
                            }
                        }
                    }

                    callback.onRevenueCalculated(totalRevenue, todayRevenue, completedTransactions)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback.onError(error.message)
                }
            })
        }

        // Fungsi untuk menghitung pendapatan berdasarkan periode
        fun calculateRevenueByPeriod(
            startDate: String,
            endDate: String,
            callback: (Double, Int) -> Unit
        ) {
            val database = FirebaseDatabase.getInstance()
            val transaksiRef = database.getReference("transaksi")

            transaksiRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var periodRevenue = 0.0
                    var transactionCount = 0

                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {
                            val transaksi = dataSnapshot.getValue(modelTransaksi::class.java)
                            if (transaksi != null && transaksi.status == modelTransaksi.STATUS_SELESAI) {
                                val transaksiDate = getDateFromTimestamp(transaksi.tanggalPengambilan)
                                if (isDateInRange(transaksiDate, startDate, endDate)) {
                                    periodRevenue += transaksi.totalBayar
                                    transactionCount++
                                }
                            }
                        }
                    }

                    callback(periodRevenue, transactionCount)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(0.0, 0)
                }
            })
        }

        // Fungsi untuk format rupiah
        fun formatRupiah(amount: Double): String {
            val localeID = Locale("in", "ID")
            val formatter = NumberFormat.getCurrencyInstance(localeID)
            return formatter.format(amount).replace("Rp", "ᴿᵖ")
        }

        // Fungsi untuk mendapatkan string tanggal hari ini
        private fun getTodayDateString(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return dateFormat.format(Calendar.getInstance().time)
        }

        // Fungsi untuk mengekstrak tanggal dari timestamp
        private fun getDateFromTimestamp(timestamp: String): String {
            return try {
                if (timestamp.contains(" ")) {
                    timestamp.split(" ")[0]
                } else if (timestamp.length >= 10) {
                    timestamp.substring(0, 10)
                } else {
                    timestamp
                }
            } catch (e: Exception) {
                ""
            }
        }

        // Fungsi untuk mengecek apakah tanggal dalam rentang
        private fun isDateInRange(dateStr: String, startDate: String, endDate: String): Boolean {
            return try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = dateFormat.parse(dateStr)
                val start = dateFormat.parse(startDate)
                val end = dateFormat.parse(endDate)

                date != null && start != null && end != null &&
                        (date.after(start) || date == start) && (date.before(end) || date == end)
            } catch (e: Exception) {
                false
            }
        }

        // Fungsi untuk mendapatkan statistik harian
        fun getDailyStatistics(callback: (Map<String, Double>) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            val transaksiRef = database.getReference("transaksi")

            transaksiRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dailyStats = mutableMapOf<String, Double>()

                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {
                            val transaksi = dataSnapshot.getValue(modelTransaksi::class.java)
                            if (transaksi != null && transaksi.status == modelTransaksi.STATUS_SELESAI) {
                                val date = getDateFromTimestamp(transaksi.tanggalPengambilan)
                                if (date.isNotEmpty()) {
                                    dailyStats[date] = (dailyStats[date] ?: 0.0) + transaksi.totalBayar
                                }
                            }
                        }
                    }

                    callback(dailyStats)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyMap())
                }
            })
        }
    }
}