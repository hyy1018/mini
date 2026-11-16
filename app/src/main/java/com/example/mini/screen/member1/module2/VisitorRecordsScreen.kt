// #member1
// All visitor records for Security and Management (Flowchart 6, staff / security side, DFD process 2.6).
package com.example.mini.screen.member1.module2

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.ResidentData
import com.example.mini.data.VisitorData
import com.example.mini.data.formatDateTime
import com.example.mini.model.PassStatus
import com.example.mini.screen.InfoRow
import com.example.mini.screen.PassStatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorRecordsScreen(navController: NavHostController) {
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<PassStatus?>(null) } // null = All

    LaunchedEffect(Unit) { VisitorData.refreshExpired() }

    // search name, phone or plate, then narrow by status
    val records = VisitorData.visitors
        .filter { statusFilter == null || it.passStatus == statusFilter }
        .filter {
            query.isBlank() ||
                it.visitorName.contains(query, ignoreCase = true) ||
                it.phoneNo.contains(query) ||
                it.vehiclePlate.contains(query, ignoreCase = true)
        }
        .sortedByDescending { it.validFrom }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visitor Records") },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it.take(100) },
                label = { Text("Search name, phone or plate") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp)
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { statusFilter = null },
                    label = { Text("All") }
                )
                PassStatus.entries.forEach { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { statusFilter = status },
                        label = { Text(status.label) }
                    )
                }
            }

            if (records.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No records found")
                }
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
                                    Text(
                                        visitor.visitorName,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
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
}
