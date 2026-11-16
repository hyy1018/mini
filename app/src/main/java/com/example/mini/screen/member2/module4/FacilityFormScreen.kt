// #member2
// Staff adds (id = -1) or edits a facility and its booking rules.
package com.example.mini.screen.member2.module4

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.isCurrentScreenResumed
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.FacilityData
import com.example.mini.data.formatHour
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.DropdownField

@Composable
fun FacilityFormScreen(facilityId: Int, navController: NavHostController) {
    val existing = FacilityData.getFacility(facilityId)

    var name by remember { mutableStateOf(existing?.facilityName ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var location by remember { mutableStateOf(existing?.location ?: "") }
    var capacityText by remember { mutableStateOf(existing?.capacity?.toString() ?: "10") }
    var openHour by remember { mutableStateOf(existing?.openHour ?: 8) }
    var closeHour by remember { mutableStateOf(existing?.closeHour ?: 22) }
    var slotHours by remember { mutableStateOf(existing?.slotHours ?: 1) }
    var maxPerDay by remember { mutableStateOf(existing?.maxBookingsPerDay ?: 1) }
    var advanceDays by remember { mutableStateOf(existing?.advanceBookingDays ?: 7) }
    var status by remember { mutableStateOf(existing?.facilityStatus ?: "Available") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { BackTopBar(if (existing != null) "Edit Facility" else "New Facility", navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it.take(100) },
                label = { Text("Facility Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = location, onValueChange = { location = it.take(100) },
                label = { Text("Location") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description, onValueChange = { description = it.take(1000) },
                label = { Text("Description") }, minLines = 2, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = capacityText, onValueChange = { capacityText = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Capacity (people)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Opening Hours", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DropdownField("Opens", (0..23).toList(), openHour, { openHour = it }, Modifier.weight(1f)) { formatHour(it) }
                DropdownField("Closes", (1..24).toList(), closeHour, { closeHour = it }, Modifier.weight(1f)) { formatHour(it) }
            }

            Text("Booking Rules", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DropdownField("Slot length", listOf(1, 2, 3, 4), slotHours, { slotHours = it }, Modifier.weight(1f)) { "$it hour(s)" }
                DropdownField("Max per day", listOf(1, 2, 3), maxPerDay, { maxPerDay = it }, Modifier.weight(1f)) { "$it booking(s)" }
            }
            DropdownField(
                label = "Advance booking period",
                options = listOf(1, 3, 7, 14, 30),
                selected = advanceDays,
                onSelected = { advanceDays = it },
                modifier = Modifier.fillMaxWidth(),
                optionLabel = { "$it days" }
            )
            DropdownField(
                label = "Status",
                options = FacilityData.facilityStatuses,
                selected = status,
                onSelected = { status = it },
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    // ignore a second tap while the first save is already leaving this page
                    if (!navController.isCurrentScreenResumed()) return@Button
                    val capacity = capacityText.toIntOrNull() ?: 0
                    errorMessage = when {
                        listOf(name, location, description).any { it.isBlank() } -> "Please fill in all the details"
                        FacilityData.isFacilityNameTaken(name, facilityId) -> "A facility with this name already exists"
                        capacity <= 0 -> "Capacity must be at least 1"
                        closeHour - openHour < slotHours -> "Opening hours must fit at least one slot"
                        else -> null
                    }
                    if (errorMessage != null) return@Button

                    if (existing == null) {
                        FacilityData.addFacility(
                            name.trim(), description.trim(), location.trim(), capacity,
                            openHour, closeHour, slotHours, maxPerDay, advanceDays, status
                        )
                    } else {
                        FacilityData.updateFacility(
                            existing.copy(
                                facilityName = name.trim(), description = description.trim(), location = location.trim(),
                                capacity = capacity, openHour = openHour, closeHour = closeHour, slotHours = slotHours,
                                maxBookingsPerDay = maxPerDay, advanceBookingDays = advanceDays, facilityStatus = status
                            )
                        )
                    }
                    navController.safePopBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (existing != null) "Save Changes" else "Add Facility")
            }
        }
    }
}
