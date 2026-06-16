package com.biglifts.workouttracker.data.repositories

import com.biglifts.workouttracker.data.models.Exercise
import com.biglifts.workouttracker.data.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class ExerciseRepository {

    private val client = SupabaseClient.client

    suspend fun getExercises(): List<Exercise> {
        return client.from("exercises")
            .select(columns = Columns.list("*")) {
                isNull("user_id")
                order("name")
            }
            .decodeList<Exercise>()
    }

    suspend fun getCustomExercises(userId: String): List<Exercise> {
        return client.from("exercises")
            .select(columns = Columns.list("*")) {
                filter { eq("user_id", userId) }
                order("name")
            }
            .decodeList<Exercise>()
    }

    suspend fun searchExercises(query: String): List<Exercise> {
        return client.from("exercises")
            .select(columns = Columns.list("*")) {
                filter { like("name", "%$query%") }
                order("name")
            }
            .decodeList<Exercise>()
    }

    suspend fun getExercisesByCategory(category: String): List<Exercise> {
        return client.from("exercises")
            .select(columns = Columns.list("*")) {
                filter { eq("category", category) }
                order("name")
            }
            .decodeList<Exercise>()
    }

    suspend fun createExercise(exercise: Exercise): Exercise {
        return client.from("exercises")
            .insert(exercise) {}
            .decodeSingle<Exercise>()
    }

    suspend fun deleteExercise(id: String) {
        client.from("exercises")
            .delete {
                filter { eq("id", id) }
            }
    }
}