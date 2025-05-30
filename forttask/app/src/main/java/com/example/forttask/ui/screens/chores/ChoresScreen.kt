package com.example.forttask.ui.screens.chores

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChoresScreen(viewModel: ChoresViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadChores(context) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            val result =
                    snackbarHostState.showSnackbar(
                            message = error,
                            actionLabel = "Retry",
                            duration = SnackbarDuration.Long
                    )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.loadChores(context, uiState.currentFilter)
            }
            viewModel.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            // Header
            Text(
                    text = "Chores",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
            )

            // Filter Chips
            Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Start
            ) {
                FilterChip(
                        onClick = { viewModel.switchFilter(context, ChoreFilter.TODO) },
                        label = { Text("To Do") },
                        selected = uiState.currentFilter == ChoreFilter.TODO,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null)
                        },
                        colors =
                                FilterChipDefaults.filterChipColors(
                                        selectedContainerColor =
                                                MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor =
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                )
                )

                Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                FilterChip(
                        onClick = { viewModel.switchFilter(context, ChoreFilter.DONE) },
                        label = { Text("Done") },
                        selected = uiState.currentFilter == ChoreFilter.DONE,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        },
                        colors =
                                FilterChipDefaults.filterChipColors(
                                        selectedContainerColor =
                                                MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor =
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                )
                )
            }

            // Main content area
            when {
                uiState.isLoading -> {
                    Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                    CardDefaults.cardColors(
                                            containerColor =
                                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                    ) {
                        Box(
                                modifier = Modifier.fillMaxWidth().padding(40.dp),
                                contentAlignment = Alignment.Center
                        ) {
                            Text(
                                    text = "Loading chores...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                uiState.chores.isEmpty() -> {
                    Box(
                            modifier = Modifier.fillMaxWidth().height(400.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme
                                                                .surfaceContainerHigh
                                        ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                    modifier =
                                            Modifier.fillMaxWidth()
                                                    .padding(vertical = 48.dp, horizontal = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                        modifier =
                                                Modifier.size(80.dp)
                                                        .background(
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .surfaceContainerHighest,
                                                                shape = CircleShape
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                            imageVector =
                                                    if (uiState.currentFilter == ChoreFilter.TODO) {
                                                        Icons.Default.Schedule
                                                    } else {
                                                        Icons.Default.CheckCircle
                                                    },
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp),
                                            tint =
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                            alpha = 0.7f
                                                    )
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                        text =
                                                if (uiState.currentFilter == ChoreFilter.TODO) {
                                                    "No pending chores"
                                                } else {
                                                    "No completed chores"
                                                },
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                        text =
                                                if (uiState.currentFilter == ChoreFilter.TODO) {
                                                    "Chores will appear here when they are created"
                                                } else {
                                                    "Completed chores will appear here"
                                                },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color =
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = 0.8f
                                                ),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) { items(uiState.chores) { chore -> ChoreItemCard(chore = chore) } }
                }
            }
        }
    }
}
