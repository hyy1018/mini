// #member1
// Security verifies a visitor's QR pass and records entry (unique feature, Flowchart 9 entry side).
// Scan with the camera, or type the code printed under the QR if the camera can't read it.
package com.example.mini.screen.member1.module2

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.data.VerifyResult
import com.example.mini.data.VisitorData
import com.example.mini.data.formatDateTime
import com.example.mini.screen.InfoRow
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyVisitorScreen(navController: NavHostController) {
    var code by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<VerifyResult?>(null) }

    fun verify(scanned: String) {
        val guardId = UserSession.currentUserId ?: return
        // a double tap on Verify would re-check the same just-used code and wrongly show
        // "already used" over the green result, so keep the granted result instead
        val last = result
        if (last != null && last.success && last.visitor?.qrCode.equals(scanned.trim(), ignoreCase = true)) return
        result = VisitorData.verifyEntry(scanned, guardId)
    }

    // opens the zxing camera scanner; contents is null if the guard backs out
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { scan ->
        scan.contents?.let {
            code = it
            verify(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Visitor") },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    scanLauncher.launch(
                        ScanOptions()
                            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            .setPrompt("Scan the visitor's QR pass")
                            .setBeepEnabled(true)
                            .setOrientationLocked(false)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Scan QR Code")
            }

            // --- Manual entry fallback ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    "or enter code",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase().trim() },
                label = { Text("Pass Code") },
                placeholder = { Text("e.g. RCMS-1A2B3C4D") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(
                onClick = { verify(code) },
                enabled = code.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Verify Code")
            }

            result?.let { VerifyResultCard(it) }
        }
    }
}

@Composable
private fun VerifyResultCard(result: VerifyResult) {
    val container = if (result.success) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val onContainer = if (result.success) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (result.success) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    if (result.success) "Access Granted" else "Access Denied",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(result.message, style = MaterialTheme.typography.bodyMedium)

            result.visitor?.let { visitor ->
                Spacer(Modifier.height(8.dp))
                InfoRow("Visitor", visitor.visitorName)
                InfoRow("Visiting", ResidentData.unitLabel(visitor.residentId))
                InfoRow("Vehicle", visitor.vehiclePlate.ifBlank { "-" })
                InfoRow("Purpose", visitor.purpose)
                InfoRow("Valid until", formatDateTime(visitor.validUntil))
                if (result.success) {
                    VisitorData.logFor(visitor.visitorId)?.let { InfoRow("Entry time", formatDateTime(it.entryTime)) }
                }
            }
        }
    }
}
