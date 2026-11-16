// #member1
// Staff (management) home page: one entry per management task across all six modules.
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.data.CommunityData
import com.example.mini.data.ComplaintData
import com.example.mini.data.FacilityData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.model.RequestStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffHomeScreen(navController: NavHostController) {
    val pendingCount = ResidentData.pendingRegistrations().size
    val openEmergencies = CommunityData.activeEmergencies().size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Management") },
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
            RoleGreeting(name = UserSession.currentUser?.name ?: "", role = "Management Staff")

            // Module 6 -- open emergencies always come first
            if (openEmergencies > 0) {
                HomeActionCard(
                    icon = Icons.Filled.Sos,
                    title = "Emergency Alerts",
                    subtitle = "$openEmergencies open alert(s) need attention",
                    badgeCount = openEmergencies,
                    onClick = { navController.safeNavigate("emergency_alerts") }
                )
            }

            SectionLabel("Residents & Visitors")
            // Module 1
            HomeActionCard(
                icon = Icons.Filled.HowToReg,
                title = "Registration Approvals",
                subtitle = "Approve or reject new resident registrations",
                badgeCount = pendingCount,
                onClick = { navController.safeNavigate("staff_registrations") }
            )
            HomeActionCard(
                icon = Icons.Filled.People,
                title = "Residents",
                subtitle = "View, edit and remove resident records",
                onClick = { navController.safeNavigate("staff_residents") }
            )
            // Module 2
            HomeActionCard(
                icon = Icons.Filled.Badge,
                title = "Visitor Records",
                subtitle = "All visitor passes with entry and exit times",
                onClick = { navController.safeNavigate("visitor_records") }
            )

            SectionLabel("Community")
            // Module 3
            HomeActionCard(
                icon = Icons.Filled.Campaign,
                title = "Announcements & Events",
                subtitle = "Publish notices, manage events, rules and FAQ",
                onClick = { navController.safeNavigate("community_engagement?tab=0") }
            )
            // Module 6
            HomeActionCard(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = "Resident Enquiries",
                subtitle = "Reply to residents' chat messages",
                badgeCount = CommunityData.totalUnreadForManagement(),
                onClick = { navController.safeNavigate("chat_inbox") }
            )
            HomeActionCard(
                icon = Icons.Filled.Flag,
                title = "Reported Posts",
                subtitle = "Review posts reported by residents",
                badgeCount = CommunityData.pendingReports().size,
                onClick = { navController.safeNavigate("post_reports") }
            )

            SectionLabel("Facilities & Requests")
            // Module 4
            HomeActionCard(
                icon = Icons.Filled.EventAvailable,
                title = "Facilities & Bookings",
                subtitle = "Manage facilities and see all bookings",
                onClick = { navController.safeNavigate("facility_maintenance?tab=0") }
            )
            HomeActionCard(
                icon = Icons.Filled.Build,
                title = "Maintenance Requests",
                subtitle = "Assign technicians and update progress",
                badgeCount = FacilityData.maintenanceRequests.count { it.requestStatus == RequestStatus.PENDING },
                onClick = { navController.safeNavigate("facility_maintenance?tab=2") }
            )
            // Module 5
            HomeActionCard(
                icon = Icons.Filled.Feedback,
                title = "Complaints & Feedback",
                subtitle = "Respond to complaints, read feedback",
                badgeCount = ComplaintData.complaints.count { it.complaintStatus == RequestStatus.PENDING },
                onClick = { navController.safeNavigate("complaint_feedback?tab=0") }
            )

            SectionLabel("Emergency")
            // Module 6
            HomeActionCard(
                icon = Icons.Filled.Sos,
                title = "Emergency Alerts",
                subtitle = "Open alerts and history",
                badgeCount = openEmergencies,
                onClick = { navController.safeNavigate("emergency_alerts") }
            )
            HomeActionCard(
                icon = Icons.Filled.Call,
                title = "Emergency Contacts",
                subtitle = "Numbers residents can call",
                onClick = { navController.safeNavigate("emergency_contacts") }
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

// Coloured header card at the top of the Staff and Security home pages
@Composable
fun RoleGreeting(name: String, role: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Welcome,",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(role, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

// One tappable action row on the Staff / Security home pages, with an optional count badge
@Composable
fun HomeActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (badgeCount > 0) {
                    BadgedBox(badge = { Badge { Text("$badgeCount") } }) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                } else {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
