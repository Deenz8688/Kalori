package com.deenzstudios.kalori

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

object GeminiHelper {

    // 🔥 Pastikan API Key sejati awak kekal dlm ni bro
    private val apiKey: String = BuildConfig.API_KEY

    // 🚀 TUKAR KEPADA MODEL GEMINI 2.5 FLASH (Kalis Traffic Sesak 503)
    private val geminiurl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

    suspend fun dapatkanKaloriDariAI(inputMenuUser: String): Food? = withContext(Dispatchers.IO) {
        try {
            val url = URL(geminiurl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val arahanPrompt = """
    Anda pakar nutrisi. Kira kalori untuk hidangan ini: "$inputMenuUser"
    
    Cara kira:
    1. Kenal pasti makanan.
    2. Anggarkan berat dalam gram berdasarkan hidangan:
    3. Guna pengetahuan umum tentang kalori makanan (contoh: nasi 130 kcal/100g, ayam goreng 250 kcal/100g, sayur 50 kcal/100g, buah 60 kcal/100g).
    4. Jika hidangan jenis "set" atau nama penuh (contoh: "nasi ayam set"), anggar kalori langsung.
    5. Kira total kalori.
    
    Output JSON sahaja: {"name":"Nama Makanan","serving":"Kuantiti User","gram":0.0,"calories":0.0}
""".trimIndent()

            val jsonRequestBody = JSONObject().apply {
                val contentsArray = org.json.JSONArray().apply {
                    val partsArray = org.json.JSONArray().apply {
                        put(JSONObject().put("text", arahanPrompt))
                    }
                    put(JSONObject().put("parts", partsArray))
                }
                put("contents", contentsArray)
            }

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(jsonRequestBody.toString())
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(responseText)

                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) return@withContext null

                val content = candidates.getJSONObject(0).optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                var rawAiText = parts?.getJSONObject(0)?.optString("text")?.trim() ?: ""

                if (rawAiText.isEmpty()) return@withContext null

                if (rawAiText.contains("```")) {
                    rawAiText = rawAiText.replace("```json", "").replace("```", "").trim()
                }

                val foodJson = JSONObject(rawAiText)

                return@withContext Food(
                    name = foodJson.optString("name", "Makanan"),
                    serving = foodJson.optString("serving", "1 hidangan"),
                    gram = foodJson.optDouble("gram", 100.0),
                    calories = foodJson.optDouble("calories", 0.0),
                    unit = "g"
                )
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.readText()
                android.util.Log.e("GEMINI_ERROR", "Server Ralat: $responseCode -> Mesej: $errorText")
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
