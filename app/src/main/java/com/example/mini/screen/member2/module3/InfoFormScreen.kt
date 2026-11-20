// #member2
// Staff adds (id = -1), edits or deletes a community rule / FAQ / guideline entry.
package com.example.mini.screen.member2.module3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.isCurrentScreenResumed
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.EngagementData
import com.example.mini.data.UserSession
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.DropdownField

@Composable
fun InfoFormScreen(infoId: Int, navController: NavHostController) {
    val existing = EngagementData.getInfo(infoId)

    var infoType by remember { mutableStateOf(existing?.infoType ?: EngagementData.infoTypes.first()) }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var content by remember { mutableStateOf(existing?.content ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BackTopBar(if (existing != null) "Edit Information" else "New Information", navController) {
                if (existing != null) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DropdownField(
                label = "Type",
                options = EngagementData.infoTypes,
                selected = infoType,
                onSelected = { infoType = it },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(150) },
                label = { Text(if (infoType == "FAQ") "Question" else "Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it.take(2000) },
                label = { Text(if (infoType == "FAQ") "Answer" else "Details") },
                // each line becomes one bullet point in the Info tab
                supportingText = { Text("One point per line -- press Enter for a new point") },
                placeholder = { Text("Open 7 AM - 10 PM\nProper swimwear only") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    // ignore a second tap while the first save is already leaving this page
                    if (!navController.isCurrentScreenResumed()) return@Button
                    if (title.isBlank() || content.isBlank()) {
                        errorMessage = "Please fill in both fields"
                        return@Button
                    }
                    if (existing == null) {
                        UserSession.currentUserId?.let { EngagementData.addInfo(it, infoType, title.trim(), content.trim()) }
                    } else {
                        EngagementData.updateInfo(infoId, infoType, title.trim(), content.trim())
                    }
                    navController.safePopBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (existing != null) "Save Changes" else "Add")
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this entry?") },
            text = { Text("Residents will no longer find it in search.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    EngagementData.deleteInfo(infoId)
                    navController.safePopBackStack()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}
