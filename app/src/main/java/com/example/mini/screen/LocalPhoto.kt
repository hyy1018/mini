// #member3
// Shows a photo saved by copyImageToAppStorage. Shows nothing if the file is missing.
package com.example.mini.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.mini.data.loadPhoto

@Composable
fun LocalPhoto(path: String, contentDescription: String?, modifier: Modifier = Modifier) {
    // decoded once per path, not on every recomposition
    val bitmap = remember(path) { loadPhoto(path)?.asImageBitmap() } ?: return
    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(12.dp))
    )
}
