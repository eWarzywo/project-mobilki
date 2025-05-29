package com.example.forttask.ui.screens.overview

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forttask.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OverviewViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OverviewUiState())
    val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun loadUpcomingEvents(context: Context, selectedDate: Date = Date()) {
        viewModelScope.launch {
            val formattedDate = dateFormatter.format(selectedDate)
            Timber.i("📅 Loading events for date: $formattedDate")
            
            _uiState.update { it.copy(
                isLoading = true,
                selectedDate = selectedDate
            )}
            
            try {
                val endpoint = "overview/events?date=${formattedDate}"
                Timber.d("📊 Fetching events from endpoint: $endpoint")
                
                val response = ApiService.getProtectedData(context, endpoint)
                
                response?.let { jsonString ->
                    try {
                        Timber.d("🔍 Parsing events response")
                        val eventsResponse = json.decodeFromString<OverviewEventsResponse>(jsonString)
                        val eventCount = eventsResponse.events.size
                        
                        Timber.i("✨ Successfully loaded $eventCount events for $formattedDate")
                        if (eventCount > 0) {
                            val eventSummary = eventsResponse.events.joinToString(", ") { it.name }
                            Timber.d("📋 Events: $eventSummary")
                        } else {
                            Timber.d("📋 No events found for this date")
                        }
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                events = eventsResponse.events,
                                hasEvents = eventsResponse.events.isNotEmpty(),
                                error = null
                            ) 
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "⚠️ JSON parsing error for events data")
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = "Failed to parse events data: ${e.localizedMessage}"
                            ) 
                        }
                    }
                } ?: run {
                    Timber.e("⚠️ Failed to fetch events - null response")
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Failed to fetch events"
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "⚠️ Error loading events for date: $formattedDate")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error: ${e.localizedMessage}"
                    ) 
                }
            }
        }
    }

    fun loadUpcomingChores(context: Context, selectedDate: Date = Date()) {
        viewModelScope.launch {
            val formattedDate = dateFormatter.format(selectedDate)
            Timber.i("📋 Loading chores for date: $formattedDate")
            
            _uiState.update { it.copy(isChoresLoading = true) }
            
            try {
                val endpoint = "overview/chores?date=${formattedDate}"
                Timber.d("📊 Fetching chores from endpoint: $endpoint")
                
                val response = ApiService.getProtectedData(context, endpoint)
                
                response?.let { jsonString ->
                    try {
                        Timber.d("🔍 Parsing chores response")
                        val choresResponse = json.decodeFromString<OverviewChoresResponse>(jsonString)
                        val choreCount = choresResponse.chores.size
                        
                        Timber.i("✨ Successfully loaded $choreCount chores for $formattedDate")
                        if (choreCount > 0) {
                            val choreSummary = choresResponse.chores.joinToString(", ") { it.name }
                            Timber.d("📋 Chores: $choreSummary")
                        } else {
                            Timber.d("📋 No chores found for this date")
                        }
                        
                        _uiState.update { 
                            it.copy(
                                isChoresLoading = false, 
                                chores = choresResponse.chores,
                                hasChores = choresResponse.chores.isNotEmpty(),
                                choreError = null
                            ) 
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "⚠️ JSON parsing error for chores data")
                        _uiState.update { 
                            it.copy(
                                isChoresLoading = false, 
                                choreError = "Failed to parse chores data: ${e.localizedMessage}"
                            ) 
                        }
                    }
                } ?: run {
                    Timber.e("⚠️ Failed to fetch chores - null response")
                    _uiState.update { 
                        it.copy(
                            isChoresLoading = false, 
                            choreError = "Failed to fetch chores"
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "⚠️ Error loading chores for date: $formattedDate")
                _uiState.update { 
                    it.copy(
                        isChoresLoading = false, 
                        choreError = "Error: ${e.localizedMessage}"
                    ) 
                }
            }
        }
    }

    fun loadUpcomingBills(context: Context, selectedDate: Date = Date()) {
        viewModelScope.launch {
            val formattedDate = dateFormatter.format(selectedDate)
            Timber.i("💰 Loading bills for date: $formattedDate")
            
            _uiState.update { it.copy(isBillsLoading = true) }
            
            try {
                val endpoint = "overview/bills?date=${formattedDate}"
                Timber.d("📊 Fetching bills from endpoint: $endpoint")
                
                val response = ApiService.getProtectedData(context, endpoint)
                
                response?.let { jsonString ->
                    try {
                        Timber.d("🔍 Parsing bills response")
                        val billsResponse = json.decodeFromString<OverviewBillsResponse>(jsonString)
                        val billCount = billsResponse.bills.size
                        
                        Timber.i("✨ Successfully loaded $billCount bills for $formattedDate")
                        if (billCount > 0) {
                            val billSummary = billsResponse.bills.joinToString(", ") { "${it.name} (${it.getFormattedAmount()})" }
                            Timber.d("💰 Bills: $billSummary")
                        } else {
                            Timber.d("💰 No bills found for this date")
                        }
                        
                        _uiState.update { 
                            it.copy(
                                isBillsLoading = false, 
                                bills = billsResponse.bills,
                                hasBills = billsResponse.bills.isNotEmpty(),
                                billError = null
                            ) 
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "⚠️ JSON parsing error for bills data")
                        _uiState.update { 
                            it.copy(
                                isBillsLoading = false, 
                                billError = "Failed to parse bills data: ${e.localizedMessage}"
                            ) 
                        }
                    }
                } ?: run {
                    Timber.e("⚠️ Failed to fetch bills - null response")
                    _uiState.update { 
                        it.copy(
                            isBillsLoading = false, 
                            billError = "Failed to fetch bills"
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "⚠️ Error loading bills for date: $formattedDate")
                _uiState.update { 
                    it.copy(
                        isBillsLoading = false, 
                        billError = "Error: ${e.localizedMessage}"
                    ) 
                }
            }
        }
    }
    
    fun loadShoppingItems(context: Context) {
        viewModelScope.launch {
            Timber.i("🛒 Loading shopping items")
            
            _uiState.update { it.copy(isShoppingLoading = true) }
            
            try {
                val endpoint = "overview/shoppingList"
                Timber.d("📊 Fetching shopping items from endpoint: $endpoint")
                
                val response = ApiService.getProtectedData(context, endpoint)
                
                response?.let { jsonString ->
                    try {
                        Timber.d("🔍 Parsing shopping items response")
                        val shoppingResponse = json.decodeFromString<OverviewShoppingResponse>(jsonString)
                        val itemCount = shoppingResponse.shoppingItems.size
                        
                        Timber.i("✨ Successfully loaded $itemCount shopping items")
                        if (itemCount > 0) {
                            val itemSummary = shoppingResponse.shoppingItems.joinToString(", ") { "${it.name} (${it.getFormattedCost()})" }
                            Timber.d("🛒 Shopping items: $itemSummary")
                        } else {
                            Timber.d("🛒 No shopping items found")
                        }
                        
                        _uiState.update { 
                            it.copy(
                                isShoppingLoading = false, 
                                shoppingItems = shoppingResponse.shoppingItems,
                                hasShoppingItems = shoppingResponse.shoppingItems.isNotEmpty(),
                                shoppingError = null
                            ) 
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "⚠️ JSON parsing error for shopping items data")
                        _uiState.update { 
                            it.copy(
                                isShoppingLoading = false, 
                                shoppingError = "Failed to parse shopping items data: ${e.localizedMessage}"
                            ) 
                        }
                    }
                } ?: run {
                    Timber.e("⚠️ Failed to fetch shopping items - null response")
                    _uiState.update { 
                        it.copy(
                            isShoppingLoading = false, 
                            shoppingError = "Failed to fetch shopping items"
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "⚠️ Error loading shopping items")
                _uiState.update { 
                    it.copy(
                        isShoppingLoading = false, 
                        shoppingError = "Error: ${e.localizedMessage}"
                    ) 
                }
            }
        }
    }

    fun setSelectedDate(context: Context, date: Date) {
        val oldDateFormatted = dateFormatter.format(_uiState.value.selectedDate)
        val newDateFormatted = dateFormatter.format(date)
        
        Timber.i("📆 Date changed: $oldDateFormatted → $newDateFormatted")
        
        _uiState.update { it.copy(selectedDate = date) }
        loadUpcomingEvents(context, date)
        loadUpcomingChores(context, date)
        loadUpcomingBills(context, date)
    }
}

data class OverviewUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val events: List<OverviewEvent> = emptyList(),
    val hasEvents: Boolean = false,
    val isChoresLoading: Boolean = false,
    val choreError: String? = null,
    val chores: List<OverviewChore> = emptyList(),
    val hasChores: Boolean = false,
    val isBillsLoading: Boolean = false,
    val billError: String? = null,
    val bills: List<OverviewBill> = emptyList(),
    val hasBills: Boolean = false,
    val isShoppingLoading: Boolean = false,
    val shoppingError: String? = null,
    val shoppingItems: List<OverviewShoppingItem> = emptyList(),
    val hasShoppingItems: Boolean = false,
    val selectedDate: Date = Date()
)