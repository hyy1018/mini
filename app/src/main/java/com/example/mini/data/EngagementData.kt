// #member2
// Module 3 data: announcements (+ read confirmations), community events (+ registrations)
// and searchable community information. In-memory for now.
package com.example.mini.data

import androidx.compose.runtime.mutableStateListOf
import com.example.mini.model.Announcement
import com.example.mini.model.AnnouncementRead
import com.example.mini.model.CommunityInfo
import com.example.mini.model.Event
import com.example.mini.model.EventRegistration

object EngagementData {
    val announcementCategories = listOf("General", "Maintenance", "Security", "Event", "Facility")
    val infoTypes = listOf("Community Rules", "FAQ", "Facility Guidelines", "Management Contact")

    val announcements = mutableStateListOf<Announcement>()
    val announcementReads = mutableStateListOf<AnnouncementRead>()
    val events = mutableStateListOf<Event>()
    val eventRegistrations = mutableStateListOf<EventRegistration>()
    val infos = mutableStateListOf<CommunityInfo>()

    init {
        val now = System.currentTimeMillis()
        announcements.addAll(
            listOf(
                Announcement(
                    1, 1, "Water Supply Interruption",
                    "Water supply will be cut from 14:00 to 18:00 this Saturday for tank cleaning. Please store water in advance.",
                    "Maintenance", isImportant = true, postedAt = now - DAY_MILLIS, updatedAt = now - DAY_MILLIS
                ),
                Announcement(
                    2, 1, "New Visitor Pass System",
                    "Visitors must now show a QR pass at the guardhouse. Create one under Visitors in the app.",
                    "Security", isImportant = false, postedAt = now - 2 * DAY_MILLIS, updatedAt = now - 2 * DAY_MILLIS
                )
            )
        )
        events.add(
            Event(
                1, 1, "Community Sports Day",
                "Annual sports day with badminton, futsal and games for kids. Free drinks provided.",
                "Multipurpose Hall", addDays(now, 7), 9, 13, 50
            )
        )
        // content is stored one point per line; the Info tab shows each line as a bullet
        infos.addAll(
            listOf(
                CommunityInfo(1, 1, "Community Rules", "Quiet Hours", "10 PM - 7 AM every day\nNo loud music or renovation work\nKeep corridor conversations low"),
                CommunityInfo(2, 1, "Community Rules", "Parking", "Park only in your allocated bay\nVisitor cars use visitor lots at Level 1\nNo parking at fire lanes"),
                CommunityInfo(3, 1, "Facility Guidelines", "Swimming Pool", "Open 7 AM - 10 PM\nProper swimwear only\nChildren under 12 must be with an adult\nNo food or glass bottles"),
                CommunityInfo(4, 1, "FAQ", "How do I register a visitor?", "Open Visitors > Invite Visitor\nFill in visitor details and visit time\nSend the QR pass to your visitor\nShow the QR at the guardhouse"),
                CommunityInfo(5, 1, "FAQ", "How do I book a facility?", "Open Booking and pick a facility\nChoose a date and a free time slot\nTap Book to confirm"),
                CommunityInfo(6, 1, "Management Contact", "Management Office", "Ground floor, Block A\nMon - Fri, 9 AM - 6 PM\nTel: 03-1234 5678")
            )
        )
    }

    // ---------- Announcements ----------
    fun getAnnouncement(id: Int): Announcement? = announcements.find { it.announcementId == id }
    fun announcementsNewestFirst(): List<Announcement> = announcements.sortedByDescending { it.postedAt }
    fun latestAnnouncement(): Announcement? = announcements.maxByOrNull { it.postedAt }

    fun addAnnouncement(staffId: Int, title: String, content: String, category: String, isImportant: Boolean) {
        val newId = (announcements.maxOfOrNull { it.announcementId } ?: 0) + 1
        announcements.add(Announcement(newId, staffId, title, content, category, isImportant))
        // tell every approved resident there's something new
        ResidentData.approvedResidents().forEach { resident ->
            NotificationData.notify(
                resident.userId, "Announcement",
                (if (isImportant) "Important: " else "New announcement: ") + title
            )
        }
    }

    fun updateAnnouncement(id: Int, title: String, content: String, category: String, isImportant: Boolean) {
        val index = announcements.indexOfFirst { it.announcementId == id }
        if (index != -1) {
            announcements[index] = announcements[index].copy(
                title = title, content = content, category = category,
                isImportant = isImportant, updatedAt = System.currentTimeMillis()
            )
        }
    }

    // read confirmations go too, so nothing points at a deleted announcement
    fun deleteAnnouncement(id: Int) {
        announcementReads.removeAll { it.announcementId == id }
        announcements.removeAll { it.announcementId == id }
    }

