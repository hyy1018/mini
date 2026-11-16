// #member3
// Copies a picked photo into the app's own files folder and returns its file path.
// The photo picker only grants short-lived access to its content URI, so keeping a private
// copy is what lets complaint / post photos still show later.
package com.example.mini.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// null if the picked file couldn't be read
suspend fun copyImageToAppStorage(context: Context, source: Uri): String? =
    withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, "photos").apply { mkdirs() }
            val dest = File(dir, "photo_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(source)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null
            dest.absolutePath
        } catch (e: Exception) {
            null
        }
    }

// Loads a stored photo scaled down to about maxSize px, so big camera photos don't use up memory
fun loadPhoto(path: String, maxSize: Int = 1024): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0) return null
    var sample = 1
    while (bounds.outWidth / sample > maxSize || bounds.outHeight / sample > maxSize) sample *= 2
    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
}
