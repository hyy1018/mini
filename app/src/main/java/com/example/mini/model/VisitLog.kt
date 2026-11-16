// #member1
// One entry/exit record created when security scans a pass (report 3NF: VisitLog).
// exitTime stays null while the visitor is still inside.
package com.example.mini.model

data class VisitLog(
    val logId: Int,
    val visitorId: Int,
    val scannedBy: Int,
    val entryTime: Long,
    val exitTime: Long? = null
)
