package com.example.forttask.ui.screens.bills

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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.forttask.network.ApiService.getProtectedData
import com.example.forttask.network.SocketManager
import com.example.forttask.network.UserDataManager
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber

@Composable
fun BillsScreen(
    modifier: Modifier = Modifier
) {
    var bills by remember { mutableStateOf<List<Bill>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun fetchBills(limit: Int = 10, skip: Int = 0) {
        Timber.i("💰 Starting to fetch bills with limit=$limit, skip=$skip")
        isLoading = true
        
        try {
            val endpoint = "bill?skip=$skip"
            Timber.d("📊 Fetching bills from endpoint: $endpoint")
            
            val response = getProtectedData(context, endpoint)
            
            response?.let { jsonString ->
                try {
                    Timber.d("🔍 Parsing bills response")
                    Timber.d("📄 Raw response: ${jsonString.take(200)}${if(jsonString.length > 200) "..." else ""}")
                    
                    bills = json.decodeFromString<List<Bill>>(jsonString)
                    val billCount = bills.size
                    
                    Timber.i("✨ Successfully loaded $billCount bills (limit=$limit, skip=$skip)")
                    if (billCount > 0) {
                        val billSummary = bills.joinToString(", ") { "${it.name} (${it.getFormattedAmount()})" }
                        Timber.d("💰 Bills: $billSummary")
                        
                        val overdueBills = bills.filter { it.isOverdue() }
                        if (overdueBills.isNotEmpty()) {
                            Timber.w("⚠️ Found ${overdueBills.size} overdue bills: ${overdueBills.joinToString(", ") { it.name }}")
                        }
                        
                        val upcomingBills = bills.filter { !it.isOverdue() && it.getDaysUntilDue() <= 7 }
                        if (upcomingBills.isNotEmpty()) {
                            Timber.i("📅 Found ${upcomingBills.size} bills due within 7 days: ${upcomingBills.joinToString(", ") { "${it.name} (${it.getDaysUntilDue()} days)" }}")
                        }
                    } else {
                        Timber.d("💰 No bills found for the given parameters")
                    }
                    
                    error = null
                } catch (e: Exception) {
                    Timber.e(e, "⚠️ JSON parsing error for bills data")
                    error = "Error parsing bills: ${e.localizedMessage}"
                }
            } ?: run {
                Timber.e("⚠️ Failed to fetch bills - null response")
                error = "No bills data found"
            }
        } catch (e: Exception) {
            Timber.e(e, "⚠️ Error loading bills")
            error = "Error loading bills: ${e.localizedMessage}"
        } finally {
            isLoading = false
            Timber.d("📊 Bills fetch completed. Loading: $isLoading, Error: $error, Bills count: ${bills.size}")
        }
    }

    LaunchedEffect(key1 = true) {
        Timber.i("🚀 BillsScreen LaunchedEffect triggered")
        
        val householdId = UserDataManager.getUserHouseholdId(context)
        Timber.d("🏠 Retrieved household ID: $householdId")
        
        if (householdId != null) {
            if (!SocketManager.isInitialized()) {
                Timber.i("🔌 Initializing SocketManager for bills")
                SocketManager.initialize(householdId)

                SocketManager.setUpdateBillsCallback {
                    Timber.i("📡 Received update-bills socket event")
                    coroutineScope.launch {
                        fetchBills()
                    }
                }
            } else {
                Timber.d("🔌 SocketManager already initialized, setting bills callback")
                SocketManager.setUpdateBillsCallback {
                    Timber.i("📡 Received update-bills socket event")
                    coroutineScope.launch {
                        fetchBills()
                    }
                }
            }
        } else {
            Timber.e("⚠️ No household ID found, cannot initialize socket")
        }

        fetchBills()
    }

    DisposableEffect(key1 = Unit) {
        Timber.d("🧹 BillsScreen DisposableEffect setup")
        onDispose {
            Timber.d("🧹 BillsScreen disposing")
            coroutineScope.launch {
                val householdId = UserDataManager.getUserHouseholdId(context)
                if (householdId != null && SocketManager.isInitialized()) {
                    Timber.d("🔌 Disconnecting from socket")
                    SocketManager.disconnect(householdId)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Bills",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        when {
            isLoading -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
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
            bills.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No bills found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Bills will appear here when they are created",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            else -> {
                BillsList(bills = bills, modifier = modifier)
            }
        }
    }
}

@Composable
fun BillsList(
    bills: List<Bill>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(bills) { bill ->
            BillCard(bill = bill)
        }
    }
}

@Composable
fun BillCard(
    bill: Bill,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation_animation"
    )

    val isOverdue = bill.isOverdue()
    val daysUntilDue = bill.getDaysUntilDue()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bill.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isOverdue) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Overdue",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "Due: ${bill.getFormattedDueDate()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        if (!isOverdue && daysUntilDue <= 7) {
                            Text(
                                text = " (${daysUntilDue} days)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Medium
                            )
                        } else if (isOverdue) {
                            Text(
                                text = " (${Math.abs(daysUntilDue)} days overdue)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = bill.getFormattedAmount(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    if (bill.description.isNotBlank()) {
                        DetailRow(
                            icon = Icons.Default.Description,
                            label = "Description",
                            value = bill.description
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    DetailRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Created",
                        value = bill.getFormattedCreatedDate()
                    )

                    bill.createdBy?.let { creator ->
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow(
                            icon = Icons.Default.Person,
                            label = "Created by",
                            value = creator.username
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