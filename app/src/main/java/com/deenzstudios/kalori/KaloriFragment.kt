package com.deenzstudios.kalori

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.io.BufferedReader
import java.io.InputStreamReader
import android.text.TextWatcher

class KaloriFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        val view =
            inflater.inflate(
                R.layout.fragment_kalori,
                container,
                false
            )

        // ================= RINGKASAN =================

        val txtTdee =
            view.findViewById<TextView>(
                R.id.txtTdee
            )

        val txtBmr =
            view.findViewById<TextView>(
                R.id.txtBmr
            )

        val txtTotalCalories =
            view.findViewById<TextView>(
                R.id.txtTotalCalories
            )

        val txtBalance =
            view.findViewById<TextView>(
                R.id.txtBalance
            )

        // ================= SARAPAN =================

        val edtBreakfastFood =
            view.findViewById<AutoCompleteTextView>(
                R.id.edtBreakfastFood
            )

        val edtBreakfastAmount =
            view.findViewById<AutoCompleteTextView>(
                R.id.edtBreakfastAmount
            )

        val btnAddBreakfast =
            view.findViewById<Button>(
                R.id.btnAddBreakfast
            )

        val txtBreakfastList =
            view.findViewById<TextView>(
                R.id.txtBreakfastList
            )

        val txtBreakfastCalories =
            view.findViewById<TextView>(
                R.id.txtBreakfastCalories
            )

        val txtBreakfastTotal =
            view.findViewById<TextView>(
                R.id.txtBreakfastTotal
            )

        val radioBreakfastGram =
            view.findViewById<RadioButton>(
                R.id.radioBreakfastGram
            )

        val radioBreakfastServing =
            view.findViewById<RadioButton>(
                R.id.radioBreakfastServing
            )

        // ================= SHARED PREF =================

        val sharedPref =
            requireActivity().getSharedPreferences(
                "UserProfile",
                Context.MODE_PRIVATE
            )

        // ================= PROFILE =================

        val savedTdee =
            sharedPref.getString(
                "tdee",
                "0 kcal"
            )

        val savedBmr =
            sharedPref.getString(
                "bmr",
                "0 kcal"
            )

        txtTdee.text =
            "TDEE: $savedTdee"

        txtBmr.text =
            "BMR: $savedBmr"

        txtTotalCalories.text =
            "Jumlah Kalori: 0 kcal"

        txtBalance.text =
            "Baki Kalori: $savedTdee"

        // ================= FOOD LIST =================

        val foodList =
            mutableListOf<Food>()

        try {

            val inputStream =
                requireContext()
                    .assets
                    .open(
                        "bank_kalori_makanan.csv"
                    )

            val reader =
                BufferedReader(
                    InputStreamReader(
                        inputStream
                    )
                )

            // Skip header
            reader.readLine()

            var line: String?

            while (
                reader.readLine()
                    .also { line = it } != null
            ) {

                val row = line!!.split(",")

                if (row.size >= 4) {

                    val food =
                        Food(

                            name =
                                row[0].trim(),

                            serving =
                                row[1].trim(),

                            gram =
                                row[2]
                                    .trim()
                                    .toDoubleOrNull()
                                    ?: 0.0,

                            calories =
                                row[3]
                                    .trim()
                                    .toDoubleOrNull()
                                    ?: 0.0
                        )

                    foodList.add(food)
                    println(food)
                }
            }

            reader.close()

        } catch (e: Exception) {

            e.printStackTrace()
        }

        // ================= AUTO COMPLETE =================

        val foodNames =
            foodList.map { it.name }

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                foodNames
            )

        edtBreakfastFood.setAdapter(
            adapter
        )
        edtBreakfastFood.addTextChangedListener(

            object : android.text.TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    val searchText =
                        s.toString().trim()

                    val foundFood =
                        foodList.find {

                            it.name.equals(
                                searchText,
                                ignoreCase = true
                            )
                        }

                    if (
                        foundFood != null &&
                        radioBreakfastServing.isChecked
                    ) {

                        edtBreakfastAmount.setText(
                            foundFood.serving
                        )
                    }
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {
                }
            }
        )

        edtBreakfastFood.threshold = 1

        // ================= TAMBAH MAKANAN =================

        var breakfastTotal = 0.0

        var breakfastText = ""

        radioBreakfastGram.setOnClickListener {

            edtBreakfastAmount.setText("")

            edtBreakfastAmount.hint =
                "Masukkan gram"

            edtBreakfastAmount.inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        radioBreakfastServing.setOnClickListener {

            val searchText =
                edtBreakfastFood.text
                    .toString()
                    .trim()

            val foundFood =
                foodList.find {

                    it.name.equals(
                        searchText,
                        ignoreCase = true
                    )
                }

            if (foundFood != null) {

                val servingList =
                    listOf(foundFood.serving)

                val servingAdapter =
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        servingList
                    )

                edtBreakfastAmount.setAdapter(
                    servingAdapter
                )

                edtBreakfastAmount.setText(
                    foundFood.serving
                )

                edtBreakfastAmount.inputType = 0
            }
        }

        btnAddBreakfast.setOnClickListener {

            val searchText =
                edtBreakfastFood.text
                    .toString()
                    .trim()

            var amount = 0.0
                edtBreakfastAmount.text
                    .toString()
                    .toDoubleOrNull()
                    ?: 0.0

            val foundFood =
                foodList.find {

                    it.name.equals(
                        searchText,
                        ignoreCase = true
                    )
                }

            if (foundFood != null) {

                var calories = 0.0

                // ================= GRAM =================

                if (radioBreakfastGram.isChecked) {

                    amount =
                        edtBreakfastAmount.text
                            .toString()
                            .toDoubleOrNull()
                            ?: 0.0

                    calories =
                        (amount / foundFood.gram) *
                                foundFood.calories

                }

                // ================= HIDANGAN =================

                else {



                    amount = 1.0

                    calories =
                        amount *
                                foundFood.calories
                }

                // ================= TOTAL =================

                breakfastTotal += calories

                // ================= LIST =================

                breakfastText +=
                    "\n• ${foundFood.name}" +
                            " (${amount})" +
                            " = %.0f kcal"
                                .format(calories)

                txtBreakfastList.text =
                    breakfastText

                // ================= DISPLAY =================

                txtBreakfastCalories.text =
                    "Kalori: %.0f kcal"
                        .format(calories)

                txtBreakfastTotal.text =
                    "Jumlah Sarapan: %.0f kcal"
                        .format(breakfastTotal)

                txtTotalCalories.text =
                    "Jumlah Kalori: %.0f kcal"
                        .format(breakfastTotal)

                // ================= BAKI =================

                val tdeeValue =
                    savedTdee
                        ?.replace(
                            "kcal",
                            ""
                        )
                        ?.trim()
                        ?.toDoubleOrNull()
                        ?: 0.0

                val balance =
                    tdeeValue -
                            breakfastTotal

                txtBalance.text =
                    "Baki Kalori: %.0f kcal"
                        .format(balance)

                // ================= CLEAR =================

                edtBreakfastFood.setText("")

                edtBreakfastAmount.setText("")
            }
        }

        return view
    }
}