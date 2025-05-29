package com.example.forttask.ui.screens.chores

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

data class ChoresUiState(
    val isLoading: Boolean = false,
    val chores: List<Chore> = emptyList(),
    val error: String? = null,
    val currentFilter: ChoreFilter = ChoreFilter.TODO,
    val totalCount: Int = 0,
    val isRefreshing: Boolean = false
)

class ChoresViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChoresUiState())
    val uiState: StateFlow<ChoresUiState> = _uiState.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private var context: Context? = null

    init {
        setupSocketListener()
    }

    private fun setupSocketListener() {
        SocketManager.setUpdateChoresCallback {
            Timber.i("🔄 Socket update received for chores, refreshing...")
            context?.let { ctx ->
                refreshChores(ctx)
            }
        }
    }

    fun loadChores(context: Context, filter: ChoreFilter = ChoreFilter.TODO, limit: Int? = null, skip: Int? = null) {
        this.context = context

        viewModelScope.launch {
            Timber.i("📋 Loading ${filter.name.lowercase()} chores")
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val endpoint = when (filter) {
                    ChoreFilter.TODO -> "chores/todo/get"
                    ChoreFilter.DONE -> "chores/done/get"
                }
                
                val params = mutableListOf<String>()
                limit?.let { params.add("limit=$it") }
                skip?.let { params.add("skip=$it") }
                
                val fullEndpoint = if (params.isNotEmpty()) {
                    "$endpoint?${params.joinToString("&")}"
                } else {
                    endpoint
                }
                
                Timber.d("📊 Fetching chores from endpoint: $fullEndpoint")
                
                val response = ApiService.getProtectedData(context, fullEndpoint)
                
                response?.let { jsonString ->
                    try {
                        Timber.d("🔍 Parsing chores response")
                        val choresResponse = json.decodeFromString<ChoresResponse>(jsonString)
                        val choreCount = choresResponse.chores.size
                        
                        Timber.i("✨ Successfully loaded $choreCount ${filter.name.lowercase()} chores")
                        if (choreCount > 0) {
                            val choreSummary = choresResponse.chores.take(3).joinToString(", ") { it.name }
                            Timber.d("📋 Chores preview: $choreSummary${if (choreCount > 3) "..." else ""}")
                        } else {
                            Timber.d("📋 No ${filter.name.lowercase()} chores found")
                        }
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                chores = choresResponse.chores,
                                totalCount = choresResponse.count,
                                currentFilter = filter,
                                error = null
                            ) 
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "⚠️ JSON parsing error for chores data")
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = "Failed to parse chores data: ${e.localizedMessage}"
                            ) 
                        }
                    }
                } ?: run {
                    Timber.e("⚠️ Failed to fetch chores - null response")
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Failed to fetch chores"
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "⚠️ Error loading ${filter.name.lowercase()} chores")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error: ${e.localizedMessage}"
                    ) 
                }
            }
        }
    }

    fun refreshChores(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadChores(context, _uiState.value.currentFilter)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun switchFilter(context: Context, filter: ChoreFilter) {
        if (_uiState.value.currentFilter != filter) {
            loadChores(context, filter)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.setUpdateChoresCallback { }
        context = null
    }
}