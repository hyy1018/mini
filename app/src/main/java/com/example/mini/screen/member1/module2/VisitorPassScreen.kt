// #member1
// One visitor's Smart Visitor Pass: QR code, validity, entry/exit times, edit and cancel (Flowchart 8).
package com.example.mini.screen.member1.module2

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.VisitorData
import com.example.mini.data.formatDateTime
import com.example.mini.data.generateQrBitmap
import com.example.mini.model.PassStatus
import com.example.mini.screen.InfoRow
import com.example.mini.screen.PassStatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorPassScreen(visitorId: Int, navController: NavHostController) {
    val visitor = VisitorData.getById(visitorId)
    val log = VisitorData.logFor(visitorId)
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { VisitorData.refreshExpired() }

    // only rebuilt if the code itself changes
    val qrImage = remember(visitor?.qrCode) { visitor?.let { generateQrBitmap(it.qrCode).asImageBitmap() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visitor Pass") },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (visitor == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- QR pass ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(visitor.visitorName, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    PassStatusBadge(visitor.passStatus)
                    Spacer(Modifier.height(16.dp))

                    if (visitor.passStatus == PassStatus.VALID && qrImage != null) {
                        Image(
                            bitmap = qrImage,
                            contentDescription = "Visitor QR code",
                            modifier = Modifier.size(220.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            visitor.qrCode,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Send this QR to your visitor. Security scans it at the guardhouse -- it works once only.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // a used / expired / cancelled code is hidden so it can't be shown again
                        Text(
                            when (visitor.passStatus) {
                                PassStatus.USED -> "This pass has been used for entry."
                                PassStatus.EXPIRED -> "This pass has expired."
                                PassStatus.CANCELLED -> "This pass was cancelled."
                                PassStatus.VALID -> ""
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // --- Details ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Phone", visitor.phoneNo)
                    InfoRow("Vehicle", visitor.vehiclePlate.ifBlank { "-" })
                    InfoRow("Purpose", visitor.purpose)
                    InfoRow("Valid from", formatDateTime(visitor.validFrom))
                    InfoRow("Valid until", formatDateTime(visitor.validUntil))
                    InfoRow("Entry", log?.entryTime?.let { formatDateTime(it) } ?: "-")
                    InfoRow("Exit", log?.exitTime?.let { formatDateTime(it) } ?: "-")
                }
            }

            // --- Edit / cancel: only before the visitor has entered ---
            if (VisitorData.canChange(visitor)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel Pass") }
                    Button(
                        onClick = { navController.safeNavigate("visitor_form?visitorId=${visitor.visitorId}") },
                        modifier = Modifier.weight(1f)
                    ) { Text("Edit") }
                }
            }
        }
    }

    // AlertDialog: confirm before cancelling
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel this pass?") },
            text = { Text("The QR code will stop working and ${visitor?.visitorName} won't be able to enter with it.") },
            confirmButton = {
                TextButton(onClick = {
                    VisitorData.cancel(visitorId)
                    showCancelDialog = false
                }) { Text("Cancel Pass") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Keep") }
            }
        )
    }
}
