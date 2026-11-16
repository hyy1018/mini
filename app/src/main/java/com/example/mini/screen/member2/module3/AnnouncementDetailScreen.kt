// #member2
// One announcement. Residents confirm they've read important ones; staff see how many confirmed,
// and can edit or delete (Module 3 Read / Delete).
package com.example.mini.screen.member2.module3

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.mini.data.EngagementData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDateTime
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.StatusBadge

@Composable
fun AnnouncementDetailScreen(announcementId: Int, navController: NavHostController) {
    val announcement = EngagementData.getAnnouncement(announcementId)
    val userId = UserSession.currentUserId ?: -1
    val isStaff = UserSession.isStaff
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BackTopBar("Announcement", navController) {
                if (isStaff && announcement != null) {
                    IconButton(onClick = { navController.safeNavigate("announcement_form?id=$announcementId") }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    ) { innerPadding ->
        if (announcement == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(announcement.category, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                if (announcement.isImportant) {
                    StatusBadge("Important", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            Text(announcement.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                "Posted by ${ResidentData.getUser(announcement.createdBy)?.name ?: "Management"} · ${formatDateTime(announcement.postedAt)}" +
                    if (announcement.updatedAt > announcement.postedAt + 1000) "\nEdited ${formatDateTime(announcement.updatedAt)}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider()
            Text(announcement.content, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))

            // --- Read confirmation for important announcements ---
            if (announcement.isImportant) {
                if (isStaff) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "${EngagementData.readCount(announcementId)} of ${ResidentData.approvedResidents().size} residents confirmed reading",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else if (UserSession.isResident) {
                    if (EngagementData.hasRead(announcementId, userId)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("You confirmed reading this", color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Button(
                            onClick = { EngagementData.confirmRead(announcementId, userId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("I have read this")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete announcement?") },
            text = { Text("Residents will no longer see this announcement.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    EngagementData.deleteAnnouncement(announcementId)
                    navController.safePopBackStack()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}
