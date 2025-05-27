package com.example.forttask.ui.screens.events

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EventsScreen(
    modifier: Modifier = Modifier
) {
    Text(
        text = "Events Screen",
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}