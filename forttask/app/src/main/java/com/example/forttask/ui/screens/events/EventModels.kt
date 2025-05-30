package com.example.forttask.ui.screens.events

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
data class EventsResponse(
    val events: List<Event>,
    val count: Int
)

@Serializable
data class Event(
    val id: Int,
    val name: String,
    val description: String,
    val date: String,
    val location: String,
    val createdAt: String,
    val attendees: List<Attendee>
) {
    fun getFormattedDate(): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        return try {
            val date = inputFormat.parse(this.date)
            date?.let { outputFormat.format(it) } ?: "Invalid date"
        } catch (e: Exception) {
            "Invalid date format"
        }
    }

    fun getAttendeeNames(): List<String> {
        return attendees.mapNotNull { it.user?.username }
    }
}

@Serializable
data class Attendee(
    val eventId: Int,
    val userId: Int,
    val user: User? = null
)

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String? = null,
    val passwordHash: String? = null,
    val createdAt: String? = null,
    val profilePictureId: Int? = null,
    val householdId: Int? = null
)
