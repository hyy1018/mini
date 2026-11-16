// #member2
// An in-app notification for one user (report 3NF: Notification). Other modules create these
// through NotificationData -- new announcements, visitor entry, request status changes, etc.
package com.example.mini.model

data class Notification(
    val notificationId: Int,
    val userId: Int,
    val notificationType: String,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
