package com.example.assignment_4

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.assignment_4.model.CloudNote
import org.junit.Rule
import org.junit.Test

class AppUiTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun protected_content_shows_login_screen_when_user_is_not_logged_in() {
        composeTestRule.setContent {
            AppRoot(
                isLoggedIn = false,
                currentScreen = AppScreen.NOTES,
                notes = emptyList<CloudNote>(),
                isLoading = false,
                isLoadingMore = false,
                isLoadingSelectedNote = false,
                canLoadMore = false,
                message = null,
                selectedNote = null,
                onLogin = { _, _ -> },
                onSignUp = { _, _ -> },
                onAddClick = {},
                onEditClick = {},
                onDeleteClick = {},
                onLoadMoreClick = {},
                onLogoutClick = {},
                onSaveNew = { _, _, _ -> },
                onUpdate = { _, _, _, _ -> },
                onBack = {}
            )
        }

        composeTestRule
            .onNodeWithText("Logg inn eller opprett konto for å bruke appen")
            .assertIsDisplayed()
    }
}