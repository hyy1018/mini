// #member1
// Management creates a new Staff or Security account. Residents still register themselves.
package com.example.mini.screen.member1.module1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import com.example.mini.model.UserRole
import com.example.mini.nav.isCurrentScreenResumed
import com.example.mini.nav.safePopBackStack
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.PasswordField

@Composable
fun AddStaffAccountScreen(navController: NavHostController) {
    var role by remember { mutableStateOf(UserRole.STAFF) }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { BackTopBar("Add Account", navController) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Role", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(UserRole.STAFF, UserRole.SECURITY).forEach { option ->
                    FilterChip(
                        selected = role == option,
                        onClick = { role = option },
                        label = { Text(if (option == UserRole.STAFF) "Management Staff" else "Security Guard") }
                    )
                }
            }
            OutlinedTextField(
                value = name, onValueChange = { name = it.take(100) },
                label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = username, onValueChange = { username = it.trim().take(30) },
                label = { Text("User ID") }, supportingText = { Text("They log in with this, e.g. guard02") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it.take(100) },
                label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone, onValueChange = { phone = InputRules.sanitizePhone(it) },
                label = { Text("Phone Number") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            PasswordField(
                value = password,
                onValueChange = { password = it.take(50) },
                label = "Initial Password",
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Give the new staff member this password in person. You can reset it later from the account list.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    // ignore a second tap while the first save is already leaving this page
                    if (!navController.isCurrentScreenResumed()) return@Button
                    errorMessage = when {
                        listOf(name, username, email, phone, password).any { it.isBlank() } -> "Please fill in all the details"
                        !InputRules.isValidName(name) -> InputRules.NAME_MESSAGE
                        !InputRules.isValidUsername(username) -> InputRules.USERNAME_MESSAGE
                        ResidentData.isUsernameTaken(username) -> "This User ID is already taken"
                        !InputRules.isValidEmail(email) -> InputRules.EMAIL_MESSAGE
                        !InputRules.isValidPhone(phone) -> InputRules.PHONE_MESSAGE
                        !InputRules.isValidPassword(password) -> InputRules.PASSWORD_MESSAGE
                        else -> null
                    }
                    if (errorMessage == null) {
                        ResidentData.addStaffAccount(name.trim(), username, email.trim(), phone, role, password)
                        navController.safePopBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Account")
            }
        }
    }
}
