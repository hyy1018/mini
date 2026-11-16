// #member1
// Module 1 data: users, households and residents, plus login / registration / approval logic.
// In-memory for now (resets when the app restarts) -- these functions get swapped for
// database calls once Room / Supabase is added, so screens don't need to change much.
package com.example.mini.data

import androidx.compose.runtime.mutableStateListOf
import com.example.mini.model.Household
import com.example.mini.model.RegistrationStatus
import com.example.mini.model.Resident
import com.example.mini.model.User
import com.example.mini.model.UserRole

data class LoginResult(val user: User?, val error: String?)

object ResidentData {
    val blocks = listOf("Block A", "Block B", "Block C")
    val residentTypes = listOf("Owner", "Tenant", "Family Member")

    val users = mutableStateListOf<User>()
    val households = mutableStateListOf<Household>()
    val residents = mutableStateListOf<Resident>()

    init {
        // Demo accounts, all with password abc123:
        // staff1 (Staff), guard1 (Security), resident1 (approved), resident2 (pending approval)
        users.addAll(
            listOf(
                User(1, "staff1", "Siti Aminah", "siti@rcms.com", "0123456789", "abc123", UserRole.STAFF),
                User(2, "guard1", "Ahmad Razak", "ahmad@rcms.com", "0129876543", "abc123", UserRole.SECURITY),
                User(3, "resident1", "Ali Tan", "ali@example.com", "0112345678", "abc123", UserRole.RESIDENT),
                User(4, "resident2", "Mei Ling", "meiling@example.com", "0198765432", "abc123", UserRole.RESIDENT)
            )
        )
        households.add(Household(1, "A-10-05", "Block A", "Jalan Melati 1, Taman Melati"))
        residents.addAll(
            listOf(
                Resident(1, 3, 1, "900101145678", "Owner", RegistrationStatus.APPROVED, verifiedBy = 1, verifiedAt = System.currentTimeMillis()),
                Resident(2, 4, 1, "950505105432", "Family Member", RegistrationStatus.PENDING)
            )
        )
    }

    // ---------- Lookups ----------
    fun getUser(userId: Int): User? = users.find { it.userId == userId }
    fun getResident(residentId: Int): Resident? = residents.find { it.residentId == residentId }
    fun residentOfUser(userId: Int): Resident? = residents.find { it.userId == userId }
    fun getHousehold(householdId: Int): Household? = households.find { it.householdId == householdId }
    fun householdMembers(householdId: Int): List<Resident> = residents.filter { it.householdId == householdId }
    fun pendingRegistrations(): List<Resident> = residents.filter { it.registrationStatus == RegistrationStatus.PENDING }
    fun approvedResidents(): List<Resident> = residents.filter { it.registrationStatus == RegistrationStatus.APPROVED }
    fun isUsernameTaken(username: String): Boolean = users.any { it.username.equals(username, ignoreCase = true) }

    // "Block A · A-10-05", used on cards and visitor records
    fun unitLabel(residentId: Int): String {
        val household = getResident(residentId)?.let { getHousehold(it.householdId) } ?: return "-"
        return "${household.blockName} · ${household.unitNo}"
    }

    // ---------- Login (role-based access) ----------
    fun checkLogin(username: String, password: String, role: UserRole): LoginResult {
        val user = users.find { it.username.equals(username, ignoreCase = true) && it.password == password }
        // wrong role gets the same message as a wrong password, so roles can't be guessed
        if (user == null || user.role != role) return LoginResult(null, "Invalid username or password")
        if (user.accountStatus != "Active") return LoginResult(null, "This account has been deactivated")
        if (role == UserRole.RESIDENT) {
            when (residentOfUser(user.userId)?.registrationStatus) {
                RegistrationStatus.PENDING ->
                    return LoginResult(null, "Your registration is still waiting for management approval")
                RegistrationStatus.REJECTED ->
                    return LoginResult(null, "Your registration was rejected. Please contact management")
                null -> return LoginResult(null, "Resident record not found")
                RegistrationStatus.APPROVED -> {}
            }
        }
        return LoginResult(user, null)
    }

