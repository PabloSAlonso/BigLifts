package com.biglifts.workouttracker.data.supabase

import android.content.Context
import io.github.jan_tennert.supabase_kt.Supabase
import io.github.jan_tennert.supabase_kt.SupabaseClient

object SupabaseManager {
    private const val SUPABASE_URL = "https://pacwbkqmuripxmfhwnek.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBhY3diaiBxbXVyaXB4bWZod25layIsInJvbGUiOiJhbm9uIiwiaWF0IjoxNzE4NjU2MDAwLCJleHAiOjIwMzQyMzIwMDB9.example-key"

    @Suppress("NOTHING_TO_INLINE")
    private var supabaseClient: SupabaseClient? = null

    fun initialize(context: Context) {
        if (supabaseClient == null) {
            supabaseClient = Supabase.create(
                url = SUPABASE_URL,
                key = SUPABASE_ANON_KEY,
                context = context
            ) {
                install(Postgrest)
                install(Realtime)
                install(Storage)
                install(Auth)
            }
        }
    }

    val client: SupabaseClient
        get() = supabaseClient ?: throw IllegalStateException("Supabase not initialized. Call initialize() first.")
}