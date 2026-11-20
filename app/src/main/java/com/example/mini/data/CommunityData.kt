// #member3
// Module 6 data: social feed (posts, likes, comments/replies, reports), resident <-> management chat,
// emergency contacts and the Emergency Alert System. In-memory for now.
package com.example.mini.data

import androidx.compose.runtime.mutableStateListOf
import com.example.mini.model.ChatMessage
import com.example.mini.model.Comment
import com.example.mini.model.EmergencyContact
import com.example.mini.model.EmergencyReport
import com.example.mini.model.EmergencyStatus
import com.example.mini.model.Post
import com.example.mini.model.PostLike
import com.example.mini.model.PostReport
import com.example.mini.model.UserRole

object CommunityData {
    val reportReasons = listOf("Spam", "Offensive or abusive", "False information", "Not community related", "Other")
    val emergencyTypes = listOf("Fire", "Medical / Accident", "Security Threat", "Other")
    val contactTypes = listOf("Police", "Ambulance", "Fire", "Security", "Management", "Other")

    val posts = mutableStateListOf<Post>()
    val likes = mutableStateListOf<PostLike>()
    val comments = mutableStateListOf<Comment>()
    val reports = mutableStateListOf<PostReport>()
    val messages = mutableStateListOf<ChatMessage>()
    val contacts = mutableStateListOf<EmergencyContact>()
    val emergencies = mutableStateListOf<EmergencyReport>()

    init {
        val now = System.currentTimeMillis()
        posts.addAll(
            listOf(
                Post(1, 3, "Anyone keen for badminton this Saturday evening? Need 2 more players!", null, createdAt = now - 3 * HOUR_MILLIS),
                Post(2, 1, "Reminder: the gym will close early on Friday at 8 PM for servicing.", null, createdAt = now - DAY_MILLIS)
            )
        )
        likes.add(PostLike(2, 3))
        comments.add(Comment(1, 1, 1, null, "I can join after 7 PM.", createdAt = now - 2 * HOUR_MILLIS))
        // sample chats: resident1 -> staff1 (answered), resident1 -> guard1
        messages.addAll(
            listOf(
                ChatMessage(1, 3, 1, "Hi, can I register two visitors for the same day?", now - 5 * HOUR_MILLIS, isRead = true),
                ChatMessage(2, 1, 3, "Yes, just create one pass for each visitor.", now - 4 * HOUR_MILLIS),
                ChatMessage(3, 3, 2, "My parcel will arrive at 3 PM, please let the rider in.", now - HOUR_MILLIS)
            )
        )
        contacts.addAll(
            listOf(
                EmergencyContact(1, "Police / Ambulance", "999", "Police"),
                EmergencyContact(2, "Fire and Rescue", "994", "Fire"),
                EmergencyContact(3, "Guardhouse", "03-1234 5000", "Security"),
                EmergencyContact(4, "Management Office", "03-1234 5678", "Management")
            )
        )
    }

    // ---------- Posts ----------
    fun getPost(id: Int): Post? = posts.find { it.postId == id }
    fun feed(): List<Post> = posts.filter { it.postStatus == "Active" }.sortedByDescending { it.createdAt }

    fun createPost(userId: Int, content: String, photoPath: String?) {
        val newId = (posts.maxOfOrNull { it.postId } ?: 0) + 1
        posts.add(Post(newId, userId, content, photoPath))
    }

    // author deletes their own post along with its likes, comments and reports
    fun deletePost(postId: Int) {
        likes.removeAll { it.postId == postId }
        comments.removeAll { it.postId == postId }
        reports.removeAll { it.postId == postId }
        posts.removeAll { it.postId == postId }
    }

    fun likeCount(postId: Int): Int = likes.count { it.postId == postId }
    fun isLiked(postId: Int, userId: Int): Boolean = likes.any { it.postId == postId && it.userId == userId }

    fun toggleLike(postId: Int, userId: Int) {
        if (isLiked(postId, userId)) likes.removeAll { it.postId == postId && it.userId == userId }
        else likes.add(PostLike(postId, userId))
    }

    // ---------- Comments & replies ----------
    fun topComments(postId: Int): List<Comment> = comments.filter { it.postId == postId && it.parentCommentId == null }.sortedBy { it.createdAt }
    fun replies(commentId: Int): List<Comment> = comments.filter { it.parentCommentId == commentId }.sortedBy { it.createdAt }
    fun commentCount(postId: Int): Int = comments.count { it.postId == postId }

