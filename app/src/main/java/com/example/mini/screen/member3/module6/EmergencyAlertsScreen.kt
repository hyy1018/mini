// #member3
// Emergency Alert System, staff / security side: open alerts first, then history.
// Acknowledge an alert, then resolve it with remarks; the resident is notified at each step.
package com.example.mini.screen.member3.module6

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.data.CommunityData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDateTime
import com.example.mini.model.EmergencyReport
import com.example.mini.model.EmergencyStatus
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmergencyStatusBadge
import com.example.mini.screen.EmptyState
import com.example.mini.screen.InfoRow

@Composable
fun EmergencyAlertsScreen(navController: NavHostController) {
    val active = CommunityData.activeEmergencies()
    val history = CommunityData.allEmergencies().filter { it !in active }
    var alertToResolve by remember { mutableStateOf<EmergencyReport?>(null) }
    var remarks by remember { mutableStateOf("") }

    Scaffold(topBar = { BackTopBar("Emergency Alerts", navController) }) { innerPadding ->
        if (active.isEmpty() && history.isEmpty()) {
            EmptyState("No emergency alerts", Modifier.padding(innerPadding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Open Alerts (${active.size})", style = MaterialTheme.typography.titleMedium) }
            if (active.isEmpty()) {
                item { Text("No open alerts right now", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(active, key = { it.emergencyId }) { alert ->
                AlertCard(alert, highlight = true) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (alert.reportStatus == EmergencyStatus.REPORTED) {
                            Button(
                                onClick = { UserSession.currentUserId?.let { CommunityData.acknowledge(alert.emergencyId, it) } },
                                modifier = Modifier.weight(1f)
                            ) { Text("Acknowledge") }
                        }
                        OutlinedButton(
                            onClick = {
                                remarks = ""
                                alertToResolve = alert
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Resolve") }
                    }
                }
            }

            if (history.isNotEmpty()) {
                item { Text("History", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
                items(history, key = { it.emergencyId }) { alert -> AlertCard(alert, highlight = false) {} }
            }
        }
    }

    alertToResolve?.let { alert ->
        AlertDialog(
            onDismissRequest = { alertToResolve = null },
            title = { Text("Resolve alert") },
            text = {
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it.take(500) },
                    label = { Text("What was done? (remarks)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        UserSession.currentUserId?.let { CommunityData.resolve(alert.emergencyId, it, remarks.trim()) }
                        alertToResolve = null
                    },
                    enabled = remarks.isNotBlank()
                ) { Text("Resolve") }
            },
            dismissButton = { TextButton(onClick = { alertToResolve = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AlertCard(alert: EmergencyReport, highlight: Boolean, actions: @Composable () -> Unit) {
    val reporter = ResidentData.getResident(alert.residentId)?.let { ResidentData.getUser(it.userId) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (highlight) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        else CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(alert.emergencyType, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                EmergencyStatusBadge(alert.reportStatus)
            }
            // the unit is the key detail for responders, so it comes first
            InfoRow("Location", ResidentData.unitLabel(alert.residentId))
            InfoRow("Resident", "${reporter?.name ?: "-"} · ${reporter?.phoneNo ?: "-"}")
            InfoRow("Reported", formatDateTime(alert.reportedAt))
            if (alert.description.isNotBlank()) InfoRow("Details", alert.description)
            alert.acknowledgedAt?.let {
                InfoRow("Acknowledged", "${formatDateTime(it)} by ${ResidentData.getUser(alert.acknowledgedBy ?: -1)?.name ?: "-"}")
            }
            alert.resolvedAt?.let { InfoRow("Resolved", formatDateTime(it)) }
            if (alert.remarks.isNotBlank()) InfoRow("Remarks", alert.remarks)
            actions()
        }
    }
}
