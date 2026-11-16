// #member3
// Staff inbox: every resident who has messaged management, newest first, with unread counts.
package com.example.mini.screen.member3.module6

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.data.CommunityData
import com.example.mini.data.ResidentData
import com.example.mini.data.formatDateTime
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmptyState

@Composable
fun ChatInboxScreen(navController: NavHostController) {
    val residents = CommunityData.conversationResidents()

    Scaffold(topBar = { BackTopBar("Resident Enquiries", navController) }) { innerPadding ->
        if (residents.isEmpty()) {
            EmptyState("No enquiries yet", Modifier.padding(innerPadding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(residents, key = { it }) { residentUserId ->
                val user = ResidentData.getUser(residentUserId)
                val resident = ResidentData.residentOfUser(residentUserId)
                val last = CommunityData.conversation(residentUserId).lastOrNull()
                val unread = CommunityData.unreadForManagement(residentUserId)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.safeNavigate("chat/$residentUserId") }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${user?.name ?: "-"} (${resident?.let { ResidentData.unitLabel(it.residentId) } ?: "-"})",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(last?.messageText ?: "", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            last?.let {
                                Text(formatDateTime(it.sentAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (unread > 0) Badge { Text("$unread") }
                    }
                }
            }
        }
    }
}
