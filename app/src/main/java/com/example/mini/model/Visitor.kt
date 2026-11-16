// #member1
// A pre-registered visitor and their Smart Visitor Pass (report 3NF: Visitor).
package com.example.mini.model

enum class PassStatus(val label: String) {
    VALID("Valid"),
    USED("Used"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled")
}

data class Visitor(
    val visitorId: Int,
    val residentId: Int,
    val visitorName: String,
    val phoneNo: String,
    val vehiclePlate: String,
    val purpose: String,
    val visitDate: Long,
    val qrCode: String,
    val validFrom: Long,
    val validUntil: Long,
    val passStatus: PassStatus = PassStatus.VALID,
    val createdAt: Long = System.currentTimeMillis()
)