    // ---------- Create: resident registration (Flowchart 1) ----------
    fun register(
        name: String,
        username: String,
        email: String,
        phoneNo: String,
        icNo: String,
        password: String,
        residentType: String,
        blockName: String,
        unitNo: String,
        address: String
    ) {
        val newUserId = (users.maxOfOrNull { it.userId } ?: 0) + 1
        users.add(User(newUserId, username, name, email, phoneNo, password, UserRole.RESIDENT))

        // residents of the same unit share one household record
        val existing = households.find {
            it.blockName == blockName && it.unitNo.equals(unitNo, ignoreCase = true)
        }
        val householdId = existing?.householdId ?: run {
            val newId = (households.maxOfOrNull { it.householdId } ?: 0) + 1
            households.add(Household(newId, unitNo.uppercase(), blockName, address))
            newId
        }

        val newResidentId = (residents.maxOfOrNull { it.residentId } ?: 0) + 1
        residents.add(Resident(newResidentId, newUserId, householdId, icNo, residentType))
    }

    // ---------- Verify: management approves or rejects ----------
    fun setRegistrationStatus(residentId: Int, status: RegistrationStatus, staffUserId: Int) {
        val index = residents.indexOfFirst { it.residentId == residentId }
        if (index != -1) {
            residents[index] = residents[index].copy(
                registrationStatus = status,
                verifiedBy = staffUserId,
                verifiedAt = System.currentTimeMillis()
            )
        }
    }

    // ---------- Update: resident or staff edits details (Flowchart 3) ----------
    fun updateResident(
        residentId: Int,
        name: String,
        email: String,
        phoneNo: String,
        icNo: String,
        residentType: String,
        address: String
    ) {
        val resident = getResident(residentId) ?: return
        val userIndex = users.indexOfFirst { it.userId == resident.userId }
        if (userIndex != -1) {
            users[userIndex] = users[userIndex].copy(name = name, email = email, phoneNo = phoneNo)
        }
        val residentIndex = residents.indexOfFirst { it.residentId == residentId }
        residents[residentIndex] = resident.copy(icNo = icNo, residentType = residentType)
        val householdIndex = households.indexOfFirst { it.householdId == resident.householdId }
        if (householdIndex != -1) {
            households[householdIndex] = households[householdIndex].copy(address = address)
        }
    }

    // ---------- Delete: staff removes a resident (Flowchart 4) ----------
    // Returns an error message if related records block the delete, or null on success.
    fun deleteResident(residentId: Int): String? {
        val resident = getResident(residentId) ?: return "Resident not found"
        if (VisitorData.hasVisitorInside(residentId)) {
            return "This resident has a visitor inside right now. Record the visitor's exit first."
        }
        if (CommunityData.activeEmergencies().any { it.residentId == residentId }) {
            return "This resident has an emergency alert still open. Resolve it first."
        }
        // remove the resident's records in every module so nothing points at a deleted user
        val userId = resident.userId
        VisitorData.deleteByResident(residentId)
        FacilityData.bookings.removeAll { it.residentId == residentId }
        FacilityData.maintenanceRequests.removeAll { it.residentId == residentId }
        ComplaintData.complaints.removeAll { it.residentId == residentId }
        ComplaintData.feedbacks.removeAll { it.residentId == residentId }
        CommunityData.emergencies.removeAll { it.residentId == residentId }
        CommunityData.posts.filter { it.userId == userId }.forEach { CommunityData.deletePost(it.postId) }
        CommunityData.comments.removeAll { it.userId == userId }
        CommunityData.likes.removeAll { it.userId == userId }
        CommunityData.messages.removeAll { it.senderUserId == userId || it.receiverUserId == userId }
        EngagementData.eventRegistrations.removeAll { it.userId == userId }
        EngagementData.announcementReads.removeAll { it.userId == userId }
        NotificationData.notifications.removeAll { it.userId == userId }
        residents.removeAll { it.residentId == residentId }
        users.removeAll { it.userId == resident.userId }
        return null
    }
}
