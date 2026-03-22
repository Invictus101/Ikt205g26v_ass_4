package com.example.assignment_4

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment_4.data.AuthDataSource
import com.example.assignment_4.data.AuthRepository
import com.example.assignment_4.data.NotesDataSource
import com.example.assignment_4.data.NotesRepository
import com.example.assignment_4.data.SupabaseModule
import com.example.assignment_4.model.CloudNote
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

enum class AppScreen {
    NOTES,
    EDIT
}

class AppViewModel(
    private val authRepository: AuthDataSource = AuthRepository(SupabaseModule.client),
    private val notesRepository: NotesDataSource = NotesRepository(
        SupabaseModule.client,
        authRepository
    ),
    observeSessionOnInit: Boolean = true
) : ViewModel() {

    private val pageSize = 5L
    private var nextFrom = 0L

    var isLoggedIn by mutableStateOf(false)
        private set

    var currentScreen by mutableStateOf(AppScreen.NOTES)
        private set

    var notes by mutableStateOf<List<CloudNote>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var isLoadingSelectedNote by mutableStateOf(false)
        private set

    var canLoadMore by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    var selectedNote by mutableStateOf<CloudNote?>(null)
        private set

    init {
        if (observeSessionOnInit) {
            observeSession()
        } else {
            isLoading = false
        }
    }

    fun clearMessage() {
        message = null
    }

    fun startCreatingNote() {
        clearMessage()
        selectedNote = null
        currentScreen = AppScreen.EDIT
    }

    fun goBackToNotes() {
        clearMessage()
        currentScreen = AppScreen.NOTES
    }

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            message = "E-post og passord kan ikke være tomme"
            return
        }

        viewModelScope.launch {
            isLoading = true
            message = null
            try {
                authRepository.signUp(email.trim(), password.trim())
                message = "Konto opprettet. Sjekk e-posten din hvis bekreftelse er aktivert."
            } catch (e: Exception) {
                message = e.message ?: "Kunne ikke opprette konto"
            } finally {
                isLoading = false
            }
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            message = "E-post og passord kan ikke være tomme"
            return
        }

        viewModelScope.launch {
            isLoading = true
            message = null
            try {
                authRepository.signIn(email.trim(), password.trim())
                message = "Innlogging vellykket"
            } catch (e: Exception) {
                message = e.message ?: "Kunne ikke logge inn"
                isLoading = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            isLoading = true
            message = null
            try {
                authRepository.signOut()
                message = "Du er logget ut"
            } catch (e: Exception) {
                message = e.message ?: "Kunne ikke logge ut"
                isLoading = false
            }
        }
    }

    fun loadNotes(reset: Boolean = true) {
        viewModelScope.launch {
            if (reset) {
                isLoading = true
                message = null
            } else {
                isLoadingMore = true
            }

            try {
                val from = if (reset) 0L else nextFrom
                val to = from + pageSize - 1L
                val page = notesRepository.getNotesPage(from, to)

                if (reset) {
                    notes = page
                    nextFrom = pageSize
                } else {
                    notes = notes + page
                    nextFrom += pageSize
                }

                canLoadMore = page.size == pageSize.toInt()
            } catch (e: Exception) {
                message = e.message ?: "Kunne ikke hente notater"
            } finally {
                if (reset) {
                    isLoading = false
                } else {
                    isLoadingMore = false
                }
            }
        }
    }

    fun openNoteForEditing(noteId: String) {
        viewModelScope.launch {
            isLoadingSelectedNote = true
            message = null
            try {
                selectedNote = notesRepository.getNoteById(noteId)
                currentScreen = AppScreen.EDIT
            } catch (e: Exception) {
                message = e.message ?: "Kunne ikke hente notatet"
            } finally {
                isLoadingSelectedNote = false
            }
        }
    }

    fun createNote(
        context: Context?,
        title: String,
        content: String,
        imageUri: Uri?,
        onSuccess: () -> Unit = {}
    ) {
        if (title.isBlank() || content.isBlank()) {
            message = "Tittel og innhold kan ikke være tomme"
            return
        }

        viewModelScope.launch {
            isLoading = true
            message = null
            try {
                notesRepository.createNote(context, title, content, imageUri)
                selectedNote = null
                currentScreen = AppScreen.NOTES
                loadFirstPageAfterMutation()
                message = "Notat lagret"
                onSuccess()
            } catch (e: Exception) {
                message = e.message ?: "Kunne ikke lagre notat"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateNote(
        context: Context?,
        noteId: String,
        title: String,
        content: String,
        imageUri: Uri?,
        onSuccess: () -> Unit = {}
    ) {
        if (title.isBlank() || content.isBlank()) {
            message = "Tittel og innhold kan ikke være tomme"
            return
        }

        viewModelScope.launch {
            isLoading = true
            message = null
            try {
                notesRepository.updateNote(context, noteId, title, content, imageUri)
                selectedNote = null
                currentScreen = AppScreen.NOTES
                loadFirstPageAfterMutation()
                message = "Notat oppdatert"
                onSuccess()
            } catch (e: Exception) {
                message = e.message ?: "Kunne ikke oppdatere notat"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            isLoading = true
            message = null
            try {
                notesRepository.deleteNote(noteId)
                if (selectedNote?.id == noteId) {
                    selectedNote = null
                }
                loadFirstPageAfterMutation()
                message = "Notat slettet"
            } catch (e: Exception) {
                message = e.message ?: "Kunne ikke slette notat"
            } finally {
                isLoading = false
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.sessionStatus().collect { status ->
                when (status) {
                    SessionStatus.Initializing -> {
                        isLoading = true
                    }

                    is SessionStatus.Authenticated -> {
                        isLoggedIn = true
                        currentScreen = AppScreen.NOTES
                        selectedNote = null
                        loadNotes(reset = true)
                    }

                    is SessionStatus.NotAuthenticated,
                    is SessionStatus.RefreshFailure -> {
                        isLoggedIn = false
                        currentScreen = AppScreen.NOTES
                        notes = emptyList()
                        selectedNote = null
                        nextFrom = 0L
                        canLoadMore = false
                        isLoading = false
                        isLoadingMore = false
                        isLoadingSelectedNote = false
                    }
                }
            }
        }
    }

    private suspend fun loadFirstPageAfterMutation() {
        val firstPage = notesRepository.getNotesPage(0L, pageSize - 1L)
        notes = firstPage
        nextFrom = pageSize
        canLoadMore = firstPage.size == pageSize.toInt()
    }
}