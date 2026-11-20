// #member3
// Pick who to chat with. Two tabs: Management & Residents, and Security.
// Search matches name or unit; tapping a person opens the conversation.
package com.example.mini.screen.member3.module6

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.model.UserRole
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmptyState
import com.example.mini.screen.StatusBadge
import com.example.mini.screen.UserAvatar

private val tabs = listOf("Management & Residents", "Security")

@Composable
fun NewChatScreen(navController: NavHostController) {
    val myId = UserSession.currentUserId ?: return
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }

    val roles = if (selectedTab == 0) setOf(UserRole.STAFF, UserRole.RESIDENT) else setOf(UserRole.SECURITY)
    val contacts = ResidentData.chatContacts(myId, roles)
        // management first, then residents, each alphabetical
        .sortedBy { if (it.role == UserRole.STAFF) 0 else 1 }
        .filter { user ->
            query.isBlank() ||
                user.name.contains(query, ignoreCase = true) ||
                unitOf(user.userId).contains(query, ignoreCase = true)
        }

    Scaffold(topBar = { BackTopBar("New Chat", navController) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it.take(100) },
                label = { Text(if (selectedTab == 0) "Search name or unit" else "Search name") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            if (contacts.isEmpty()) {
                EmptyState("No one found")
                return@Column
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts, key = { it.userId }) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.safeNavigate("chat/${user.userId}") }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(user.userId, size = 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) { ContactName(user.userId) }
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Message ${user.name}", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

private fun unitOf(userId: Int): String =
    ResidentData.residentOfUser(userId)?.let { ResidentData.unitLabel(it.residentId) } ?: ""

// name + role badge (Management / Security) or the resident's unit -- shared by the inbox and New Chat
@Composable
fun ContactName(userId: Int) {
    val user = ResidentData.getUser(userId)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(user?.name ?: "Deleted account", style = MaterialTheme.typography.titleSmall)
        when (user?.role) {
            UserRole.STAFF -> {
                Spacer(Modifier.width(6.dp))
                StatusBadge("Management", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
            }
            UserRole.SECURITY -> {
                Spacer(Modifier.width(6.dp))
                StatusBadge("Security", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
            }
            else -> {}
        }
    }
    if (user?.role == UserRole.RESIDENT) {
        Text(unitOf(userId), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
