package com.example.forttask.ui.screens.passwordvault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.forttask.CredentialsApplication
import com.example.forttask.data.entity.Credentials

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordVaultScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PasswordVaultViewModel = viewModel(
        factory = PasswordVaultViewModel.Factory
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Password Vault") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.credentialsList.isEmpty()) {
                EmptyVaultContent()
            } else {
                CredentialsList(
                    credentials = uiState.credentialsList,
                    onCredentialSelected = { username, password ->
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "selectedUsername", username
                        )
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "selectedPassword", password
                        )
                        navController.popBackStack()
                    },
                    onDeleteCredential = { viewModel.deleteCredentials(it) }
                )
            }
        }
    }
}

@Composable
private fun EmptyVaultContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No saved credentials found",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun CredentialsList(
    credentials: List<Credentials>,
    onCredentialSelected: (String, String) -> Unit,
    onDeleteCredential: (Credentials) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(credentials) { credential ->
            CredentialItem(
                credential = credential,
                onCredentialSelected = onCredentialSelected,
                onDeleteCredential = onDeleteCredential
            )
        }
    }
}

@Composable
private fun CredentialItem(
    credential: Credentials,
    onCredentialSelected: (String, String) -> Unit,
    onDeleteCredential: (Credentials) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onCredentialSelected(credential.username, credential.password) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = credential.username,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "••••••••",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(onClick = { onDeleteCredential(credential) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete credential"
                )
            }
        }
    }
}
