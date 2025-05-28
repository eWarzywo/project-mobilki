package com.example.forttask.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class SessionCookieJar : CookieJar {
    private val cookieStore: MutableMap<String, List<Cookie>> = mutableMapOf()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host()] = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host()] ?: listOf()
    }

    fun getCookiesAsString(): String {
        if (cookieStore.isEmpty()) {
            return "No cookies stored"
        }

        return buildString {
            cookieStore.forEach { (host, cookies) ->
                append("Host: $host\n")
                if (cookies.isEmpty()) {
                    append("  No cookies\n")
                } else {
                    cookies.forEach { cookie ->
                        append("  ${cookie.name()}: ${cookie.value()} (expires: ${if (cookie.expiresAt() == Long.MAX_VALUE) "session cookie" else java.util.Date(cookie.expiresAt())})\n")
                    }
                }
            }
        }
    }
}
