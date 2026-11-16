// #member2
// The logged-in user's notifications, newest first. Opening the page marks them as read.
package com.example.mini.screen.member2.module3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.data.NotificationData
import com.example.mini.data.UserSession
import com.example.mini.data.formatDateTime
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmptyState

@Composable
fun NotificationScreen(navController: NavHostController) {
    val userId = UserSession.currentUserId ?: return
    // which ones were unread when the page opened, so they stay highlighted while it's open
    val unreadIds = remember { NotificationData.forUser(userId).filter { !it.isRead }.map { it.notificationId }.toSet() }
    val notifications = NotificationData.forUser(userId)

    // mark everything read when leaving, so the bell badge clears
    DisposableEffect(Unit) {
        onDispose { NotificationData.markAllRead(userId) }
    }

    Scaffold(topBar = { BackTopBar("Notifications", navController) }) { innerPadding ->
        if (notifications.isEmpty()) {
            EmptyState("No notifications yet", Modifier.padding(innerPadding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications, key = { it.notificationId }) { notification ->
                val isNew = notification.notificationId in unreadIds
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNew) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isNew) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(8.dp)) {}
                            Spacer(Modifier.width(12.dp))
                        }
                        Column {
                            Text(notification.notificationType, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(notification.message, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                formatDateTime(notification.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
