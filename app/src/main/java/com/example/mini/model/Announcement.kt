// #member2
// Community announcement and its read confirmations (report 3NF: Announcement, AnnouncementRead).
package com.example.mini.model

data class Announcement(
    val announcementId: Int,
    val createdBy: Int,
    val title: String,
    val content: String,
    val category: String,
    val isImportant: Boolean,
    val postedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// one row per user who confirmed they read an important announcement
data class AnnouncementRead(
    val announcementId: Int,
    val userId: Int,
    val readAt: Long = System.currentTimeMillis()
)
