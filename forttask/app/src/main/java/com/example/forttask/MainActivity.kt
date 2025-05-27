package com.example.forttask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.example.forttask.ui.theme.ForttaskTheme
import androidx.navigation.compose.rememberNavController

import com.example.forttask.ui.components.NavigationBar
import com.example.forttask.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForttaskTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        ForttaskApp(navController = navController)
                    }
                ) { innerPadding ->
                    AppNavHost(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        navController = navController,
                        startDestination = "overview"
                    )
                }
            }
        }
    }
}

@Composable
fun ForttaskApp(navController: NavHostController) {
    NavigationBar(navController = navController)
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 640,
)
@Composable
fun AppPreview() {
    val navController = rememberNavController()

    ForttaskTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                ForttaskApp(navController = navController)
            }
        ) { innerPadding ->
            AppNavHost(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                navController = navController,
                startDestination = "overview"
            )
        }
    }
}