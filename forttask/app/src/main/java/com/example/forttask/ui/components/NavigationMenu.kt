package com.example.forttask.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.forttask.ui.navigation.NavigationItem
import androidx.compose.material3.MaterialTheme

@Composable
fun NavigationStateKeeper(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    isHorizontal: Boolean = false
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavigationItem.Overview.route

    var selectedItem by rememberSaveable { mutableStateOf(currentRoute) }

    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.destination?.route?.let {
            selectedItem = it
        }
    }

    fun navigateToDestination(route: String) {
        if (route != selectedItem) {
            selectedItem = route
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    if (isHorizontal) {
        NavigationRail(
            modifier = modifier,
            selectedItem = selectedItem,
            onNavigate = { route -> navigateToDestination(route) }
        )
    } else {
        NavigationBar(
            modifier = modifier,
            selectedItem = selectedItem,
            onNavigate = { route -> navigateToDestination(route) }
        )
    }
}

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    selectedItem: String = NavigationItem.Overview.route,
    onNavigate: (String) -> Unit = {}
) {
    NavigationBar(
        modifier = modifier,
    ) {
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Receipt, contentDescription = "Bills") },
            label = { Text("Bills") },
            selected = selectedItem == NavigationItem.Bills.route,
            onClick = { onNavigate(NavigationItem.Bills.route) }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.CleaningServices, contentDescription = "Chores") },
            label = { Text("Chores") },
            selected = selectedItem == NavigationItem.Chores.route,
            onClick = { onNavigate(NavigationItem.Chores.route) }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Overview") },
            selected = selectedItem == NavigationItem.Overview.route,
            onClick = { onNavigate(NavigationItem.Overview.route) }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Event, contentDescription = "Events") },
            label = { Text("Events") },
            selected = selectedItem == NavigationItem.Events.route,
            onClick = { onNavigate(NavigationItem.Events.route) }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Shopping list") },
            label = { Text("List") },
            selected = selectedItem == NavigationItem.ShoppingList.route,
            onClick = { onNavigate(NavigationItem.ShoppingList.route) }
        )
    }
}

@Composable
fun NavigationRail(
    modifier: Modifier = Modifier,
    selectedItem: String = NavigationItem.Overview.route,
    onNavigate: (String) -> Unit = {}
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            NavigationRailItem(
                icon = { Icon(imageVector = Icons.Default.Receipt, contentDescription = "Bills") },
                label = { Text("Bills") },
                selected = selectedItem == NavigationItem.Bills.route,
                onClick = { onNavigate(NavigationItem.Bills.route) }
            )
            NavigationRailItem(
                icon = { Icon(imageVector = Icons.Default.CleaningServices, contentDescription = "Chores") },
                label = { Text("Chores") },
                selected = selectedItem == NavigationItem.Chores.route,
                onClick = { onNavigate(NavigationItem.Chores.route) }
            )
            NavigationRailItem(
                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Overview") },
                selected = selectedItem == NavigationItem.Overview.route,
                onClick = { onNavigate(NavigationItem.Overview.route) }
            )
            NavigationRailItem(
                icon = { Icon(imageVector = Icons.Default.Event, contentDescription = "Events") },
                label = { Text("Events") },
                selected = selectedItem == NavigationItem.Events.route,
                onClick = { onNavigate(NavigationItem.Events.route) }
            )
            NavigationRailItem(
                icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Shopping list") },
                label = { Text("List") },
                selected = selectedItem == NavigationItem.ShoppingList.route,
                onClick = { onNavigate(NavigationItem.ShoppingList.route) }
            )
        }
    }
}
