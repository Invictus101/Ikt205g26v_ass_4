package com.example.assignment_4

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ImageUriFactory {
    fun createTempImageUri(context: Context): Uri {
        val imageDir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File.createTempFile("note_camera_", ".jpg", imageDir)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}