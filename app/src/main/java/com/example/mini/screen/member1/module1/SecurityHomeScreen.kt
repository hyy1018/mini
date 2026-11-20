// #member1
// Security guard home page: icon grid grouped by category, same style as the Resident and Staff homes.
// A red banner appears on top whenever an emergency alert is still open.
package com.example.mini.screen.member1.module1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.data.CommunityData
import com.example.mini.data.UserSession
import com.example.mini.data.VisitorData
import com.example.mini.nav.safeNavigate
import com.example.mini.screen.HomeShortcut
import com.example.mini.screen.IconGridSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityHomeScreen(navController: NavHostController) {
    val myId = UserSession.currentUserId ?: -1
    val openEmergencies = CommunityData.activeEmergencies().size
    val open: (String) -> Unit = { route -> navController.safeNavigate(route) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security") },
                actions = {
                    NotificationBell(navController)
                    MeButton(navController)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            RoleGreeting(userId = myId, role = "Security Guard")

            // Module 6 -- security receives emergency alerts too
            if (openEmergencies > 0) {
                EmergencyBanner(openEmergencies) { open("emergency_alerts") }
            }

            // Module 2 -- Smart Visitor Pass
            IconGridSection(
                "Visitors",
                listOf(
                    HomeShortcut("Verify", Icons.Filled.QrCodeScanner, "verify_visitor"),
                    HomeShortcut("Exit", Icons.AutoMirrored.Filled.ExitToApp, "visitor_exit", VisitorData.activeVisits().size),
                    HomeShortcut("Records", Icons.Filled.Badge, "visitor_records")
                ),
                open
            )
            // Module 6
            IconGridSection(
                "Emergency & Messages",
                listOf(
                    HomeShortcut("Alerts", Icons.Filled.Sos, "emergency_alerts", openEmergencies),
                    HomeShortcut("Messages", Icons.AutoMirrored.Filled.Chat, "messages", CommunityData.totalUnread(myId))
                ),
                open
            )
        }
    }
}
