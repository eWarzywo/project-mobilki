package com.example.forttask.ui.screens.shoppinglist

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
data class ShoppingItem(
    val id: Int,
    val name: String,
    val cost: Double,
    val createdAt: String,
    val createdBy: CreatedBy? = null
) {
    fun getFormattedCost(): String {
        return "$${"%.2f".format(cost)}"
    }

    fun getFormattedCreatedDate(): String {
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