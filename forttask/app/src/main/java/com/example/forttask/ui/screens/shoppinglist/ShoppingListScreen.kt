package com.example.forttask.ui.screens.shoppinglist

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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.forttask.network.ApiService.getProtectedData
import com.example.forttask.network.SocketManager
import com.example.forttask.network.UserDataManager
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber

@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier
) {
    var shoppingItems by remember { mutableStateOf<List<ShoppingItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun fetchShoppingItems(limit: Int = 10, skip: Int = 0) {
        Timber.i("🛒 Starting to fetch shopping items with limit=$limit, skip=$skip")
        isLoading = true
        
        try {
            val endpoint = "shoppingList?skip=$skip"
            Timber.d("📊 Fetching shopping items from endpoint: $endpoint")
            
            val response = getProtectedData(context, endpoint)
            
            response?.let { jsonString ->
                try {
                    Timber.d("🔍 Parsing shopping items response")
                    Timber.d("📄 Raw response: ${jsonString.take(200)}${if(jsonString.length > 200) "..." else ""}")
                    
                    shoppingItems = json.decodeFromString<List<ShoppingItem>>(jsonString)
                    val itemCount = shoppingItems.size
                    
                    Timber.i("✨ Successfully loaded $itemCount shopping items (limit=$limit, skip=$skip)")
                    if (itemCount > 0) {
                        val itemSummary = shoppingItems.joinToString(", ") { "${it.name} (${it.getFormattedCost()})" }
                        Timber.d("🛒 Shopping items: $itemSummary")
                        
                        val totalCost = shoppingItems.sumOf { it.cost }
                        Timber.d("💰 Total shopping list cost: $${"%.2f".format(totalCost)}")
                    } else {
                        Timber.d("🛒 No shopping items found for the given parameters")
                    }
                    
                    error = null
                } catch (e: Exception) {
                    Timber.e(e, "⚠️ JSON parsing error for shopping items data")
                    error = "Error parsing shopping items: ${e.localizedMessage}"
                }
            } ?: run {
                Timber.e("⚠️ Failed to fetch shopping items - null response")
                error = "No shopping items data found"
            }
        } catch (e: Exception) {
            Timber.e(e, "⚠️ Error loading shopping items")
            error = "Error loading shopping items: ${e.localizedMessage}"
        } finally {
            isLoading = false
            Timber.d("📊 Shopping items fetch completed. Loading: $isLoading, Error: $error, Items count: ${shoppingItems.size}")
        }
    }

    LaunchedEffect(key1 = true) {
        Timber.i("🚀 ShoppingListScreen LaunchedEffect triggered")
        
        val householdId = UserDataManager.getUserHouseholdId(context)
        Timber.d("🏠 Retrieved household ID: $householdId")
        
        if (householdId != null) {
            if (!SocketManager.isInitialized()) {
                Timber.i("🔌 Initializing SocketManager for shopping list")
                SocketManager.initialize(householdId)

                SocketManager.setUpdateShoppingListCallback {
                    Timber.i("📡 Received update-shopping-list socket event")
                    coroutineScope.launch {
                        fetchShoppingItems()
                    }
                }
            } else {
                Timber.d("🔌 SocketManager already initialized, setting shopping list callback")
                SocketManager.setUpdateShoppingListCallback {
                    Timber.i("📡 Received update-shopping-list socket event")
                    coroutineScope.launch {
                        fetchShoppingItems()
                    }
                }
            }
        } else {
            Timber.e("⚠️ No household ID found, cannot initialize socket")
        }

        fetchShoppingItems()
    }

    DisposableEffect(key1 = Unit) {
        Timber.d("🧹 ShoppingListScreen DisposableEffect setup")
        onDispose {
            Timber.d("🧹 ShoppingListScreen disposing")
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shopping List",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when {
            isLoading -> {
                LoadingCard()
            }
            error != null -> {
                ErrorCard(errorMessage = error)
            }
            shoppingItems.isEmpty() -> {
                EmptyStateCard()
            }
            else -> {
                ShoppingItemsList(items = shoppingItems, modifier = modifier)
            }
        }
    }
}

@Composable
private fun LoadingCard() {
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

@Composable
private fun ErrorCard(errorMessage: String?) {
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
                text = errorMessage ?: "Unknown error",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun EmptyStateCard() {
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
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No items found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Shopping items will appear here when they are added",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ShoppingItemsList(
    items: List<ShoppingItem>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            ShoppingItemCard(item = item)
        }
    }
}

@Composable
private fun ShoppingItemCard(
    item: ShoppingItem,
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
            .clip(RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
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
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = item.getFormattedCost(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "Added: ${item.getFormattedCreatedDate()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium
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

                    DetailRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Created",
                        value = item.getFormattedCreatedDate()
                    )

                    item.createdBy?.let { creator ->
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow(
                            icon = Icons.Default.Person,
                            label = "Added by",
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
    icon: ImageVector,
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