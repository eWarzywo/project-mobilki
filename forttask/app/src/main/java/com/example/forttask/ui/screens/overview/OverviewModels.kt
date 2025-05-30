package com.example.forttask.ui.screens.overview

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
data class OverviewEventsResponse(
    val events: List<OverviewEvent>
)

@Serializable
data class OverviewChoresResponse(
    val chores: List<OverviewChore>
)

@Serializable
data class OverviewBillsResponse(
    val bills: List<OverviewBill>
)

@Serializable
data class OverviewShoppingResponse(
    val shoppingItems: List<OverviewShoppingItem>
)

@Serializable
data class OverviewEvent(
    val id: Int,
    val name: String,
    val description: String,
    val date: String,
    val location: String,
    val createdBy: CreatedBy? = null
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
}

@Serializable
data class OverviewChore(
    val id: Int,
    val name: String,
    val description: String,
    val dueDate: String,
    val priority: Int,
    val createdBy: CreatedBy? = null
) {
    fun getFormattedDate(): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        return try {
            val date = inputFormat.parse(this.dueDate)
            date?.let { outputFormat.format(it) } ?: "Invalid date"
        } catch (e: Exception) {
            "Invalid date format"
        }
    }

    fun getPriorityDisplay(): String {
        return when (priority) {
            1 -> "Low Priority"
            2 -> "Medium Priority"
            3 -> "High Priority"
            else -> "Priority $priority"
        }
    }

    fun getPriorityColor(): String {
        return when (priority) {
            1 -> "#4CAF50"
            2 -> "#FF9800"
            3 -> "#FF5722"
            else -> "#757575"
        }
    }
}

@Serializable
data class OverviewBill(
    val id: Int,
    val name: String,
    val description: String,
    val amount: Double,
    val dueDate: String,
    val createdBy: CreatedBy? = null
) {
    fun getFormattedDate(): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        return try {
            val date = inputFormat.parse(this.dueDate)
            date?.let { outputFormat.format(it) } ?: "Invalid date"
        } catch (e: Exception) {
            "Invalid date format"
        }
    }

    fun getFormattedAmount(): String {
        return "$${"%.2f".format(amount)}"
    }
}

@Serializable
data class OverviewShoppingItem(
    val id: Int,
    val name: String,
    val cost: Double,
    val createdAt: String,
    val createdBy: CreatedBy? = null
) {
    fun getFormattedCost(): String {
        return "$${"%.2f".format(cost)}"
    }

    fun getFormattedDate(): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        return try {
            val date = inputFormat.parse(this.createdAt)
            date?.let { outputFormat.format(it) } ?: "Invalid date"
        } catch (e: Exception) {
            "Invalid date format"
        }
    }
}

@Serializable
data class CreatedBy(
    val username: String
)