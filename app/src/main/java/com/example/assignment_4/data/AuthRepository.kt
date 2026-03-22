package com.example.assignment_4.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(
    private val client: SupabaseClient
) : AuthDataSource {

    override suspend fun signUp(email: String, password: String) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signOut() {
        client.auth.signOut()
    }

    override fun sessionStatus(): StateFlow<SessionStatus> {
        return client.auth.sessionStatus
    }

    override fun isLoggedIn(): Boolean {
        return client.auth.currentSessionOrNull() != null
    }

    override fun currentUserId(): String? {
        return client.auth.currentSessionOrNull()?.user?.id
    }

    override fun currentUserEmail(): String? {
        return client.auth.currentSessionOrNull()?.user?.email
    }
}