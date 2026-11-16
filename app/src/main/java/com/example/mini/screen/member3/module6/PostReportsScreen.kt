// #member3
// Staff post moderation: review reported posts and remove them or dismiss the report.
package com.example.mini.screen.member3.module6

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.data.CommunityData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDateTime
import com.example.mini.model.PostReport
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmptyState

@Composable
fun PostReportsScreen(navController: NavHostController) {
    val reports = CommunityData.pendingReports()
    var reportToRemove by remember { mutableStateOf<PostReport?>(null) }

    Scaffold(topBar = { BackTopBar("Reported Posts", navController) }) { innerPadding ->
        if (reports.isEmpty()) {
            EmptyState("No reports to review", Modifier.padding(innerPadding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reports, key = { it.reportId }) { report ->
                val post = CommunityData.getPost(report.postId)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Reason: ${report.reportReason}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                        Text(
                            "Reported by ${ResidentData.getUser(report.reportedByUserId)?.name ?: "-"} · ${formatDateTime(report.reportedAt)}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        if (post != null) {
                            PostAuthorRow(post.userId, post.createdAt)
                            Text(post.content, style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { UserSession.currentUserId?.let { CommunityData.reviewReport(report.reportId, false, it) } },
                                modifier = Modifier.weight(1f)
                            ) { Text("Dismiss") }
                            Button(
                                onClick = { reportToRemove = report },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) { Text("Remove Post") }
                        }
                    }
                }
            }
        }
    }

    reportToRemove?.let { report ->
        AlertDialog(
            onDismissRequest = { reportToRemove = null },
            title = { Text("Remove this post?") },
            text = { Text("The post will be hidden from the feed and the author will be notified.") },
            confirmButton = {
                TextButton(onClick = {
                    UserSession.currentUserId?.let { CommunityData.reviewReport(report.reportId, true, it) }
                    reportToRemove = null
                }) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { reportToRemove = null }) { Text("Cancel") } }
        )
    }
}
