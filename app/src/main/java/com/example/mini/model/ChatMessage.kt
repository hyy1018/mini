// #member3
// One message between a resident and management (report 3NF: ChatMessage).
// Residents write to the shared management inbox (receiverUserId = CommunityData.MANAGEMENT_INBOX),
// so any staff member can answer.
package com.example.mini.model

data class ChatMessage(
    val messageId: Int,
    val senderUserId: Int,
    val receiverUserId: Int,
    val messageText: String,
    val sentAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
