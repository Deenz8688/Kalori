package com.deenzstudios.kalori

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class MeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_me, container, false)

        val edtWeight = view.findViewById<EditText>(R.id.edtWeight)
        val edtName = view.findViewById<EditText>(R.id.edtName)
        val edtHeight = view.findViewById<EditText>(R.id.edtHeight)
        val edtAge = view.findViewById<EditText>(R.id.edtAge)
        val spinnerActivity = view.findViewById<Spinner>(R.id.spinnerActivity)

        val radioMale = view.findViewById<RadioButton>(R.id.radioMale)
        val radioFemale = view.findViewById<RadioButton>(R.id.radioFemale)

        val btnCalculate = view.findViewById<Button>(R.id.btnCalculate)



        val profileLayout = view.findViewById<LinearLayout>(R.id.profileLayout)
        val formLayout = view.findViewById<LinearLayout>(R.id.formLayout)
        val txtProfileName = view.findViewById<TextView>(R.id.txtProfileName)
        val txtProfileWeight = view.findViewById<TextView>(R.id.txtProfileWeight)
        val txtProfileHeight = view.findViewById<TextView>(R.id.txtProfileHeight)
        val txtProfileAge = view.findViewById<TextView>(R.id.txtProfileAge)
        val txtProfileGender = view.findViewById<TextView>(R.id.txtProfileGender)
        val txtProfileActivity = view.findViewById<TextView>(R.id.txtProfileActivity)
        val txtProfileBMI = view.findViewById<TextView>(R.id.txtProfileBMI)
        val txtProfileBMR = view.findViewById<TextView>(R.id.txtProfileBMR)
        val txtProfileTDEE = view.findViewById<TextView>(R.id.txtProfileTDEE)

        val btnEdit = view.findViewById<Button>(R.id.btnEdit)

        val sharedPref = requireActivity().getSharedPreferences(
            "UserProfile",
            android.content.Context.MODE_PRIVATE
        )

        val activityLevels = arrayOf(
            "Tidak Aktif",
            "Ringan",
            "Sederhana",
            "Aktif",
            "Sangat Aktif"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            activityLevels
        )

        spinnerActivity.adapter = adapter

        val savedName = sharedPref.getString("name", null)

        if (savedName != null) {

            profileLayout.visibility = View.VISIBLE
            formLayout.visibility = View.GONE

            txtProfileName.text = "Nama: " + sharedPref.getString("name", "")
            val savedBmi = sharedPref.getString("bmi", "")
            val savedStatus = sharedPref.getString("bmiStatus", "")
            val savedColor = sharedPref.getInt(
                "bmiColor",
                android.graphics.Color.BLACK
            )

            txtProfileBMI.text =
                "BMI: $savedBmi ($savedStatus)"

            txtProfileBMI.setTextColor(savedColor)
            txtProfileBMR.text = "BMR: " + sharedPref.getString("bmr", "")
            txtProfileTDEE.text = "TDEE: " + sharedPref.getString("tdee", "")
            txtProfileWeight.text = "Berat: " + sharedPref.getString("weight", "") + " kg"
            txtProfileHeight.text = "Tinggi: " + sharedPref.getString("height", "") + " cm"
            txtProfileAge.text = "Umur: " + sharedPref.getString("age", "") + " Tahun"
            txtProfileGender.text = "Jantina: " + sharedPref.getString("gender", "")
            txtProfileActivity.text = "Aktiviti: " + sharedPref.getString("activity", "")
        }

        btnCalculate.setOnClickListener {

            val weight = edtWeight.text.toString().toDoubleOrNull()
            val height = edtHeight.text.toString().toDoubleOrNull()
            val age = edtAge.text.toString().toIntOrNull()
            val name = edtName.text.toString()

            if (name.isNotEmpty() && weight != null && height != null && age != null) {

                // BMI
                val heightMeter = height / 100
                val bmi = weight / (heightMeter * heightMeter)


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


                    val multiplier = when (spinnerActivity.selectedItem.toString()) {

                        "Tidak Aktif" -> 1.2
                        "Ringan" -> 1.375
                        "Sederhana" -> 1.55
                        "Aktif" -> 1.725
                        else -> 1.9
                    }

                    val tdee = bmr * multiplier


                    Toast.makeText(
                        requireContext(),
                        "Profile berjaya disimpan",
                        Toast.LENGTH_SHORT
                    ).show()

                    profileLayout.visibility = View.VISIBLE
                    formLayout.visibility = View.GONE

                    txtProfileName.text = "Nama: $name"
                    val gender = if (radioMale.isChecked) "Lelaki" else "Perempuan"

                    txtProfileWeight.text = "Berat: $weight kg"
                    txtProfileHeight.text = "Tinggi: $height cm"
                    txtProfileAge.text = "Umur: $age Tahun"
                    txtProfileGender.text = "Jantina: $gender"
                    txtProfileActivity.text = "Aktiviti: ${spinnerActivity.selectedItem}"
                    val bmiStatus: String
                    val bmiColor: Int

                    when {

                        bmi < 18.5 -> {

                            bmiStatus = "KURUS"
                            bmiColor = android.graphics.Color.RED
                        }

                        bmi < 25 -> {

                            bmiStatus = "NORMAL"
                            bmiColor = android.graphics.Color.parseColor("#4CAF50")
                        }

                        bmi < 30 -> {

                            bmiStatus = "BERLEBIHAN"
                            bmiColor = android.graphics.Color.YELLOW
                        }

                        bmi < 35 -> {

                            bmiStatus = "OBESITI 1"
                            bmiColor = android.graphics.Color.parseColor("#FF9800")
                        }

                        else -> {

                            bmiStatus = "OBESITI 2"
                            bmiColor = android.graphics.Color.parseColor("#B71C1C")
                        }
                    }

                    txtProfileBMI.text =
                        "BMI: %.2f".format(bmi) + " ($bmiStatus)"

                    txtProfileBMI.setTextColor(bmiColor)
                    txtProfileBMR.text = "BMR: %.0f kcal".format(bmr)
                    txtProfileTDEE.text = "TDEE: %.0f kcal".format(tdee)
                    sharedPref.edit()
                        .putString("name", name)
                        .putString("bmi", "%.2f".format(bmi))
                        .putString("bmiStatus", bmiStatus)
                        .putInt("bmiColor", bmiColor)
                        .putString("bmr", "%.0f kcal".format(bmr))
                        .putString("tdee", "%.0f kcal".format(tdee))
                        .putString("weight", weight.toString())
                        .putString("height", height.toString())
                        .putString("age", age.toString())
                        .putString("gender", gender)
                        .putString("activity", spinnerActivity.selectedItem.toString())

                        .apply()
                } else {

                }
            }
        }
        btnEdit.setOnClickListener {

            profileLayout.visibility = View.GONE
            formLayout.visibility = View.VISIBLE
        }

        return view
    }
}