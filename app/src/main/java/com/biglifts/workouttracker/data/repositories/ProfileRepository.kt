package com.biglifts.workouttracker.data.repositories

import com.biglifts.workouttracker.data.models.Profile
import com.biglifts.workouttracker.data.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class ProfileRepository {

    private val client = SupabaseClient.client

    suspend fun getProfile(userId: String): Profile? {
        return client.from("profiles")
            .select(columns = Columns.list("*")) {
                filter { eq("id", userId) }
            }
            .decodeSingleOrNull<Profile>()
    }

    suspend fun updateProfile(profile: Profile): Profile {
        return client.from("profiles")
            .update(profile) {
                filter { eq("id", profile.id) }
            }
            .decodeSingle<Profile>()
    }

    suspend fun createProfile(profile: Profile): Profile {
        return client.from("profiles")
            .insert(profile) {}
            .decodeSingle<Profile>()
    }
}