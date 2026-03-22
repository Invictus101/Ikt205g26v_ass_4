package com.example.assignment_4

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthGuardUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun protected_content_is_hidden_when_user_is_logged_out() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText("Logg inn")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        val loginNodes = composeTestRule
            .onAllNodesWithText("Logg inn")
            .fetchSemanticsNodes()

        val newNoteNodes = composeTestRule
            .onAllNodesWithText("Nytt notat")
            .fetchSemanticsNodes()

        assertTrue(loginNodes.isNotEmpty())
        assertTrue(newNoteNodes.isEmpty())
    }
}