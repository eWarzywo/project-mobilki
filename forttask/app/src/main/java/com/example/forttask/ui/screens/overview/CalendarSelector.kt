package com.example.forttask.ui.screens.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarSelector(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewingDate by remember { mutableStateOf(selectedDate) }
    
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthYearText = monthFormat.format(viewingDate)
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val calendar = Calendar.getInstance()
                calendar.time = viewingDate
                calendar.add(Calendar.MONTH, -1)
                viewingDate = calendar.time
            }) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous month"
                )
            }
            
            Text(
                text = monthYearText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = {
                val calendar = Calendar.getInstance()
                calendar.time = viewingDate
                calendar.add(Calendar.MONTH, 1)
                viewingDate = calendar.time
            }) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next month"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val scrollState = rememberScrollState()
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)
            
            val selectedCal = Calendar.getInstance()
            selectedCal.time = selectedDate
            selectedCal.set(Calendar.HOUR_OF_DAY, 0)
            selectedCal.set(Calendar.MINUTE, 0)
            selectedCal.set(Calendar.SECOND, 0)
            selectedCal.set(Calendar.MILLISECOND, 0)
            
            val viewingMonth = Calendar.getInstance()
            viewingMonth.time = viewingDate
            val month = viewingMonth.get(Calendar.MONTH)
            val year = viewingMonth.get(Calendar.YEAR)
            
            val startOfMonth = Calendar.getInstance()
            startOfMonth.set(year, month, 1, 0, 0, 0)
            startOfMonth.set(Calendar.MILLISECOND, 0)
            
            val daysInMonth = viewingMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            val dayFormat = SimpleDateFormat("E", Locale.getDefault())
            val dateFormat = SimpleDateFormat("d", Locale.getDefault())
            
            for (i in 0 until daysInMonth) {
                val currentCal = Calendar.getInstance()
                currentCal.time = startOfMonth.time
                currentCal.add(Calendar.DAY_OF_MONTH, i)
                
                val isSelected = currentCal.timeInMillis == selectedCal.timeInMillis
                val isToday = currentCal.timeInMillis == today.timeInMillis
                
                val backgroundColor = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.surface
                }
                
                val textColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onDateSelected(currentCal.time) }
                        .then(
                            if (isSelected) {
                                Modifier.background(backgroundColor)
                            } else if (isToday) {
                                Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = dayFormat.format(currentCal.time),
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dateFormat.format(currentCal.time),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}