// #member2
// One community event. Residents register / cancel registration; staff see participants,
// edit, cancel or delete the event.
package com.example.mini.screen.member2.module3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.EngagementData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDate
import com.example.mini.data.formatHour
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.InfoRow

@Composable
fun EventDetailScreen(eventId: Int, navController: NavHostController) {
    val event = EngagementData.getEvent(eventId)
    val userId = UserSession.currentUserId ?: -1
    val isStaff = UserSession.isStaff

    var errorMessage by remember { mutableStateOf<String?>(null) }
    // which staff action is waiting for confirmation: "cancel" or "delete"
    var pendingAction by remember { mutableStateOf<String?>(null) }
    var lastToggleAt by remember { mutableLongStateOf(0L) }

    Scaffold(
        topBar = {
            BackTopBar("Event", navController) {
                if (isStaff && event != null) {
                    IconButton(onClick = { navController.safeNavigate("event_form?id=$eventId") }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { pendingAction = "delete" }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    ) { innerPadding ->
        if (event == null) return@Scaffold
        val status = EngagementData.displayStatus(event)
        val joinedCount = EngagementData.registrationCount(eventId)
        val isRegistered = EngagementData.isRegistered(eventId, userId)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EventStatusBadge(status)
            Text(event.title, style = MaterialTheme.typography.headlineSmall)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Date", formatDate(event.eventDate))
                    InfoRow("Time", "${formatHour(event.startHour)} - ${formatHour(event.endHour)}")
                    InfoRow("Venue", event.venue)
                    InfoRow("Participants", "$joinedCount / ${event.maxParticipants}")
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (joinedCount.toFloat() / event.maxParticipants).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Text(event.description, style = MaterialTheme.typography.bodyLarge)

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            // --- Resident: register / unregister ---
            if (UserSession.isResident && status == "Upcoming") {
                // Register and Cancel swap places after a tap, so the second tap of a double tap
                // would undo the first. Taps within 800 ms of the last one are ignored.
                if (isRegistered) {
                    OutlinedButton(
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (now - lastToggleAt < 800) return@OutlinedButton
                            lastToggleAt = now
                            EngagementData.unregisterFromEvent(eventId, userId)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancel My Registration") }
                } else {
                    Button(
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (now - lastToggleAt < 800) return@Button
                            lastToggleAt = now
                            errorMessage = EngagementData.registerForEvent(eventId, userId)
                        },
                        enabled = joinedCount < event.maxParticipants,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (joinedCount < event.maxParticipants) "Register for Event" else "Event Full") }
                }
            }

            // --- Staff: participants and cancel event ---
            if (isStaff) {
                Text("Participants", style = MaterialTheme.typography.titleMedium)
                val participants = EngagementData.participants(eventId)
                if (participants.isEmpty()) {
                    Text("No one has registered yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    participants.forEach { registration ->
                        val resident = ResidentData.residentOfUser(registration.userId)
                        Text(
                            "• ${ResidentData.getUser(registration.userId)?.name ?: "-"}" +
                                (resident?.let { " (${ResidentData.unitLabel(it.residentId)})" } ?: "")
                        )
                    }
                }
                if (status == "Upcoming") {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { pendingAction = "cancel" },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancel Event") }
                }
            }
        }
    }

    pendingAction?.let { action ->
        val isDelete = action == "delete"
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text(if (isDelete) "Delete event?" else "Cancel event?") },
            text = {
                Text(
                    if (isDelete) "The event and its registrations will be removed."
                    else "Registered residents will be notified that the event is cancelled."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingAction = null
                    if (isDelete) {
                        EngagementData.deleteEvent(eventId)
                        navController.safePopBackStack()
                    } else {
                        EngagementData.cancelEvent(eventId)
                    }
                }) { Text(if (isDelete) "Delete" else "Cancel Event") }
            },
            dismissButton = { TextButton(onClick = { pendingAction = null }) { Text("Back") } }
        )
    }
}