    fun addComment(postId: Int, userId: Int, content: String, parentCommentId: Int?) {
        val newId = (comments.maxOfOrNull { it.commentId } ?: 0) + 1
        comments.add(Comment(newId, postId, userId, parentCommentId, content))
    }

    // ---------- Reports & moderation ----------
    fun pendingReports(): List<PostReport> = reports.filter { it.reportStatus == "Pending" }.sortedByDescending { it.reportedAt }

    // returns an error message, or null when reported
    fun reportPost(postId: Int, userId: Int, reason: String): String? {
        if (reports.any { it.postId == postId && it.reportedByUserId == userId && it.reportStatus == "Pending" }) {
            return "You have already reported this post"
        }
        val newId = (reports.maxOfOrNull { it.reportId } ?: 0) + 1
        reports.add(PostReport(newId, postId, userId, reason))
        return null
    }

    // staff decision: remove the post (closes every pending report on it) or dismiss this report
    fun reviewReport(reportId: Int, removePost: Boolean, staffId: Int) {
        val report = reports.find { it.reportId == reportId } ?: return
        val now = System.currentTimeMillis()
        if (removePost) {
            val postIndex = posts.indexOfFirst { it.postId == report.postId }
            if (postIndex != -1) {
                val post = posts[postIndex]
                posts[postIndex] = post.copy(postStatus = "Removed", updatedAt = now)
                NotificationData.notify(post.userId, "Community", "Your post was removed by management for breaking community rules")
            }
            reports.forEachIndexed { index, r ->
                if (r.postId == report.postId && r.reportStatus == "Pending") {
                    reports[index] = r.copy(reportStatus = "Removed", reviewedByUserId = staffId, reviewedAt = now)
                }
            }
        } else {
            reports[reports.indexOf(report)] = report.copy(reportStatus = "Dismissed", reviewedByUserId = staffId, reviewedAt = now)
        }
    }

    // ---------- Chat (one-to-one, between any two accounts) ----------
    // every message between me and one partner, oldest first
    fun conversation(myUserId: Int, partnerUserId: Int): List<ChatMessage> = messages.filter {
        (it.senderUserId == myUserId && it.receiverUserId == partnerUserId) ||
            (it.senderUserId == partnerUserId && it.receiverUserId == myUserId)
    }.sortedBy { it.sentAt }

    fun getMessage(id: Int): ChatMessage? = messages.find { it.messageId == id }

    fun sendMessage(fromUserId: Int, toUserId: Int, text: String) {
        val newId = (messages.maxOfOrNull { it.messageId } ?: 0) + 1
        messages.add(ChatMessage(newId, fromUserId, toUserId, text))
        val senderName = ResidentData.getUser(fromUserId)?.name ?: "Someone"
        NotificationData.notify(toUserId, "Chat", "New message from $senderName")
    }

    // only the sender may edit or delete -- the screen only offers it on your own messages,
    // and this check stops anything else from changing someone else's message
    fun editMessage(messageId: Int, byUserId: Int, newText: String) {
        val index = messages.indexOfFirst { it.messageId == messageId && it.senderUserId == byUserId }
        if (index != -1) messages[index] = messages[index].copy(messageText = newText)
    }

    // one shared row per message, so deleting removes it for both people
    fun deleteMessage(messageId: Int, byUserId: Int) {
        messages.removeAll { it.messageId == messageId && it.senderUserId == byUserId }
    }

    // everyone I've exchanged messages with, most recent conversation first
    fun conversationPartners(myUserId: Int): List<Int> = messages
        .filter { it.senderUserId == myUserId || it.receiverUserId == myUserId }
        .map { if (it.senderUserId == myUserId) it.receiverUserId else it.senderUserId }
        .distinct()
        .sortedByDescending { partner -> conversation(myUserId, partner).lastOrNull()?.sentAt ?: 0L }

    fun unreadFrom(partnerUserId: Int, myUserId: Int): Int =
        messages.count { it.senderUserId == partnerUserId && it.receiverUserId == myUserId && !it.isRead }

    fun totalUnread(myUserId: Int): Int = messages.count { it.receiverUserId == myUserId && !it.isRead }

    // opening a conversation marks the partner's messages to me as read
    fun markConversationRead(myUserId: Int, partnerUserId: Int) {
        messages.forEachIndexed { index, m ->
            if (m.senderUserId == partnerUserId && m.receiverUserId == myUserId && !m.isRead) {
                messages[index] = m.copy(isRead = true)
            }
        }
    }

    // ---------- Emergency contacts ----------
    fun getContact(id: Int): EmergencyContact? = contacts.find { it.contactId == id }

