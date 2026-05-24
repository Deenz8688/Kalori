package com.deenzstudios.kalori

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref =
            getSharedPreferences(
                "LoginSession",
                MODE_PRIVATE
            )

        val isLoggedIn =
            sharedPref.getBoolean(
                "isLoggedIn",
                false
            )

        if (isLoggedIn) {

            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )

            finish()

            return
        }

        setContentView(R.layout.activity_login)

        val edtEmail =
            findViewById<EditText>(R.id.edtEmail)

        val edtPassword =
            findViewById<EditText>(R.id.edtPassword)

        val btnLogin =
            findViewById<Button>(R.id.btnLogin)

        val btnRegister =
            findViewById<Button>(R.id.btnRegister)

        val txtGuest =
            findViewById<TextView>(R.id.txtGuest)

        // ================= REGISTER =================

        btnRegister.setOnClickListener {

            val email =
                edtEmail.text.toString().trim()

            val password =
                edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "Sila isi email dan password",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            Thread {

                try {

                    val url =
                        URL(
                            "https://specmb.org/kalori_api/register.php"
                        )

                    val postData =
                        "email=" +
                                URLEncoder.encode(email, "UTF-8") +
                                "&password=" +
                                URLEncoder.encode(password, "UTF-8")

                    val conn =
                        url.openConnection()
                                as HttpURLConnection

                    conn.requestMethod = "POST"

                    conn.doOutput = true

                    conn.outputStream.write(
                        postData.toByteArray()
                    )

                    val response =
                        BufferedReader(
                            InputStreamReader(conn.inputStream)
                        ).readText()

                    runOnUiThread {

                        Toast.makeText(
                            this,
                            response,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (e: Exception) {

                    e.printStackTrace()

                    runOnUiThread {

                        Toast.makeText(
                            this,
                            "Register Error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }.start()
        }

        // ================= LOGIN =================

        btnLogin.setOnClickListener {

            val email =
                edtEmail.text.toString().trim()

            val password =
                edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "Sila isi email dan password",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            Thread {

                try {

                    val url =
                        URL(
                            "https://specmb.org/kalori_api/login.php"
                        )

                    val postData =
                        "email=" +
                                URLEncoder.encode(email, "UTF-8") +
                                "&password=" +
                                URLEncoder.encode(password, "UTF-8")

                    val conn =
                        url.openConnection()
                                as HttpURLConnection

                    conn.requestMethod = "POST"

                    conn.doOutput = true

                    conn.outputStream.write(
                        postData.toByteArray()
                    )

                    val response =
                        BufferedReader(
                            InputStreamReader(conn.inputStream)
                        ).readText()

                    runOnUiThread {

                        if (response.contains("success")) {

                            val userId =
                                response.substringAfter("\"user_id\":\"")
                                    .substringBefore("\"")

                            val sharedPref =
                                getSharedPreferences(
                                    "LoginSession",
                                    MODE_PRIVATE
                                )

                            val editor =
                                sharedPref.edit()

                            editor.putBoolean(
                                "isLoggedIn",
                                true
                            )

                            editor.putString(
                                "userEmail",
                                email
                            )
                            editor.putString(
                                "userId",
                                userId
                            )

                            editor.apply()

                            Thread {

                                try {

                                    val profileUrl =
                                        URL(
                                            "https://specmb.org/kalori_api/get_profile.php"
                                        )

                                    val profilePostData =
                                        "user_id=" +
                                                URLEncoder.encode(
                                                    userId,
                                                    "UTF-8"
                                                )

                                    val profileConn =
                                        profileUrl.openConnection()
                                                as HttpURLConnection

                                    profileConn.requestMethod = "POST"

                                    profileConn.doOutput = true

                                    profileConn.outputStream.write(
                                        profilePostData.toByteArray()
                                    )

                                    val profileResponse =
                                        BufferedReader(
                                            InputStreamReader(
                                                profileConn.inputStream
                                            )
                                        ).readText()

                                    if (profileResponse.contains("success")) {

                                        val profilePref =
                                            getSharedPreferences(
                                                "UserProfile",
                                                MODE_PRIVATE
                                            )

                                        val profileEditor =
                                            profilePref.edit()

                                        fun getValue(key: String): String {

                                            return profileResponse
                                                .substringAfter("\"$key\":\"")
                                                .substringBefore("\"")
                                        }

                                        profileEditor.putString(
                                            "name",
                                            getValue("full_name")
                                        )

                                        profileEditor.putString(
                                            "profile_image",
                                            getValue("profile_image")
                                        )

                                        profileEditor.putString(
                                            "activity",
                                            getValue("activity_level")
                                        )

                                        profileEditor.putString(
                                            "gender",
                                            getValue("gender")
                                        )

                                        profileEditor.putString(
                                            "age",
                                            getValue("age")
                                        )

                                        profileEditor.putString(
                                            "weight",
                                            getValue("weight")
                                        )

                                        profileEditor.putString(
                                            "height",
                                            getValue("height")
                                        )

                                        profileEditor.putString(
                                            "bmi",
                                            getValue("bmi")
                                        )

                                        val bmiValue =
                                            getValue("bmi").toDoubleOrNull() ?: 0.0

                                        val bmiStatus: String
                                        val bmiColor: Int

                                        when {

                                            bmiValue < 18.5 -> {

                                                bmiStatus = "KURUS"

                                                bmiColor =
                                                    android.graphics.Color.RED
                                            }

                                            bmiValue < 25 -> {

                                                bmiStatus = "NORMAL"

                                                bmiColor =
                                                    android.graphics.Color.parseColor(
                                                        "#4CAF50"
                                                    )
                                            }

                                            bmiValue < 30 -> {

                                                bmiStatus = "BERLEBIHAN"

                                                bmiColor =
                                                    android.graphics.Color.YELLOW
                                            }

                                            bmiValue < 35 -> {

                                                bmiStatus = "OBESITI 1"

                                                bmiColor =
                                                    android.graphics.Color.parseColor(
                                                        "#FF9800"
                                                    )
                                            }

                                            else -> {

                                                bmiStatus = "OBESITI 2"

                                                bmiColor =
                                                    android.graphics.Color.parseColor(
                                                        "#B71C1C"
                                                    )
                                            }
                                        }

                                        profileEditor.putString(
                                            "bmiStatus",
                                            bmiStatus
                                        )

                                        profileEditor.putInt(
                                            "bmiColor",
                                            bmiColor
                                        )

                                        profileEditor.putString(
                                            "bmr",
                                            getValue("bmr")
                                        )

                                        profileEditor.putString(
                                            "tdee",
                                            getValue("tdee")
                                        )

                                        profileEditor.apply()
                                    }

                                } catch (e: Exception) {

                                    e.printStackTrace()
                                }

                            }.start()
                            
                            Toast.makeText(
                                this,
                                "Login Berjaya",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(
                                Intent(
                                    this,
                                    MainActivity::class.java
                                )
                            )

                            finish()

                        } else {

                            Toast.makeText(
                                this,
                                response,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                } catch (e: Exception) {

                    e.printStackTrace()

                    runOnUiThread {

                        Toast.makeText(
                            this,
                            "Login Error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }.start()
        }

        // ================= GUEST =================

        txtGuest.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )

            finish()
        }
    }
}