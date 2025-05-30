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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.forttask.CredentialsApplication
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
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val hasCredentials by remember { viewModel.hasCredentials }

    // Listen for credentials selected from password vault
    val navBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(navBackStackEntry) {
        val savedStateHandle = navBackStackEntry?.savedStateHandle
        val selectedUsername = savedStateHandle?.get<String>("selectedUsername")
        val selectedPassword = savedStateHandle?.get<String>("selectedPassword")

        if (selectedUsername != null && selectedPassword != null) {
            username = selectedUsername
            password = selectedPassword
            savedStateHandle.remove<String>("selectedUsername")
            savedStateHandle.remove<String>("selectedPassword")
        }
    }

    if (showSaveCredentialsDialog) {
        SaveCredentialsDialog(
            onDismiss = {
                showSaveCredentialsDialog = false
                // Navigate to Overview after user dismisses the dialog
                if (isLoginSuccessful) {
                    navController.navigate(NavigationItem.Overview.route) {
                        popUpTo(NavigationItem.Login.route) { inclusive = true }
                    }
                }
            },
            onConfirm = {
                viewModel.saveCredentials(
                    Credentials(
                        username = username,
                        password = password
                    )
                )
                showSaveCredentialsDialog = false
                // Navigate to Overview after user confirms saving credentials
                if (isLoginSuccessful) {
                    navController.navigate(NavigationItem.Overview.route) {
                        popUpTo(NavigationItem.Login.route) { inclusive = true }
                    }
                }
            }
        )
    }

    // When login is successful and dialog is closed, if we need to navigate
    LaunchedEffect(isLoginSuccessful, showSaveCredentialsDialog) {
        if (isLoginSuccessful && !showSaveCredentialsDialog) {
            navController.navigate(NavigationItem.Overview.route) {
                popUpTo(NavigationItem.Login.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                scope.launch {
                    val result = AuthManager.login(context, username, password)
                    if (result.success) {
                        Toast.makeText(context, "Login success", Toast.LENGTH_SHORT).show()
                        isLoginSuccessful = true
                        showSaveCredentialsDialog = true
                        // Navigation is handled after dialog is dismissed
                    } else {
                        val errorMessage = result.errorMessage ?: "Unknown error occurred"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (hasCredentials) {
            TextButton(
                onClick = { navController.navigate(NavigationItem.PasswordVault.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Password Vault")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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
