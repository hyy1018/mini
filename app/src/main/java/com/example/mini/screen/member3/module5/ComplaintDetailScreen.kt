// #member3
// One complaint. Residents track status and read the management response (and can edit / withdraw
// while it's still pending); staff reply and update the status.
package com.example.mini.screen.member3.module5

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.mini.nav.safeNavigate
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.ComplaintData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDateTime
import com.example.mini.model.RequestStatus
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.DropdownField
import com.example.mini.screen.InfoRow
import com.example.mini.screen.LocalPhoto
import com.example.mini.screen.RequestStatusBadge

@Composable
fun ComplaintDetailScreen(complaintId: Int, navController: NavHostController) {
    val complaint = ComplaintData.getComplaint(complaintId)
    val isStaff = UserSession.isStaff
    val canEdit = !isStaff && complaint != null && ComplaintData.canEdit(complaint)

    var reply by remember { mutableStateOf(complaint?.responseText ?: "") }
    var status by remember { mutableStateOf(complaint?.complaintStatus ?: RequestStatus.PENDING) }
    var message by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BackTopBar("Complaint", navController) {
                if (canEdit) {
                    IconButton(onClick = { navController.safeNavigate("complaint_form?id=$complaintId") }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Withdraw")
                    }
                }
            }
        }
    ) { innerPadding ->
        if (complaint == null) return@Scaffold
        val reporter = ResidentData.getResident(complaint.residentId)?.let { ResidentData.getUser(it.userId) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(complaint.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        RequestStatusBadge(complaint.complaintStatus)
                    }
                    Spacer(Modifier.height(8.dp))
                    InfoRow("Category", complaint.category)
                    InfoRow("Submitted", formatDateTime(complaint.submittedAt))
                    InfoRow("Last update", formatDateTime(complaint.updatedAt))
                    if (isStaff) InfoRow("From", "${reporter?.name ?: "-"} (${ResidentData.unitLabel(complaint.residentId)})")
                }
            }
            Text(complaint.description, style = MaterialTheme.typography.bodyLarge)
            complaint.photoPath?.let { path ->
                LocalPhoto(path, "Complaint photo", Modifier.fillMaxWidth().height(220.dp))
            }

            // --- Management response ---
            if (complaint.responseText.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Management Response", style = MaterialTheme.typography.labelLarge)
                        Text(complaint.responseText, style = MaterialTheme.typography.bodyMedium)
                        complaint.respondedAt?.let {
                            Text(
                                "${ResidentData.getUser(complaint.respondedBy ?: -1)?.name ?: "Management"} · ${formatDateTime(it)}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            } else if (!isStaff) {
                Text("Management hasn't replied yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // --- Staff actions ---
            if (isStaff) {
                Text("Reply to Resident", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = reply,
                    onValueChange = { reply = it.take(1000) },
                    label = { Text("Response") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        UserSession.currentUserId?.let { ComplaintData.respond(complaintId, reply.trim(), it) }
                        message = "Response sent"
                    },
                    enabled = reply.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Send Response") }

                Text("Status", style = MaterialTheme.typography.titleMedium)
                DropdownField(
                    label = "Status",
                    options = RequestStatus.entries,
                    selected = status,
                    onSelected = { status = it },
                    optionLabel = { it.label },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        ComplaintData.updateStatus(complaintId, status)
                        message = "Status updated -- the resident has been notified"
                    },
                    enabled = status != complaint.complaintStatus,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Update Status") }

                message?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Withdraw complaint?") },
            text = { Text("This removes the complaint. Management won't see it any more.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    ComplaintData.deleteComplaint(complaintId)
                    navController.safePopBackStack()
                }) { Text("Withdraw") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}
