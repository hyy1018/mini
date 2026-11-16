// #member1
// Resident Home: greeting header, service icon grid, latest announcement, SOS button and bottom nav.
package com.example.mini.screen.member1.module1

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.data.EngagementData
import com.example.mini.data.MainHubData
import com.example.mini.data.NotificationData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.model.MainHubItem
import com.example.mini.screen.ResidentBottomBar
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentHomeScreen(navController: NavHostController) {
    val user = UserSession.currentUser
    val resident = UserSession.currentResident
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    IconButton(onClick = { backToLogin(navController) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to login")
                    }
                },
                actions = { NotificationBell(navController) }
            )
        },
        bottomBar = { ResidentBottomBar(navController) },
        floatingActionButton = {
            // SOS is always one tap away -- Emergency Alert lives in Module 6
            ExtendedFloatingActionButton(
                onClick = { navController.safeNavigate("emergency_sos") },
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                icon = { Icon(Icons.Filled.Sos, contentDescription = null) },
                // the Sos icon already reads "SOS", so the label says what it does
                text = { Text("Emergency") }
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
            // --- Greeting header ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "$greeting,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        user?.name ?: "",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        resident?.let { ResidentData.unitLabel(it.residentId) } ?: "Welcome back to your community",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // --- Service icon grid (4 per row) ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Services", style = MaterialTheme.typography.titleMedium)
                // chunked rows instead of LazyVerticalGrid, since a lazy grid can't sit inside a scrolling Column
                MainHubData.services.chunked(4).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { item ->
                            ServiceTile(
                                item = item,
                                onClick = { navController.safeNavigate(item.route) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // --- Latest announcement (Module 3) ---
            EngagementData.latestAnnouncement()?.let { latest ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Latest Announcement", style = MaterialTheme.typography.titleMedium)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.safeNavigate("announcement/${latest.announcementId}") }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Campaign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(latest.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    latest.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }

            // keeps the last card clear of the SOS button
            Spacer(Modifier.height(56.dp))
        }
    }
}

@Composable
private fun ServiceTile(item: MainHubItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            item.title,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}

// Bell with an unread count, shown on every role's home page; opens the notification list (Module 3)
@Composable
fun NotificationBell(navController: NavHostController) {
    val unread = UserSession.currentUserId?.let { NotificationData.unreadCount(it) } ?: 0
    IconButton(onClick = { navController.safeNavigate("notifications") }) {
        if (unread > 0) {
            BadgedBox(badge = { Badge { Text("$unread") } }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
            }
        } else {
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
        }
    }
}

// Logs out and clears the whole back stack so pressing back afterwards doesn't return into the app
fun backToLogin(navController: NavHostController) {
    UserSession.logout()
    navController.safeNavigate("login") {
        popUpTo(0) { inclusive = true }
    }
}
