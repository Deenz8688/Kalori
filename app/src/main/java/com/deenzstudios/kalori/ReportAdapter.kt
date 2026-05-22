package com.deenzstudios.kalori

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportAdapter(

    private val reportList:
    List<ReportData>

) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val txtReportDate:
                TextView =
            itemView.findViewById(
                R.id.txtReportDate
            )

        val txtReportWeight:
                TextView =
            itemView.findViewById(
                R.id.txtReportWeight
            )

        val txtBreakfast:
                TextView =
            itemView.findViewById(
                R.id.txtBreakfast
            )

        val txtLunch:
                TextView =
            itemView.findViewById(
                R.id.txtLunch
            )

        val txtDinner:
                TextView =
            itemView.findViewById(
                R.id.txtDinner
            )

        val txtTotalCalories:
                TextView =
            itemView.findViewById(
                R.id.txtTotalCalories
            )

        val txtBmr:
                TextView =
            itemView.findViewById(
                R.id.txtBmr
            )

        val txtTdee:
                TextView =
            itemView.findViewById(
                R.id.txtTdee
            )
    }

    override fun onCreateViewHolder(

        parent: ViewGroup,
        viewType: Int

    ): ReportViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_report,
                parent,
                false
            )

        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(

        holder: ReportViewHolder,
        position: Int

    ) {

        val report =
            reportList[position]

        holder.txtReportDate.text =
            report.date

        holder.txtReportWeight.text =
            "Berat: ${report.weight}"

        holder.txtBreakfast.text =
            "Sarapan: ${report.breakfast}"

        holder.txtLunch.text =
            "Tengah Hari: ${report.lunch}"

        holder.txtDinner.text =
            "Malam: ${report.dinner}"

        holder.txtTotalCalories.text =
            "Jumlah: ${report.total}"

        holder.txtBmr.text =
            "BMR: ${report.bmr}"

        holder.txtTdee.text =
            "TDEE: ${report.tdee}"
    }

    override fun getItemCount():
            Int {

        return reportList.size
    }
}