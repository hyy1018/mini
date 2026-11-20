// #member1
// App colours (still the Android Studio template palette for now).
package com.example.mini.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Fixed status colours. Not taken from the theme on purpose: dynamic colour changes the theme
// with the wallpaper, but warnings must look the same on every phone.
val AlertOrange = Color(0xFFE65100)          // emergency just reported -- urgent, needs action
val OnAlertOrange = Color(0xFFFFFFFF)
val MaintenanceAmber = Color(0xFFFFE0B2)     // facility under maintenance -- temporarily unavailable
val OnMaintenanceAmber = Color(0xFF5D3200)