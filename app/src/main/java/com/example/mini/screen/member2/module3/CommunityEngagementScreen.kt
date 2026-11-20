// #member2
// Module 3 hub with three tabs: Announcements (digital notice board), Events, and Community Info search.
// Residents read and take part; staff also get an Add button for the open tab.
package com.example.mini.screen.member2.module3

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.data.EngagementData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDate
import com.example.mini.data.formatDateTime
import com.example.mini.data.formatHour
import com.example.mini.model.Announcement
import com.example.mini.model.CommunityInfo
import com.example.mini.model.Event
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmptyState
import com.example.mini.screen.StatusBadge

private val tabs = listOf("Announcements", "Events", "Info & FAQ")

@Composable
fun CommunityEngagementScreen(initialTab: Int, navController: NavHostController) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab.coerceIn(0, tabs.lastIndex)) }
    val isStaff = UserSession.isStaff

    Scaffold(
        topBar = { BackTopBar("Community", navController) },
        floatingActionButton = {
            if (isStaff) {
                FloatingActionButton(onClick = {
                    when (selectedTab) {
                        0 -> navController.safeNavigate("announcement_form")
                        1 -> navController.safeNavigate("event_form")
                        else -> navController.safeNavigate("info_form")
                    }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> AnnouncementsTab(navController)
                1 -> EventsTab(navController)
                else -> InfoTab(isStaff, navController)
            }
        }
    }
}

// ---------- Announcements ----------

@Composable
private fun AnnouncementsTab(navController: NavHostController) {
    val announcements = EngagementData.announcementsNewestFirst()
    if (announcements.isEmpty()) {
        EmptyState("No announcements yet")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(announcements, key = { it.announcementId }) { announcement ->
            AnnouncementCard(announcement) { navController.safeNavigate("announcement/${announcement.announcementId}") }
        }
    }
}

@Composable
private fun AnnouncementCard(announcement: Announcement, onClick: () -> Unit) {
    val userId = UserSession.currentUserId ?: -1
    // important + not yet confirmed = needs the resident's attention
    val needsConfirm = announcement.isImportant && UserSession.isResident &&
        !EngagementData.hasRead(announcement.announcementId, userId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(
                    announcement.category,
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.onSecondaryContainer
                )
                if (announcement.isImportant) {
                    StatusBadge("Important", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(announcement.title, style = MaterialTheme.typography.titleMedium)
            Text(
                announcement.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatDateTime(announcement.postedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (needsConfirm) {
                    Icon(Icons.Filled.PriorityHigh, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("Please confirm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ---------- Events ----------

@Composable
private fun EventsTab(navController: NavHostController) {
    val events = EngagementData.eventsByDate()
    if (events.isEmpty()) {
        EmptyState("No events yet")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(events, key = { it.eventId }) { event ->
            EventCard(event) { navController.safeNavigate("event/${event.eventId}") }
        }
    }
}

@Composable
private fun EventCard(event: Event, onClick: () -> Unit) {
    val status = EngagementData.displayStatus(event)
    val joined = EngagementData.isRegistered(event.eventId, UserSession.currentUserId ?: -1)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(event.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                EventStatusBadge(status)
            }
            Text(
                "${formatDate(event.eventDate)} · ${formatHour(event.startHour)}-${formatHour(event.endHour)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(event.venue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row {
                Text(
                    "${EngagementData.registrationCount(event.eventId)} / ${event.maxParticipants} joined",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
                if (joined) {
                    Text("You're going", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun EventStatusBadge(status: String) {
    val (container, onContainer) = when (status) {
        "Upcoming" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "Cancelled" -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    StatusBadge(status, container, onContainer)
}

// ---------- Community information search ----------

@Composable
private fun InfoTab(isStaff: Boolean, navController: NavHostController) {
    var query by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf<String?>(null) }
    val results = EngagementData.searchInfo(query, typeFilter)

    Column {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it.take(100) },
            label = { Text("Search rules, FAQ, guidelines...") },
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
            FilterChip(selected = typeFilter == null, onClick = { typeFilter = null }, label = { Text("All") })
            EngagementData.infoTypes.forEach { type ->
                FilterChip(selected = typeFilter == type, onClick = { typeFilter = type }, label = { Text(type) })
            }
        }
        if (results.isEmpty()) {
            EmptyState("No information found")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results, key = { it.infoId }) { info ->
                    InfoCard(info, isStaff) { navController.safeNavigate("info_form?id=${info.infoId}") }
                }
            }
        }
    }
}

// tap to expand the answer; staff get an edit button
@Composable
private fun InfoCard(info: CommunityInfo, isStaff: Boolean, onEdit: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(info.infoType, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(info.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                }
                if (isStaff) {
                    IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
                }
                // arrow hints that the card opens
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            if (expanded) {
                Spacer(Modifier.height(6.dp))
                // one bullet per line; leading "-" or "•" typed by staff is dropped so it isn't doubled
                info.content.lines()
                    .map { it.trim().trimStart('-', '•', '*').trim() }
                    .filter { it.isNotBlank() }
                    .forEach { point ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("•", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(point, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
            }
        }
    }
}
