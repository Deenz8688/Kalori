package com.deenzstudios.kalori

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

    private lateinit var recyclerReport:
            RecyclerView

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

        spinnerReportFilter =
            view.findViewById(
                R.id.spinnerReportFilter
            )

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

        spinnerReportFilter.adapter =
            adapter

        fun loadFilteredReports(
            filter: String
        ) {

            val allReports =
                ReportManager.getReports(
                    requireContext()
                )

            val filteredList = mutableListOf<ReportData>()

            val today =
                java.text.SimpleDateFormat(
                    "dd/MM/yyyy",
                    java.util.Locale.getDefault()
                )

            val calendar =
                java.util.Calendar.getInstance()

            when (filter) {

                "Hari Ini" -> {

                    val todayDate =
                        today.format(calendar.time)

                    filteredList.addAll(

                        allReports.filter {

                            it.date == todayDate
                        }
                    )
                }

                "Semalam" -> {

                    calendar.add(
                        java.util.Calendar.DAY_OF_MONTH,
                        -1
                    )

                    val yesterday =
                        today.format(calendar.time)

                    filteredList.addAll(

                        allReports.filter {

                            it.date == yesterday
                        }
                    )
                }

                "Minggu Lepas" -> {

                    calendar.add(
                        java.util.Calendar.DAY_OF_MONTH,
                        -7
                    )

                    val weekAgo =
                        calendar.timeInMillis

                    filteredList.addAll(

                        allReports.filter {

                            val reportDate =
                                today.parse(it.date)

                            reportDate != null &&
                                    reportDate.time >= weekAgo
                        }
                    )
                }

                "Bulan Lepas" -> {

                    calendar.add(
                        java.util.Calendar.MONTH,
                        -1
                    )

                    val monthAgo =
                        calendar.timeInMillis

                    filteredList.addAll(

                        allReports.filter {

                            val reportDate =
                                today.parse(it.date)

                            reportDate != null &&
                                    reportDate.time >= monthAgo
                        }
                    )
                }
            }

            recyclerReport.adapter =
                ReportAdapter(filteredList)
        }
        spinnerReportFilter.onItemSelectedListener =

            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(

                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long

                ) {

                    loadFilteredReports(
                        filterList[position]
                    )
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }
            }

        // ================= RECYCLER VIEW =================

        recyclerReport =
            view.findViewById(
                R.id.recyclerReport
            )

        recyclerReport.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        
        
        val sharedPref = requireActivity()
            .getSharedPreferences(
                "UserProfile",
                android.content.Context.MODE_PRIVATE
            )

        val weight =
            sharedPref.getString(
                "weight",
                "0"
            ) + " kg"

        val bmr =
            sharedPref.getString(
                "bmr",
                "0 kcal"
            )

        val tdee =
            sharedPref.getString(
                "tdee",
                "0 kcal"
            )

        val reportList =
            ReportManager.getReports(
                requireContext()
            )

        val reportAdapter =
            ReportAdapter(reportList)

        recyclerReport.adapter =
            reportAdapter

        recyclerReport.adapter =
            reportAdapter

        return view
    }
}