package com.example.forttask.ui.screens.chores

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
data class ChoresResponse(
    val chores: List<Chore>,
    val count: Int
)

@Serializable
data class Chore(
    val id: Int,
    val name: String,
    val description: String,
    val dueDate: String,
    val priority: Int,
    val done: Boolean,
    val createdAt: String,
    val updatedAt: String? = null,
    val createdById: Int,
    val doneById: Int? = null,
    val householdId: Int,
    val createdBy: CreatedBy? = null,
    val doneBy: CreatedBy? = null
) {
    fun getFormattedDueDate(): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        return try {
            val date = inputFormat.parse(this.dueDate)
            date?.let { outputFormat.format(it) } ?: "Invalid date"
        } catch (e: Exception) {
            "Invalid date format"
        }
    }

    fun getFormattedCreatedDate(): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy 'at' HH:mm", Locale.getDefault())

        return try {
            val date = inputFormat.parse(this.createdAt)
            date?.let { outputFormat.format(it) } ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getPriorityDisplay(): String {
        return when (priority) {
            1 -> "Low"
            2 -> "Medium"
            3 -> "High"
            4 -> "Urgent"
            else -> "Unknown"
        }
    }

    fun getPriorityColor(): String {
        return when (priority) {
            1 -> "#4CAF50"
            2 -> "#FF9800"
            3 -> "#F44336"
            4 -> "#9C27B0"
            else -> "#757575"
        }
    }

    fun isOverdue(): Boolean {
        if (done) return false
        
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        return try {
            val dueDateTime = inputFormat.parse(this.dueDate)
            val currentTime = System.currentTimeMillis()
            dueDateTime?.time?.let { it < currentTime } ?: false
        } catch (e: Exception) {
            false
        }
    }
}

@Serializable
data class CreatedBy(
    val username: String
)

enum class ChoreFilter {
    TODO, DONE
}