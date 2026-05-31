package com.deenzstudios.kalori

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Cari komponen ikut ID baru
        val edtForgotEmail = findViewById<EditText>(R.id.edtForgotEmail)
        val edtNewPassword = findViewById<TextInputEditText>(R.id.edtNewPassword)
        val edtConfirmPassword = findViewById<TextInputEditText>(R.id.edtConfirmPassword)
        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)

        btnResetPassword.setOnClickListener {
            val email = edtForgotEmail.text.toString().trim()
            val newPassword = edtNewPassword.text.toString().trim()
            val confirmPassword = edtConfirmPassword.text.toString().trim()

            // 1. Semak jika ada ruangan kosong
            if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Sila isi semua ruangan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Semak jika password baru tak sama dngan confirm password
            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Kata laluan baharu tidak sepadan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Hantar data ke server menggunakan Thread
            Thread {
                try {
                    val url = URL("https://specmb.org/kalori_api/reset_password.php")
                    val postData = "email=" + URLEncoder.encode(email, "UTF-8") +
                            "&new_password=" + URLEncoder.encode(newPassword, "UTF-8")

                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.outputStream.write(postData.toByteArray())

                    val response = BufferedReader(InputStreamReader(conn.inputStream)).readText()

                    runOnUiThread {
                        if (response.contains("success")) {
                            Toast.makeText(this, "Kata laluan berjaya ditukar!", Toast.LENGTH_LONG).show()
                            finish() // Tutup skrin ni untuk balik ke Login
                        } else {
                            Toast.makeText(this, response, Toast.LENGTH_LONG).show()
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this, "Ralat sambungan server!", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }
}