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
    val cycle: Int? = null,
    val repeatCount: Int? = null,
    val location: String,
    val createdAt: String,
    val updatedAt: String? = null,
    val createdById: Int,
    val householdId: Int? = null,
    val parentEventId: Int? = null,
    val attendees: List<Attendee>? = null
) {
    // Format the date to show only day, month, and year
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

    // Get list of attendee usernames
    fun getAttendeeNames(): List<String> {
        return attendees?.mapNotNull { it.user?.username } ?: emptyList()
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
