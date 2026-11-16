// #member2
// Module 4 data: facilities, bookings (Smart Facility Booking rules) and maintenance requests.
// In-memory for now.
package com.example.mini.data

import androidx.compose.runtime.mutableStateListOf
import com.example.mini.model.Booking
import com.example.mini.model.BookingStatus
import com.example.mini.model.Facility
import com.example.mini.model.MaintenanceRequest
import com.example.mini.model.RequestStatus

object FacilityData {
    val maintenanceCategories = listOf("Plumbing", "Electrical", "Lift", "Door / Lock", "Common Area", "Other")
    val priorities = listOf("Low", "Medium", "High")
    val facilityStatuses = listOf("Available", "Under Maintenance")

    val facilities = mutableStateListOf<Facility>()
    val bookings = mutableStateListOf<Booking>()
    val maintenanceRequests = mutableStateListOf<MaintenanceRequest>()

    init {
        facilities.addAll(
            listOf(
                Facility(1, "Badminton Court", "Indoor court, 2 rackets provided", "Block A, Level 2", 4, 8, 22, 1, 1, 7),
                Facility(2, "Swimming Pool", "25m pool with kids area", "Block B, Level 3", 20, 7, 22, 2, 1, 7),
                Facility(3, "Gymnasium", "Treadmills, weights and bikes", "Block A, Level 3", 10, 6, 23, 1, 2, 3),
                Facility(4, "Multipurpose Hall", "Events and gatherings, up to 80 people", "Block C, Ground Floor", 80, 9, 23, 3, 1, 30),
                Facility(5, "BBQ Area", "2 BBQ pits with tables", "Garden, near Block B", 15, 16, 23, 3, 1, 14)
            )
        )
        maintenanceRequests.add(
            MaintenanceRequest(1, 1, null, "Plumbing", "Unit A-10-05 kitchen", "Kitchen sink is leaking under the cabinet.", "Medium")
        )
    }

    // ---------- Facilities ----------
    fun getFacility(id: Int): Facility? = facilities.find { it.facilityId == id }

    // start hours of every slot, e.g. 8,9,...,21 for 08:00-22:00 with 1-hour slots
    fun slotStartHours(facility: Facility): List<Int> =
        (facility.openHour until facility.closeHour step facility.slotHours)
            .filter { it + facility.slotHours <= facility.closeHour }

    fun addFacility(
        name: String, description: String, location: String, capacity: Int, openHour: Int, closeHour: Int,
        slotHours: Int, maxBookingsPerDay: Int, advanceBookingDays: Int, status: String
    ) {
        val newId = (facilities.maxOfOrNull { it.facilityId } ?: 0) + 1
        facilities.add(
            Facility(newId, name, description, location, capacity, openHour, closeHour, slotHours, maxBookingsPerDay, advanceBookingDays, status)
        )
    }

    fun updateFacility(facility: Facility) {
        val index = facilities.indexOfFirst { it.facilityId == facility.facilityId }
        if (index != -1) facilities[index] = facility
    }

    fun isFacilityNameTaken(name: String, exceptId: Int): Boolean =
        facilities.any { it.facilityName.equals(name.trim(), ignoreCase = true) && it.facilityId != exceptId }

    // ---------- Bookings ----------
    fun getBooking(id: Int): Booking? = bookings.find { it.bookingId == id }

    fun confirmedOn(facilityId: Int, date: Long): List<Booking> = bookings.filter {
        it.facilityId == facilityId && it.bookingDate == startOfDay(date) && it.bookingStatus == BookingStatus.CONFIRMED
    }

    fun bookingsOfResident(residentId: Int): List<Booking> =
        bookings.filter { it.residentId == residentId }.sortedByDescending { atHour(it.bookingDate, it.startHour) }

    fun allBookings(): List<Booking> = bookings.sortedByDescending { atHour(it.bookingDate, it.startHour) }

    fun hasStarted(booking: Booking): Boolean = atHour(booking.bookingDate, booking.startHour) <= System.currentTimeMillis()

    // Smart Facility Booking rule check. Returns why a slot can't be booked, or null if it can.
    // excludeBookingId lets a booking being moved ignore its own current slot.
    fun checkBooking(facility: Facility, residentId: Int, date: Long, startHour: Int, excludeBookingId: Int = -1): String? {
        val day = startOfDay(date)
        val today = startOfDay(System.currentTimeMillis())
        val lastDay = addDays(today, facility.advanceBookingDays)
        val endHour = startHour + facility.slotHours
        val sameDay = confirmedOn(facility.facilityId, day).filter { it.bookingId != excludeBookingId }
        return when {
            facility.facilityStatus != "Available" -> "${facility.facilityName} is under maintenance"
            day < today -> "You can't book a past date"
            day > lastDay -> "Bookings open only ${facility.advanceBookingDays} days in advance"
            atHour(day, startHour) <= System.currentTimeMillis() -> "This time slot has already passed"
            // two bookings overlap if each starts before the other ends
            sameDay.any { startHour < it.endHour && it.startHour < endHour } -> "This slot is already booked"
            sameDay.count { it.residentId == residentId } >= facility.maxBookingsPerDay ->
                "Daily limit reached: ${facility.maxBookingsPerDay} booking(s) per day for this facility"
            else -> null
        }
    }

