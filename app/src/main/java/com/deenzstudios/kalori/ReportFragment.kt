package com.deenzstudios.kalori

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deenzstudios.kalori.data.ProfileRepository
import com.deenzstudios.kalori.data.WaterRepository
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportFragment : Fragment() {

    private lateinit var txtReportTitle: TextView
    private lateinit var btnGoToGraph: View
    private lateinit var recyclerReport: RecyclerView

    private lateinit var groupReportType: MaterialButtonToggleGroup
    private lateinit var spinnerReportFilter: Spinner

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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

        txtReportTitle = view.findViewById(R.id.txtReportTitle)
        btnGoToGraph = view.findViewById(R.id.btnGoToGraph)

        // ================= BUTANG TOGGLE (JENIS & TAPISAN) =================
        groupReportType = view.findViewById(R.id.groupReportType)

        // ================= SPINNER TAPISAN MASA =================
        spinnerReportFilter = view.findViewById(R.id.spinnerReportFilter)
        val filterList = listOf("Hari Ini", "Semalam", "Minggu Lepas", "Bulan Lepas")
        spinnerReportFilter.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            filterList
        )

        // ================= RECYCLER VIEW =================
        recyclerReport = view.findViewById(R.id.recyclerReport)
        recyclerReport.layoutManager = LinearLayoutManager(requireContext())

        spinnerReportFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                loadData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        groupReportType.addOnButtonCheckedListener { _, _, isChecked ->
            if (isChecked) loadData()
        }

        loadData()

        // ================= 🔥 HUBUNGKAN BUTANG GRAF =================
        btnGoToGraph.setOnClickListener {
            val intent = Intent(requireContext(), GraphActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    // Tapis senarai tarikh ikut pilihan masa
    private fun filterDates(filter: String, allDates: List<String>): List<String> {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        return when (filter) {
            "Hari Ini" -> {
                val todayDate = dateFormat.format(calendar.time)
                allDates.filter { it == todayDate }
            }
            "Semalam" -> {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                val yesterday = dateFormat.format(calendar.time)
                allDates.filter { it == yesterday }
            }
            "Minggu Lepas" -> {
                calendar.add(Calendar.DAY_OF_MONTH, -7)
                val from = calendar.timeInMillis
                allDates.filter {
                    val d = dateFormat.parse(it)
                    d != null && d.time >= from && d.time <= now
                }
            }
            "Bulan Lepas" -> {
                calendar.add(Calendar.MONTH, -1)
                val from = calendar.timeInMillis
                allDates.filter {
                    val d = dateFormat.parse(it)
                    d != null && d.time >= from && d.time <= now
                }
            }
            else -> allDates
        }
    }

    // Baca tapisan masa yang terpilih
    private fun currentFilter(): String =
        spinnerReportFilter.selectedItem?.toString() ?: "Hari Ini"

    // Muat data ikut JENIS (Kalori/Air) + TAPISAN masa
    private fun loadData() {
        if (!isAdded) return
        val type = if (groupReportType.checkedButtonId == R.id.buttonAir) "Air" else "Kalori"
        val filter = currentFilter()

        lifecycleScope.launch {
            if (type == "Air") {
                txtReportTitle.text = "Laporan Air"
                btnGoToGraph.visibility = View.GONE

                val allWater = WaterRepository.getAll(requireContext())
                val map = allWater.associateBy { it.date }
                val list = filterDates(filter, allWater.map { it.date })
                    .sortedByDescending { dateFormat.parse(it) }
                    .mapNotNull { map[it] }

                val targetMl = ProfileRepository.getProfile(requireContext())?.waterTargetMl
                    ?: WaterRepository.DEFAULT_TARGET_ML
                recyclerReport.adapter = WaterReportAdapter(list, targetMl)
            } else {
                txtReportTitle.text = "Laporan Kalori"
                btnGoToGraph.visibility = View.VISIBLE

                val allReports = ReportManager.getReports(requireContext())
                val map = allReports.associateBy { it.date }
                val list = filterDates(filter, allReports.map { it.date })
                    .sortedByDescending { dateFormat.parse(it) }
                    .mapNotNull { map[it] }

                recyclerReport.adapter = ReportAdapter(list)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!::recyclerReport.isInitialized) return

        lifecycleScope.launch {
            val context = requireContext()

            // ⭐ 1. KEMASKINI DATA LAPORAN KALORI HARI INI SEBELUM REFRESH SKRIN
            val todayDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Calendar.getInstance().time)
            val allReports = ReportManager.getReports(context)

            val todayReport = allReports.find { it.date == todayDateStr }

            if (todayReport != null) {
                val profile = ProfileRepository.getProfile(context)
                val freshWeight = (profile?.weight?.ifEmpty { "0" } ?: "0") + " kg"
                val freshBmr = profile?.bmr?.ifEmpty { "0 kcal" } ?: "0 kcal"
                val freshTdee = profile?.tdee?.ifEmpty { "0 kcal" } ?: "0 kcal"

                val updatedReport = ReportData(
                    todayReport.date,
                    freshWeight,
                    todayReport.breakfast,
                    todayReport.lunch,
                    todayReport.dinner,
                    todayReport.total,
                    freshBmr,
                    freshTdee
                )

                ReportManager.saveReport(context, updatedReport)
            }

            // ⭐ 2. REFRESH PAPARAN KAD
            loadData()
        }
    }
}
