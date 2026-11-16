// #member3
// Emergency contact numbers and resident emergency reports (report 3NF: EmergencyContact, EmergencyReport).
// The resident's unit isn't stored on the report -- it comes from Resident -> Household, so it's always current.
package com.example.mini.model

enum class EmergencyStatus(val label: String) {
    REPORTED("Reported"),
    ACKNOWLEDGED("Acknowledged"),
    RESOLVED("Resolved"),
    CANCELLED("Cancelled")
}

data class EmergencyContact(
    val contactId: Int,
    val contactName: String,
    val phoneNo: String,
    val contactType: String
)

data class EmergencyReport(
    val emergencyId: Int,
    val residentId: Int,
    val emergencyType: String,
    val description: String,
    val reportStatus: EmergencyStatus = EmergencyStatus.REPORTED,
    val reportedAt: Long = System.currentTimeMillis(),
    val acknowledgedBy: Int? = null,
    val acknowledgedAt: Long? = null,
    val resolvedAt: Long? = null,
    val remarks: String = ""
)