    // returns an error message, or null when booked
    fun book(facility: Facility, residentId: Int, date: Long, startHour: Int): String? {
        checkBooking(facility, residentId, date, startHour)?.let { return it }
        val newId = (bookings.maxOfOrNull { it.bookingId } ?: 0) + 1
        bookings.add(Booking(newId, facility.facilityId, residentId, startOfDay(date), startHour, startHour + facility.slotHours))
        ResidentData.getResident(residentId)?.let {
            NotificationData.notify(
                it.userId, "Booking",
                "Booking confirmed: ${facility.facilityName}, ${formatDate(date)} ${formatHour(startHour)}"
            )
        }
        return null
    }

    // Update (Flowchart c): move an existing booking to another slot
    fun reschedule(bookingId: Int, date: Long, startHour: Int): String? {
        val booking = getBooking(bookingId) ?: return "Booking not found"
        val facility = getFacility(booking.facilityId) ?: return "Facility not found"
        if (booking.bookingStatus != BookingStatus.CONFIRMED || hasStarted(booking)) return "This booking can no longer be changed"
        checkBooking(facility, booking.residentId, date, startHour, excludeBookingId = bookingId)?.let { return it }
        bookings[bookings.indexOf(booking)] = booking.copy(
            bookingDate = startOfDay(date), startHour = startHour, endHour = startHour + facility.slotHours
        )
        return null
    }

    // Cancel (Flowchart d): kept as Cancelled so the record stays; not allowed once the slot has started
    fun cancelBooking(bookingId: Int): String? {
        val booking = getBooking(bookingId) ?: return "Booking not found"
        if (booking.bookingStatus == BookingStatus.CANCELLED) return "Already cancelled"
        if (hasStarted(booking)) return "This booking has already started"
        bookings[bookings.indexOf(booking)] = booking.copy(
            bookingStatus = BookingStatus.CANCELLED,
            cancelledAt = System.currentTimeMillis()
        )
        return null
    }

    // ---------- Maintenance requests ----------
    fun getRequest(id: Int): MaintenanceRequest? = maintenanceRequests.find { it.requestId == id }
    fun requestsOfResident(residentId: Int): List<MaintenanceRequest> =
        maintenanceRequests.filter { it.residentId == residentId }.sortedByDescending { it.submittedAt }
    fun allRequests(): List<MaintenanceRequest> = maintenanceRequests.sortedByDescending { it.submittedAt }

    fun submitRequest(residentId: Int, facilityId: Int?, category: String, location: String, description: String, priority: String) {
        val newId = (maintenanceRequests.maxOfOrNull { it.requestId } ?: 0) + 1
        maintenanceRequests.add(MaintenanceRequest(newId, residentId, facilityId, category, location, description, priority))
    }

    // staff assigns a technician; a pending request moves to In Progress
    fun assignTechnician(requestId: Int, technician: String, staffId: Int) {
        val index = maintenanceRequests.indexOfFirst { it.requestId == requestId }
        if (index == -1) return
        val request = maintenanceRequests[index]
        maintenanceRequests[index] = request.copy(
            assignedTechnician = technician,
            assignedByUserId = staffId,
            requestStatus = if (request.requestStatus == RequestStatus.PENDING) RequestStatus.IN_PROGRESS else request.requestStatus
        )
        notifyResident(request.residentId, "Repair #$requestId assigned to $technician")
    }

    fun updateRequestStatus(requestId: Int, status: RequestStatus, remarks: String) {
        val index = maintenanceRequests.indexOfFirst { it.requestId == requestId }
        if (index == -1) return
        val request = maintenanceRequests[index]
        maintenanceRequests[index] = request.copy(
            requestStatus = status,
            remarks = remarks,
            completedAt = if (status == RequestStatus.COMPLETED) System.currentTimeMillis() else null
        )
        notifyResident(request.residentId, "Repair #$requestId is now ${status.label}")
    }

    private fun notifyResident(residentId: Int, message: String) {
        ResidentData.getResident(residentId)?.let { NotificationData.notify(it.userId, "Maintenance", message) }
    }
}
