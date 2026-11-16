// #member1
// Security guard home page: verify visitor QR (entry), record exit, and visitor records.
package com.example.mini.screen.member1.module1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.mini.nav.safeNavigate
import com.example.mini.data.CommunityData
import com.example.mini.data.UserSession
import com.example.mini.data.VisitorData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityHomeScreen(navController: NavHostController) {
    val insideCount = VisitorData.activeVisits().size
    val openEmergencies = CommunityData.activeEmergencies().size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security") },
                navigationIcon = {
                    IconButton(onClick = { backToLogin(navController) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to login")
                    }
                },
                actions = { NotificationBell(navController) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RoleGreeting(name = UserSession.currentUser?.name ?: "", role = "Security Guard")

            // Module 6 -- Emergency Alert System: security receives alerts too
            HomeActionCard(
                icon = Icons.Filled.Sos,
                title = "Emergency Alerts",
                subtitle = if (openEmergencies > 0) "$openEmergencies open alert(s) -- respond now" else "No open alerts",
                badgeCount = openEmergencies,
                onClick = { navController.safeNavigate("emergency_alerts") }
            )

            // Module 2 -- Smart Visitor Pass
            HomeActionCard(
                icon = Icons.Filled.QrCodeScanner,
                title = "Verify Visitor (Entry)",
                subtitle = "Scan or enter the visitor's QR pass",
                onClick = { navController.safeNavigate("verify_visitor") }
            )
            HomeActionCard(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Record Exit",
                subtitle = "$insideCount visitor(s) inside right now",
                badgeCount = insideCount,
                onClick = { navController.safeNavigate("visitor_exit") }
            )
            HomeActionCard(
                icon = Icons.Filled.Badge,
                title = "Visitor Records",
                subtitle = "Search all visitor passes and visit logs",
                onClick = { navController.safeNavigate("visitor_records") }
            )
        }
    }
}
