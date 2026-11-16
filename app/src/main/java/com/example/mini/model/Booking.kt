// #member2
// One facility reservation (report 3NF: Booking). Cancelling keeps the row with a cancelledAt time.
package com.example.mini.model

enum class BookingStatus(val label: String) {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled")
}

data class Booking(
    val bookingId: Int,
    val facilityId: Int,
    val residentId: Int,
    val bookingDate: Long,
    val startHour: Int,
    val endHour: Int,
    val bookingStatus: BookingStatus = BookingStatus.CONFIRMED,
    val createdAt: Long = System.currentTimeMillis(),
    val cancelledAt: Long? = null
)
