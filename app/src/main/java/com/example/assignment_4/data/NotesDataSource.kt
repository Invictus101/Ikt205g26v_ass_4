package com.example.assignment_4.data

import android.content.Context
import android.net.Uri
import com.example.assignment_4.model.CloudNote

interface NotesDataSource {
    suspend fun getNotesPage(from: Long, to: Long): List<CloudNote>
    suspend fun getNoteById(noteId: String): CloudNote
    suspend fun createNote(
        context: Context?,
        title: String,
        content: String,
        imageUri: Uri? = null
    )

    suspend fun updateNote(
        context: Context?,
        noteId: String,
        newTitle: String,
        newContent: String,
        imageUri: Uri? = null
    )

    suspend fun deleteNote(noteId: String)
}