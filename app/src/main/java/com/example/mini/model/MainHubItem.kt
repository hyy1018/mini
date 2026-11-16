// #member1
// One service icon on the Resident Home grid.
package com.example.mini.model

import androidx.compose.ui.graphics.vector.ImageVector

data class MainHubItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val route: String
)
