package com.example.forttask.ui.screens.passwordvault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.forttask.CredentialsApplication
import com.example.forttask.data.entity.Credentials
import com.example.forttask.data.repository.CredentialsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PasswordVaultViewModel(
    private val credentialsRepository: CredentialsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordVaultUiState())
    val uiState: StateFlow<PasswordVaultUiState> = _uiState.asStateFlow()

    init {
        loadCredentials()
    }

    private fun loadCredentials() {
        viewModelScope.launch {
            credentialsRepository.getAllCredentials().collect { credentials ->
                _uiState.update { currentState ->
                    currentState.copy(credentialsList = credentials)
                }
            }
        }
    }

    fun deleteCredentials(credentials: Credentials) {
        viewModelScope.launch {
            credentialsRepository.delete(credentials)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CredentialsApplication)
                PasswordVaultViewModel(application.container.credentialsRepository)
            }
        }
    }
}

data class PasswordVaultUiState(
    val credentialsList: List<Credentials> = emptyList()
)
