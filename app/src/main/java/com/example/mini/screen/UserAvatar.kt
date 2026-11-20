// #member1
// Round profile picture: the user's photo if they set one, otherwise the first letter of their name.
// Used on profiles, the home greeting, posts, comments and chat.
package com.example.mini.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mini.data.ResidentData

@Composable
fun UserAvatar(
    userId: Int,
    size: Dp = 40.dp,
    // lets a screen preview a newly picked photo before it's saved
    photoPathOverride: String? = null,
    // false = ignore the saved photo (the photo picker previews only what the form holds)
    showSavedPhoto: Boolean = true,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    content: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    val user = ResidentData.getUser(userId)
    val path = photoPathOverride ?: if (showSavedPhoto) user?.avatarPath else null

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        if (path != null) {
            LocalPhoto(path, "Profile photo", Modifier.fillMaxSize())
        } else {
            Text(
                (user?.name ?: "?").take(1).uppercase(),
                color = content,
                fontSize = (size.value * 0.42f).sp
            )
        }
    }
}
