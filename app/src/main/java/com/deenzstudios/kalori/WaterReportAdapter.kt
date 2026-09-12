package com.deenzstudios.kalori

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.deenzstudios.kalori.data.WaterEntity

/**
 * Adapter senarai laporan AIR dalam menu Laporan.
 */
class WaterReportAdapter(
    private val list: List<WaterEntity>,
    private val targetMl: Int
) : RecyclerView.Adapter<WaterReportAdapter.WaterViewHolder>() {

    class WaterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtWaterReportDate: TextView = itemView.findViewById(R.id.txtWaterReportDate)
        val txtWaterReportAmount: TextView = itemView.findViewById(R.id.txtWaterReportAmount)
        val txtWaterReportPercent: TextView = itemView.findViewById(R.id.txtWaterReportPercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_water, parent, false)
        return WaterViewHolder(view)
    }

    override fun onBindViewHolder(holder: WaterViewHolder, position: Int) {
        val item = list[position]
        val target = if (targetMl <= 0) 3000 else targetMl
        val percent = item.consumedMl * 100f / target

        holder.txtWaterReportDate.text = item.date
        holder.txtWaterReportAmount.text =
            "💧 %.2f L / %.2f L".format(item.consumedMl / 1000f, target / 1000f)

        holder.txtWaterReportPercent.text = "%.0f%% daripada sasaran".format(percent)
        holder.txtWaterReportPercent.setTextColor(
            when {
                percent >= 100f -> Color.parseColor("#4CAF50") // hijau — capai sasaran
                percent >= 50f -> Color.parseColor("#F0583A")  // oren — separuh jalan
                else -> Color.parseColor("#E53935")            // merah — kurang minum
            }
        )
    }

    override fun getItemCount(): Int = list.size
}
