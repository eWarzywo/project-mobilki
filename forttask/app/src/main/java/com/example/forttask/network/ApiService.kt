package com.example.forttask.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException

object ApiService {
    suspend fun getProtectedData(context: Context, uri: String): String? {
        return withContext(Dispatchers.IO) {
            val client = ApiClient.getHttpClient()
            val fullUrl = "http://10.90.83.206:3000/api/$uri"
            val request = ApiClient.getAuthenticatedRequest(context, fullUrl)
            
            Timber.d("🔄 API Request: GET $fullUrl")
            val startTime = System.currentTimeMillis()
            
            try {
                val response = client.newCall(request).execute()
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    Timber.d("✅ API Success: GET $fullUrl (${response.code()}) in ${duration}ms")
                    
                    responseBody?.let { body ->
                        if (body.length > 200) {
                            Timber.v("📥 Response: ${body.substring(0, 200)}... (${body.length} chars total)")
                        } else {
                            Timber.v("📥 Response: $body")
                        }
                    }
                    
                    responseBody
                } else {
                    Timber.e("❌ API Error: GET $fullUrl (${response.code()}) in ${duration}ms")
                    null
                }
            } catch (e: IOException) {
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                Timber.e(e, "❌ API Failure: GET $fullUrl in ${duration}ms")
                null
            }
        }
    }
}
