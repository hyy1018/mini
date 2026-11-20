// #member1
// Management's list of approved residents with search (Flowchart 2, staff side).
package com.example.mini.screen.member1.module1

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.mini.nav.safeNavigate
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.ResidentData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentListScreen(navController: NavHostController) {
    var query by remember { mutableStateOf("") }

    // matches name, username or unit, e.g. "ali", "resident1" or "A-10"
    val results = ResidentData.approvedResidents().filter { resident ->
        val user = ResidentData.getUser(resident.userId)
        query.isBlank() ||
            user?.name?.contains(query, ignoreCase = true) == true ||
            user?.username?.contains(query, ignoreCase = true) == true ||
            ResidentData.unitLabel(resident.residentId).contains(query, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Residents") },
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
                label = { Text("Search name, User ID or unit") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (results.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No records found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results, key = { it.residentId }) { resident ->
                        val user = ResidentData.getUser(resident.userId)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.safeNavigate("staff_resident_detail/${resident.residentId}") }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(user?.name ?: "-", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    ResidentData.unitLabel(resident.residentId) + " · " + resident.residentType,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
