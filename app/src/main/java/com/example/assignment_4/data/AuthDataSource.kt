package com.example.assignment_4.data

import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

interface AuthDataSource {
    suspend fun signUp(email: String, password: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
    fun sessionStatus(): StateFlow<SessionStatus>
    fun isLoggedIn(): Boolean
    fun currentUserId(): String?
    fun currentUserEmail(): String?
}