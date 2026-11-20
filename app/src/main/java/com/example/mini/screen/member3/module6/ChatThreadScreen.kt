// #member3
// One conversation between the logged-in user and one partner, plus a reply box.
// Your own messages have Edit and Delete, like the earlier group project.
package com.example.mini.screen.member3.module6

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.data.CommunityData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDateTime
import com.example.mini.model.ChatMessage
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmptyState

@Composable
fun ChatThreadScreen(partnerUserId: Int, navController: NavHostController) {
    val myId = UserSession.currentUserId ?: return
    val partner = ResidentData.getUser(partnerUserId)
    val messages = CommunityData.conversation(myId, partnerUserId)
    val listState = rememberLazyListState()

    var reply by remember { mutableStateOf("") }
    var messagePendingDelete by remember { mutableStateOf<ChatMessage?>(null) }
    var messageBeingEdited by remember { mutableStateOf<ChatMessage?>(null) }
    // TextFieldValue (not String) so the cursor can start at the end of the message being edited
    var editDraft by remember { mutableStateOf(TextFieldValue("")) }

    // open at the newest message, and mark the partner's messages as read
    LaunchedEffect(messages.size) {
        CommunityData.markConversationRead(myId, partnerUserId)
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Scaffold(
        topBar = { BackTopBar(partner?.name ?: "Deleted account", navController) },
        bottomBar = {
            // no reply box if the other account was deleted -- nobody would receive it
            if (partner != null) {
                Surface(tonalElevation = 3.dp, modifier = Modifier.imePadding()) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = reply,
                            onValueChange = { reply = it.take(1000) },
                            placeholder = { Text("Message ${partner.name}...") },
                            maxLines = 4,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                CommunityData.sendMessage(myId, partnerUserId, reply.trim())
                                reply = ""
                            },
                            enabled = reply.isNotBlank()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (messages.isEmpty()) {
            EmptyState("No messages yet. Say hello!", Modifier.padding(innerPadding))
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
                val isOwn = message.senderUserId == myId
                MessageBubble(
                    message = message,
                    isOwn = isOwn,
                    // edit / delete only on your own messages
                    onEdit = if (isOwn) {
                        {
                            messageBeingEdited = message
                            editDraft = TextFieldValue(message.messageText, selection = TextRange(message.messageText.length))
                        }
                    } else null,
                    onDelete = if (isOwn) { { messagePendingDelete = message } } else null
                )
            }
        }
    }

    messagePendingDelete?.let { message ->
        AlertDialog(
            onDismissRequest = { messagePendingDelete = null },
            title = { Text("Delete message?") },
            text = { Text("This removes it from both sides of the conversation. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    CommunityData.deleteMessage(message.messageId, myId)
                    messagePendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { messagePendingDelete = null }) { Text("Cancel") } }
        )
    }

    messageBeingEdited?.let { message ->
        AlertDialog(
            onDismissRequest = { messageBeingEdited = null },
            title = { Text("Edit message") },
            text = {
                OutlinedTextField(
                    value = editDraft,
                    onValueChange = { if (it.text.length <= 1000) editDraft = it },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        CommunityData.editMessage(message.messageId, myId, editDraft.text.trim())
                        messageBeingEdited = null
                    },
                    // an empty edit would leave a blank bubble -- delete it instead
                    enabled = editDraft.text.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { messageBeingEdited = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isOwn: Boolean,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.78f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isOwn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(start = 12.dp, top = 10.dp, end = 4.dp, bottom = 4.dp)) {
                Text(message.messageText, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(end = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatDateTime(message.sentAt) + if (isOwn && message.isRead) " · Read" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (onEdit != null) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit message", modifier = Modifier.size(18.dp))
                        }
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete message", modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                }
            }
        }
    }
}
