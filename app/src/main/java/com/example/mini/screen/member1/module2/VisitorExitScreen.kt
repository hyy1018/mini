// #member1
// Security records a visitor's exit against their active visit (Flowchart 9 exit side, DFD process 2.5).
package com.example.mini.screen.member1.module2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.example.mini.model.VisitLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorExitScreen(navController: NavHostController) {
    var query by remember { mutableStateOf("") }
    var logPendingExit by remember { mutableStateOf<VisitLog?>(null) }

    // search active visits by visitor name or vehicle plate
    val activeVisits = VisitorData.activeVisits().filter { log ->
        val visitor = VisitorData.getById(log.visitorId)
        query.isBlank() ||
            visitor?.visitorName?.contains(query, ignoreCase = true) == true ||
            visitor?.vehiclePlate?.contains(query, ignoreCase = true) == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Exit") },
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
                label = { Text("Search name or vehicle plate") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (activeVisits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active visit found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeVisits, key = { it.logId }) { log ->
                        val visitor = VisitorData.getById(log.visitorId)
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(visitor?.visitorName ?: "-", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        (visitor?.let { ResidentData.unitLabel(it.residentId) } ?: "-") +
                                            (visitor?.vehiclePlate?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Entered ${formatDateTime(log.entryTime)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(onClick = { logPendingExit = log }) { Text("Exit") }
                            }
                        }
                    }
                }
            }
        }
    }

    // AlertDialog: confirm before recording the exit time
    logPendingExit?.let { log ->
        val name = VisitorData.getById(log.visitorId)?.visitorName ?: "this visitor"
        AlertDialog(
            onDismissRequest = { logPendingExit = null },
            title = { Text("Record exit?") },
            text = { Text("Record $name leaving now?") },
            confirmButton = {
                TextButton(onClick = {
                    VisitorData.recordExit(log.logId)
                    logPendingExit = null
                }) { Text("Record Exit") }
            },
            dismissButton = {
                TextButton(onClick = { logPendingExit = null }) { Text("Cancel") }
            }
        )
    }
}
