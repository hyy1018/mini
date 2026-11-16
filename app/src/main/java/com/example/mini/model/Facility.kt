// #member2
// Shared facility and its booking rules (report 3NF: Facility).
// Hours are 0-23; e.g. openHour 8, closeHour 22, slotHours 1 gives slots 08:00 ... 21:00.
package com.example.mini.model

data class Facility(
    val facilityId: Int,
    val facilityName: String,
    val description: String,
    val location: String,
    val capacity: Int,
    val openHour: Int,
    val closeHour: Int,
    val slotHours: Int,
    val maxBookingsPerDay: Int,
    val advanceBookingDays: Int,
    val facilityStatus: String = "Available" // Available or Under Maintenance
)
