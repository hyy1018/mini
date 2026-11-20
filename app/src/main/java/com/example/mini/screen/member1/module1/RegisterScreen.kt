// #member1
// Resident self-registration (Flowchart 1). New accounts start as Pending until management approves.
package com.example.mini.screen.member1.module1

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.InputRules
import com.example.mini.data.ResidentData
import com.example.mini.screen.DropdownField
import com.example.mini.screen.PasswordField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var icNo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var residentType by remember { mutableStateOf(ResidentData.residentTypes.first()) }
    var block by remember { mutableStateOf(ResidentData.blocks.first()) }
    var floor by remember { mutableStateOf(ResidentData.floors.first()) }
    var unit by remember { mutableStateOf(ResidentData.unitsPerFloor.first()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resident Registration") },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
            // --- Personal details ---
            Text("Personal Details", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(100) },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = icNo,
                onValueChange = { icNo = InputRules.sanitizeIc(it) },
                label = { Text("IC Number") },
                placeholder = { Text("e.g. 900101-14-5678") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                placeholder = { Text("e.g. 012-3456789") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            // --- Household details ---
            Text("Household Details", style = MaterialTheme.typography.titleMedium)
            DropdownField(
                label = "Block",
                options = ResidentData.blocks,
                selected = block,
                onSelected = { block = it },
                modifier = Modifier.fillMaxWidth()
            )
            // floor and unit are picked, not typed, so everyone in the same unit matches exactly
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DropdownField(
                    label = "Floor",
                    options = ResidentData.floors,
                    selected = floor,
                    onSelected = { floor = it },
                    optionLabel = { "%02d".format(it) },
                    modifier = Modifier.weight(1f)
                )
                DropdownField(
                    label = "Unit",
                    options = ResidentData.unitsPerFloor,
                    selected = unit,
                    onSelected = { unit = it },
                    optionLabel = { "%02d".format(it) },
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                "Your unit: ${ResidentData.unitNumber(block, floor, unit)}. " +
                    "Family members joining your household pick the same unit.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Resident Type", style = MaterialTheme.typography.titleSmall)
            Column {
                ResidentData.residentTypes.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = type == residentType,
                                onClick = { residentType = type },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = type == residentType, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(type)
                    }
                }
            }

            // --- Account details ---
            Text("Account", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it.trim() },
                label = { Text("User ID") },
                supportingText = { Text("You will log in with this, e.g. alitan88 (not your full name)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            PasswordField(
                value = password,
                onValueChange = { password = it.take(50) },
                label = "Password",
                modifier = Modifier.fillMaxWidth()
            )
            PasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it.take(50) },
                label = "Confirm Password",
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    // a second tap after success would hit "User ID taken", so stop once submitted
                    if (showSuccess) return@Button
                    // checked in order, so the resident fixes one problem at a time
                    errorMessage = when {
                        listOf(name, icNo, email, phone, username, password).any { it.isBlank() } ->
                            "Please fill in all the details"
                        !InputRules.isValidName(name) -> InputRules.NAME_MESSAGE
                        !InputRules.isValidIc(icNo) -> InputRules.IC_MESSAGE
                        !InputRules.isValidEmail(email) -> InputRules.EMAIL_MESSAGE
                        !InputRules.isValidPhone(phone) -> InputRules.PHONE_MESSAGE
                        !InputRules.isValidUsername(username) -> InputRules.USERNAME_MESSAGE
                        ResidentData.isUsernameTaken(username) -> "This User ID is already taken"
                        !InputRules.isValidPassword(password) -> InputRules.PASSWORD_MESSAGE
                        password != confirmPassword -> "Passwords do not match"
                        else -> null
                    }
                    if (errorMessage == null) {
                        ResidentData.register(
                            name = name.trim(),
                            username = username,
                            email = email.trim(),
                            phoneNo = phone,
                            icNo = icNo,
                            password = password,
                            residentType = residentType,
                            blockName = block,
                            floor = floor,
                            unit = unit
                        )
                        showSuccess = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Registration")
            }
        }
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Registration Submitted") },
            text = { Text("Your registration is pending. You can log in once management approves it.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccess = false
                    navController.safePopBackStack()
                }) { Text("Back to Login") }
            }
        )
    }
}
