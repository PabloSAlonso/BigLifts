package com.biglifts.workouttracker.data.repositories

import com.biglifts.workouttracker.data.models.*
import com.biglifts.workouttracker.data.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class WorkoutSessionRepository {

    private val client = SupabaseClient.client

    suspend fun getSessions(userId: String): List<WorkoutSession> {
        return client.from("workout_sessions")
            .select(columns = Columns.list("*")) {
                filter { eq("user_id", userId) }
                order("started_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<WorkoutSession>()
    }

    suspend fun getSessionById(id: String): WorkoutSession? {
        return client.from("workout_sessions")
            .select(columns = Columns.list("*")) {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<WorkoutSession>()
    }

    suspend fun createSession(session: WorkoutSession): WorkoutSession {
        return client.from("workout_sessions")
            .insert(session) {}
            .decodeSingle<WorkoutSession>()
    }

    suspend fun updateSession(session: WorkoutSession): WorkoutSession {
        return client.from("workout_sessions")
            .update(session) {
                filter { eq("id", session.id ?: "") }
            }
            .decodeSingle<WorkoutSession>()
    }

    suspend fun deleteSession(id: String) {
        client.from("workout_sessions")
            .delete {
                filter { eq("id", id) }
            }
    }

    suspend fun getSessionExercises(sessionId: String): List<SessionExercise> {
        return client.from("session_exercises")
            .select(columns = Columns.list("*")) {
                filter { eq("session_id", sessionId) }
                order("order_index")
            }
            .decodeList<SessionExercise>()
    }

    suspend fun addExerciseToSession(sessionExercise: SessionExercise): SessionExercise {
        return client.from("session_exercises")
            .insert(sessionExercise) {}
            .decodeSingle<SessionExercise>()
    }

    suspend fun getSets(sessionExerciseId: String): List<WorkoutSet> {
        return client.from("sets")
            .select(columns = Columns.list("*")) {
                filter { eq("session_exercise_id", sessionExerciseId) }
                order("set_number")
            }
            .decodeList<WorkoutSet>()
    }

    suspend fun addSet(set: WorkoutSet): WorkoutSet {
        return client.from("sets")
            .insert(set) {}
            .decodeSingle<WorkoutSet>()
    }

    suspend fun updateSet(set: WorkoutSet): WorkoutSet {
        return client.from("sets")
            .update(set) {
                filter { eq("id", set.id ?: "") }
            }
            .decodeSingle<WorkoutSet>()
    }

    suspend fun deleteSet(id: String) {
        client.from("sets")
            .delete {
                filter { eq("id", id) }
            }
    }

    suspend fun getPersonalRecords(userId: String, exerciseId: String): List<PersonalRecord> {
        return client.from("personal_records")
            .select(columns = Columns.list("*")) {
                filter {
                    eq("user_id", userId)
                    eq("exercise_id", exerciseId)
                }
                order("achieved_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<PersonalRecord>()
    }

    suspend fun addPersonalRecord(pr: PersonalRecord): PersonalRecord {
        return client.from("personal_records")
            .insert(pr) {}
            .decodeSingle<PersonalRecord>()
    }
}