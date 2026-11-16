// #member1
// A login account (report 3NF: User). username is the login ID, as in the class diagram.
package com.example.mini.model

enum class UserRole(val label: String) {
    RESIDENT("Resident"),
    STAFF("Staff"),
    SECURITY("Security")
}

data class User(
    val userId: Int,
    val username: String,
    val name: String,
    val email: String,
    val phoneNo: String,
    val password: String,
    val role: UserRole,
    val accountStatus: String = "Active",
    val createdAt: Long = System.currentTimeMillis()
)
