package com.dayxday.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dayxday.app.ui.calendar.CalendarScreen
import com.dayxday.app.ui.datatypes.DataTypesScreen
import com.dayxday.app.ui.day.DayDetailSheet
import com.dayxday.app.ui.stats.StatsScreen
import com.dayxday.app.ui.theme.DayXDayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DayXDayTheme {
                DayXDayApp()
            }
        }
    }
}

private sealed class Screen(val route: String, val label: String) {
    data object Calendar : Screen("calendar", "Calendar")
    data object Stats : Screen("stats", "Stats")
    data object DataTypes : Screen("types", "Types")
}

@Composable
fun DayXDayApp(viewModel: TrackerViewModel = viewModel()) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val selectedDate by viewModel.selectedDate.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Screen.Calendar.route,
                    onClick = { navController.navigate(Screen.Calendar.route) {
                        popUpTo(Screen.Calendar.route) { inclusive = true }
                        launchSingleTop = true
                    } },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                    label = { Text(Screen.Calendar.label) }
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Stats.route,
                    onClick = { navController.navigate(Screen.Stats.route) {
                        popUpTo(Screen.Calendar.route)
                        launchSingleTop = true
                    } },
                    icon = { Icon(Icons.Default.Insights, contentDescription = "Stats") },
                    label = { Text(Screen.Stats.label) }
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.DataTypes.route,
                    onClick = { navController.navigate(Screen.DataTypes.route) {
                        popUpTo(Screen.Calendar.route)
                        launchSingleTop = true
                    } },
                    icon = { Icon(Icons.Default.Category, contentDescription = "Data types") },
                    label = { Text(Screen.DataTypes.label) }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Calendar.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Calendar.route) {
                CalendarScreen(viewModel)
            }
            composable(Screen.Stats.route) {
                StatsScreen(viewModel)
            }
            composable(Screen.DataTypes.route) {
                DataTypesScreen(viewModel)
            }
        }
    }

    if (selectedDate != null) {
        DayDetailSheet(viewModel)
    }
}
