// #member2
// Module 3 notifications. Any module calls notify() when something changes for a user
// (new announcement, visitor entered, request status updated, ...). In-memory for now.
package com.example.mini.data

import androidx.compose.runtime.mutableStateListOf
import com.example.mini.model.Notification

object NotificationData {
    val notifications = mutableStateListOf<Notification>()

    fun notify(userId: Int, type: String, message: String) {
        val newId = (notifications.maxOfOrNull { it.notificationId } ?: 0) + 1
        notifications.add(Notification(newId, userId, type, message))
    }

    fun forUser(userId: Int): List<Notification> =
        notifications.filter { it.userId == userId }.sortedByDescending { it.createdAt }

    fun unreadCount(userId: Int): Int = notifications.count { it.userId == userId && !it.isRead }

    fun markAllRead(userId: Int) {
        notifications.forEachIndexed { index, n ->
            if (n.userId == userId && !n.isRead) notifications[index] = n.copy(isRead = true)
        }
    }
}
