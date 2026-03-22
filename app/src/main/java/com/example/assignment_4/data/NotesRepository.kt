package com.example.assignment_4.data

import android.content.Context
import android.net.Uri
import com.example.assignment_4.model.CloudNote
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.util.UUID

class NotesRepository(
    private val client: SupabaseClient,
    private val authRepository: AuthDataSource
) : NotesDataSource {

    companion object {
        private const val IMAGE_BUCKET = "note-images"
    }

    override suspend fun getNotesPage(from: Long, to: Long): List<CloudNote> {
        requireLoggedInUser()

        return client
            .from("notes")
            .select {
                order(column = "updated_at", order = Order.DESCENDING)
                range(from..to)
            }
            .decodeList<CloudNote>()
    }

    override suspend fun getNoteById(noteId: String): CloudNote {
        requireLoggedInUser()

        return client
            .from("notes")
            .select {
                filter {
                    eq("id", noteId)
                }
            }
            .decodeList<CloudNote>()
            .firstOrNull() ?: error("Fant ikke notatet")
    }

    override suspend fun createNote(
        context: Context?,
        title: String,
        content: String,
        imageUri: Uri?
    ) {
        val userId = authRepository.currentUserId()
            ?: error("Bruker er ikke logget inn")

        val userEmail = authRepository.currentUserEmail()
            ?: error("Fant ikke brukerens e-post")

        val imageUrl = imageUri?.let {
            val safeContext = context ?: error("Mangler context for bildeopplasting")
            uploadNoteImage(safeContext, it, userId)
        }

        val note = CloudNote(
            title = title.trim(),
            content = content.trim(),
            ownerId = userId,
            ownerEmail = userEmail,
            imageUrl = imageUrl
        )

        client.from("notes").insert(note)
    }

    override suspend fun updateNote(
        context: Context?,
        noteId: String,
        newTitle: String,
        newContent: String,
        imageUri: Uri?
    ) {
        requireLoggedInUser()

        val userId = authRepository.currentUserId()
            ?: error("Bruker er ikke logget inn")

        val imageUrl = imageUri?.let {
            val safeContext = context ?: error("Mangler context for bildeopplasting")
            uploadNoteImage(safeContext, it, userId)
        }

        client.from("notes").update(
            {
                set("title", newTitle.trim())
                set("content", newContent.trim())

                if (imageUrl != null) {
                    set("image_url", imageUrl)
                }
            }
        ) {
            filter {
                eq("id", noteId)
            }
        }
    }

    override suspend fun deleteNote(noteId: String) {
        requireLoggedInUser()

        client.from("notes").delete {
            filter {
                eq("id", noteId)
            }
        }
    }

    private suspend fun uploadNoteImage(
        context: Context,
        uri: Uri,
        userId: String
    ): String {
        NoteImageValidator.validateOrThrow(context, uri)

        val mimeType = NoteImageValidator.getMimeType(context, uri)
            ?: error("Kunne ikke lese bildefilens format.")

        val extension = NoteImageValidator.extensionForMimeType(mimeType)

        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: error("Kunne ikke lese bildefilen.")

        val path = "$userId/${UUID.randomUUID()}.$extension"

        client.storage.from(IMAGE_BUCKET).upload(path, bytes) {
            upsert = false
            contentType = ContentType.parse(mimeType)
        }

        return client.storage.from(IMAGE_BUCKET).publicUrl(path)
    }

    private fun requireLoggedInUser() {
        check(authRepository.isLoggedIn()) {
            "Du må være logget inn for å bruke notater"
        }
    }
}