    fun hasRead(announcementId: Int, userId: Int): Boolean =
        announcementReads.any { it.announcementId == announcementId && it.userId == userId }

    fun confirmRead(announcementId: Int, userId: Int) {
        if (!hasRead(announcementId, userId)) announcementReads.add(AnnouncementRead(announcementId, userId))
    }

    fun readCount(announcementId: Int): Int = announcementReads.count { it.announcementId == announcementId }

    // ---------- Events ----------
    fun getEvent(id: Int): Event? = events.find { it.eventId == id }
    fun eventsByDate(): List<Event> = events.sortedBy { it.eventDate * 100 + it.startHour }
    fun isPast(event: Event): Boolean = atHour(event.eventDate, event.endHour) < System.currentTimeMillis()

    // Upcoming / Cancelled are stored; an upcoming event whose end time has passed shows as Completed
    fun displayStatus(event: Event): String = if (event.eventStatus == "Upcoming" && isPast(event)) "Completed" else event.eventStatus

    fun addEvent(
        staffId: Int, title: String, description: String, venue: String,
        eventDate: Long, startHour: Int, endHour: Int, maxParticipants: Int
    ) {
        val newId = (events.maxOfOrNull { it.eventId } ?: 0) + 1
        events.add(Event(newId, staffId, title, description, venue, eventDate, startHour, endHour, maxParticipants))
        ResidentData.approvedResidents().forEach { resident ->
            NotificationData.notify(resident.userId, "Event", "New event: $title on ${formatDate(eventDate)}")
        }
    }

    fun updateEvent(
        id: Int, title: String, description: String, venue: String,
        eventDate: Long, startHour: Int, endHour: Int, maxParticipants: Int
    ) {
        val index = events.indexOfFirst { it.eventId == id }
        if (index != -1) {
            events[index] = events[index].copy(
                title = title, description = description, venue = venue, eventDate = eventDate,
                startHour = startHour, endHour = endHour, maxParticipants = maxParticipants
            )
        }
    }

    fun cancelEvent(id: Int) {
        val event = getEvent(id) ?: return
        events[events.indexOf(event)] = event.copy(eventStatus = "Cancelled")
        // everyone who signed up gets told
        eventRegistrations.filter { it.eventId == id }.forEach {
            NotificationData.notify(it.userId, "Event", "Event cancelled: ${event.title}")
        }
    }

    fun deleteEvent(id: Int) {
        eventRegistrations.removeAll { it.eventId == id }
        events.removeAll { it.eventId == id }
    }

    fun registrationCount(eventId: Int): Int = eventRegistrations.count { it.eventId == eventId }
    fun isRegistered(eventId: Int, userId: Int): Boolean = eventRegistrations.any { it.eventId == eventId && it.userId == userId }
    fun participants(eventId: Int): List<EventRegistration> = eventRegistrations.filter { it.eventId == eventId }

    // returns an error message, or null when registered
    fun registerForEvent(eventId: Int, userId: Int): String? {
        val event = getEvent(eventId) ?: return "Event not found"
        return when {
            event.eventStatus == "Cancelled" -> "This event has been cancelled"
            isPast(event) -> "This event has already ended"
            isRegistered(eventId, userId) -> "You are already registered"
            registrationCount(eventId) >= event.maxParticipants -> "Sorry, this event is full"
            else -> {
                eventRegistrations.add(EventRegistration(eventId, userId))
                null
            }
        }
    }

    fun unregisterFromEvent(eventId: Int, userId: Int) {
        eventRegistrations.removeAll { it.eventId == eventId && it.userId == userId }
    }

    // ---------- Community information ----------
    fun getInfo(id: Int): CommunityInfo? = infos.find { it.infoId == id }

    // searches title and content, optionally within one type
    fun searchInfo(query: String, type: String?): List<CommunityInfo> = infos.filter { info ->
        (type == null || info.infoType == type) &&
            (query.isBlank() || info.title.contains(query, ignoreCase = true) || info.content.contains(query, ignoreCase = true))
    }

    fun addInfo(staffId: Int, infoType: String, title: String, content: String) {
        val newId = (infos.maxOfOrNull { it.infoId } ?: 0) + 1
        infos.add(CommunityInfo(newId, staffId, infoType, title, content))
    }

    fun updateInfo(id: Int, infoType: String, title: String, content: String) {
        val index = infos.indexOfFirst { it.infoId == id }
        if (index != -1) infos[index] = infos[index].copy(infoType = infoType, title = title, content = content)
    }

    fun deleteInfo(id: Int) {
        infos.removeAll { it.infoId == id }
    }
}
