// #member1
// Bottom nav bar shared by the Resident Home and its sibling tab screens.
package com.example.mini.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import androidx.navigation.compose.currentBackStackEntryAsState

const val RESIDENT_HOME_ROUTE = "resident_home"

private data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(RESIDENT_HOME_ROUTE, "Home", Icons.Filled.Home),
    BottomNavItem("visitor_security", "Visitors", Icons.Filled.ConfirmationNumber),
    BottomNavItem("community_emergency", "Community", Icons.Filled.Forum),
    BottomNavItem("resident_household", "Me", Icons.Filled.Person)
)

@Composable
fun ResidentBottomBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        if (item.route == RESIDENT_HOME_ROUTE) {
                            // Home is already at the bottom of the resident back stack, so just pop back to it
                            navController.popBackStack(RESIDENT_HOME_ROUTE, inclusive = false)
                        } else {
                            navController.safeNavigate(item.route) {
                                popUpTo(RESIDENT_HOME_ROUTE) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
