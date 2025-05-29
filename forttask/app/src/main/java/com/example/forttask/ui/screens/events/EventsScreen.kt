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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.sp
// nie usuwac tych importow, bo niby nic nie robia ale bez nich nie dziala XDD
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.example.forttask.network.ApiService.getProtectedData

/* example events output
{"events":[{"id":1,"name":"cpanie","description":"cpanie w sgb","date":"2025-05-29T00:00:00.000Z","cycle":0,"repeatCount":0,"location":"sgb","createdAt":"2025-05-29T07:54:39.193Z","updatedAt":"2025-05-29T07:54:39.193Z","createdById":1,"householdId":1,"parentEventId":null,"attendees":[{"eventId":1,"userId":1,"user":{"id":1,"username":"emati_emati","email":"emati@emati.emati","passwordHash":"$2b$10$AH3MSpx3v1PJ2RB2Lzu7Mubocs8ZgjsaWW6RIyRc.szzY/3TlrW86","createdAt":"2025-05-22T12:26:58.347Z","profilePictureId":null,"householdId":1}}]}],"count":1}
 */

@Composable
fun EventsScreen(
    modifier: Modifier = Modifier
) {
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    LaunchedEffect(key1 = true) {
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

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator()
            error != null -> Text(
                text = error ?: "Unknown error",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            events.isEmpty() -> Text(
                text = "No events found",
                style = MaterialTheme.typography.bodyLarge
            )
            else -> EventsList(events = events, modifier = modifier)
        }
    }
}

@Composable
fun EventsList(
    events: List<Event>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Events",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        items(events) { event ->
            EventCard(event = event)
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation_animation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = event.getFormattedDate(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotationState)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Description:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Location:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Created:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    val createdAt = try {
                        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                        val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                        val date = inputFormat.parse(event.createdAt)
                        date?.let { outputFormat.format(it) } ?: "Unknown"
                    } catch (e: Exception) {
                        "Unknown"
                    }
                    Text(
                        text = createdAt,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val attendees = event.getAttendeeNames()
                    if (attendees.isNotEmpty()) {
                        Text(
                            text = "Attendees:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = attendees.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
