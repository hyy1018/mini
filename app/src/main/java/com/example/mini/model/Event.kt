// #member2
// Community event and who signed up for it (report 3NF: Event, EventRegistration).
// eventDate is local midnight; startHour / endHour are 0-23.
package com.example.mini.model

data class Event(
    val eventId: Int,
    val createdBy: Int,
    val title: String,
    val description: String,
    val venue: String,
    val eventDate: Long,
    val startHour: Int,
    val endHour: Int,
    val maxParticipants: Int,
    val eventStatus: String = "Upcoming" // Upcoming or Cancelled; past events show as Completed
)

data class EventRegistration(
    val eventId: Int,
    val userId: Int,
    val registeredAt: Long = System.currentTimeMillis()
)
