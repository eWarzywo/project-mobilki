package com.example.forttask.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object ApiService {
    suspend fun getProtectedData(context: Context): String? {
        return withContext(Dispatchers.IO) {
            val client = ApiClient.getHttpClient()
            val request = ApiClient.getAuthenticatedRequest(
                context,
                "http://10.90.83.206:3000/api/events/get"
            )

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body()?.string()
                } else {
                    null
                }
            } catch (e: IOException) {
                null
            }
        }
    }
}
