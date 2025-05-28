package com.example.forttask.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import java.io.IOException

data class LoginResult(
    val success: Boolean,
    val errorMessage: String? = null
)

object AuthManager {
    private const val SERVER_IP = "10.90.83.206"
    private const val SERVER_PORT = 3000
    private const val SERVER_URL = "http://$SERVER_IP:$SERVER_PORT"

    suspend fun csrf(context: Context): Pair<String?, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val client = ApiClient.getHttpClient()
                val request = Request.Builder()
                    .url("$SERVER_URL/api/auth/csrf")
                    .header("Host", "$SERVER_IP:$SERVER_PORT")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body()?.string()
                    val cookieHeader = response.headers("Set-Cookie")
                    val csrfCookie = cookieHeader.firstOrNull { it.startsWith("next-auth.csrf-token=") }
                        ?.split(";")?.firstOrNull()
                    return@withContext Pair(body?.let {
                        Regex("\"csrfToken\":\"([^\"]+)\"").find(it)?.groupValues?.get(1)
                    }, csrfCookie)
                } else {
                    Pair(null, null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Pair(null, null)
            }
        }
    }

    suspend fun login(context: Context, username: String, password: String): LoginResult {
        return withContext(Dispatchers.IO) {
            val client = ApiClient.getHttpClient()
            val (csrfToken, csrfCookie) = csrf(context)

            if (csrfToken == null || csrfCookie == null) {
                return@withContext LoginResult(false, "Failed to retrieve CSRF token or cookie.")
            }

            val formBody = FormBody.Builder()
                .add("csrfToken", csrfToken)
                .add("username", username)
                .add("password", password)
                .build()

            val request = Request.Builder()
                .url("$SERVER_URL/api/auth/callback/credentials")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", csrfCookie)
                .post(formBody)
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    LoginResult(true)
                } else {
                    val errorBody = response.body()?.string() ?: ""
                    LoginResult(false, "Server error: ${response.code()} - $errorBody")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                LoginResult(false, "Network error: ${e.message}")
            }
        }
    }
}

