package com.example.forttask.ui.screens.bills

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forttask.network.ApiService
import com.example.forttask.network.SocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber

data class BillsUiState(
    val isLoading: Boolean = false,
    val bills: List<Bill> = emptyList(),
    val error: String? = null,
    val currentFilter: BillFilter = BillFilter.NOTPAID,
    val isRefreshing: Boolean = false
)

class BillsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BillsUiState())
    val uiState: StateFlow<BillsUiState> = _uiState.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private var context: Context? = null

    init {
        setupSocketListener()
    }

    private fun setupSocketListener() {
        SocketManager.setUpdateBillsCallback {
            Timber.i("🔄 Socket update received for bills, refreshing...")
            context?.let { ctx -> refreshBills(ctx) }
        }
    }

    fun loadBills(context: Context, filter: BillFilter = BillFilter.NOTPAID) {
        this.context = context

        viewModelScope.launch {
            Timber.i("💰 Loading ${filter.name.lowercase()} bills")

            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val endpoint =
                    when (filter) {
                        BillFilter.NOTPAID -> "bill/mobile/notpaid"
                        BillFilter.PAID -> "bill/mobile/paid"
                    }

                Timber.d("📊 Fetching bills from endpoint: $endpoint")

                val response = ApiService.getProtectedData(context, endpoint)

                response?.let { jsonString ->
                    try {
                        Timber.d("🔍 Parsing bills response")
                        Timber.d(
                            "📄 Raw response: ${jsonString.take(200)}${if (jsonString.length > 200) "..." else ""}"
                        )

                        val bills = json.decodeFromString<List<Bill>>(jsonString)
                        val billCount = bills.size

                        Timber.i(
                            "✨ Successfully loaded $billCount ${filter.name.lowercase()} bills"
                        )
                        if (billCount > 0) {
                            val billSummary =
                                bills.joinToString(", ") {
                                    "${it.name} (${it.getFormattedAmount()})"
                                }
                            Timber.d("💰 Bills: $billSummary")

                            val overdueBills = bills.filter { it.isOverdue() }
                            if (overdueBills.isNotEmpty()) {
                                Timber.w(
                                    "⚠️ Found ${overdueBills.size} overdue bills: ${
                                        overdueBills.joinToString(
                                            ", "
                                        ) { it.name }
                                    }"
                                )
                            }

                            val upcomingBills =
                                bills.filter { !it.isOverdue() && it.getDaysUntilDue() <= 7 }
                            if (upcomingBills.isNotEmpty()) {
                                Timber.i(
                                    "📅 Found ${upcomingBills.size} bills due within 7 days: ${
                                        upcomingBills.joinToString(
                                            ", "
                                        ) { "${it.name} (${it.getDaysUntilDue()} days)" }
                                    }"
                                )
                            }
                        } else {
                            Timber.d("💰 No ${filter.name.lowercase()} bills found")
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                bills = bills,
                                currentFilter = filter,
                                error = null
                            )
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "⚠️ JSON parsing error for bills data")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to parse bills data: ${e.localizedMessage}"
                            )
                        }
                    }
                }
                    ?: run {
                        Timber.e("⚠️ Failed to fetch bills - null response")
                        _uiState.update {
                            it.copy(isLoading = false, error = "Failed to fetch bills")
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "⚠️ Error loading ${filter.name.lowercase()} bills")
                _uiState.update {
                    it.copy(isLoading = false, error = "Error: ${e.localizedMessage}")
                }
            }
        }
    }

    fun refreshBills(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadBills(context, _uiState.value.currentFilter)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun switchFilter(context: Context, filter: BillFilter) {
        if (_uiState.value.currentFilter != filter) {
            loadBills(context, filter)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.setUpdateBillsCallback {}
        context = null
    }
}

enum class BillFilter {
    NOTPAID,
    PAID
}
