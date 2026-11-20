// #member3
// Messages inbox, shared by every role: everyone this user has chatted with, newest first,
// with the last message and an unread count. "+ New Chat" opens the contact list.
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.data.CommunityData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDateTime
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmptyState
import com.example.mini.screen.UserAvatar

@Composable
fun ChatInboxScreen(navController: NavHostController) {
    val myId = UserSession.currentUserId ?: return
    val partners = CommunityData.conversationPartners(myId)

    Scaffold(
        topBar = { BackTopBar("Messages", navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.safeNavigate("new_chat") },
                icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                text = { Text("New Chat") }
            )
        }
    ) { innerPadding ->
        if (partners.isEmpty()) {
            EmptyState("No messages yet.\nTap New Chat to message someone.", Modifier.padding(innerPadding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(partners, key = { it }) { partnerId ->
                val last = CommunityData.conversation(myId, partnerId).lastOrNull()
                val unread = CommunityData.unreadFrom(partnerId, myId)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.safeNavigate("chat/$partnerId") }
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(partnerId, size = 44.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            ContactName(partnerId)
                            Text(
                                (if (last?.senderUserId == myId) "You: " else "") + (last?.messageText ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                // unread conversations stand out in bold
                                fontWeight = if (unread > 0) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            last?.let {
                                Text(
                                    formatDateTime(it.sentAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (unread > 0) Badge { Text("$unread") }
                    }
                }
            }
        }
    }
}
