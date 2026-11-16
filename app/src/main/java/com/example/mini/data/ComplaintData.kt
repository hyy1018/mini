// #member3
// Module 5 data: complaints (with photo, management response and status) and service feedback.
// In-memory for now.
package com.example.mini.data

import androidx.compose.runtime.mutableStateListOf
import com.example.mini.model.Complaint
import com.example.mini.model.Feedback
import com.example.mini.model.RequestStatus

object ComplaintData {
    val categories = listOf("Noise", "Cleanliness", "Facility Damage", "Security", "Parking", "Other")
    val serviceTypes = listOf("Security", "Cleaning", "Maintenance", "Management Office", "Facilities", "App")

    val complaints = mutableStateListOf<Complaint>()
    val feedbacks = mutableStateListOf<Feedback>()

    init {
        complaints.add(
            Complaint(1, 1, "Noise", "Loud music at night", "Unit above plays loud music after midnight on weekends.", null)
        )
        feedbacks.add(Feedback(1, 1, "Security", 4, "Guards are friendly and quick at the gate."))
    }

    // ---------- Complaints ----------
    fun getComplaint(id: Int): Complaint? = complaints.find { it.complaintId == id }
    fun complaintsOfResident(residentId: Int): List<Complaint> =
        complaints.filter { it.residentId == residentId }.sortedByDescending { it.submittedAt }
    fun allComplaints(): List<Complaint> = complaints.sortedByDescending { it.submittedAt }

    // residents may only change or withdraw a complaint before management starts on it
    fun canEdit(complaint: Complaint): Boolean = complaint.complaintStatus == RequestStatus.PENDING && complaint.responseText.isBlank()

    fun submitComplaint(residentId: Int, category: String, title: String, description: String, photoPath: String?) {
        val newId = (complaints.maxOfOrNull { it.complaintId } ?: 0) + 1
        complaints.add(Complaint(newId, residentId, category, title, description, photoPath))
    }

    fun updateComplaint(id: Int, category: String, title: String, description: String, photoPath: String?) {
        val index = complaints.indexOfFirst { it.complaintId == id }
        if (index != -1) {
            complaints[index] = complaints[index].copy(
                category = category, title = title, description = description,
                photoPath = photoPath, updatedAt = System.currentTimeMillis()
            )
        }
    }

    fun deleteComplaint(id: Int) {
        complaints.removeAll { it.complaintId == id }
    }

    // management reply; the resident is notified
    fun respond(id: Int, responseText: String, staffId: Int) {
        val index = complaints.indexOfFirst { it.complaintId == id }
        if (index == -1) return
        val now = System.currentTimeMillis()
        complaints[index] = complaints[index].copy(
            responseText = responseText, respondedBy = staffId, respondedAt = now, updatedAt = now
        )
        notifyResident(complaints[index].residentId, "Management replied to your complaint: ${complaints[index].title}")
    }

    fun updateStatus(id: Int, status: RequestStatus) {
        val index = complaints.indexOfFirst { it.complaintId == id }
        if (index == -1) return
        complaints[index] = complaints[index].copy(complaintStatus = status, updatedAt = System.currentTimeMillis())
        notifyResident(complaints[index].residentId, "Complaint \"${complaints[index].title}\" is now ${status.label}")
    }

    private fun notifyResident(residentId: Int, message: String) {
        ResidentData.getResident(residentId)?.let { NotificationData.notify(it.userId, "Complaint", message) }
    }

    // ---------- Feedback ----------
    fun feedbackOfResident(residentId: Int): List<Feedback> =
        feedbacks.filter { it.residentId == residentId }.sortedByDescending { it.submittedAt }
    fun allFeedback(): List<Feedback> = feedbacks.sortedByDescending { it.submittedAt }

    fun submitFeedback(residentId: Int, serviceType: String, rating: Int, comments: String) {
        val newId = (feedbacks.maxOfOrNull { it.feedbackId } ?: 0) + 1
        feedbacks.add(Feedback(newId, residentId, serviceType, rating, comments))
    }

    fun averageRating(): Double = if (feedbacks.isEmpty()) 0.0 else feedbacks.map { it.rating }.average()
}
