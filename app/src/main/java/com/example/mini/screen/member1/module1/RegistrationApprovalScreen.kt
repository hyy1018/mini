// #member1
// Management reviews pending resident registrations and approves or rejects them (DFD process 1.2).
package com.example.mini.screen.member1.module1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDateTime
import com.example.mini.model.RegistrationStatus
import com.example.mini.model.Resident
import com.example.mini.screen.InfoRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationApprovalScreen(navController: NavHostController) {
    val pending = ResidentData.pendingRegistrations()

    // Dialog state: which resident and which decision is waiting for confirmation
    var pendingDecision by remember { mutableStateOf<Pair<Resident, RegistrationStatus>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registration Approvals") },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (pending.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No registrations waiting for approval")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pending, key = { it.residentId }) { resident ->
                    val user = ResidentData.getUser(resident.userId)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(user?.name ?: "-", style = MaterialTheme.typography.titleMedium)
                            Text(
                                ResidentData.unitLabel(resident.residentId) + " · " + resident.residentType,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            InfoRow("User ID", user?.username ?: "-")
                            InfoRow("IC Number", resident.icNo)
                            InfoRow("Phone", user?.phoneNo ?: "-")
                            InfoRow("Email", user?.email ?: "-")
                            InfoRow("Submitted", formatDateTime(resident.registeredAt))
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = { pendingDecision = resident to RegistrationStatus.REJECTED },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Reject") }
                                Button(
                                    onClick = { pendingDecision = resident to RegistrationStatus.APPROVED },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Approve") }
                            }
                        }
                    }
                }
            }
        }
    }

    // AlertDialog: confirm before approving / rejecting
    pendingDecision?.let { (resident, decision) ->
        val name = ResidentData.getUser(resident.userId)?.name ?: "this resident"
        val action = if (decision == RegistrationStatus.APPROVED) "Approve" else "Reject"
        AlertDialog(
            onDismissRequest = { pendingDecision = null },
            title = { Text("$action registration?") },
            text = { Text("$action the registration of $name?") },
            confirmButton = {
                TextButton(onClick = {
                    UserSession.currentUserId?.let { staffId ->
                        ResidentData.setRegistrationStatus(resident.residentId, decision, staffId)
                    }
                    pendingDecision = null
                }) { Text(action) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDecision = null }) { Text("Cancel") }
            }
        )
    }
}
