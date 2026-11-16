// #member3
// Resident rates a community service (1-5 stars) and leaves comments.
package com.example.mini.screen.member3.module5

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.isCurrentScreenResumed
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.ComplaintData
import com.example.mini.data.UserSession
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.DropdownField

private val ratingLabels = listOf("", "Very poor", "Poor", "Okay", "Good", "Excellent")

@Composable
fun FeedbackFormScreen(navController: NavHostController) {
    var serviceType by remember { mutableStateOf(ComplaintData.serviceTypes.first()) }
    var rating by remember { mutableIntStateOf(0) }
    var comments by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { BackTopBar("Give Feedback", navController) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DropdownField(
                label = "Service",
                options = ComplaintData.serviceTypes,
                selected = serviceType,
                onSelected = { serviceType = it },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Your rating", style = MaterialTheme.typography.titleSmall)
            Row {
                (1..5).forEach { star ->
                    IconButton(onClick = { rating = star }) {
                        Icon(
                            if (star <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "$star star",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
            if (rating > 0) Text(ratingLabels[rating], color = MaterialTheme.colorScheme.tertiary)
            OutlinedTextField(
                value = comments,
                onValueChange = { comments = it.take(500) },
                label = { Text("Comments (optional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    // ignore a second tap while the first save is already leaving this page
                    if (!navController.isCurrentScreenResumed()) return@Button
                    if (rating == 0) {
                        errorMessage = "Please tap a star rating"
                        return@Button
                    }
                    UserSession.currentResident?.let { ComplaintData.submitFeedback(it.residentId, serviceType, rating, comments.trim()) }
                    navController.safePopBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Feedback")
            }
        }
    }
}
