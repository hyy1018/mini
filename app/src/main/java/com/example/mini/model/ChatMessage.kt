// #member3
// One message from one account to another (report 3NF: ChatMessage).
// Any two accounts can chat: resident, management or security.
package com.example.mini.model

data class ChatMessage(
    val messageId: Int,
    val senderUserId: Int,
    val receiverUserId: Int,
    val messageText: String,
    val sentAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
