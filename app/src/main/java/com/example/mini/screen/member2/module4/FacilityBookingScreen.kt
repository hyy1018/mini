// #member2
// Smart Facility Booking (unique feature, flowchart f): pick a date, see every time slot's real-time
// availability, and the booking rules (conflicts, daily limit, advance booking period) are checked
// automatically. bookingId != -1 means moving an existing booking to a new slot (flowchart c).
package com.example.mini.screen.member2.module4

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.FacilityData
import com.example.mini.data.UserSession
import com.example.mini.data.addDays
import com.example.mini.data.atHour
import com.example.mini.data.formatDate
import com.example.mini.data.formatHour
import com.example.mini.data.formatShortDate
import com.example.mini.data.startOfDay
import com.example.mini.screen.BackTopBar

// what a slot looks like to the resident on the grid
private enum class SlotState { AVAILABLE, BOOKED, YOURS, PAST }

@Composable
fun FacilityBookingScreen(facilityId: Int, bookingId: Int, navController: NavHostController) {
    val facility = FacilityData.getFacility(facilityId)
    val movingBooking = if (bookingId != -1) FacilityData.getBooking(bookingId) else null
    val residentId = UserSession.currentResident?.residentId ?: -1
    val today = startOfDay(System.currentTimeMillis())

    var selectedDate by remember { mutableStateOf(movingBooking?.bookingDate ?: today) }
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { BackTopBar(if (movingBooking != null) "Change Booking" else "Book Facility", navController) }
    ) { innerPadding ->
        if (facility == null) return@Scaffold
        val dayBookings = FacilityData.confirmedOn(facilityId, selectedDate).filter { it.bookingId != bookingId }
        val myCountToday = dayBookings.count { it.residentId == residentId }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Facility + rules ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(facility.facilityName, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${facility.location} · capacity ${facility.capacity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Booking rules", style = MaterialTheme.typography.labelLarge)
                    Text("• ${facility.slotHours}-hour slots, ${formatHour(facility.openHour)} to ${formatHour(facility.closeHour)}", style = MaterialTheme.typography.bodySmall)
                    Text("• Up to ${facility.maxBookingsPerDay} booking(s) per resident per day", style = MaterialTheme.typography.bodySmall)
                    Text("• Book at most ${facility.advanceBookingDays} days in advance", style = MaterialTheme.typography.bodySmall)
                    Text("• Cancel or change any time before the slot starts", style = MaterialTheme.typography.bodySmall)
                }
            }

            if (facility.facilityStatus != "Available") {
                Text(
                    "${facility.facilityName} is under maintenance and can't be booked right now.",
                    color = MaterialTheme.colorScheme.error
                )
                return@Column
            }

            // --- Date chips: today up to the advance booking limit ---
            Text("Choose a date", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (0..facility.advanceBookingDays).forEach { offset ->
                    val day = addDays(today, offset)
                    val selected = day == selectedDate
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable {
                            selectedDate = day
                            selectedHour = null
                            errorMessage = null
                        }
                    ) {
                        Text(
                            if (offset == 0) "Today\n" + formatShortDate(day).substringAfter("\n") else formatShortDate(day),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // --- Time slot grid (real-time availability) ---
            Text("Choose a time slot", style = MaterialTheme.typography.titleMedium)
            SlotLegend()
            FacilityData.slotStartHours(facility).chunked(3).forEach { rowHours ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowHours.forEach { hour ->
                        val booking = dayBookings.find { hour < it.endHour && it.startHour < hour + facility.slotHours }
                        val state = when {
                            atHour(selectedDate, hour) <= System.currentTimeMillis() -> SlotState.PAST
                            booking?.residentId == residentId -> SlotState.YOURS
                            booking != null -> SlotState.BOOKED
                            else -> SlotState.AVAILABLE
                        }
                        SlotTile(
                            label = "${formatHour(hour)}\n${formatHour(hour + facility.slotHours)}",
                            state = state,
                            selected = selectedHour == hour,
                            onClick = {
                                selectedHour = hour
                                errorMessage = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // keep the last row's tiles the same width
                    repeat(3 - rowHours.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            Text(
                "Your bookings for this facility on ${formatDate(selectedDate)}: $myCountToday / ${facility.maxBookingsPerDay}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            val hour = selectedHour
            Button(
                onClick = {
                    // ignore taps while the confirmation dialog is up, so a slot is never booked twice
                    if (hour == null || successMessage != null) return@Button
                    // the data layer re-checks every rule, so a stale screen can't double-book
                    errorMessage = if (movingBooking != null) {
                        FacilityData.reschedule(movingBooking.bookingId, selectedDate, hour)
                    } else {
                        FacilityData.book(facility, residentId, selectedDate, hour)
                    }
                    if (errorMessage == null) {
                        successMessage = "${facility.facilityName}\n${formatDate(selectedDate)}, ${formatHour(hour)}-${formatHour(hour + facility.slotHours)}"
                    }
                },
                enabled = hour != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        hour == null -> "Select a time slot"
                        movingBooking != null -> "Move booking to ${formatHour(hour)}"
                        else -> "Book ${formatHour(hour)}-${formatHour(hour + facility.slotHours)}"
                    }
                )
            }
        }
    }

    successMessage?.let { message ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text(if (movingBooking != null) "Booking Updated" else "Booking Confirmed") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    successMessage = null
                    navController.safePopBackStack()
                }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun SlotTile(label: String, state: SlotState, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (container, content) = slotColors(state)
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else container,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else content,
        border = if (state == SlotState.AVAILABLE && !selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
        modifier = modifier.then(
            // only free slots can be picked
            if (state == SlotState.AVAILABLE) Modifier.clickable(onClick = onClick) else Modifier
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
            Text(
                when (state) {
                    SlotState.AVAILABLE -> if (selected) "Selected" else "Free"
                    SlotState.BOOKED -> "Booked"
                    SlotState.YOURS -> "Yours"
                    SlotState.PAST -> "Past"
                },
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun slotColors(state: SlotState): Pair<Color, Color> = when (state) {
    SlotState.AVAILABLE -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
    SlotState.BOOKED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    SlotState.YOURS -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    SlotState.PAST -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun SlotLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf(
            SlotState.AVAILABLE to "Free",
            SlotState.BOOKED to "Booked",
            SlotState.YOURS to "Yours",
            SlotState.PAST to "Past"
        ).forEach { (state, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = slotColors(state).first,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.size(12.dp)
                ) {}
                Spacer(Modifier.width(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
