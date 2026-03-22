package com.example.assignment_4

import android.content.Context
import android.net.Uri
import com.example.assignment_4.data.AuthDataSource
import com.example.assignment_4.data.NotesDataSource
import com.example.assignment_4.model.CloudNote
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTests {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun create_note_navigates_back_to_notes() = runTest {
        val fakeAuth = FakeAuthDataSource()
        val fakeNotes = FakeNotesDataSource()
        val viewModel = AppViewModel(
            authRepository = fakeAuth,
            notesRepository = fakeNotes,
            observeSessionOnInit = false
        )

        viewModel.startCreatingNote()
        assertEquals(AppScreen.EDIT, viewModel.currentScreen)

        viewModel.createNote(
            context = null,
            title = "Testnotat",
            content = "Dette er innhold",
            imageUri = null
        )

        advanceUntilIdle()

        assertTrue(fakeNotes.createCalled)
        assertEquals(AppScreen.NOTES, viewModel.currentScreen)
        assertEquals("Notat lagret", viewModel.message)
    }

    @Test
    fun loader_is_shown_while_fetching_note() = runTest {
        val gate = CompletableDeferred<CloudNote>()
        val fakeAuth = FakeAuthDataSource()
        val fakeNotes = DelayedNoteDataSource(gate)
        val viewModel = AppViewModel(
            authRepository = fakeAuth,
            notesRepository = fakeNotes,
            observeSessionOnInit = false
        )

        viewModel.openNoteForEditing("note-1")

        assertTrue(viewModel.isLoadingSelectedNote)

        gate.complete(
            CloudNote(
                id = "note-1",
                title = "Møte",
                content = "Husk demo",
                ownerId = "user-1",
                ownerEmail = "test@example.com"
            )
        )

        advanceUntilIdle()

        assertEquals(false, viewModel.isLoadingSelectedNote)
        assertEquals(AppScreen.EDIT, viewModel.currentScreen)
        assertEquals("Møte", viewModel.selectedNote?.title)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeAuthDataSource : AuthDataSource {
    private val status = MutableStateFlow<SessionStatus>(SessionStatus.Initializing)

    override suspend fun signUp(email: String, password: String) = Unit
    override suspend fun signIn(email: String, password: String) = Unit
    override suspend fun signOut() = Unit
    override fun sessionStatus() = status
    override fun isLoggedIn() = true
    override fun currentUserId() = "user-1"
    override fun currentUserEmail() = "test@example.com"
}

private class FakeNotesDataSource : NotesDataSource {
    var createCalled = false

    override suspend fun getNotesPage(from: Long, to: Long): List<CloudNote> = emptyList()

    override suspend fun getNoteById(noteId: String): CloudNote {
        return CloudNote(
            id = noteId,
            title = "Test",
            content = "Innhold",
            ownerId = "user-1",
            ownerEmail = "test@example.com"
        )
    }

    override suspend fun createNote(
        context: Context?,
        title: String,
        content: String,
        imageUri: Uri?
    ) {
        createCalled = true
    }

    override suspend fun updateNote(
        context: Context?,
        noteId: String,
        newTitle: String,
        newContent: String,
        imageUri: Uri?
    ) = Unit

    override suspend fun deleteNote(noteId: String) = Unit
}

private class DelayedNoteDataSource(
    private val gate: CompletableDeferred<CloudNote>
) : NotesDataSource {

    override suspend fun getNotesPage(from: Long, to: Long): List<CloudNote> = emptyList()

    override suspend fun getNoteById(noteId: String): CloudNote {
        return gate.await()
    }

    override suspend fun createNote(
        context: Context?,
        title: String,
        content: String,
        imageUri: Uri?
    ) = Unit

    override suspend fun updateNote(
        context: Context?,
        noteId: String,
        newTitle: String,
        newContent: String,
        imageUri: Uri?
    ) = Unit

    override suspend fun deleteNote(noteId: String) = Unit
}