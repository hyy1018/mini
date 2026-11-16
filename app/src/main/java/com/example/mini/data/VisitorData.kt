// #member1
// Module 2 data: visitor passes and visit logs, plus the Smart Visitor Pass rules
// (time-limited, single-use, expired / used codes rejected). In-memory for now.
package com.example.mini.data

import androidx.compose.runtime.mutableStateListOf
import com.example.mini.model.PassStatus
import com.example.mini.model.Visitor
import com.example.mini.model.VisitLog
import java.util.UUID

data class VerifyResult(val success: Boolean, val message: String, val visitor: Visitor?)

object VisitorData {
    val purposes = listOf("Family visit", "Friend visit", "Delivery", "Contractor / Repair", "Other")
    val durationHours = listOf(2, 4, 8, 12, 24)

    val visitors = mutableStateListOf<Visitor>()
    val visitLogs = mutableStateListOf<VisitLog>()

    init {
        // one sample pass for resident1 (Ali Tan), valid from now for 8 hours
        val now = System.currentTimeMillis()
        visitors.add(
            Visitor(
                visitorId = 1,
                residentId = 1,
                visitorName = "John Lee",
                phoneNo = "0112345678",
                vehiclePlate = "ABC1234",
                purpose = "Family visit",
                visitDate = now,
                qrCode = "RCMS-DEMO0001",
                validFrom = now,
                validUntil = now + 8 * 60 * 60 * 1000L
            )
        )
    }

    // ---------- Lookups ----------
    fun getById(visitorId: Int): Visitor? = visitors.find { it.visitorId == visitorId }
    fun byResident(residentId: Int): List<Visitor> =
        visitors.filter { it.residentId == residentId }.sortedByDescending { it.validFrom }
    fun logFor(visitorId: Int): VisitLog? = visitLogs.find { it.visitorId == visitorId }
    fun activeVisits(): List<VisitLog> = visitLogs.filter { it.exitTime == null }.sortedByDescending { it.entryTime }
    fun hasVisitorInside(residentId: Int): Boolean =
        activeVisits().any { log -> getById(log.visitorId)?.residentId == residentId }

    // a pass can only be edited or cancelled before the visitor has entered
    fun canChange(visitor: Visitor): Boolean = visitor.passStatus == PassStatus.VALID && logFor(visitor.visitorId) == null

    // marks passes whose time window has ended as Expired
    fun refreshExpired() {
        val now = System.currentTimeMillis()
        visitors.forEachIndexed { index, visitor ->
            if (visitor.passStatus == PassStatus.VALID && now > visitor.validUntil) {
                visitors[index] = visitor.copy(passStatus = PassStatus.EXPIRED)
            }
        }
    }

    // ---------- Create: pre-register a visitor and generate the QR pass (Flowchart 5) ----------
    fun register(
        residentId: Int,
        visitorName: String,
        phoneNo: String,
        vehiclePlate: String,
        purpose: String,
        validFrom: Long,
        validUntil: Long
    ): Visitor {
        val newId = (visitors.maxOfOrNull { it.visitorId } ?: 0) + 1
        val visitor = Visitor(
            visitorId = newId,
            residentId = residentId,
            visitorName = visitorName,
            phoneNo = phoneNo,
            vehiclePlate = vehiclePlate.uppercase(),
            purpose = purpose,
            visitDate = validFrom,
            qrCode = newQrCode(),
            validFrom = validFrom,
            validUntil = validUntil
        )
        visitors.add(visitor)
        return visitor
    }

    // random, hard-to-guess code so a pass can't be forged from its id
    private fun newQrCode(): String = "RCMS-" + UUID.randomUUID().toString().replace("-", "").take(8).uppercase()

    // ---------- Update: edit an unused pass (Flowchart 7) ----------
    fun update(
        visitorId: Int,
        visitorName: String,
        phoneNo: String,
        vehiclePlate: String,
        purpose: String,
        validFrom: Long,
        validUntil: Long
    ) {
        val index = visitors.indexOfFirst { it.visitorId == visitorId }
        if (index != -1) {
            visitors[index] = visitors[index].copy(
                visitorName = visitorName,
                phoneNo = phoneNo,
                vehiclePlate = vehiclePlate.uppercase(),
                purpose = purpose,
                visitDate = validFrom,
                validFrom = validFrom,
                validUntil = validUntil
            )
        }
    }

    // ---------- Delete: cancel a pass before entry (Flowchart 8) ----------
    // kept as a Cancelled record instead of removed, so the history stays complete
    fun cancel(visitorId: Int) {
        val index = visitors.indexOfFirst { it.visitorId == visitorId }
        if (index != -1) {
            visitors[index] = visitors[index].copy(passStatus = PassStatus.CANCELLED)
        }
    }

    fun deleteByResident(residentId: Int) {
        val ids = visitors.filter { it.residentId == residentId }.map { it.visitorId }
        visitLogs.removeAll { it.visitorId in ids }
        visitors.removeAll { it.residentId == residentId }
    }

    // ---------- Unique feature: verify QR and record entry (Flowchart 9) ----------
    fun verifyEntry(code: String, guardUserId: Int): VerifyResult {
        val index = visitors.indexOfFirst { it.qrCode.equals(code.trim(), ignoreCase = true) }
        if (index == -1) return VerifyResult(false, "Invalid QR code -- no visitor pass found", null)

        val visitor = visitors[index]
        val now = System.currentTimeMillis()
        when {
            visitor.passStatus == PassStatus.CANCELLED ->
                return VerifyResult(false, "This pass was cancelled by the resident", visitor)
            visitor.passStatus == PassStatus.USED ->
                return VerifyResult(false, "This QR code has already been used", visitor)
            visitor.passStatus == PassStatus.EXPIRED || now > visitor.validUntil -> {
                visitors[index] = visitor.copy(passStatus = PassStatus.EXPIRED)
                return VerifyResult(false, "This QR code has expired", visitor)
            }
            now < visitor.validFrom ->
                return VerifyResult(false, "This pass is not valid yet (starts ${formatDateTime(visitor.validFrom)})", visitor)
        }

        // valid: record entry time and mark the code as used so it can't be scanned again
        val newLogId = (visitLogs.maxOfOrNull { it.logId } ?: 0) + 1
        visitLogs.add(VisitLog(newLogId, visitor.visitorId, guardUserId, entryTime = now))
        val used = visitor.copy(passStatus = PassStatus.USED)
        visitors[index] = used
        // Flowchart 9 "Notify Resident"
        ResidentData.getResident(visitor.residentId)?.let {
            NotificationData.notify(it.userId, "Visitor", "${visitor.visitorName} has arrived at the guardhouse")
        }
        return VerifyResult(true, "Access granted", used)
    }

    // ---------- Record exit against the active visit ----------
    fun recordExit(logId: Int) {
        val index = visitLogs.indexOfFirst { it.logId == logId }
        if (index != -1 && visitLogs[index].exitTime == null) {
            visitLogs[index] = visitLogs[index].copy(exitTime = System.currentTimeMillis())
        }
    }
}
