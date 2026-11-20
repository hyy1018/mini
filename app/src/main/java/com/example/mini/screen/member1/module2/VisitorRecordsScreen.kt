// #member1
// Visitor records for Security and Management, one day at a time (Flowchart 6, staff / security side,
// DFD process 2.6). Opens on today; arrows step a day, the calendar jumps to any date (past too).
// A summary shows the day's expected / arrived / still-inside counts.
package com.example.mini.screen.member1.module2

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.data.ResidentData
import com.example.mini.data.VisitorData
import com.example.mini.data.addDays
import com.example.mini.data.formatDate
import com.example.mini.data.formatDateTime
import com.example.mini.data.localDateToUtc
import com.example.mini.data.startOfDay
import com.example.mini.data.utcToLocalDate
import com.example.mini.model.PassStatus
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmptyState
import com.example.mini.screen.InfoRow
import com.example.mini.screen.PassStatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorRecordsScreen(navController: NavHostController) {
    val today = startOfDay(System.currentTimeMillis())
    var selectedDay by rememberSaveable { mutableLongStateOf(today) }
    var showCalendar by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<PassStatus?>(null) } // null = All

    LaunchedEffect(Unit) { VisitorData.refreshExpired() }

    val dayVisitors = VisitorData.visitorsOn(selectedDay)
    // day summary: cancelled passes aren't expected any more
    val expected = dayVisitors.count { it.passStatus != PassStatus.CANCELLED }
    val arrived = dayVisitors.count { VisitorData.logFor(it.visitorId) != null }
    val inside = dayVisitors.count { v -> VisitorData.logFor(v.visitorId)?.let { it.exitTime == null } == true }

    // search name, phone or plate, then narrow by status -- both within the chosen day
    val records = dayVisitors
        .filter { statusFilter == null || it.passStatus == statusFilter }
        .filter {
            query.isBlank() ||
                it.visitorName.contains(query, ignoreCase = true) ||
                it.phoneNo.contains(query) ||
                it.vehiclePlate.contains(query, ignoreCase = true)
        }

    Scaffold(topBar = { BackTopBar("Visitor Records", navController) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- Date bar: previous day / chosen day / next day / calendar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedDay = addDays(selectedDay, -1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous day")
                }
                Text(
                    dayLabel(selectedDay, today),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { selectedDay = addDays(selectedDay, 1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next day")
                }
                IconButton(onClick = { showCalendar = true }) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = "Pick a date")
                }
            }
            if (selectedDay != today) {
                TextButton(
                    onClick = { selectedDay = today },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { Text("Back to today") }
            }

            // --- Day summary ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryTile("Expected", expected, Modifier.weight(1f))
                SummaryTile("Arrived", arrived, Modifier.weight(1f))
                SummaryTile("Inside", inside, Modifier.weight(1f))
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it.take(100) },
                label = { Text("Search name, phone or plate") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp)
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = statusFilter == null, onClick = { statusFilter = null }, label = { Text("All") })
                PassStatus.entries.forEach { status ->
                    FilterChip(selected = statusFilter == status, onClick = { statusFilter = status }, label = { Text(status.label) })
                }
            }

            if (records.isEmpty()) {
                EmptyState(if (dayVisitors.isEmpty()) "No visitors on ${formatDate(selectedDay)}" else "No records match your search")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(records, key = { it.visitorId }) { visitor ->
                        val log = VisitorData.logFor(visitor.visitorId)
                        val host = ResidentData.getResident(visitor.residentId)?.let { ResidentData.getUser(it.userId) }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(visitor.visitorName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                    PassStatusBadge(visitor.passStatus)
                                }
                                Spacer(Modifier.height(4.dp))
                                InfoRow("Visiting", "${host?.name ?: "-"} (${ResidentData.unitLabel(visitor.residentId)})")
                                InfoRow("Phone", visitor.phoneNo)
                                InfoRow("Vehicle", visitor.vehiclePlate.ifBlank { "-" })
                                InfoRow("Valid", "${formatDateTime(visitor.validFrom)} –\n${formatDateTime(visitor.validUntil)}")
                                InfoRow("Entry", log?.entryTime?.let { formatDateTime(it) } ?: "-")
                                InfoRow("Exit", log?.exitTime?.let { formatDateTime(it) } ?: if (log != null) "Still inside" else "-")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCalendar) {
        // past dates allowed -- records are looked up, not booked
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = localDateToUtc(selectedDay))
        DatePickerDialog(
            onDismissRequest = { showCalendar = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { selectedDay = utcToLocalDate(it) }
                    showCalendar = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showCalendar = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

// "Today, 20 Nov 2026" / "Yesterday, ..." / "Tomorrow, ..." / "20 Nov 2026"
private fun dayLabel(day: Long, today: Long): String = when (day) {
    today -> "Today, ${formatDate(day)}"
    addDays(today, -1) -> "Yesterday, ${formatDate(day)}"
    addDays(today, 1) -> "Tomorrow, ${formatDate(day)}"
    else -> formatDate(day)
}

@Composable
private fun SummaryTile(label: String, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$count", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
