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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forttask.network.ApiService.getProtectedData
import com.example.forttask.network.SocketManager
import com.example.forttask.network.UserDataManager
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber

@Composable
fun ShoppingListScreen(modifier: Modifier = Modifier) {
    var shoppingItems by remember { mutableStateOf<List<ShoppingItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentFilter by remember { mutableStateOf(ShoppingFilter.ALL) }
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
                    Timber.d(
                        "📄 Raw response: ${jsonString.take(200)}${if (jsonString.length > 200) "..." else ""}"
                    )

                    shoppingItems = json.decodeFromString<List<ShoppingItem>>(jsonString)
                    val itemCount = shoppingItems.size

                    Timber.i(
                        "✨ Successfully loaded $itemCount shopping items (limit=$limit, skip=$skip)"
                    )
                    if (itemCount > 0) {
                        val itemSummary = shoppingItems.joinToString(", ") {
                            "${it.name} (${it.getFormattedCost()})"
                        }
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
            Timber.d(
                "📊 Shopping items fetch completed. Loading: $isLoading, Error: $error, Items count: ${shoppingItems.size}"
            )
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
                    coroutineScope.launch { fetchShoppingItems() }
                }
            } else {
                Timber.d("🔌 SocketManager already initialized, setting shopping list callback")
                SocketManager.setUpdateShoppingListCallback {
                    Timber.i("📡 Received update-shopping-list socket event")
                    coroutineScope.launch { fetchShoppingItems() }
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

        if (shoppingItems.isNotEmpty()) {
            FilterChipsRow(currentFilter = currentFilter,
                onFilterChanged = { currentFilter = it },
                allItemsCount = shoppingItems.size,
                boughtItemsCount = shoppingItems.count { it.isBought() },
                unboughtItemsCount = shoppingItems.count { !it.isBought() })
            Spacer(modifier = Modifier.height(16.dp))
        }

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
                val filteredItems = when (currentFilter) {
                    ShoppingFilter.ALL -> shoppingItems
                    ShoppingFilter.UNBOUGHT -> shoppingItems.filter { !it.isBought() }
                    ShoppingFilter.BOUGHT -> shoppingItems.filter { it.isBought() }
                }

                if (filteredItems.isEmpty()) {
                    EmptyFilterStateCard(currentFilter)
                } else {
                    Column {
                        TotalCostCard(items = filteredItems, currentFilter = currentFilter)
                        Spacer(modifier = Modifier.height(16.dp))
                        ShoppingItemsList(items = filteredItems, modifier = modifier)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp), contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
    }
}

@Composable
private fun ErrorCard(errorMessage: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp), contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = CircleShape
                        ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No items found",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Shopping items will appear here when they are added",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    currentFilter: ShoppingFilter,
    onFilterChanged: (ShoppingFilter) -> Unit,
    allItemsCount: Int,
    boughtItemsCount: Int,
    unboughtItemsCount: Int
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = currentFilter == ShoppingFilter.ALL,
            onClick = { onFilterChanged(ShoppingFilter.ALL) },
            label = { Text("All ($allItemsCount)") },
            leadingIcon = if (currentFilter == ShoppingFilter.ALL) {
                {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else null)

        FilterChip(selected = currentFilter == ShoppingFilter.BOUGHT,
            onClick = { onFilterChanged(ShoppingFilter.BOUGHT) },
            label = { Text("Bought ($boughtItemsCount)") },
            leadingIcon = if (currentFilter == ShoppingFilter.BOUGHT) {
                {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else null)

        FilterChip(selected = currentFilter == ShoppingFilter.UNBOUGHT,
            onClick = { onFilterChanged(ShoppingFilter.UNBOUGHT) },
            label = { Text("Unbought ($unboughtItemsCount)") },
            leadingIcon = if (currentFilter == ShoppingFilter.UNBOUGHT) {
                {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else null)
    }
}

@Composable
private fun EmptyFilterStateCard(currentFilter: ShoppingFilter) {
    val message = when (currentFilter) {
        ShoppingFilter.BOUGHT -> "No bought items found"
        ShoppingFilter.UNBOUGHT -> "No unbought items found"
        ShoppingFilter.ALL -> "No items found"
    }

    val description = when (currentFilter) {
        ShoppingFilter.BOUGHT -> "Items will appear here when they are marked as bought"
        ShoppingFilter.UNBOUGHT -> "Items will appear here when they are added but not yet bought"

        ShoppingFilter.ALL -> "Shopping items will appear here when they are added"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp), contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = CircleShape
                        ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun TotalCostCard(
    items: List<ShoppingItem>, currentFilter: ShoppingFilter = ShoppingFilter.ALL
) {
    val totalCost = items.sumOf { it.cost }

    val titleText = when (currentFilter) {
        ShoppingFilter.ALL -> "Total Cost"
        ShoppingFilter.BOUGHT -> "Total Bought"
        ShoppingFilter.UNBOUGHT -> "Total Unbought"
    }

    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = when (currentFilter) {
                ShoppingFilter.BOUGHT -> MaterialTheme.colorScheme.secondaryContainer

                ShoppingFilter.UNBOUGHT -> MaterialTheme.colorScheme.tertiaryContainer

                ShoppingFilter.ALL -> MaterialTheme.colorScheme.primaryContainer
            }
        ), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium,
                    color = when (currentFilter) {
                        ShoppingFilter.BOUGHT -> MaterialTheme.colorScheme.onSecondaryContainer

                        ShoppingFilter.UNBOUGHT -> MaterialTheme.colorScheme.onTertiaryContainer

                        ShoppingFilter.ALL -> MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${items.size} item${if (items.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (currentFilter) {
                        ShoppingFilter.BOUGHT -> MaterialTheme.colorScheme.onSecondaryContainer.copy(
                            alpha = 0.8f
                        )

                        ShoppingFilter.UNBOUGHT -> MaterialTheme.colorScheme.onTertiaryContainer.copy(
                            alpha = 0.8f
                        )

                        ShoppingFilter.ALL -> MaterialTheme.colorScheme.onPrimaryContainer.copy(
                            alpha = 0.8f
                        )
                    }
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = when (currentFilter) {
                        ShoppingFilter.BOUGHT -> MaterialTheme.colorScheme.onSecondaryContainer

                        ShoppingFilter.UNBOUGHT -> MaterialTheme.colorScheme.onTertiaryContainer

                        ShoppingFilter.ALL -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
                Text(
                    text = "%.2f".format(totalCost),
                    style = MaterialTheme.typography.headlineSmall,
                    color = when (currentFilter) {
                        ShoppingFilter.BOUGHT -> MaterialTheme.colorScheme.onSecondaryContainer

                        ShoppingFilter.UNBOUGHT -> MaterialTheme.colorScheme.onTertiaryContainer

                        ShoppingFilter.ALL -> MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ShoppingItemsList(items: List<ShoppingItem>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)
    ) { items(items) { item -> ShoppingItemCard(item = item) } }
}

@Composable
private fun ShoppingItemCard(item: ShoppingItem, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "rotation_animation"
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                expanded = !expanded
            }, colors = CardDefaults.cardColors(
            containerColor = if (item.isBought()) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = if (item.isBought()) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f)
                        )

                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                            color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape
                        ), contentAlignment = Alignment.Center
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

                    if (item.isBought()) {
                        item.boughtBy?.let { buyer ->
                            Spacer(modifier = Modifier.height(16.dp))
                            DetailRow(
                                icon = Icons.Default.Person,
                                label = "Bought by",
                                value = buyer.username
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier
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

enum class ShoppingFilter {
    ALL, UNBOUGHT, BOUGHT
}
