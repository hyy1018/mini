// #member3
// Enquiry chat between one resident and management (Community Chat Function).
// Residents open their own thread; staff open a resident's thread from the inbox.
package com.example.mini.screen.member3.module6

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.data.CommunityData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDateTime
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmptyState

@Composable
fun ChatThreadScreen(residentUserId: Int, navController: NavHostController) {
    val isStaff = UserSession.isStaff
    val myId = UserSession.currentUserId ?: return
    val messages = CommunityData.conversation(residentUserId)
    val listState = rememberLazyListState()
    var text by remember { mutableStateOf("") }

    val title = if (isStaff) {
        val resident = ResidentData.residentOfUser(residentUserId)
        "${ResidentData.getUser(residentUserId)?.name ?: "Resident"} (${resident?.let { ResidentData.unitLabel(it.residentId) } ?: "-"})"
    } else {
        "Chat with Management"
    }

    // mark the other side's messages read, and keep the newest message in view
    LaunchedEffect(messages.size) {
        CommunityData.markConversationRead(residentUserId, byManagement = isStaff)
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Scaffold(
        topBar = { BackTopBar(title, navController) },
        bottomBar = {
            Surface(tonalElevation = 3.dp, modifier = Modifier.imePadding()) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it.take(1000) },
                        placeholder = { Text(if (isStaff) "Reply to resident..." else "Ask management a question...") },
                        maxLines = 4,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (isStaff) CommunityData.replyToResident(myId, residentUserId, text.trim())
                            else CommunityData.sendToManagement(myId, text.trim())
                            text = ""
                        },
                        enabled = text.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    ) { innerPadding ->
        if (messages.isEmpty()) {
            EmptyState(
                if (isStaff) "No messages" else "Send management a question -- e.g. about visitors, bookings or fees.",
                Modifier.padding(innerPadding)
            )
            return@Scaffold
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.messageId }) { message ->
                // "mine" = sent from this side of the conversation
                val fromResident = message.senderUserId == residentUserId
                val mine = if (isStaff) !fromResident else fromResident
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            if (!fromResident && !isStaff) {
                                Text(
                                    ResidentData.getUser(message.senderUserId)?.name ?: "Management",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Text(message.messageText, style = MaterialTheme.typography.bodyMedium)
                            Text(formatDateTime(message.sentAt), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
