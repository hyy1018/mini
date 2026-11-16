// #member3
// Staff manages the emergency contact numbers residents see in the Emergency tab.
package com.example.mini.screen.member3.module6

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mini.nav.safeNavigate
import com.example.mini.data.CommunityData
import com.example.mini.screen.BackTopBar
import com.example.mini.screen.EmptyState

@Composable
fun EmergencyContactsScreen(navController: NavHostController) {
    Scaffold(
        topBar = { BackTopBar("Emergency Contacts", navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.safeNavigate("emergency_contact_form") }) {
                Icon(Icons.Filled.Add, contentDescription = "Add contact")
            }
        }
    ) { innerPadding ->
        if (CommunityData.contacts.isEmpty()) {
            EmptyState("No emergency contacts", Modifier.padding(innerPadding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CommunityData.contacts, key = { it.contactId }) { contact ->
                EmergencyContactRow(contact) {
                    IconButton(onClick = { navController.safeNavigate("emergency_contact_form?id=${contact.contactId}") }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                }
            }
        }
    }
}
