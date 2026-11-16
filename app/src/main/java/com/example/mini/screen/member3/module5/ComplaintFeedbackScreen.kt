// #member3
// Module 5 hub with two tabs: Complaints and Feedback.
// Residents see their own history (Request Tracking); staff see everything submitted.
package com.example.mini.screen.member3.module5

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.data.ComplaintData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDate
import com.example.mini.model.RequestStatus
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmptyState
import com.example.mini.screen.RequestStatusBadge

@Composable
fun ComplaintFeedbackScreen(initialTab: Int, navController: NavHostController) {
    val isStaff = UserSession.isStaff
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab.coerceIn(0, 1)) }

    Scaffold(
        topBar = { BackTopBar("Complaints & Feedback", navController) },
        floatingActionButton = {
            if (!isStaff) {
                ExtendedFloatingActionButton(
                    onClick = { navController.safeNavigate(if (selectedTab == 0) "complaint_form" else "feedback_form") },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(if (selectedTab == 0) "New Complaint" else "Give Feedback") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                listOf("Complaints", "Feedback").forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
            if (selectedTab == 0) ComplaintsTab(isStaff, navController) else FeedbackTab(isStaff)
        }
    }
}

@Composable
private fun ComplaintsTab(isStaff: Boolean, navController: NavHostController) {
    var statusFilter by remember { mutableStateOf<RequestStatus?>(null) }
    val residentId = UserSession.currentResident?.residentId
    val complaints = (if (isStaff) ComplaintData.allComplaints() else residentId?.let { ComplaintData.complaintsOfResident(it) } ?: emptyList())
        .filter { statusFilter == null || it.complaintStatus == statusFilter }

    Column {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = statusFilter == null, onClick = { statusFilter = null }, label = { Text("All") })
            RequestStatus.entries.forEach { status ->
                FilterChip(selected = statusFilter == status, onClick = { statusFilter = status }, label = { Text(status.label) })
            }
        }
        if (complaints.isEmpty()) {
            EmptyState(if (isStaff) "No complaints" else "No complaints yet.\nTap New Complaint to submit one.")
            return@Column
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(complaints, key = { it.complaintId }) { complaint ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.safeNavigate("complaint/${complaint.complaintId}") }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(complaint.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            RequestStatusBadge(complaint.complaintStatus)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${complaint.category} · ${formatDate(complaint.submittedAt)}" +
                                    if (isStaff) " · ${ResidentData.unitLabel(complaint.residentId)}" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            if (complaint.photoPath != null) {
                                Icon(Icons.Filled.Image, contentDescription = "Has photo", modifier = Modifier.size(16.dp))
                            }
                        }
                        if (complaint.responseText.isNotBlank()) {
                            Text(
                                "Management: ${complaint.responseText}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackTab(isStaff: Boolean) {
    val residentId = UserSession.currentResident?.residentId
    val feedbacks = if (isStaff) ComplaintData.allFeedback() else residentId?.let { ComplaintData.feedbackOfResident(it) } ?: emptyList()

    if (feedbacks.isEmpty()) {
        EmptyState(if (isStaff) "No feedback yet" else "No feedback yet.\nTap Give Feedback to rate a service.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isStaff) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Average rating: ${"%.1f".format(ComplaintData.averageRating())} / 5 from ${feedbacks.size} response(s)",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        items(feedbacks, key = { it.feedbackId }) { feedback ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(feedback.serviceType, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        StarRow(feedback.rating)
                    }
                    if (feedback.comments.isNotBlank()) Text(feedback.comments, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        formatDate(feedback.submittedAt) + if (isStaff) " · ${ResidentData.unitLabel(feedback.residentId)}" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// read-only 5-star display
@Composable
fun StarRow(rating: Int) {
    Row {
        (1..5).forEach { star ->
            Icon(
                if (star <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
