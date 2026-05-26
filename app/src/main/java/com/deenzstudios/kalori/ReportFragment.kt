package com.deenzstudios.kalori

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.AdapterView

class ReportFragment : Fragment() {

    private lateinit var spinnerReportFilter: Spinner
    private lateinit var recyclerReport: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.fragment_report,
            container,
            false
        )

        // ================= SPINNER =================
        spinnerReportFilter = view.findViewById(R.id.spinnerReportFilter)

        val filterList = listOf(
            "Hari Ini",
            "Semalam",
            "Minggu Lepas",
            "Bulan Lepas"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            filterList
        )

        spinnerReportFilter.adapter = adapter

        fun loadFilteredReports(filter: String) {
            val allReports = ReportManager.getReports(requireContext())
            val filteredList = mutableListOf<ReportData>()
            val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val calendar = java.util.Calendar.getInstance()

            when (filter) {
                "Hari Ini" -> {
                    val todayDate = today.format(calendar.time)
                    filteredList.addAll(
                        allReports.filter { it.date == todayDate }
                    )
                }

                "Semalam" -> {
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
                    val yesterday = today.format(calendar.time)
                    filteredList.addAll(
                        allReports.filter { it.date == yesterday }
                    )
                }

                "Minggu Lepas" -> {
                    val currentMillis = calendar.timeInMillis
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, -7)
                    val weekAgo = calendar.timeInMillis

                    filteredList.addAll(
                        allReports.filter {
                            val reportDate = today.parse(it.date)
                            reportDate != null && reportDate.time >= weekAgo && reportDate.time <= currentMillis
                        }
                    )
                }

                "Bulan Lepas" -> {
                    val currentMillis = calendar.timeInMillis
                    calendar.add(java.util.Calendar.MONTH, -1)
                    val monthAgo = calendar.timeInMillis

                    filteredList.addAll(
                        allReports.filter {
                            val reportDate = today.parse(it.date)
                            reportDate != null && reportDate.time >= monthAgo && reportDate.time <= currentMillis
                        }
                    )
                }
            }

            recyclerReport.adapter = ReportAdapter(filteredList)
        }

        spinnerReportFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadFilteredReports(filterList[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // ================= RECYCLER VIEW =================
        recyclerReport = view.findViewById(R.id.recyclerReport)
        recyclerReport.layoutManager = LinearLayoutManager(requireContext())

        loadFilteredReports("Hari Ini")

        // ================= 🔥 HUBUNGKAN BUTANG GRAF BARU =================
        val btnGoToGraph = view.findViewById<View>(R.id.btnGoToGraph)
        btnGoToGraph.setOnClickListener {
            // Logik buka skrin GraphActivity
            val intent = Intent(requireContext(), GraphActivity::class.java)
            startActivity(intent)
        }

        return view
    }
    override fun onResume() {
        super.onResume()

        val context = requireContext()
        // Pastikan nama ID spinnerReportFilter ini sebiji ikut XML fragment_report awak
        val viewFilter = view?.findViewById<Spinner>(R.id.spinnerReportFilter)

        if (viewFilter != null) {
            // ⭐ 1. KEMASKINI DATABASE DULU SEBELUM REFRESH SKRIN DISPLAY
            val todayDateStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Calendar.getInstance().time)
            val allReports = ReportManager.getReports(context)

            // Cari rekod hari ini dlm fail data laporan
            val todayReport = allReports.find { it.date == todayDateStr }

            if (todayReport != null) {
                // Sedut berat terkini yang baru diubah dlm profile tadi
                val profilePref = context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
                val freshWeight = profilePref.getString("weight", "0") + " kg"
                val freshBmr = profilePref.getString("bmr", "0 kcal") ?: "0 kcal"
                val freshTdee = profilePref.getString("tdee", "0 kcal") ?: "0 kcal"

                // Cipta data laporan segar untuk hari ini dengan kandungan kalori makanan asal yang sedia ada
                val updatedReport = ReportData(
                    todayReport.date,
                    freshWeight, // 🔥 Tindih berat lama dengan berat profil baru!
                    todayReport.breakfast,
                    todayReport.lunch,
                    todayReport.dinner,
                    todayReport.total,
                    freshBmr,
                    freshTdee
                )

                // Paksa ReportManager tulis data segar ni masuk ke fail memori phone secara kekal
                ReportManager.saveReport(context, updatedReport)
            }

            // ⭐ 2. SIMULASI SENTUHAN SPINNER UNTUK REFRESH PAPARAN KAD DI SKRIN UI
            val currentFilter = viewFilter.selectedItem.toString()
            viewFilter.postDelayed({
                val position = (viewFilter.adapter as? ArrayAdapter<String>)?.getPosition(currentFilter) ?: 0
                viewFilter.onItemSelectedListener?.onItemSelected(viewFilter, viewFilter.selectedView, position, position.toLong())
            }, 200)
        }
    }
    
}