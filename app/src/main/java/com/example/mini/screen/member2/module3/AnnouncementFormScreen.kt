// #member2
// Staff creates (id = -1) or edits an announcement (Module 3 Create / Update).
package com.example.mini.screen.member2.module3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.isCurrentScreenResumed
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.EngagementData
import com.example.mini.data.UserSession
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.DropdownField

@Composable
fun AnnouncementFormScreen(announcementId: Int, navController: NavHostController) {
    val existing = EngagementData.getAnnouncement(announcementId)

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var content by remember { mutableStateOf(existing?.content ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: EngagementData.announcementCategories.first()) }
    var isImportant by remember { mutableStateOf(existing?.isImportant ?: false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { BackTopBar(if (existing != null) "Edit Announcement" else "New Announcement", navController) }
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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(150) },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownField(
                label = "Category",
                options = EngagementData.announcementCategories,
                selected = category,
                onSelected = { category = it },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it.take(2000) },
                label = { Text("Content") },
                minLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
            // important announcements ask residents to confirm they've read them
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = isImportant, onValueChange = { isImportant = it }, role = Role.Checkbox)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = isImportant, onCheckedChange = null)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Mark as important")
                    Text(
                        "Residents will be asked to confirm they've read it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    // ignore a second tap while the first save is already leaving this page
                    if (!navController.isCurrentScreenResumed()) return@Button
                    if (title.isBlank() || content.isBlank()) {
                        errorMessage = "Please enter a title and content"
                        return@Button
                    }
                    if (existing == null) {
                        UserSession.currentUserId?.let { staffId ->
                            EngagementData.addAnnouncement(staffId, title.trim(), content.trim(), category, isImportant)
                        }
                    } else {
                        EngagementData.updateAnnouncement(announcementId, title.trim(), content.trim(), category, isImportant)
                    }
                    navController.safePopBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (existing != null) "Save Changes" else "Publish")
            }
        }
    }
}
