// #member3
// Resident submits (id = -1) or edits a pending complaint, with an optional photo as evidence.
package com.example.mini.screen.member3.module5

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.isCurrentScreenResumed
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.ComplaintData
import com.example.mini.data.UserSession
import com.example.mini.data.copyImageToAppStorage
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.DropdownField
import com.example.mini.screen.LocalPhoto
import kotlinx.coroutines.launch

@Composable
fun ComplaintFormScreen(complaintId: Int, navController: NavHostController) {
    val existing = ComplaintData.getComplaint(complaintId)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var category by remember { mutableStateOf(existing?.category ?: ComplaintData.categories.first()) }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var photoPath by remember { mutableStateOf(existing?.photoPath) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // system photo picker -- no storage permission needed; the picked file is copied into app storage
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val stored = copyImageToAppStorage(context, uri)
                if (stored != null) photoPath = stored else errorMessage = "Couldn't read that photo"
            }
        }
    }

    Scaffold(
        topBar = { BackTopBar(if (existing != null) "Edit Complaint" else "New Complaint", navController) }
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
                label = "Category",
                options = ComplaintData.categories,
                selected = category,
                onSelected = { category = it },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(100) },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it.take(1000) },
                label = { Text("Describe the issue") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // --- Photo evidence (optional) ---
            Text("Photo evidence (optional)", style = MaterialTheme.typography.labelLarge)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clickable {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                val path = photoPath
                if (path != null) {
                    LocalPhoto(path, "Complaint photo", Modifier.fillMaxSize())
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("Tap to add a photo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (photoPath != null) {
                TextButton(onClick = { photoPath = null }) { Text("Remove photo") }
            }

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    // ignore a second tap while the first save is already leaving this page
                    if (!navController.isCurrentScreenResumed()) return@Button
                    if (title.isBlank() || description.isBlank()) {
                        errorMessage = "Please enter a title and describe the issue"
                        return@Button
                    }
                    if (existing == null) {
                        UserSession.currentResident?.let {
                            ComplaintData.submitComplaint(it.residentId, category, title.trim(), description.trim(), photoPath)
                        }
                    } else {
                        ComplaintData.updateComplaint(complaintId, category, title.trim(), description.trim(), photoPath)
                    }
                    navController.safePopBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (existing != null) "Save Changes" else "Submit Complaint")
            }
        }
    }
}
