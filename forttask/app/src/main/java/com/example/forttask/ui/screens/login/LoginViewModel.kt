package com.example.forttask.ui.screens.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.forttask.CredentialsApplication
import com.example.forttask.data.entity.Credentials
import com.example.forttask.data.repository.CredentialsRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginViewModel(
    private val credentialsRepository: CredentialsRepository
) : ViewModel() {

    val hasCredentials = mutableStateOf(false)

    init {
        viewModelScope.launch {
            credentialsRepository.getAllCredentials().collectLatest { credentialsList ->
                hasCredentials.value = credentialsList.isNotEmpty()
            }
        }
    }

    fun saveCredentials(credentials: Credentials) {
        viewModelScope.launch {
            credentialsRepository.insert(credentials)
        }
    }

    suspend fun checkDuplicateCredentials(username: String): Credentials? {
        val allCredentials = credentialsRepository.getAllCredentials().first()
        return allCredentials.find { it.username == username }
    }

    fun updateCredentials(credentials: Credentials) {
        viewModelScope.launch {
            credentialsRepository.update(credentials)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CredentialsApplication)
                LoginViewModel(application.container.credentialsRepository)
            }
        }
    }
}
