package com.example.forttask.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request

object ApiClient {
    val cookieJar = SessionCookieJar()

    fun getAuthenticatedRequest(context: Context, url: String): Request {
        return Request.Builder()
            .url(url)
            .build()
    }

    fun getHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()
}

