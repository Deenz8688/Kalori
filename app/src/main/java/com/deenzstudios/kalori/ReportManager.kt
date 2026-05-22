package com.deenzstudios.kalori

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ReportManager {

    fun saveReport(

        context: Context,

        reportData: ReportData

    ) {


        val sharedPref =
            context.getSharedPreferences(
                "ReportHistory",
                Context.MODE_PRIVATE
            )

        val oldData =
            sharedPref.getString(
                "report_list",
                "[]"
            )

        val jsonArray =
            JSONArray(oldData)

        val jsonObject =
            JSONObject()

        jsonObject.put(
            "date",
            reportData.date
        )

        jsonObject.put(
            "weight",
            reportData.weight
        )

        jsonObject.put(
            "breakfast",
            reportData.breakfast
        )

        jsonObject.put(
            "lunch",
            reportData.lunch
        )

        jsonObject.put(
            "dinner",
            reportData.dinner
        )

        jsonObject.put(
            "total",
            reportData.total
        )

        jsonObject.put(
            "bmr",
            reportData.bmr
        )

        jsonObject.put(
            "tdee",
            reportData.tdee
        )

        var updated = false

        for (i in 0 until jsonArray.length()) {

            val obj =
                jsonArray.getJSONObject(i)

            if (
                obj.getString("date")
                ==
                reportData.date
            ) {

                jsonArray.put(i, jsonObject)

                updated = true

                break
            }
        }

        if (!updated) {

            jsonArray.put(jsonObject)
        }

        sharedPref.edit()
            .putString(
                "report_list",
                jsonArray.toString()
            )
            .apply()
    }
    fun getReports(
        context: Context
    ): List<ReportData> {

        val sharedPref =
            context.getSharedPreferences(
                "ReportHistory",
                Context.MODE_PRIVATE
            )

        val savedData =
            sharedPref.getString(
                "report_list",
                "[]"
            )

        val jsonArray =
            JSONArray(savedData)

        val reportList =
            mutableListOf<ReportData>()

        for (i in 0 until jsonArray.length()) {

            val obj =
                jsonArray.getJSONObject(i)

            reportList.add(

                ReportData(

                    obj.getString("date"),

                    obj.getString("weight"),

                    obj.getString("breakfast"),

                    obj.getString("lunch"),

                    obj.getString("dinner"),

                    obj.getString("total"),

                    obj.getString("bmr"),

                    obj.getString("tdee")
                )
            )
        }

        return reportList.reversed()
    }
}