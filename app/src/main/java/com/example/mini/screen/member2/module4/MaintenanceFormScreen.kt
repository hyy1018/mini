// #member2
// Resident submits a maintenance request.
package com.example.mini.screen.member2.module4

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.isCurrentScreenResumed
import com.example.mini.nav.safePopBackStack
import com.example.mini.data.FacilityData
import com.example.mini.data.ResidentData
import com.example.mini.data.UserSession
import com.example.mini.model.Facility
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.DropdownField

@Composable
fun MaintenanceFormScreen(navController: NavHostController) {
    val resident = UserSession.currentResident

    var category by remember { mutableStateOf(FacilityData.maintenanceCategories.first()) }
    var facility by remember { mutableStateOf<Facility?>(null) } // null = my unit / not a shared facility
    // defaults to the resident's own unit, since that's the most common case
    var location by remember { mutableStateOf(resident?.let { "Unit " + ResidentData.unitLabel(it.residentId) } ?: "") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { BackTopBar("Report Repair", navController) }) { innerPadding ->
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
                label = "Category",
                options = FacilityData.maintenanceCategories,
                selected = category,
                onSelected = { category = it },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownField(
                label = "Related facility",
                options = listOf<Facility?>(null) + FacilityData.facilities,
                selected = facility,
                onSelected = {
                    facility = it
                    if (it != null) location = it.location
                },
                optionLabel = { it?.facilityName ?: "None (my unit / common area)" },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it.take(100) },
                label = { Text("Location") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it.take(1000) },
                label = { Text("What needs fixing?") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Priority", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FacilityData.priorities.forEach { p ->
                    FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(p) })
                }
            }

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    // ignore a second tap while the first save is already leaving this page
                    if (!navController.isCurrentScreenResumed()) return@Button
                    if (location.isBlank() || description.isBlank()) {
                        errorMessage = "Please enter the location and describe the problem"
                        return@Button
                    }
                    resident?.let {
                        FacilityData.submitRequest(it.residentId, facility?.facilityId, category, location.trim(), description.trim(), priority)
                    }
                    navController.safePopBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Request")
            }
        }
    }
}
