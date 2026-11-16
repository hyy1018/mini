// #member2
// Module 4 hub with three tabs: Facilities, Bookings and Repairs.
// Residents book facilities and submit repair requests; staff manage facilities, bookings and requests.
package com.example.mini.screen.member2.module4

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.data.FacilityData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDate
import com.example.mini.data.formatHour
import com.example.mini.model.Booking
import com.example.mini.model.BookingStatus
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmptyState
import com.example.mini.screen.RequestStatusBadge
import com.example.mini.screen.StatusBadge

@Composable
fun FacilityMaintenanceScreen(initialTab: Int, navController: NavHostController) {
    val isStaff = UserSession.isStaff
    val tabs = listOf("Facilities", if (isStaff) "Bookings" else "My Bookings", "Repairs")
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab.coerceIn(0, 2)) }

    Scaffold(
        topBar = { BackTopBar("Facility & Maintenance", navController) },
        floatingActionButton = {
            // staff adds facilities; residents submit repair requests
            if (isStaff && selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { navController.safeNavigate("facility_form") },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Add Facility") }
                )
            } else if (!isStaff && selectedTab == 2) {
                ExtendedFloatingActionButton(
                    onClick = { navController.safeNavigate("maintenance_form") },
                    icon = { Icon(Icons.Filled.Build, contentDescription = null) },
                    text = { Text("Report Repair") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
            when (selectedTab) {
                0 -> FacilitiesTab(isStaff, navController)
                1 -> BookingsTab(isStaff, navController)
                else -> RepairsTab(isStaff, navController)
            }
        }
    }
}

// ---------- Facilities ----------

@Composable
private fun FacilitiesTab(isStaff: Boolean, navController: NavHostController) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(FacilityData.facilities, key = { it.facilityId }) { facility ->
            val available = facility.facilityStatus == "Available"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // staff edit the facility; residents go to Smart Facility Booking
                        if (isStaff) navController.safeNavigate("facility_form?id=${facility.facilityId}")
                        else navController.safeNavigate("facility_booking/${facility.facilityId}")
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(facility.facilityName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        if (available) {
                            StatusBadge("Available", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                        } else {
                            StatusBadge("Maintenance", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    Text(facility.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${facility.location} · ${formatHour(facility.openHour)}-${formatHour(facility.closeHour)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${facility.slotHours}h slots · max ${facility.maxBookingsPerDay}/day · book up to ${facility.advanceBookingDays} days ahead",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ---------- Bookings ----------

@Composable
private fun BookingsTab(isStaff: Boolean, navController: NavHostController) {
    val residentId = UserSession.currentResident?.residentId
    val bookings = if (isStaff) FacilityData.allBookings() else residentId?.let { FacilityData.bookingsOfResident(it) } ?: emptyList()
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (bookings.isEmpty()) {
        EmptyState(if (isStaff) "No bookings yet" else "No bookings yet.\nOpen the Facilities tab to book one.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        errorMessage?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }
        items(bookings, key = { it.bookingId }) { booking ->
            val facility = FacilityData.getFacility(booking.facilityId)
            val changeable = booking.bookingStatus == BookingStatus.CONFIRMED && !FacilityData.hasStarted(booking)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(facility?.facilityName ?: "-", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        val (label, isGood) = when {
                            booking.bookingStatus == BookingStatus.CANCELLED -> "Cancelled" to false
                            FacilityData.hasStarted(booking) -> "Completed" to true
                            else -> "Confirmed" to true
                        }
                        StatusBadge(
                            label,
                            if (isGood) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            if (isGood) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "${formatDate(booking.bookingDate)} · ${formatHour(booking.startHour)}-${formatHour(booking.endHour)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (isStaff) {
                        val user = ResidentData.getResident(booking.residentId)?.let { ResidentData.getUser(it.userId) }
                        Text(
                            "${user?.name ?: "-"} (${ResidentData.unitLabel(booking.residentId)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (changeable) {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { bookingToCancel = booking }) { Text("Cancel") }
                            if (!isStaff) {
                                OutlinedButton(onClick = {
                                    navController.safeNavigate("facility_booking/${booking.facilityId}?bookingId=${booking.bookingId}")
                                }) { Text("Change Slot") }
                            }
                        }
                    }
                }
            }
        }
    }

    bookingToCancel?.let { booking ->
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            title = { Text("Cancel booking?") },
            text = { Text("The slot will be released for other residents.") },
            confirmButton = {
                TextButton(onClick = {
                    errorMessage = FacilityData.cancelBooking(booking.bookingId)
                    bookingToCancel = null
                }) { Text("Cancel Booking") }
            },
            dismissButton = { TextButton(onClick = { bookingToCancel = null }) { Text("Keep") } }
        )
    }
}

// ---------- Repairs ----------

@Composable
private fun RepairsTab(isStaff: Boolean, navController: NavHostController) {
    val residentId = UserSession.currentResident?.residentId
    val requests = if (isStaff) FacilityData.allRequests() else residentId?.let { FacilityData.requestsOfResident(it) } ?: emptyList()

    if (requests.isEmpty()) {
        EmptyState(if (isStaff) "No maintenance requests" else "No repair requests yet.\nTap Report Repair to submit one.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(requests, key = { it.requestId }) { request ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.safeNavigate("maintenance/${request.requestId}") }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "#${request.requestId} ${request.category}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        RequestStatusBadge(request.requestStatus)
                    }
                    Text(request.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(request.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                    Text(
                        "Priority ${request.priority} · ${formatDate(request.submittedAt)}" +
                            if (isStaff) " · ${ResidentData.unitLabel(request.residentId)}" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
