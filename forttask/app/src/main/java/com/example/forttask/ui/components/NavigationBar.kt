package com.example.forttask.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    var selectedItem by rememberSaveable { mutableStateOf("overview") }

    NavigationBar(
        modifier = modifier,
    ) {
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Receipt, contentDescription = "Bills") },
            label = { Text("Bills") },
            selected = selectedItem == "bills",
            onClick = { /* todo: Navigate to Bills screen */ }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.CleaningServices, contentDescription = "Chores") },
            label = { Text("Chores") },
            selected = selectedItem == "chores",
            onClick = { /* todo: Navigate to Chores screen */ }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Overview") },
            selected = selectedItem == "overview",
            onClick = {
                navController.navigate("overview")
                selectedItem = "overview"
            }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Event, contentDescription = "Events") },
            label = { Text("Events") },
            selected = selectedItem == "events",
            onClick = {
                navController.navigate("events")
                selectedItem = "events"
            }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Shopping list") },
            label = { Text("List") },
            selected = selectedItem == "list",
            onClick = { /* todo: Navigate to Bills screen */ }
        )
    }
}