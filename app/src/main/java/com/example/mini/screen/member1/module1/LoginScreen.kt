// #member1
// Login gate: pick Resident / Staff / Security, then sign in with username and password.
package com.example.mini.screen.member1.module1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.model.UserRole
import com.example.mini.screen.PasswordField

@Composable
fun LoginScreen(navController: NavHostController) {
    var selectedRole by remember { mutableStateOf(UserRole.RESIDENT) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            // --- Brand header ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Apartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "RCMS",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    "Residential Community Management System",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // --- Login card ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Role selector: switching role clears the form so nothing carries over
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        UserRole.entries.forEachIndexed { index, role ->
                            SegmentedButton(
                                selected = selectedRole == role,
                                onClick = {
                                    selectedRole = role
                                    username = ""
                                    password = ""
                                    errorMessage = null
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = UserRole.entries.size
                                )
                            ) {
                                Text(role.label)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        label = { Text("${selectedRole.label} Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    PasswordField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = "Password",
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Forgot sits right under the password field
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { /* no action yet -- forgot password page comes later */ }) {
                            Text("Forgot?")
                        }
                    }

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            if (username.isBlank() || password.isEmpty()) {
                                errorMessage = "Enter your username and password"
                                return@Button
                            }
                            val result = ResidentData.checkLogin(username.trim(), password, selectedRole)
                            val user = result.user
                            if (user == null) {
                                errorMessage = result.error
                                return@Button
                            }
                            UserSession.login(user)
                            val destination = when (user.role) {
                                UserRole.RESIDENT -> "resident_home"
                                UserRole.STAFF -> "staff_home"
                                UserRole.SECURITY -> "security_home"
                            }
                            navController.safeNavigate(destination) {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Login as ${selectedRole.label}")
                    }

                    // Only residents register themselves -- Staff and Security
                    // accounts are created by management, so they don't see this.
                    if (selectedRole == UserRole.RESIDENT) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("New resident?", style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { navController.safeNavigate("register") }) {
                                Text("Register")
                            }
                        }
                    }
                }
            }

            // --- Visitor entry, kept apart from the login card ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    "or",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { navController.safeNavigate("visitor") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Icon(Icons.Filled.ConfirmationNumber, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("I'm a Visitor")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
