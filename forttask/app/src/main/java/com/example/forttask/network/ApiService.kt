package com.example.forttask.network

import retrofit2.http.GET

data class HelloResponse(val message: String)

interface ApiService {
    @GET("/api/hello")
    suspend fun getHello(): HelloResponse
}