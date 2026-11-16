// #member1
// Management's view of one resident: details, household, edit and delete (Flowcharts 2-4, staff side).
package com.example.mini.screen.member1.module1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.ResidentData
import com.example.mini.data.VisitorData
import com.example.mini.data.formatDateTime
import com.example.mini.screen.InfoRow
import com.example.mini.screen.RegistrationStatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentDetailScreen(residentId: Int, navController: NavHostController) {
    val resident = ResidentData.getResident(residentId)
    val user = resident?.let { ResidentData.getUser(it.userId) }
    val household = resident?.let { ResidentData.getHousehold(it.householdId) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resident Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        // the record is gone right after a successful delete, before popBackStack finishes
        if (resident == null || user == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row {
                        Text(user.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        RegistrationStatusBadge(resident.registrationStatus)
                    }
                    Spacer(Modifier.size(8.dp))
                    InfoRow("Username", user.username)
                    InfoRow("Type", resident.residentType)
                    InfoRow("IC Number", resident.icNo)
                    InfoRow("Email", user.email)
                    InfoRow("Phone", user.phoneNo)
                    InfoRow("Registered", formatDateTime(resident.registeredAt))
                    resident.verifiedAt?.let { InfoRow("Verified", formatDateTime(it)) }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Household", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.size(8.dp))
                    InfoRow("Unit", household?.unitNo ?: "-")
                    InfoRow("Block", household?.blockName ?: "-")
                    InfoRow("Address", household?.address ?: "-")
                    InfoRow(
                        "Members",
                        ResidentData.householdMembers(resident.householdId)
                            .joinToString { ResidentData.getUser(it.userId)?.name ?: "-" }
                    )
                    InfoRow("Visitor passes", VisitorData.byResident(residentId).size.toString())
                }
            }

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = { navController.safeNavigate("edit_resident/$residentId") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Resident")
            }
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Resident")
            }
        }
    }

    // AlertDialog: confirm, then check related records before deleting
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete resident?") },
            text = { Text("This removes ${user?.name}'s account and visitor passes. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    val error = ResidentData.deleteResident(residentId)
                    if (error == null) {
                        navController.safePopBackStack()
                    } else {
                        errorMessage = error
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
