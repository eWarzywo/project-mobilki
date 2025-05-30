package com.example.forttask.network

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.lang.Exception

@Serializable
data class UserData(
    val id: Int,
    val username: String,
    val email: String?,
    val passwordHash: String?,
    val createdAt: String?,
    val profilePictureId: Int?,
    val householdId: Int?
)

object UserDataManager {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun getUserHouseholdId(context: Context): String? {
        return try {
            val response = ApiService.getProtectedData(context, "/user/get")
            if (response != null) {
                val userData = json.decodeFromString<UserData>(response)
                userData.householdId?.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
