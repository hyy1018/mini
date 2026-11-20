// #member1
// "Me" page for Staff and Security: profile photo, name, email and phone, plus Log Out.
// Residents use their own Me tab instead, since they also have household details.
package com.example.mini.screen.member1.module1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.data.InputRules
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.screen.AvatarPicker
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.InfoRow

@Composable
fun AccountScreen(navController: NavHostController) {
    val user = UserSession.currentUser

    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phoneNo ?: "") }
    var photoPath by remember { mutableStateOf(user?.avatarPath) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    Scaffold(topBar = { BackTopBar("My Profile", navController) }) { innerPadding ->
        // nullable, not !!: this can compose for a moment right after logout
        if (user == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AvatarPicker(userId = user.userId, photoPath = photoPath, onPhotoChange = { photoPath = it })

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("User ID", user.username)
                    InfoRow("Role", user.role.label)
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(100); message = null },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.take(100); message = null },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = InputRules.sanitizePhone(it); message = null },
                label = { Text("Phone Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            message?.let {
                Text(
                    it,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    val error = when {
                        listOf(name, email, phone).any { it.isBlank() } -> "Please fill in all the details"
                        !InputRules.isValidName(name) -> InputRules.NAME_MESSAGE
                        !InputRules.isValidEmail(email) -> InputRules.EMAIL_MESSAGE
                        !InputRules.isValidPhone(phone) -> InputRules.PHONE_MESSAGE
                        else -> null
                    }
                    isError = error != null
                    if (error == null) {
                        ResidentData.updateAccount(user.userId, name.trim(), email.trim(), phone)
                        ResidentData.updateAvatar(user.userId, photoPath)
                    }
                    // stays on this page, so a tick message confirms the save
                    message = error ?: "Profile saved"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
            LogoutButton(navController)
        }
    }
}
