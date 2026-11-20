// #member3
// Resident "Community" tab with two tabs: the social Feed and Emergency (SOS, my alerts, contact numbers).
// The chat icon in the top bar opens the enquiry chat with management.
package com.example.mini.screen.member3.module6

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.data.CommunityData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDateTime
import com.example.mini.model.EmergencyContact
import com.example.mini.model.Post
import com.example.mini.model.UserRole
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmergencyStatusBadge
import com.example.mini.screen.EmptyState
import com.example.mini.screen.LocalPhoto
import com.example.mini.screen.ResidentBottomBar
import com.example.mini.screen.StatusBadge
import com.example.mini.screen.UserAvatar

@Composable
fun CommunityEmergencyScreen(navController: NavHostController) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val userId = UserSession.currentUserId ?: return
    // chat messages the resident hasn't opened yet, from anyone
    val unreadMessages = CommunityData.totalUnread(userId)

    Scaffold(
        topBar = {
            BackTopBar("Community", navController) {
                IconButton(onClick = { navController.safeNavigate("messages") }) {
                    if (unreadMessages > 0) {
                        BadgedBox(badge = { Badge { Text("$unreadMessages") } }) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Messages")
                        }
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Messages")
                    }
                }
            }
        },
        bottomBar = { ResidentBottomBar(navController) },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { navController.safeNavigate("new_post") },
                    icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    text = { Text("New Post") }
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
                listOf("Feed", "Emergency").forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
            if (selectedTab == 0) FeedTab(userId, navController) else EmergencyTab(navController)
        }
    }
}

// ---------- Feed ----------

@Composable
private fun FeedTab(userId: Int, navController: NavHostController) {
    val posts = CommunityData.feed()
    if (posts.isEmpty()) {
        EmptyState("No posts yet.\nBe the first to share something!")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(posts, key = { it.postId }) { post ->
            PostCard(
                post = post,
                userId = userId,
                onClick = { navController.safeNavigate("post/${post.postId}") }
            )
        }
    }
}

// shared by the feed and the post detail page
@Composable
fun PostCard(post: Post, userId: Int, onClick: (() -> Unit)?) {
    val liked = CommunityData.isLiked(post.postId, userId)
    // management posts get a blue border and tint so residents can tell official notices apart
    val fromManagement = isManagement(post.userId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = if (fromManagement) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
        } else {
            CardDefaults.cardColors()
        },
        border = if (fromManagement) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            PostAuthorRow(post.userId, post.createdAt)
            Spacer(Modifier.height(8.dp))
            Text(post.content, style = MaterialTheme.typography.bodyLarge)
            post.photoPath?.let { path ->
                Spacer(Modifier.height(8.dp))
                LocalPhoto(path, "Post photo", Modifier.fillMaxWidth().height(200.dp))
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { CommunityData.toggleLike(post.postId, userId) }) {
                    Icon(
                        if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (liked) "Unlike" else "Like",
                        tint = if (liked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("${CommunityData.likeCount(post.postId)}")
                Spacer(Modifier.width(16.dp))
                Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text("${CommunityData.commentCount(post.postId)}")
            }
        }
    }
}

fun isManagement(userId: Int): Boolean = ResidentData.getUser(userId)?.role == UserRole.STAFF

// avatar initial + name (+ MANAGEMENT badge for staff, unit for residents) + time
@Composable
fun PostAuthorRow(userId: Int, time: Long) {
    val user = ResidentData.getUser(userId)
    val subtitle = if (user?.role == UserRole.RESIDENT) {
        ResidentData.residentOfUser(userId)?.let { ResidentData.unitLabel(it.residentId) } ?: ""
    } else {
        ""
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        UserAvatar(userId, size = 36.dp)
        Spacer(Modifier.width(10.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(user?.name ?: "Unknown", style = MaterialTheme.typography.titleSmall)
                if (user?.role == UserRole.STAFF) {
                    Spacer(Modifier.width(6.dp))
                    StatusBadge("Management", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
                }
            }
            Text(
                listOf(subtitle, formatDateTime(time)).filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------- Emergency ----------

@Composable
private fun EmergencyTab(navController: NavHostController) {
    val residentId = UserSession.currentResident?.residentId
    val myAlerts = residentId?.let { CommunityData.emergenciesOfResident(it) } ?: emptyList()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = { navController.safeNavigate("emergency_sos") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Icon(Icons.Filled.Sos, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Text("Report an Emergency", style = MaterialTheme.typography.titleMedium)
            }
        }

        if (myAlerts.isNotEmpty()) {
            item { Text("My Alerts", style = MaterialTheme.typography.titleMedium) }
            items(myAlerts, key = { "alert${it.emergencyId}" }) { alert ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(alert.emergencyType, style = MaterialTheme.typography.titleSmall)
                            Text(formatDateTime(alert.reportedAt), style = MaterialTheme.typography.labelSmall)
                            if (alert.remarks.isNotBlank()) Text(alert.remarks, style = MaterialTheme.typography.bodySmall)
                        }
                        EmergencyStatusBadge(alert.reportStatus)
                    }
                }
            }
        }

        item { Text("Emergency Contacts", style = MaterialTheme.typography.titleMedium) }
        items(CommunityData.contacts, key = { "contact${it.contactId}" }) { contact ->
            EmergencyContactRow(contact)
        }
    }
}

// one contact with a call button (opens the phone dialer, so no call permission is needed)
@Composable
fun EmergencyContactRow(contact: EmergencyContact, trailing: @Composable () -> Unit = {}) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.contactName, style = MaterialTheme.typography.titleSmall)
                Text("${contact.contactType} · ${contact.phoneNo}", style = MaterialTheme.typography.bodySmall)
            }
            trailing()
            IconButton(onClick = {
                // devices without a phone app (some tablets) have nothing to open -- don't crash
                try {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phoneNo}")))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No phone app found. Please call ${contact.phoneNo}", Toast.LENGTH_LONG).show()
                }
            }) {
                Icon(Icons.Filled.Call, contentDescription = "Call ${contact.contactName}", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
