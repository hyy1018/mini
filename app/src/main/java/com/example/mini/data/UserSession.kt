// #member1
// Who's currently logged in. In-memory only, so a fresh app launch always lands on Login.
package com.example.mini.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.mini.model.Resident
import com.example.mini.model.User
import com.example.mini.model.UserRole

object UserSession {
    var currentUserId by mutableStateOf<Int?>(null)
        private set

    // looked up live from ResidentData, so profile edits show up straight away
    val currentUser: User? get() = currentUserId?.let { ResidentData.getUser(it) }
    val currentResident: Resident? get() = currentUserId?.let { ResidentData.residentOfUser(it) }

    // screens shared by several roles use these to show / hide role-only actions
    val isStaff: Boolean get() = currentUser?.role == UserRole.STAFF
    val isResident: Boolean get() = currentUser?.role == UserRole.RESIDENT

    fun login(user: User) {
        currentUserId = user.userId
    }

    fun logout() {
        currentUserId = null
    }
}
