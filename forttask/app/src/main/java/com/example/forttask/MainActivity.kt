package com.example.forttask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.example.forttask.ui.theme.ForttaskTheme
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue

import com.example.forttask.ui.navigation.AppNavHost
import com.example.forttask.ui.components.NavigationStateKeeper
import com.example.forttask.ui.navigation.NavigationItem

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForttaskTheme {
                val navController = rememberNavController()
                val windowSize = calculateWindowSizeClass(this)
                val backStackEntry by navController.currentBackStackEntryAsState()

                ForttaskApp(
                    navController = navController,
                    windowSize = windowSize,
                    backStackEntry = backStackEntry
                )
            }
        }
    }
}

@Composable
fun ForttaskApp(
    navController: NavHostController,
    windowSize: WindowSizeClass,
    backStackEntry: androidx.navigation.NavBackStackEntry? = null
) {
    when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> ForttaskVerticalApp(navController, backStackEntry)
        WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> ForttaskHorizontalApp(navController, backStackEntry)
        else -> ForttaskVerticalApp(navController, backStackEntry)
    }
}

@Composable
fun ForttaskVerticalApp(
    navController: NavHostController,
    backStackEntry: androidx.navigation.NavBackStackEntry? = null
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (backStackEntry?.destination?.route != NavigationItem.Login.route &&
                backStackEntry?.destination?.route != NavigationItem.PasswordVault.route) {
                NavigationStateKeeper(
                    navController = navController,
                    isHorizontal = false
                )
            }
        }
    ) { innerPadding ->
        AppNavHost(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            navController = navController,
            startDestination = NavigationItem.Login.route
        )
    }
}

@Composable
fun ForttaskHorizontalApp(
    navController: NavHostController,
    backStackEntry: androidx.navigation.NavBackStackEntry? = null
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (backStackEntry?.destination?.route != NavigationItem.Login.route &&
                backStackEntry?.destination?.route != NavigationItem.PasswordVault.route) {
                NavigationStateKeeper(
                    navController = navController,
                    isHorizontal = true
                )
            }
            AppNavHost(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                navController = navController,
                startDestination = NavigationItem.Login.route
            )
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 640,
)
@Composable
fun AppPreview() {
    val navController = rememberNavController()
    val windowSize = calculateWindowSizeClass(MainActivity())

    ForttaskApp(
        navController = navController,
        windowSize = windowSize
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(
    showBackground = true,
    widthDp = 600,
    heightDp = 400,
)
@Composable
fun AppPreviewHorizontal() {
    val navController = rememberNavController()
    val windowSize = calculateWindowSizeClass(MainActivity())

    ForttaskApp(
        navController = navController,
        windowSize = windowSize
    )
}
