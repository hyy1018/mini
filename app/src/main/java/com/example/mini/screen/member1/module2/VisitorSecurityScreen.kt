// #member1
// "Visitors" tab: the resident's own visitor passes (Flowchart 6, resident side) and the invite button.
package com.example.mini.screen.member1.module2

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.UserSession
import com.example.mini.data.VisitorData
import com.example.mini.data.formatDateTime
import com.example.mini.model.Visitor
import com.example.mini.screen.PassStatusBadge
import com.example.mini.screen.ResidentBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorSecurityScreen(navController: NavHostController) {
    val resident = UserSession.currentResident
    val visitors = resident?.let { VisitorData.byResident(it.residentId) } ?: emptyList()

    // passes whose time window has ended switch to Expired when the list opens
    LaunchedEffect(Unit) { VisitorData.refreshExpired() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Visitors") },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = { ResidentBottomBar(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.safeNavigate("visitor_form") },
                icon = { Icon(Icons.Filled.PersonAdd, contentDescription = null) },
                text = { Text("Invite Visitor") }
            )
        }
    ) { innerPadding ->
        if (visitors.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No visitors yet.\nTap Invite Visitor to create a QR pass.",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                // bottom padding keeps the last card clear of the floating button
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visitors, key = { it.visitorId }) { visitor ->
                    VisitorCard(visitor) { navController.safeNavigate("visitor_pass/${visitor.visitorId}") }
                }
            }
        }
    }
}

@Composable
private fun VisitorCard(visitor: Visitor, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(visitor.visitorName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                PassStatusBadge(visitor.passStatus)
            }
            Text(
                visitor.purpose + if (visitor.vehiclePlate.isNotBlank()) " · ${visitor.vehiclePlate}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "From ${formatDateTime(visitor.validFrom)}\nUntil ${formatDateTime(visitor.validUntil)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
