// #member2
// A repair request from a resident (report 3NF: MaintenanceRequest).
// assignedTechnician is a name for now -- there are no technician accounts yet.
package com.example.mini.model

data class MaintenanceRequest(
    val requestId: Int,
    val residentId: Int,
    val facilityId: Int?,
    val category: String,
    val location: String,
    val description: String,
    val priority: String,
    val requestStatus: RequestStatus = RequestStatus.PENDING,
    val assignedTechnician: String = "",
    val assignedByUserId: Int? = null,
    val remarks: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
