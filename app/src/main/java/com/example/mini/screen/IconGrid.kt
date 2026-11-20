// #member1
// Icon grid used by the Resident, Staff and Security home pages: a round icon with its name
// underneath, 4 per row, and an optional red count badge (e.g. pending approvals).
package com.example.mini.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class HomeShortcut(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val badgeCount: Int = 0
)

// a titled group of shortcuts, e.g. "Community" with News, Messages, Reports
@Composable
fun IconGridSection(title: String, shortcuts: List<HomeShortcut>, onClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        // chunked rows instead of LazyVerticalGrid, since a lazy grid can't sit inside a scrolling Column
        shortcuts.chunked(4).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { shortcut ->
                    IconTile(
                        label = shortcut.label,
                        icon = shortcut.icon,
                        badgeCount = shortcut.badgeCount,
                        onClick = { onClick(shortcut.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // keeps a short last row aligned with the rows above
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun IconTile(label: String, icon: ImageVector, badgeCount: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BadgedBox(badge = { if (badgeCount > 0) Badge { Text("$badgeCount") } }) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
    }
}
