package com.example.assignment_4

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import com.example.assignment_4.ui.theme.Assignment_3Theme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createChannel(this)

        setContent {
            Assignment_3Theme {
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }

                LaunchedEffect(viewModel.isLoggedIn) {
                    if (
                        viewModel.isLoggedIn &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                AppRoot(
                    isLoggedIn = viewModel.isLoggedIn,
                    currentScreen = viewModel.currentScreen,
                    notes = viewModel.notes,
                    isLoading = viewModel.isLoading,
                    isLoadingMore = viewModel.isLoadingMore,
                    isLoadingSelectedNote = viewModel.isLoadingSelectedNote,
                    canLoadMore = viewModel.canLoadMore,
                    message = viewModel.message,
                    selectedNote = viewModel.selectedNote,
                    onLogin = { email, password ->
                        viewModel.signIn(email, password)
                    },
                    onSignUp = { email, password ->
                        viewModel.signUp(email, password)
                    },
                    onAddClick = {
                        viewModel.startCreatingNote()
                    },
                    onEditClick = { note ->
                        note.id?.let(viewModel::openNoteForEditing)
                    },
                    onDeleteClick = { note ->
                        note.id?.let(viewModel::deleteNote)
                    },
                    onLoadMoreClick = {
                        viewModel.loadNotes(reset = false)
                    },
                    onLogoutClick = {
                        viewModel.signOut()
                    },
                    onSaveNew = { title, content, imageUri ->
                        viewModel.createNote(
                            context = applicationContext,
                            title = title,
                            content = content,
                            imageUri = imageUri
                        ) {
                            NotificationHelper.showNewNoteNotification(
                                applicationContext,
                                title
                            )
                        }
                    },
                    onUpdate = { id, title, content, imageUri ->
                        viewModel.updateNote(
                            context = applicationContext,
                            noteId = id,
                            title = title,
                            content = content,
                            imageUri = imageUri
                        )
                    },
                    onBack = {
                        viewModel.goBackToNotes()
                    }
                )
            }
        }
    }
}