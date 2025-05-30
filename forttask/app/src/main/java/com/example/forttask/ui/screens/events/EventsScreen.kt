package com.example.forttask.ui.screens.events

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forttask.network.ApiService.getProtectedData
import com.example.forttask.network.SocketManager
import com.example.forttask.network.UserDataManager
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
fun EventsScreen(modifier: Modifier = Modifier) {
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun fetchEvents() {
        isLoading = true
        try {
            val response = getProtectedData(context, "/events/get")
            if (response != null) {
                val eventsResponse = json.decodeFromString<EventsResponse>(response)
                events = eventsResponse.events
            } else {
                error = "No events data found"
            }
        } catch (e: Exception) {
            error = "Error parsing events: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(key1 = true) {
        val householdId = UserDataManager.getUserHouseholdId(context)
        if (householdId != null) {
            if (!SocketManager.isInitialized()) {
                SocketManager.initialize(householdId)

                SocketManager.setUpdateEventsCallback { coroutineScope.launch { fetchEvents() } }
            }
        }

        fetchEvents()
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            coroutineScope.launch {
                val householdId = UserDataManager.getUserHouseholdId(context)
                if (householdId != null && SocketManager.isInitialized()) {
                    SocketManager.disconnect(householdId)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
                text = "Events",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            isLoading -> {
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
                    ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
                }
            }
            error != null -> {
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                ) {
                    Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                text = error ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            events.isEmpty() -> {
                Box(
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        contentAlignment = Alignment.Center
                ) {
                    Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            colors =
                                    CardDefaults.cardColors(
                                            containerColor =
                                                    MaterialTheme.colorScheme.surfaceContainerHigh
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
                                        imageVector = Icons.Default.Event,
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
                                    text = "No events found",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                    text = "Events will appear here when they are created",
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
                EventsList(events = events, modifier = modifier)
            }
        }
    }
}

@Composable
fun EventsList(events: List<Event>, modifier: Modifier = Modifier) {
    LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) { items(events) { event -> EventCard(event = event) } }
}

@Composable
fun EventCard(event: Event, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by
            animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    label = "rotation_animation"
            )

    Card(
            modifier =
                    modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable {
                        expanded = !expanded
                    },
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = event.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                                text = event.getFormattedDate(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                        modifier =
                                Modifier.size(40.dp)
                                        .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = CircleShape
                                        ),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            modifier = Modifier.rotate(rotationState),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    if (event.description.isNotBlank()) {
                        DetailRow(
                                icon = Icons.Default.Description,
                                label = "Description",
                                value = event.description
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (event.location.isNotBlank()) {
                        DetailRow(
                                icon = Icons.Default.LocationOn,
                                label = "Location",
                                value = event.location
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    val createdAt =
                            try {
                                val inputFormat =
                                        java.text.SimpleDateFormat(
                                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                                java.util.Locale.getDefault()
                                        )
                                val outputFormat =
                                        java.text.SimpleDateFormat(
                                                "dd MMM yyyy 'at' HH:mm",
                                                java.util.Locale.getDefault()
                                        )
                                val date = inputFormat.parse(event.createdAt)
                                date?.let { outputFormat.format(it) } ?: "Unknown"
                            } catch (e: Exception) {
                                "Unknown"
                            }

                    DetailRow(icon = Icons.Default.Event, label = "Created", value = createdAt)

                    val attendees = event.getAttendeeNames()
                    if (attendees.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow(
                                icon = Icons.Default.Person,
                                label = "Attendees",
                                value = attendees.joinToString(", ")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        label: String,
        value: String,
        modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 20.dp)
        )
    }
}