    fun addContact(name: String, phone: String, type: String) {
        val newId = (contacts.maxOfOrNull { it.contactId } ?: 0) + 1
        contacts.add(EmergencyContact(newId, name, phone, type))
    }

    fun updateContact(id: Int, name: String, phone: String, type: String) {
        val index = contacts.indexOfFirst { it.contactId == id }
        if (index != -1) contacts[index] = contacts[index].copy(contactName = name, phoneNo = phone, contactType = type)
    }

    fun deleteContact(id: Int) {
        contacts.removeAll { it.contactId == id }
    }

    // ---------- Emergency Alert System (unique feature) ----------
    fun getEmergency(id: Int): EmergencyReport? = emergencies.find { it.emergencyId == id }
    fun emergenciesOfResident(residentId: Int): List<EmergencyReport> =
        emergencies.filter { it.residentId == residentId }.sortedByDescending { it.reportedAt }
    fun allEmergencies(): List<EmergencyReport> = emergencies.sortedByDescending { it.reportedAt }
    fun activeEmergencies(): List<EmergencyReport> = emergencies
        .filter { it.reportStatus == EmergencyStatus.REPORTED || it.reportStatus == EmergencyStatus.ACKNOWLEDGED }
        .sortedByDescending { it.reportedAt }

    // anti-abuse: at most MAX_ALERTS per resident within ALERT_WINDOW_MILLIS.
    // Cancelled alerts count too, so "send, cancel, send again" can't be repeated endlessly.
    const val MAX_ALERTS = 2
    const val ALERT_WINDOW_MILLIS = 5 * 60 * 1000L

    fun recentAlertCount(residentId: Int): Int {
        val since = System.currentTimeMillis() - ALERT_WINDOW_MILLIS
        return emergencies.count { it.residentId == residentId && it.reportedAt >= since }
    }

    fun isAlertLimitReached(residentId: Int): Boolean = recentAlertCount(residentId) >= MAX_ALERTS

    // one tap from the resident: the unit comes from their household record, and every
    // staff and security account is notified straight away.
    // Returns null when the resident has hit the 2-alerts-in-5-minutes limit.
    fun sendAlert(residentId: Int, type: String, description: String): EmergencyReport? {
        // a panicked double tap must not raise two alerts -- reuse the one that's still open
        activeEmergencies().firstOrNull { it.residentId == residentId }?.let { return it }
        if (isAlertLimitReached(residentId)) return null
        val newId = (emergencies.maxOfOrNull { it.emergencyId } ?: 0) + 1
        val report = EmergencyReport(newId, residentId, type, description)
        emergencies.add(report)
        val unit = ResidentData.unitLabel(residentId)
        ResidentData.users
            .filter { it.role != UserRole.RESIDENT }
            .forEach { NotificationData.notify(it.userId, "Emergency", "EMERGENCY: $type at $unit") }
        return report
    }

    fun acknowledge(id: Int, userId: Int) {
        val index = emergencies.indexOfFirst { it.emergencyId == id }
        if (index == -1 || emergencies[index].reportStatus != EmergencyStatus.REPORTED) return
        emergencies[index] = emergencies[index].copy(
            reportStatus = EmergencyStatus.ACKNOWLEDGED, acknowledgedBy = userId, acknowledgedAt = System.currentTimeMillis()
        )
        notifyReporter(emergencies[index], "Help is on the way -- your emergency alert was acknowledged")
    }

    fun resolve(id: Int, userId: Int, remarks: String) {
        val index = emergencies.indexOfFirst { it.emergencyId == id }
        if (index == -1) return
        val report = emergencies[index]
        emergencies[index] = report.copy(
            reportStatus = EmergencyStatus.RESOLVED,
            acknowledgedBy = report.acknowledgedBy ?: userId,
            acknowledgedAt = report.acknowledgedAt ?: System.currentTimeMillis(),
            resolvedAt = System.currentTimeMillis(),
            remarks = remarks
        )
        notifyReporter(emergencies[index], "Your emergency report has been resolved")
    }

    // resident pressed SOS by mistake
    fun cancelAlert(id: Int) {
        val index = emergencies.indexOfFirst { it.emergencyId == id }
        if (index != -1 && emergencies[index].reportStatus == EmergencyStatus.REPORTED) {
            emergencies[index] = emergencies[index].copy(reportStatus = EmergencyStatus.CANCELLED, remarks = "Cancelled by resident (false alarm)")
        }
    }

    private fun notifyReporter(report: EmergencyReport, message: String) {
        ResidentData.getResident(report.residentId)?.let { NotificationData.notify(it.userId, "Emergency", message) }
    }
}
