package com.example.forttask.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.forttask.data.entity.Credentials
import com.example.forttask.network.AuthManager
import com.example.forttask.ui.navigation.NavigationItem

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.Factory
    )
) {
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("Login") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginSuccessful by remember { mutableStateOf(false) }
    var showSaveCredentialsDialog by remember { mutableStateOf(false) }
    var showUpdateCredentialsDialog by remember { mutableStateOf(false) }
    var duplicateCredentials by remember { mutableStateOf<Credentials?>(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val hasCredentials by remember { viewModel.hasCredentials }
    var isLoading by remember { mutableStateOf(false) }

    var isComingFromVault by remember { mutableStateOf(false) }

    val navBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(navBackStackEntry) {
        val savedStateHandle = navBackStackEntry?.savedStateHandle
        val selectedUsername = savedStateHandle?.get<String>("selectedUsername")
        val selectedPassword = savedStateHandle?.get<String>("selectedPassword")

        if (selectedUsername != null && selectedPassword != null) {
            username = selectedUsername
            password = selectedPassword
            isComingFromVault = true
            savedStateHandle.remove<String>("selectedUsername")
            savedStateHandle.remove<String>("selectedPassword")
        }
    }

    if (showSaveCredentialsDialog) {
        SaveCredentialsDialog(
            onDismiss = {
                showSaveCredentialsDialog = false
                if (isLoginSuccessful) {
                    navController.navigate(NavigationItem.Overview.route) {
                        popUpTo(NavigationItem.Login.route) { inclusive = true }
                    }
                }
            },
            onConfirm = {
                scope.launch {
                    val existingCredentials = viewModel.checkDuplicateCredentials(username)
                    if (existingCredentials != null) {
                        duplicateCredentials = existingCredentials
                        showUpdateCredentialsDialog = true
                        showSaveCredentialsDialog = false
                    } else {
                        viewModel.saveCredentials(
                            Credentials(
                                username = username,
                                password = password
                            )
                        )
                        showSaveCredentialsDialog = false
                        if (isLoginSuccessful) {
                            navController.navigate(NavigationItem.Overview.route) {
                                popUpTo(NavigationItem.Login.route) { inclusive = true }
                            }
                        }
                    }
                }
            }
        )
    }

    if (showUpdateCredentialsDialog) {
        UpdateCredentialsDialog(
            username = username,
            onDismiss = {
                showUpdateCredentialsDialog = false
                if (isLoginSuccessful) {
                    navController.navigate(NavigationItem.Overview.route) {
                        popUpTo(NavigationItem.Login.route) { inclusive = true }
                    }
                }
            },
            onConfirm = {
                duplicateCredentials?.let { existingCredentials ->
                    val updatedCredentials = Credentials(
                        id = existingCredentials.id,
                        username = username,
                        password = password
                    )
                    viewModel.updateCredentials(updatedCredentials)
                }
                showUpdateCredentialsDialog = false
                if (isLoginSuccessful) {
                    navController.navigate(NavigationItem.Overview.route) {
                        popUpTo(NavigationItem.Login.route) { inclusive = true }
                    }
                }
            }
        )
    }

    LaunchedEffect(isLoginSuccessful, showSaveCredentialsDialog, showUpdateCredentialsDialog) {
        if (isLoginSuccessful && !showSaveCredentialsDialog && !showUpdateCredentialsDialog) {
            navController.navigate(NavigationItem.Overview.route) {
                popUpTo(NavigationItem.Login.route) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = !isLoading
            )

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val result = AuthManager.login(context, username, password)
                            if (result.success) {
                                isLoginSuccessful = true
                                showSaveCredentialsDialog = true
                            } else {
                                Toast.makeText(context, result.errorMessage ?: "Invalid credentials", Toast.LENGTH_LONG).show()
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (hasCredentials && !isComingFromVault) {
                TextButton(
                    onClick = { navController.navigate(NavigationItem.PasswordVault.route) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Open Password Vault")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
fun SaveCredentialsDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Credentials") },
        text = { Text("Would you like to save your credentials to the password vault?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UpdateCredentialsDialog(
    username: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Credentials") },
        text = { Text("Credentials for '$username' already exist. Would you like to update them?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
