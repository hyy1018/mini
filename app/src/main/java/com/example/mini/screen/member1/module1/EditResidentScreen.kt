// #member1
// Update resident information (Flowchart 3). Used by the resident ("Edit Profile")
// and by management staff ("Edit Resident") -- same form, different title.
package com.example.mini.screen.member1.module1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.mini.data.InputRules
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.model.UserRole
import com.example.mini.screen.AvatarPicker
import com.example.mini.screen.DropdownField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditResidentScreen(residentId: Int, navController: NavHostController) {
    val resident = ResidentData.getResident(residentId)
    val user = resident?.let { ResidentData.getUser(it.userId) }
    val household = resident?.let { ResidentData.getHousehold(it.householdId) }
    val isStaff = UserSession.currentUser?.role == UserRole.STAFF

    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phoneNo ?: "") }
    var icNo by remember { mutableStateOf(resident?.icNo ?: "") }
    var residentType by remember { mutableStateOf(resident?.residentType ?: ResidentData.residentTypes.first()) }
    var photoPath by remember { mutableStateOf(user?.avatarPath) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // only the resident changes their own photo -- staff editing a resident doesn't see the picker
    val isOwnProfile = user != null && user.userId == UserSession.currentUserId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isStaff) "Edit Resident" else "Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (resident == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isOwnProfile) {
                AvatarPicker(userId = resident.userId, photoPath = photoPath, onPhotoChange = { photoPath = it })
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(100) },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.take(100) },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = InputRules.sanitizePhone(it) },
                label = { Text("Phone Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = icNo,
                onValueChange = { icNo = InputRules.sanitizeIc(it) },
                label = { Text("IC Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            DropdownField(
                label = "Resident Type",
                options = ResidentData.residentTypes,
                selected = residentType,
                onSelected = { residentType = it },
                modifier = Modifier.fillMaxWidth()
            )
            // unit and block are fixed: moving unit means a new household, so management handles that
            Text(
                "Unit: ${household?.blockName} · ${household?.unitNo} (contact management to change unit)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    // ignore a second tap while the first save is already leaving this page
                    if (!navController.isCurrentScreenResumed()) return@Button
                    errorMessage = when {
                        listOf(name, email, phone, icNo).any { it.isBlank() } -> "Please fill in all the details"
                        !InputRules.isValidName(name) -> InputRules.NAME_MESSAGE
                        !InputRules.isValidEmail(email) -> InputRules.EMAIL_MESSAGE
                        !InputRules.isValidPhone(phone) -> InputRules.PHONE_MESSAGE
                        !InputRules.isValidIc(icNo) -> InputRules.IC_MESSAGE
                        else -> null
                    }
                    if (errorMessage == null) {
                        ResidentData.updateResident(
                            residentId = residentId,
                            name = name.trim(),
                            email = email.trim(),
                            phoneNo = phone,
                            icNo = icNo,
                            residentType = residentType
                        )
                        if (isOwnProfile) ResidentData.updateAvatar(resident.userId, photoPath)
                        navController.safePopBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}
