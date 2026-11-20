// #member1
// "Me" tab: the resident's own profile, household details and household members (Flowchart 2, resident side).
package com.example.mini.screen.member1.module1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.screen.InfoRow
import com.example.mini.screen.RegistrationStatusBadge
import com.example.mini.screen.ResidentBottomBar
import com.example.mini.screen.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentHouseholdScreen(navController: NavHostController) {
    val user = UserSession.currentUser
    val resident = UserSession.currentResident
    val household = resident?.let { ResidentData.getHousehold(it.householdId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = { ResidentBottomBar(navController) }
    ) { innerPadding ->
        // nullable, not !!: this can compose for a moment right after logout
        if (user == null || resident == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Profile header ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        user.userId,
                        size = 64.dp,
                        container = MaterialTheme.colorScheme.primary,
                        content = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "ID: ${user.username} · ${resident.residentType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    RegistrationStatusBadge(resident.registrationStatus)
                }
            }

            // --- Personal details ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Personal Details", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.size(8.dp))
                    InfoRow("Email", user.email)
                    InfoRow("Phone", user.phoneNo)
                    InfoRow("IC Number", resident.icNo)
                }
            }

            // --- Household ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Household", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.size(8.dp))
                    InfoRow("Unit", household?.unitNo ?: "-")
                    InfoRow("Block", household?.blockName ?: "-")

                    Spacer(Modifier.size(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.size(8.dp))

                    // everyone registered under the same unit counts as a household member
                    Text("Household Members", style = MaterialTheme.typography.titleSmall)
                    ResidentData.householdMembers(resident.householdId).forEach { member ->
                        val memberUser = ResidentData.getUser(member.userId)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    (memberUser?.name ?: "-") + if (member.residentId == resident.residentId) " (You)" else "",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    member.residentType,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RegistrationStatusBadge(member.registrationStatus)
                        }
                    }
                    Text(
                        "Family members join this household by picking the same block, floor and unit when they register.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = { navController.safeNavigate("edit_resident/${resident.residentId}") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Edit Profile")
            }
            LogoutButton(navController)
        }
    }
}
