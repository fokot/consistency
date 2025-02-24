package com.fokot.consistency

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.fokot.consistency.navigation.Screen
import com.fokot.consistency.screens.*

@Composable
@Preview
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.HABITS) }

    MaterialTheme {
        Scaffold(
            bottomBar = {
                BottomNavigation {
                    BottomNavigationItem(
                        icon = { Text("H") },
                        label = { Text("Habits") },
                        selected = currentScreen == Screen.HABITS,
                        onClick = { currentScreen = Screen.HABITS }
                    )
                    BottomNavigationItem(
                        icon = { Text("S") },
                        label = { Text("Statistics") },
                        selected = currentScreen == Screen.STATISTICS,
                        onClick = { currentScreen = Screen.STATISTICS }
                    )
                    BottomNavigationItem(
                        icon = { Text("⚙") },
                        label = { Text("Settings") },
                        selected = currentScreen == Screen.SETTINGS,
                        onClick = { currentScreen = Screen.SETTINGS }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (currentScreen) {
                    Screen.HABITS -> HabitsScreen()
                    Screen.STATISTICS -> StatisticsScreen()
                    Screen.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}