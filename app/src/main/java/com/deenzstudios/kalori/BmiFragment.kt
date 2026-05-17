package com.deenzstudios.kalori

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class BmiFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_bmi, container, false)

        val edtWeight = view.findViewById<EditText>(R.id.edtWeight)
        val edtHeight = view.findViewById<EditText>(R.id.edtHeight)
        val edtAge = view.findViewById<EditText>(R.id.edtAge)

        val radioMale = view.findViewById<RadioButton>(R.id.radioMale)
        val radioFemale = view.findViewById<RadioButton>(R.id.radioFemale)

        val btnCalculate = view.findViewById<Button>(R.id.btnCalculate)

        val txtResult = view.findViewById<TextView>(R.id.txtResult)
        val txtBmrResult = view.findViewById<TextView>(R.id.txtBmrResult)

        btnCalculate.setOnClickListener {

            val weight = edtWeight.text.toString().toDoubleOrNull()
            val height = edtHeight.text.toString().toDoubleOrNull()
            val age = edtAge.text.toString().toIntOrNull()

            if (weight != null && height != null && age != null) {

                // BMI
                val heightMeter = height / 100
                val bmi = weight / (heightMeter * heightMeter)

                txtResult.text = "BMI Anda: %.2f".format(bmi)

                // BMR ikut jantina
                val bmr = if (radioMale.isChecked) {

                    // Lelaki
                    88.36 + (13.4 * weight) + (4.8 * height) - (5.7 * age)

                } else if (radioFemale.isChecked) {

                    // Perempuan
                    447.6 + (9.2 * weight) + (3.1 * height) - (4.3 * age)

                } else {

                    0.0
                }

                if (bmr > 0) {

                    txtBmrResult.text = "BMR Anda: %.0f kcal".format(bmr)

                } else {

                    txtBmrResult.text = "Sila pilih jantina"
                }

            } else {

                txtResult.text = "Sila masukkan data"
                txtBmrResult.text = ""
            }
        }

        return view
    }
}