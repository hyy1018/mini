// #member1
// Management's list of Staff and Security accounts (role-based access, Module 1).
// Accounts are deactivated rather than deleted, so the alerts, replies and approvals they
// handled still show who did them. Nobody can deactivate their own account.
package com.example.mini.screen.member1.module1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.mini.data.InputRules
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.model.User
import com.example.mini.nav.safeNavigate
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.PasswordField
import com.example.mini.screen.StatusBadge
import com.example.mini.screen.UserAvatar
import com.example.mini.screen.member3.module6.ContactName

@Composable
fun StaffAccountsScreen(navController: NavHostController) {
    val myId = UserSession.currentUserId ?: -1
    val accounts = ResidentData.staffAndSecurityAccounts()

    var accountToToggle by remember { mutableStateOf<User?>(null) }
    var accountToReset by remember { mutableStateOf<User?>(null) }
    var newPassword by remember { mutableStateOf("") }
    var resetError by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { BackTopBar("Staff & Security Accounts", navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.safeNavigate("add_account") },
                icon = { Icon(Icons.Filled.PersonAdd, contentDescription = null) },
                text = { Text("Add Account") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            message?.let { text ->
                item { Text(text, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
            }
            items(accounts, key = { it.userId }) { account ->
                val active = account.accountStatus == "Active"
                val isMe = account.userId == myId
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(account.userId, size = 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                ContactName(account.userId)
                                Text(
                                    "ID: ${account.username} · ${account.phoneNo}" + if (isMe) " · You" else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (active) {
                                StatusBadge("Active", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                            } else {
                                // deactivated = access refused, so it gets the red "rejected" colour
                                StatusBadge("Inactive", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                newPassword = ""
                                resetError = null
                                accountToReset = account
                            }) { Text("Reset Password") }
                            // you can't lock yourself out
                            if (!isMe) {
                                OutlinedButton(
                                    onClick = { accountToToggle = account },
                                    colors = if (active) {
                                        ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    } else {
                                        ButtonDefaults.outlinedButtonColors()
                                    }
                                ) { Text(if (active) "Deactivate" else "Reactivate") }
                            }
                        }
                    }
                }
            }
        }
    }

    accountToToggle?.let { account ->
        val deactivate = account.accountStatus == "Active"
        AlertDialog(
            onDismissRequest = { accountToToggle = null },
            title = { Text(if (deactivate) "Deactivate account?" else "Reactivate account?") },
            text = {
                Text(
                    if (deactivate) "${account.name} will no longer be able to log in. Their past records are kept."
                    else "${account.name} will be able to log in again."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    ResidentData.setAccountActive(account.userId, active = !deactivate)
                    message = "${account.name} ${if (deactivate) "deactivated" else "reactivated"}"
                    accountToToggle = null
                }) { Text(if (deactivate) "Deactivate" else "Reactivate") }
            },
            dismissButton = { TextButton(onClick = { accountToToggle = null }) { Text("Cancel") } }
        )
    }

    accountToReset?.let { account ->
        AlertDialog(
            onDismissRequest = { accountToReset = null },
            title = { Text("Reset password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Set a new password for ${account.name}, then tell them in person.")
                    PasswordField(
                        value = newPassword,
                        onValueChange = { newPassword = it.take(50); resetError = null },
                        label = "New Password",
                        modifier = Modifier.fillMaxWidth()
                    )
                    resetError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // same password rule as resident registration
                    if (!InputRules.isValidPassword(newPassword)) {
                        resetError = InputRules.PASSWORD_MESSAGE
                    } else {
                        ResidentData.resetPassword(account.userId, newPassword)
                        message = "Password reset for ${account.name}"
                        accountToReset = null
                    }
                }) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { accountToReset = null }) { Text("Cancel") } }
        )
    }
}
