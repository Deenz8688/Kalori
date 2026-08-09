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
            Kira kalori tepat: "$inputMenuUser"
            Output JSON: {"name":"Nama","serving":"Teks User","gram":0.0,"calories":0.0}
            """.trimIndent()

            val jsonRequestBody = JSONObject().apply {
                val contentsArray = org.json.JSONArray().apply {
                    val partsArray = org.json.JSONArray().apply {
                        put(JSONObject().put("text", arahanPrompt))
                    }
                    put(JSONObject().put("parts", partsArray))
                }
                put("contents", contentsArray)
                // 🔥 TAMBAH INI UNTUK SET TEMPERATURE = 0
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.0)
                })
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
                    serving = foodJson.optString("serving", inputMenuUser),
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

    suspend fun analisisGambarMakananAI(bitmap: android.graphics.Bitmap): Food? = withContext(Dispatchers.IO) {
        try {
            val url = URL(geminiurl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            // Convert Bitmap to Base64 - Naikkan kualiti ke 80% untuk lebih detail
            val byteArrayOutputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val encodedImage = android.util.Base64.encodeToString(byteArrayOutputStream.toByteArray(), android.util.Base64.NO_WRAP)

            val arahanPrompt = """
            Anda adalah Pakar Nutrisi Malaysia yang sangat teliti. Tugas anda adalah mengenalpasti menu dalam gambar dengan tepat.
            
            Sila ikut langkah analisis ini:
            1. Lihat tekstur protein: Adakah ia mempunyai urat daging ayam atau tekstur lembut ikan? Perhatikan bentuk tulang atau kulit.
            2. Lihat bahan sampingan: Kari ikan biasanya mempunyai bendi/terung. Kari ayam biasanya mempunyai kentang.
            3. Jika kuah terlalu pekat, buat anggaran paling logik berdasarkan bentuk potongan objek.
            
            Berikan hasil dalam format JSON SAHAJA:
            {
              "name": "Nama Makanan Spesifik",
              "serving": "Anggaran Berat (PROTEIN: 0g, KARBOHIDRAT: 0g, LEMAK: 0g)",
              "gram": 0.0,
              "calories": 0.0
            }
            
            PENTING: Gunakan ejaan penuh PROTEIN, KARBOHIDRAT, dan LEMAK dalam ruangan 'serving'.
            """.trimIndent()

            val jsonRequestBody = JSONObject().apply {
                val contentsArray = org.json.JSONArray().apply {
                    val partsArray = org.json.JSONArray().apply {
                        put(JSONObject().put("text", arahanPrompt))
                        put(JSONObject().apply {
                            put("inline_data", JSONObject().apply {
                                put("mime_type", "image/jpeg")
                                put("data", encodedImage)
                            })
                        })
                    }
                    put(JSONObject().put("parts", partsArray))
                }
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.0)
                })
            }

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(jsonRequestBody.toString())
            writer.flush()
            writer.close()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(responseText)
                val candidates = jsonResponse.optJSONArray("candidates") ?: return@withContext null
                val rawAiText = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text").trim()

                var cleanJson = rawAiText
                if (cleanJson.contains("```")) {
                    cleanJson = cleanJson.replace("```json", "").replace("```", "").trim()
                }

                val foodJson = JSONObject(cleanJson)
                return@withContext Food(
                    name = foodJson.optString("name", "Makanan"),
                    serving = foodJson.optString("serving", "1 hidangan"),
                    gram = foodJson.optDouble("gram", 100.0),
                    calories = foodJson.optDouble("calories", 0.0),
                    unit = "g"
                )
            }
            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
