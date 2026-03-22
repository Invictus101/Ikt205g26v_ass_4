package com.example.assignment_4

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.assignment_4.model.CloudNote

@Composable
fun AppRoot(
    isLoggedIn: Boolean,
    currentScreen: AppScreen,
    notes: List<CloudNote>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    isLoadingSelectedNote: Boolean,
    canLoadMore: Boolean,
    message: String?,
    selectedNote: CloudNote?,
    onLogin: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (CloudNote) -> Unit,
    onDeleteClick: (CloudNote) -> Unit,
    onLoadMoreClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSaveNew: (String, String, android.net.Uri?) -> Unit,
    onUpdate: (String, String, String, android.net.Uri?) -> Unit,
    onBack: () -> Unit
) {
    if (!isLoggedIn) {
        AuthScreen(
            isLoading = isLoading,
            message = message,
            onLogin = onLogin,
            onSignUp = onSignUp
        )
        return
    }

    when (currentScreen) {
        AppScreen.NOTES -> {
            Box(modifier = Modifier.fillMaxSize()) {
                NotesScreen(
                    notes = notes,
                    isLoading = isLoading,
                    isLoadingMore = isLoadingMore,
                    canLoadMore = canLoadMore,
                    message = message,
                    onAddClick = onAddClick,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    onLoadMoreClick = onLoadMoreClick,
                    onLogoutClick = onLogoutClick
                )

                if (isLoadingSelectedNote) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        AppScreen.EDIT -> {
            EditNoteScreen(
                note = selectedNote,
                isLoading = isLoading,
                message = message,
                onSaveNew = onSaveNew,
                onUpdate = onUpdate,
                onBack = onBack
            )
        }
    }
}