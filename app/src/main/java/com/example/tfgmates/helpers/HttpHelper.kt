package com.example.tfgmates.helpers

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object HttpHelper {

    // Función para enviar una solicitud HTTP que espera respuesta
    fun sendHttpRequest(url: String, jsonBody: JSONObject, callback: (String?) -> Unit) {
        val client = OkHttpClient()
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("HTTP_ERROR", "Falló la llamada HTTP: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseData ->
                    callback(responseData)
                } ?: callback(null)
            }
        })
    }

    // Función para enviar una solicitud HTTP sin esperar respuesta (notificación)
    fun sendHttpNotice(url: String, jsonBody: JSONObject, callback: (Boolean) -> Unit) {
        val client = OkHttpClient()
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(false)  // Si falla la solicitud, pasamos false al callback
            }

            override fun onResponse(call: Call, response: Response) {
                // No procesamos la respuesta, solo aseguramos que la solicitud fue exitosa
                callback(response.isSuccessful)  // Si la respuesta es exitosa, pasamos true al callback
            }
        })
    }

    fun getHttpText(url: String, callback: (String?) -> Unit) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("HTTP_ERROR", "Falló GET: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseData ->
                    callback(responseData)
                } ?: callback(null)
            }
        })
    }
}