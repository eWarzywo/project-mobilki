package com.example.forttask.ui.screens.bills

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
data class BillsResponse(
    val bills: List<Bill>
)

@Serializable
data class Bill(
    val id: Int,
    val name: String,
    val description: String,
    val amount: Double,
    val dueDate: String,
    val createdAt: String,
    val updatedAt: String? = null,
    val createdById: Int,
    val householdId: Int,
    val createdBy: CreatedBy? = null
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

    fun getFormattedAmount(): String {
        return "$${"%.2f".format(amount)}"
    }

    fun isOverdue(): Boolean {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        return try {
            val dueDate = inputFormat.parse(this.dueDate)
            val currentDate = java.util.Date()
            dueDate?.before(currentDate) ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun getDaysUntilDue(): Int {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        return try {
            val dueDate = inputFormat.parse(this.dueDate)
            val currentDate = java.util.Date()
            val diffInMillis = dueDate!!.time - currentDate.time
            (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            0
        }
    }
}

@Serializable
data class CreatedBy(
    val username: String
)