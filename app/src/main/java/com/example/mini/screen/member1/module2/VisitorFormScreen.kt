// #member1
// Invite (Flowchart 5) or edit (Flowchart 7) a visitor. visitorId = -1 means a new pass.
// The pass is time-limited: visit date + start time + how many hours it stays valid.
package com.example.mini.screen.member1.module2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.nav.isCurrentScreenResumed
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.HOUR_MILLIS
import com.example.mini.data.InputRules
import com.example.mini.data.atHour
import com.example.mini.data.hourOf
import com.example.mini.data.localDateToUtc
import com.example.mini.data.startOfDay
import com.example.mini.data.utcToLocalDate
import com.example.mini.data.UserSession
import com.example.mini.data.VisitorData
import com.example.mini.data.formatDate
import com.example.mini.data.formatDateTime
import com.example.mini.screen.DropdownField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorFormScreen(visitorId: Int, navController: NavHostController) {
    val existing = if (visitorId != -1) VisitorData.getById(visitorId) else null
    val isEdit = existing != null
    val now = System.currentTimeMillis()

    // defaults: a new pass starts at the next full hour and lasts 4 hours
    val initialStart = existing?.validFrom ?: (now + HOUR_MILLIS)
    val initialHours = existing?.let { ((it.validUntil - it.validFrom) / HOUR_MILLIS).toInt() }
        ?.takeIf { it in VisitorData.durationHours } ?: 4

    var name by remember { mutableStateOf(existing?.visitorName ?: "") }
    var phone by remember { mutableStateOf(existing?.phoneNo ?: "") }
    var plate by remember { mutableStateOf(existing?.vehiclePlate ?: "") }
    var purpose by remember { mutableStateOf(existing?.purpose ?: VisitorData.purposes.first()) }
    var visitDate by remember { mutableStateOf(startOfDay(initialStart)) }
    var startHour by remember { mutableStateOf(hourOf(initialStart)) }
    var durationHours by remember { mutableStateOf(initialHours) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val validFrom = atHour(visitDate, startHour)
    val validUntil = validFrom + durationHours * HOUR_MILLIS

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Visitor" else "Invite Visitor") },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        // a pass that's been used, cancelled or expired can't be edited any more
        if (existing != null && !VisitorData.canChange(existing)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("This pass can no longer be edited")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Visitor details ---
            Text("Visitor Details", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(100) },
                label = { Text("Visitor Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = InputRules.sanitizePhone(it) },
                label = { Text("Phone Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = plate,
                onValueChange = { plate = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(10) },
                label = { Text("Vehicle Plate (optional)") },
                placeholder = { Text("e.g. ABC1234") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth()
            )
            DropdownField(
                label = "Purpose of Visit",
                options = VisitorData.purposes,
                selected = purpose,
                onSelected = { purpose = it },
                modifier = Modifier.fillMaxWidth()
            )

            // --- Pass validity ---
            Text("Pass Validity", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = formatDate(visitDate),
                onValueChange = {},
                readOnly = true,
                label = { Text("Visit Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Pick date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DropdownField(
                    label = "Start Time",
                    options = (0..23).toList(),
                    selected = startHour,
                    onSelected = { startHour = it },
                    optionLabel = { "%02d:00".format(it) },
                    modifier = Modifier.weight(1f)
                )
                DropdownField(
                    label = "Valid For",
                    options = VisitorData.durationHours,
                    selected = durationHours,
                    onSelected = { durationHours = it },
                    optionLabel = { "$it hours" },
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                "QR code works from ${formatDateTime(validFrom)} until ${formatDateTime(validUntil)}, for one entry only.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    // ignore a second tap while the first save is already leaving this page
                    if (!navController.isCurrentScreenResumed()) return@Button
                    errorMessage = when {
                        name.isBlank() -> "Please enter the visitor's name"
                        !InputRules.isValidPhone(phone) -> InputRules.PHONE_MESSAGE
                        validUntil <= System.currentTimeMillis() -> "This time window has already passed"
                        else -> null
                    }
                    val resident = UserSession.currentResident
                    if (errorMessage != null || resident == null) return@Button

                    if (existing == null) {
                        val visitor = VisitorData.register(
                            resident.residentId, name.trim(), phone, plate, purpose, validFrom, validUntil
                        )
                        // go straight to the new QR pass; the form is removed from the back stack
                        navController.safeNavigate("visitor_pass/${visitor.visitorId}") {
                            popUpTo("visitor_form?visitorId={visitorId}") { inclusive = true }
                        }
                    } else {
                        VisitorData.update(existing.visitorId, name.trim(), phone, plate, purpose, validFrom, validUntil)
                        navController.safePopBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEdit) "Save Changes" else "Generate QR Pass")
            }
        }
    }

    if (showDatePicker) {
        val todayUtc = remember { localDateToUtc(System.currentTimeMillis()) }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = localDateToUtc(visitDate),
            // past days can't be picked
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayUtc
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { visitDate = utcToLocalDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

