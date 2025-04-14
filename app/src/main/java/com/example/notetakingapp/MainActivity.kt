package com.example.notetakingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.notetakingapp.ui.theme.NoteTakingAppTheme
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteTakingAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home_screen") {
        // Home screen route
        composable("home_screen") {
            HomePage(navController)
        }

        // Settings screen route
        composable("settings_screen") {
            SettingsPage(navController)
        }
    }

}

@Composable
fun HomePage(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        //Header
        Header(navController)

        //Sub-header
        SubHeader()

        //Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Text(text = "Notes go here")
        }
    }
}

@Composable
fun Header(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(Color(0xFF1e3e73))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Home Button
        IconButton(onClick = {navController.navigate("home_screen")})
        {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        // Settings Button
        IconButton(onClick = {navController.navigate("settings_screen")})
        {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

@Composable
fun SubHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF435f8c)),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "Sub-header for optional features",
            modifier = Modifier.padding(start = 16.dp),
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SettingsPage(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        //Header
        Header(navController)

        //Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Text(text = "Settings go here")
        }
    }
}


