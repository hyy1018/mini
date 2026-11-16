// #member1
// Resident details linked to a User and a Household (report 3NF: Resident).
package com.example.mini.model

enum class RegistrationStatus(val label: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

data class Resident(
    val residentId: Int,
    val userId: Int,
    val householdId: Int,
    val icNo: String,
    val residentType: String,
    val registrationStatus: RegistrationStatus = RegistrationStatus.PENDING,
    val verifiedBy: Int? = null,
    val verifiedAt: Long? = null,
    val registeredAt: Long = System.currentTimeMillis()
)
