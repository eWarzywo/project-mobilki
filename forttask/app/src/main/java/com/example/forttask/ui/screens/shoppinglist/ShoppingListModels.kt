package com.example.forttask.ui.screens.shoppinglist

import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingItem(
    val id: Int,
    val name: String,
    val cost: Double,
    val createdAt: String,
    val createdBy: CreatedBy? = null,
    val boughtBy: CreatedBy? = null,
    val boughtAt: String? = null
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

    fun getFormattedBoughtDate(): String {
        if (boughtAt == null) return "Not bought"

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        return try {
            val date = inputFormat.parse(boughtAt)
            date?.let { outputFormat.format(it) } ?: "Invalid date"
        } catch (e: Exception) {
            "Invalid date format"
        }
    }

    fun isBought(): Boolean = boughtBy != null
}

@Serializable
data class CreatedBy(val username: String)
