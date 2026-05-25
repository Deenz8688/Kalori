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

class GraphActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)


        val lineChart = findViewById<LineChart>(R.id.lineChart)
        

        // 1. Ambil data asal dari ReportManager (Sama dengan senarai Report awak)
        val allReports = ReportManager.getReports(this)

        // Bersihkan dan susun tarikh menaik (paling lama ke paling baru)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sortedReports = allReports.sortedBy { sdf.parse(it.date) }

        // Ambil maksimum 7 @ 30 data berat yang paling terbaru untuk graf harian
        val latestReports = if (sortedReports.size > 30) {
            sortedReports.takeLast(30)
        } else {
            sortedReports
        }

        val entries = ArrayList<Entry>()
        val datesList = ArrayList<String>()

        // 2. Tukar string berat "70 kg" kepada nilai Float (contoh: 70.0f) untuk Paksi Y graf
        for (i in latestReports.indices) {
            val report = latestReports[i]

            // Buang perkataan "kg" dan ambil nombor sahaja
            val weightClean = report.weight.replace("kg", "").trim().toFloatOrNull() ?: 0f

            if (weightClean > 0f) {
                entries.add(Entry(i.toFloat(), weightClean))
                datesList.add(report.date.substring(0, 5)) // Ambil format "dd/MM" sahaja supaya tak sempit dlm graf
            }
        }

        // Jika ada data berat tersimpan, jom kita lukis graf melengkung premium
        if (entries.isNotEmpty()) {
            val dataSet = LineDataSet(entries, "Berat Badan (kg)")

            // ✨ MEKAP GARISAN GRAF BIAK PREMIUM & LICIN (CUBIC)
            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Buat garisan graf jadi melengkung lembut, bukan tajam zik-zak
            dataSet.color = Color.parseColor("#4CAF50") // Warna garisan Hijau Moden
            dataSet.setCircleColor(Color.parseColor("#4CAF50")) // Warna titik data
            dataSet.lineWidth = 3f // Ketebalan garisan
            dataSet.circleRadius = 5f // Saiz titik data
            dataSet.setDrawCircleHole(true)
            dataSet.circleHoleColor = Color.WHITE
            dataSet.valueTextSize = 10f
            dataSet.valueTextColor = Color.DKGRAY

            // 🎨 KASIK EFEK GRADIENT FILL BAWAH GARISAN
            dataSet.setDrawFilled(true)
            dataSet.fillColor = Color.parseColor("#C8E6C9") // Hijau pudar pastel bawah graf
            dataSet.fillAlpha = 85

            val lineData = LineData(dataSet)
            lineChart.data = lineData

            // 📊 SETING PAKSI X (TARIKH)
            val xAxis = lineChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(datesList)
            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false) // Buang petak-petak background supaya nampak clean

            // SETING PAKSI Y (BERAT)
            lineChart.axisRight.isEnabled = false // Sorok paksi Y sebelah kanan
            lineChart.axisLeft.setDrawGridLines(true)
            lineChart.axisLeft.gridColor = Color.parseColor("#E0E0E0")

            // SETING AM (ANIMASI & ZOOM)
            lineChart.description.isEnabled = false // Buang teks info standard yang sempit
            lineChart.legend.isEnabled = true
            lineChart.animateX(1000) // Efek animasi garisan bergerak dari kiri ke kanan selama 1 saat!
            lineChart.invalidate() // Refresh graf
        } else {
            lineChart.setNoDataText("Tiada data rekod berat badan untuk dilukis.")
        }
    }
}