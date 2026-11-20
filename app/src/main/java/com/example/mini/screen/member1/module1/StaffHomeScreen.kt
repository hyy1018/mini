// #member1
// Staff (management) home page: icon grid grouped by category, same style as the Resident home.
// A red banner appears on top whenever an emergency alert is still open.
package com.example.mini.screen.member1.module1

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sos
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.data.CommunityData
import com.example.mini.data.ComplaintData
import com.example.mini.data.FacilityData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.model.RequestStatus
import com.example.mini.nav.safeNavigate
import com.example.mini.screen.HomeShortcut
import com.example.mini.screen.IconGridSection
import com.example.mini.screen.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffHomeScreen(navController: NavHostController) {
    val myId = UserSession.currentUserId ?: -1
    val openEmergencies = CommunityData.activeEmergencies().size
    val open: (String) -> Unit = { route -> navController.safeNavigate(route) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Management") },
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
            RoleGreeting(userId = myId, role = "Management Staff")

            // an open emergency must not be just a small badge
            if (openEmergencies > 0) {
                EmergencyBanner(openEmergencies) { open("emergency_alerts") }
            }

            // Module 1 & 2
            IconGridSection(
                "Residents & Visitors",
                listOf(
                    HomeShortcut("Approvals", Icons.Filled.HowToReg, "staff_registrations", ResidentData.pendingRegistrations().size),
                    HomeShortcut("Residents", Icons.Filled.People, "staff_residents"),
                    HomeShortcut("Visitors", Icons.Filled.Badge, "visitor_records"),
                    HomeShortcut("Accounts", Icons.Filled.ManageAccounts, "staff_accounts")
                ),
                open
            )
            // Module 3 & 6
            IconGridSection(
                "Community",
                listOf(
                    HomeShortcut("News & Events", Icons.Filled.Campaign, "community_engagement?tab=0"),
                    HomeShortcut("Messages", Icons.AutoMirrored.Filled.Chat, "messages", CommunityData.totalUnread(myId)),
                    HomeShortcut("Reports", Icons.Filled.Flag, "post_reports", CommunityData.pendingReports().size)
                ),
                open
            )
            // Module 4 & 5
            IconGridSection(
                "Facilities & Requests",
                listOf(
                    HomeShortcut("Facilities", Icons.Filled.EventAvailable, "facility_maintenance?tab=0"),
                    HomeShortcut(
                        "Repairs", Icons.Filled.Build, "facility_maintenance?tab=2",
                        FacilityData.maintenanceRequests.count { it.requestStatus == RequestStatus.PENDING }
                    ),
                    HomeShortcut(
                        "Complaints", Icons.Filled.Feedback, "complaint_feedback?tab=0",
                        ComplaintData.complaints.count { it.complaintStatus == RequestStatus.PENDING }
                    )
                ),
                open
            )
            // Module 6
            IconGridSection(
                "Emergency",
                listOf(
                    HomeShortcut("Alerts", Icons.Filled.Sos, "emergency_alerts", openEmergencies),
                    HomeShortcut("Contacts", Icons.Filled.Call, "emergency_contacts")
                ),
                open
            )
        }
    }
}

// Coloured header card at the top of the Staff and Security home pages, with the profile photo
@Composable
fun RoleGreeting(userId: Int, role: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Welcome,", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                Text(
                    ResidentData.getUser(userId)?.name ?: "",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(role, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary)
            }
            UserAvatar(
                userId,
                size = 56.dp,
                container = MaterialTheme.colorScheme.onPrimary,
                content = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Red strip shown on the Staff / Security home while an emergency alert is still open
@Composable
fun EmergencyBanner(openCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Sos, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(
                "$openCount open emergency alert(s) -- respond now",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

// Profile photo in the top bar of the Staff / Security home -- opens their "Me" page
@Composable
fun MeButton(navController: NavHostController) {
    IconButton(onClick = { navController.safeNavigate("account") }) {
        UserAvatar(UserSession.currentUserId ?: -1, size = 30.dp)
    }
}
