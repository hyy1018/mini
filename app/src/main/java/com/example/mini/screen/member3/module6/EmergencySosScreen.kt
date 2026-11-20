// #member3
// Emergency Alert System (unique feature), resident side: one tap on an emergency type sends the alert.
// The unit number is attached automatically, and management + security are notified immediately.
// After sending, the resident sees live status and can cancel a false alarm.
package com.example.mini.screen.member3.module6

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.data.CommunityData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDateTime
import com.example.mini.model.EmergencyStatus
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmergencyStatusBadge
import com.example.mini.screen.InfoRow
import kotlinx.coroutines.delay

private fun iconFor(type: String): ImageVector = when (type) {
    "Fire" -> Icons.Filled.LocalFireDepartment
    "Medical / Accident" -> Icons.Filled.LocalHospital
    "Security Threat" -> Icons.Filled.HealthAndSafety
    else -> Icons.Filled.Warning
}

@Composable
fun EmergencySosScreen(navController: NavHostController) {
    val resident = UserSession.currentResident ?: return
    var description by remember { mutableStateOf("") }
    // shows the most recent open alert, so reopening the page shows its status instead of the buttons
    val openAlert = CommunityData.emergenciesOfResident(resident.residentId)
        .firstOrNull { it.reportStatus == EmergencyStatus.REPORTED || it.reportStatus == EmergencyStatus.ACKNOWLEDGED }

    // re-checked every 15 s while blocked, so the buttons come back by themselves once the 5 minutes pass
    var tick by remember { mutableIntStateOf(0) }
    val limitReached = tick >= 0 && CommunityData.isAlertLimitReached(resident.residentId)
    LaunchedEffect(limitReached, tick) {
        if (limitReached) {
            delay(15_000)
            tick++
        }
    }

    Scaffold(topBar = { BackTopBar("Emergency SOS", navController) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (openAlert != null) {
                // --- Alert sent: live status ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(iconFor(openAlert.emergencyType), contentDescription = null, modifier = Modifier.size(48.dp))
                        Text("Alert Sent", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(4.dp))
                        EmergencyStatusBadge(openAlert.reportStatus)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (openAlert.reportStatus == EmergencyStatus.ACKNOWLEDGED) {
                                "Help is on the way. ${ResidentData.getUser(openAlert.acknowledgedBy ?: -1)?.name ?: "Staff"} acknowledged your alert."
                            } else {
                                "Management and security have been notified. Stay safe and wait for help."
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow("Type", openAlert.emergencyType)
                        InfoRow("Location", ResidentData.unitLabel(resident.residentId))
                        InfoRow("Sent", formatDateTime(openAlert.reportedAt))
                        openAlert.acknowledgedAt?.let { InfoRow("Acknowledged", formatDateTime(it)) }
                        if (openAlert.description.isNotBlank()) InfoRow("Details", openAlert.description)
                    }
                }
                if (openAlert.reportStatus == EmergencyStatus.REPORTED) {
                    OutlinedButton(
                        onClick = { CommunityData.cancelAlert(openAlert.emergencyId) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancel Alert (False Alarm)") }
                }
                Text(
                    "In a life-threatening emergency, also call 999 from the Emergency tab.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (limitReached) {
                // --- Blocked: too many alerts in 5 minutes ---
                // a real emergency must never be stuck here, so the call buttons are right on this page
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Too many alerts", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "You have sent ${CommunityData.MAX_ALERTS} alerts in the last 5 minutes. " +
                                "If this is a real emergency, call for help now.",
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Text("Call for help", style = MaterialTheme.typography.titleMedium)
                CommunityData.contacts
                    .filter { it.contactType == "Police" || it.contactType == "Security" || it.contactType == "Ambulance" }
                    .forEach { contact -> EmergencyContactRow(contact) }
            } else {
                // --- Send an alert ---
                Text(
                    "Tap the emergency type to alert management and security immediately.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Your location will be sent: ${ResidentData.unitLabel(resident.residentId)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.take(300) },
                    label = { Text("Details (optional)") },
                    placeholder = { Text("e.g. smoke from kitchen, 1 person injured") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                CommunityData.emergencyTypes.chunked(2).forEach { rowTypes ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowTypes.forEach { type ->
                            Button(
                                onClick = { CommunityData.sendAlert(resident.residentId, type, description.trim()) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(110.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(iconFor(type), contentDescription = null, modifier = Modifier.size(36.dp))
                                    Spacer(Modifier.height(6.dp))
                                    Text(type, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
                Text(
                    "To prevent misuse, up to ${CommunityData.MAX_ALERTS} alerts can be sent within 5 minutes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
