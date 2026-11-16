// #member3
// Resident complaint with optional photo and the management response (report 3NF: Complaint),
// plus service feedback (report 3NF: Feedback).
package com.example.mini.model

data class Complaint(
    val complaintId: Int,
    val residentId: Int,
    val category: String,
    val title: String,
    val description: String,
    val photoPath: String?,
    val complaintStatus: RequestStatus = RequestStatus.PENDING,
    val submittedAt: Long = System.currentTimeMillis(),
    val responseText: String = "",
    val respondedBy: Int? = null,
    val respondedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

data class Feedback(
    val feedbackId: Int,
    val residentId: Int,
    val serviceType: String,
    val rating: Int, // 1-5 stars
    val comments: String,
    val submittedAt: Long = System.currentTimeMillis()
)
