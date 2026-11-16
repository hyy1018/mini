// #member2
// Status shared by maintenance requests (Module 4) and complaints (Module 5),
// as listed in the report: Pending, In Progress, Completed.
package com.example.mini.model

enum class RequestStatus(val label: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed")
}
