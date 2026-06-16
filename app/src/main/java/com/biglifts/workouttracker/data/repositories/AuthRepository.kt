package com.biglifts.workouttracker.data.repositories

import com.biglifts.workouttracker.data.models.Profile
import com.biglifts.workouttracker.data.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class AuthRepository {

    private val client = SupabaseClient.client

    val currentUser get() = client.gotrue.currentUserOrNull()

    suspend fun signUp(email: String, password: String, username: String) {
        client.gotrue.signUpWith(Email) {
            this.email = email
            this.password = password
            data = mapOf(
                "username" to username,
                "full_name" to username
            )
        }
    }

    suspend fun signIn(email: String, password: String) {
        client.gotrue.loginWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        client.gotrue.logout()
    }

    suspend fun resetPassword(email: String) {
        client.gotrue.resetPasswordForEmail(email)
    }

    suspend fun getProfile(userId: String): Profile? {
        return client.from("profiles")
            .select(columns = Columns.list("*")) {
                filter { eq("id", userId) }
            }
            .decodeSingleOrNull<Profile>()
    }
}