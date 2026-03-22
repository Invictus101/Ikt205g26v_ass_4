package com.example.assignment_4.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap

object NoteImageValidator {

    private const val MAX_SIZE_BYTES = 15L * 1024L * 1024L

    private val allowedMimeTypes = setOf(
        "image/jpeg",
        "image/png",
        "image/webp"
    )

    fun validateOrThrow(context: Context, uri: Uri) {
        val mimeType = getMimeType(context, uri)
            ?: error("Kun JPG, PNG eller WebP er tillatt.")

        if (mimeType !in allowedMimeTypes) {
            error("Kun JPG, PNG eller WebP er tillatt.")
        }

        val size = getFileSize(context, uri)
            ?: error("Kunne ikke lese filstørrelsen.")

        if (size > MAX_SIZE_BYTES) {
            error("Bildet er for stort. Maks størrelse er 15 MB.")
        }
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        val resolverMime = context.contentResolver.getType(uri)?.lowercase()
        if (resolverMime != null) return resolverMime

        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            ?.lowercase()
            ?.trim()

        return if (extension.isNullOrBlank()) {
            null
        } else {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)?.lowercase()
        }
    }

    fun extensionForMimeType(mimeType: String): String {
        return when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> error("Ukjent bildeformat.")
        }
    }

    private fun getFileSize(context: Context, uri: Uri): Long? {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            if (pfd.statSize > 0) return pfd.statSize
        }

        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                    return cursor.getLong(sizeIndex)
                }
            }
        }

        return null
    }
}