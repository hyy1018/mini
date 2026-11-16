// #member2
// One maintenance request. Residents track progress; staff assign a technician and update the status.
package com.example.mini.screen.member2.module4

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.data.FacilityData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDateTime
import com.example.mini.model.RequestStatus
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.DropdownField
import com.example.mini.screen.InfoRow
import com.example.mini.screen.RequestStatusBadge

@Composable
fun MaintenanceDetailScreen(requestId: Int, navController: NavHostController) {
    val request = FacilityData.getRequest(requestId)
    val isStaff = UserSession.isStaff

    var technician by remember { mutableStateOf(request?.assignedTechnician ?: "") }
    var status by remember { mutableStateOf(request?.requestStatus ?: RequestStatus.PENDING) }
    var remarks by remember { mutableStateOf(request?.remarks ?: "") }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { BackTopBar("Repair #$requestId", navController) }) { innerPadding ->
        if (request == null) return@Scaffold
        val reporter = ResidentData.getResident(request.residentId)?.let { ResidentData.getUser(it.userId) }

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
                        Text(request.category, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        RequestStatusBadge(request.requestStatus)
                    }
                    Spacer(Modifier.height(8.dp))
                    InfoRow("Location", request.location)
                    request.facilityId?.let { id -> InfoRow("Facility", FacilityData.getFacility(id)?.facilityName ?: "-") }
                    InfoRow("Priority", request.priority)
                    InfoRow("Submitted", formatDateTime(request.submittedAt))
                    if (isStaff) InfoRow("Reported by", "${reporter?.name ?: "-"} (${ResidentData.unitLabel(request.residentId)})")
                    InfoRow("Technician", request.assignedTechnician.ifBlank { "Not assigned yet" })
                    request.completedAt?.let { InfoRow("Completed", formatDateTime(it)) }
                    if (request.remarks.isNotBlank()) InfoRow("Remarks", request.remarks)
                }
            }
            Text("Description", style = MaterialTheme.typography.titleSmall)
            Text(request.description, style = MaterialTheme.typography.bodyLarge)

            // --- Staff actions ---
            if (isStaff) {
                Text("Assign Technician", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = technician,
                    onValueChange = { technician = it.take(100) },
                    label = { Text("Technician name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        UserSession.currentUserId?.let { FacilityData.assignTechnician(requestId, technician.trim(), it) }
                        // assigning moves a pending request to In Progress, so keep the dropdown in sync
                        status = FacilityData.getRequest(requestId)?.requestStatus ?: status
                        message = "Technician assigned"
                    },
                    enabled = technician.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Assign") }

                Text("Update Progress", style = MaterialTheme.typography.titleMedium)
                DropdownField(
                    label = "Status",
                    options = RequestStatus.entries,
                    selected = status,
                    onSelected = { status = it },
                    optionLabel = { it.label },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it.take(500) },
                    label = { Text("Remarks / progress update") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        FacilityData.updateRequestStatus(requestId, status, remarks.trim())
                        message = "Status updated -- the resident has been notified"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save Status") }

                message?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}
