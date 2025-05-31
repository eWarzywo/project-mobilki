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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BillsScreen(viewModel: BillsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.loadBills(context) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            text = "Bills",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            FilterChip(
                onClick = { viewModel.switchFilter(context, BillFilter.NOTPAID) },
                label = { Text("Not Paid") },
                selected = uiState.currentFilter == BillFilter.NOTPAID,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null)
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
                onClick = { viewModel.switchFilter(context, BillFilter.PAID) },
                label = { Text("Paid") },
                selected = uiState.currentFilter == BillFilter.PAID,
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

        Spacer(modifier = Modifier.height(16.dp))

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
                }
            }

            uiState.bills.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors =
                        CardDefaults.cardColors(
                            containerColor =
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier =
                                Modifier
                                    .size(80.dp)
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
                                    if (uiState.currentFilter == BillFilter.NOTPAID) {
                                        Icons.Default.Receipt
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
                                if (uiState.currentFilter == BillFilter.NOTPAID) {
                                    "No unpaid bills"
                                } else {
                                    "No paid bills"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text =
                                if (uiState.currentFilter == BillFilter.NOTPAID) {
                                    "Unpaid bills will appear here when they are created"
                                } else {
                                    "Paid bills will appear here when they are marked as paid"
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
                ) {
                    items(uiState.bills) { bill ->
                        BillCard(bill = bill, currentFilter = uiState.currentFilter)
                    }
                }
            }
        }
    }
}

@Composable
fun BillCard(bill: Bill, currentFilter: BillFilter, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by
    animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation_animation"
    )

    val isPaid = currentFilter == BillFilter.PAID
    val isOverdue = bill.isOverdue() && !isPaid
    val daysUntilDue = bill.getDaysUntilDue()

    Card(
        modifier =
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                expanded = !expanded
            },
        colors =
        CardDefaults.cardColors(
            containerColor =
            if (isPaid) {
                MaterialTheme.colorScheme.secondaryContainer
            } else if (isOverdue) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = bill.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color =
                            if (isPaid) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint =
                            if (isPaid) {
                                MaterialTheme.colorScheme.primary
                            } else if (isOverdue) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "Due: ${bill.getFormattedDueDate()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                            if (isPaid) {
                                MaterialTheme.colorScheme.primary
                            } else if (isOverdue) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            fontWeight = FontWeight.Medium
                        )
                        if (!isPaid && !isOverdue && daysUntilDue <= 7) {
                            Text(
                                text = " (${daysUntilDue} days)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Medium
                            )
                        } else if (!isPaid && isOverdue) {
                            Text(
                                text = " (${Math.abs(daysUntilDue)} days overdue)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                if (isPaid) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Paid",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Box(
                        modifier =
                        Modifier
                            .size(40.dp)
                            .background(
                                color =
                                MaterialTheme.colorScheme
                                    .primaryContainer,
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
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)) {
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

            if (isPaid) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ Bill has been paid",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
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
