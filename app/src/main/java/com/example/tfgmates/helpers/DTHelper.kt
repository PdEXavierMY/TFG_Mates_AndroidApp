package com.example.tfgmates.helpers

import android.util.Log
import com.example.tfgmates.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object DTHelper {

    private val client = OkHttpClient()

    fun getAccessToken(callback: (String?) -> Unit) {
        val tokenUrl = "https://login.microsoftonline.com/${BuildConfig.TENANT_ID}/oauth2/v2.0/token"

        val formBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", BuildConfig.CLIENT_ID)
            .add("client_secret", BuildConfig.CLIENT_SECRET)
            .add("scope", "api://${BuildConfig.API_CLIENT_ID}/.default")
            .build()

        val request = Request.Builder()
            .url(tokenUrl)
            .post(formBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string()
                if (responseText != null) {
                    try {
                        val json = JSONObject(responseText)
                        if (json.has("access_token")) {
                            callback(json.getString("access_token"))
                        } else {
                            Log.e("TOKEN_ERROR", json.toString())
                            callback(null)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
        })
    }

    fun getTwinData(endpoint: String, token: String, callback: (JSONObject?) -> Unit) {
        val url = "${BuildConfig.API_URL}/api/$endpoint"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string()
                if (responseText != null) {
                    try {
                        callback(JSONObject(responseText))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
        })
    }

    fun putTwinData(endpoint: String, jsonBody: JSONObject, token: String, callback: (Boolean) -> Unit) {
        val url = "${BuildConfig.API_URL}/api/$endpoint"

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.isSuccessful)
            }
        })
    }
}