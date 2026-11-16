// #member2
// Staff creates (id = -1) or edits a community event.
package com.example.mini.screen.member2.module3

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
import com.example.mini.data.EngagementData
import com.example.mini.data.UserSession
import com.example.mini.data.addDays
import com.example.mini.data.atHour
import com.example.mini.data.formatHour
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.DatePickerField
import com.example.mini.screen.DropdownField

@Composable
fun EventFormScreen(eventId: Int, navController: NavHostController) {
    val existing = EngagementData.getEvent(eventId)

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var venue by remember { mutableStateOf(existing?.venue ?: "") }
    var eventDate by remember { mutableStateOf(existing?.eventDate ?: addDays(System.currentTimeMillis(), 7)) }
    var startHour by remember { mutableStateOf(existing?.startHour ?: 9) }
    var endHour by remember { mutableStateOf(existing?.endHour ?: 12) }
    var maxText by remember { mutableStateOf(existing?.maxParticipants?.toString() ?: "50") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { BackTopBar(if (existing != null) "Edit Event" else "New Event", navController) }
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
                value = title,
                onValueChange = { title = it.take(150) },
                label = { Text("Event Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = venue,
                onValueChange = { venue = it.take(100) },
                label = { Text("Venue") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            DatePickerField(
                label = "Event Date",
                dateMillis = eventDate,
                onDateSelected = { eventDate = it },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DropdownField(
                    label = "Start",
                    options = (0..23).toList(),
                    selected = startHour,
                    onSelected = { startHour = it },
                    optionLabel = { formatHour(it) },
                    modifier = Modifier.weight(1f)
                )
                DropdownField(
                    label = "End",
                    options = (1..23).toList(),
                    selected = endHour,
                    onSelected = { endHour = it },
                    optionLabel = { formatHour(it) },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = maxText,
                onValueChange = { maxText = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Maximum Participants") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it.take(1000) },
                label = { Text("Description") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    // ignore a second tap while the first save is already leaving this page
                    if (!navController.isCurrentScreenResumed()) return@Button
                    val max = maxText.toIntOrNull() ?: 0
                    val alreadyJoined = if (existing != null) EngagementData.registrationCount(eventId) else 0
                    errorMessage = when {
                        listOf(title, venue, description).any { it.isBlank() } -> "Please fill in all the details"
                        endHour <= startHour -> "End time must be after start time"
                        existing == null && atHour(eventDate, endHour) <= System.currentTimeMillis() -> "Event time has already passed"
                        max <= 0 -> "Maximum participants must be at least 1"
                        max < alreadyJoined -> "$alreadyJoined residents already registered -- the limit can't be lower"
                        else -> null
                    }
                    if (errorMessage != null) return@Button

                    if (existing == null) {
                        UserSession.currentUserId?.let { staffId ->
                            EngagementData.addEvent(staffId, title.trim(), description.trim(), venue.trim(), eventDate, startHour, endHour, max)
                        }
                    } else {
                        EngagementData.updateEvent(eventId, title.trim(), description.trim(), venue.trim(), eventDate, startHour, endHour, max)
                    }
                    navController.safePopBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (existing != null) "Save Changes" else "Create Event")
            }
        }
    }
}
