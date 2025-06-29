package com.example.forttask.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

import com.example.forttask.ui.screens.overview.OverviewScreen
import com.example.forttask.ui.screens.events.EventsScreen
import com.example.forttask.ui.screens.login.LoginScreen
import com.example.forttask.ui.screens.bills.BillsScreen
import com.example.forttask.ui.screens.shoppinglist.ShoppingListScreen
import com.example.forttask.ui.screens.chores.ChoresScreen
import com.example.forttask.ui.screens.passwordvault.PasswordVaultScreen

enum class Screen {
    OVERVIEW,
    LOGIN,
    EVENTS,
    CHORES,
    BILLS,
    SHOPPING_LIST,
    PASSWORD_VAULT,
}

sealed class NavigationItem(val route: String) {
    object Login : NavigationItem(Screen.LOGIN.name)
    object Overview : NavigationItem(Screen.OVERVIEW.name)
    object Events : NavigationItem(Screen.EVENTS.name)
    object Chores : NavigationItem(Screen.CHORES.name)
    object Bills : NavigationItem(Screen.BILLS.name)
    object ShoppingList : NavigationItem(Screen.SHOPPING_LIST.name)
    object PasswordVault : NavigationItem(Screen.PASSWORD_VAULT.name)
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = NavigationItem.Login.route
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavigationItem.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(NavigationItem.Overview.route) {
            OverviewScreen()
        }
        composable(NavigationItem.Events.route) {
            EventsScreen()
        }
        composable(NavigationItem.Chores.route) {
            ChoresScreen()
        }
        composable(NavigationItem.Bills.route) {
            BillsScreen()
        }
        composable(NavigationItem.ShoppingList.route) {
            ShoppingListScreen()
        }
        composable(NavigationItem.PasswordVault.route) {
            PasswordVaultScreen(navController = navController)
        }
    }
}

