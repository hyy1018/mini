// #member1
// Small coloured label for statuses (registration status, visitor pass status, ...),
// plus a label/value row used on detail cards.
package com.example.mini.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mini.model.EmergencyStatus
import com.example.mini.model.PassStatus
import com.example.mini.model.RegistrationStatus
import com.example.mini.model.RequestStatus
import com.example.mini.ui.theme.AlertOrange
import com.example.mini.ui.theme.OnAlertOrange

@Composable
fun StatusBadge(label: String, container: Color, onContainer: Color) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = onContainer,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(container)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
fun PassStatusBadge(status: PassStatus) {
    val (container, onContainer) = when (status) {
        PassStatus.VALID -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        PassStatus.USED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        PassStatus.EXPIRED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        PassStatus.CANCELLED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }
    StatusBadge(status.label, container, onContainer)
}

@Composable
fun RegistrationStatusBadge(status: RegistrationStatus) {
    val (container, onContainer) = when (status) {
        RegistrationStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        RegistrationStatus.APPROVED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        RegistrationStatus.REJECTED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }
    StatusBadge(status.label, container, onContainer)
}

// maintenance requests (Module 4) and complaints (Module 5) share these statuses
@Composable
fun RequestStatusBadge(status: RequestStatus) {
    val (container, onContainer) = when (status) {
        RequestStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        RequestStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        RequestStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    StatusBadge(status.label, container, onContainer)
}

@Composable
fun EmergencyStatusBadge(status: EmergencyStatus) {
    val (container, onContainer) = when (status) {
        EmergencyStatus.REPORTED -> AlertOrange to OnAlertOrange
        EmergencyStatus.ACKNOWLEDGED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        EmergencyStatus.RESOLVED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        EmergencyStatus.CANCELLED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }
    StatusBadge(status.label, container, onContainer)
}

// "Label      value" on one line, for detail cards
@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}
