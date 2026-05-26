package com.deenzstudios.kalori

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.view.View
import android.widget.AdapterView

class GraphActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)


        val lineChart = findViewById<LineChart>(R.id.lineChart)

        // ================= 🔥 1. ISI DATA DROP DOWN SPINNER =================
        val spinnerGraphFilter = findViewById<Spinner>(R.id.spinnerGraphFilter)
        val filterList = listOf("Minggu Ini", "Minggu Lepas", "Bulan Ini", "Bulan Lepas")

        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filterList)
        spinnerGraphFilter.adapter = filterAdapter

        // ================= 🔥 2. FUNGSI DINAMIK UNTUK TAPIS & LUKIS =================
        fun loadGraphByFilter(filter: String) {
            val allReports = ReportManager.getReports(this)
            val filteredList = mutableListOf<ReportData>()

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()

            when (filter) {
                "Minggu Ini" -> {
                    val currentMillis = calendar.timeInMillis
                    calendar.add(Calendar.DAY_OF_MONTH, -7)
                    val weekAgo = calendar.timeInMillis

                    filteredList.addAll(allReports.filter {
                        val reportDate = sdf.parse(it.date)
                        reportDate != null && reportDate.time >= weekAgo && reportDate.time <= currentMillis
                    })
                }

                "Minggu Lepas" -> {
                    calendar.add(Calendar.DAY_OF_MONTH, -7)
                    val endOfLastWeek = calendar.timeInMillis
                    calendar.add(Calendar.DAY_OF_MONTH, -7)
                    val startOfLastWeek = calendar.timeInMillis

                    filteredList.addAll(allReports.filter {
                        val reportDate = sdf.parse(it.date)
                        reportDate != null && reportDate.time >= startOfLastWeek && reportDate.time <= endOfLastWeek
                    })
                }

                "Bulan Ini" -> {
                    val currentMillis = calendar.timeInMillis
                    calendar.add(Calendar.MONTH, -1)
                    val monthAgo = calendar.timeInMillis

                    filteredList.addAll(allReports.filter {
                        val reportDate = sdf.parse(it.date)
                        reportDate != null && reportDate.time >= monthAgo && reportDate.time <= currentMillis
                    })
                }

                "Bulan Lepas" -> {
                    calendar.add(Calendar.MONTH, -1)
                    val endOfLastMonth = calendar.timeInMillis
                    calendar.add(Calendar.MONTH, -1)
                    val startOfLastMonth = calendar.timeInMillis

                    filteredList.addAll(allReports.filter {
                        val reportDate = sdf.parse(it.date)
                        reportDate != null && reportDate.time >= startOfLastMonth && reportDate.time <= endOfLastMonth
                    })
                }
            }

            // Susun tarikh dari lama ke baru
            val sortedReports = filteredList.sortedBy { sdf.parse(it.date) }

            val entries = ArrayList<Entry>()
            val datesList = ArrayList<String>()

            // Tukar string berat kepada Float
            for (i in sortedReports.indices) {
                val report = sortedReports[i]
                val weightClean = report.weight
                    .replace("kg", "")
                    .replace("Berat:", "")
                    .trim()
                    .toFloatOrNull() ?: 0f

                if (weightClean > 0f) {
                    entries.add(Entry(i.toFloat(), weightClean))
                    datesList.add(report.date.substring(0, 5)) // Ambil format "dd/MM"
                }
            }

            // ================= 🔥 3. LUKIS GRAF BERDASARKAN HASIL TAPISAN =================
            if (entries.isNotEmpty()) {
                val dataSet = LineDataSet(entries, "Berat Badan (kg)")

                // Kekalkan reka bentuk premium asal awak
                dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                dataSet.color = Color.parseColor("#4CAF50")
                dataSet.setCircleColor(Color.parseColor("#4CAF50"))
                dataSet.lineWidth = 3f
                dataSet.circleRadius = 5f
                dataSet.setDrawCircleHole(true)
                dataSet.circleHoleColor = Color.WHITE
                dataSet.valueTextSize = 10f
                dataSet.valueTextColor = Color.DKGRAY

                dataSet.setDrawFilled(true)
                dataSet.fillColor = Color.parseColor("#C8E6C9")
                dataSet.fillAlpha = 85

                val lineData = LineData(dataSet)
                lineChart.data = lineData

                // SETING PAKSI X (TARIKH ASAL AWAK)
                val xAxis = lineChart.xAxis
                xAxis.valueFormatter = IndexAxisValueFormatter(datesList)
                xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)
                xAxis.spaceMin = 0.5f
                xAxis.spaceMax = 0.5f

                // SETING PAKSI Y (NOMBOR BULAT ASAL AWAK)
                lineChart.axisRight.isEnabled = false
                val leftAxis = lineChart.axisLeft
                leftAxis.setDrawGridLines(true)
                leftAxis.gridColor = Color.parseColor("#E0E0E0")
                leftAxis.granularity = 1f
                leftAxis.isGranularityEnabled = true
                leftAxis.axisMinimum = 55f
                leftAxis.axisMaximum = 65f
                leftAxis.setLabelCount(11, true)
                leftAxis.valueFormatter = com.github.mikephil.charting.formatter.DefaultAxisValueFormatter(0)

                // SETING AM
                lineChart.description.isEnabled = false
                lineChart.legend.isEnabled = true
                lineChart.animateX(1000)
            } else {
                lineChart.clear() // Bersihkan graf jika tiada data dlm julat tarikh tersebut
                lineChart.setNoDataText("Tiada data rekod berat badan untuk dilukis.")
            }
            lineChart.invalidate()
        }

        // ================= 🔥 4. AKSI APABILA DROPDOWN DIPILIH =================
        spinnerGraphFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadGraphByFilter(filterList[position]) // Panggil fungsi di atas secara live
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}