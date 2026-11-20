// #member1
// Profile photo with "tap to change" and "Remove photo", shared by Edit Profile and the
// Staff / Security Me page. The picked photo is copied into app storage (same reason as
// complaint photos: the picker's link stops working later), and only shown here until the
// caller saves it.
package com.example.mini.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mini.data.copyImageToAppStorage
import kotlinx.coroutines.launch

/**
 * @param photoPath the photo currently shown (may be an unsaved pick), or null for the initial
 * @param onPhotoChange called with the new stored path, or null when the photo is removed
 */
@Composable
fun AvatarPicker(userId: Int, photoPath: String?, onPhotoChange: (String?) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            scope.launch { copyImageToAppStorage(context, uri)?.let(onPhotoChange) }
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        ) {
            // shows only what the form holds, so "Remove photo" goes back to the initial before saving
            UserAvatar(userId, size = 96.dp, photoPathOverride = photoPath, showSavedPhoto = false)
            // small camera badge so it's obvious the photo can be tapped
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.PhotoCamera,
                        contentDescription = "Change photo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Text("Tap the photo to change it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (photoPath != null) {
            TextButton(onClick = { onPhotoChange(null) }) { Text("Remove photo") }
        }
    }
}
