// #member3
// Staff adds (id = -1), edits or deletes an emergency contact.
package com.example.mini.screen.member3.module6

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.isCurrentScreenResumed
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.CommunityData
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.DropdownField

@Composable
fun EmergencyContactFormScreen(contactId: Int, navController: NavHostController) {
    val existing = CommunityData.getContact(contactId)

    var name by remember { mutableStateOf(existing?.contactName ?: "") }
    var phone by remember { mutableStateOf(existing?.phoneNo ?: "") }
    var type by remember { mutableStateOf(existing?.contactType ?: CommunityData.contactTypes.first()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BackTopBar(if (existing != null) "Edit Contact" else "New Contact", navController) {
                if (existing != null) {
                    IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it.take(60) },
                label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                // short codes like 999 are allowed here, so only digits / dashes / spaces are enforced
                onValueChange = { phone = it.filter { c -> c.isDigit() || c == '-' || c == ' ' }.take(15) },
                label = { Text("Phone Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            DropdownField(
                label = "Type",
                options = CommunityData.contactTypes,
                selected = type,
                onSelected = { type = it },
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    // ignore a second tap while the first save is already leaving this page
                    if (!navController.isCurrentScreenResumed()) return@Button
                    errorMessage = when {
                        name.isBlank() || phone.isBlank() -> "Please enter a name and phone number"
                        phone.count { it.isDigit() } < 3 -> "Phone number is too short"
                        else -> null
                    }
                    if (errorMessage != null) return@Button
                    if (existing == null) CommunityData.addContact(name.trim(), phone.trim(), type)
                    else CommunityData.updateContact(contactId, name.trim(), phone.trim(), type)
                    navController.safePopBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (existing != null) "Save Changes" else "Add Contact")
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete contact?") },
            text = { Text("Residents will no longer see this number.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    CommunityData.deleteContact(contactId)
                    navController.safePopBackStack()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}
