package com.biglifts.workouttracker.data.repositories

import com.biglifts.workouttracker.data.models.*
import com.biglifts.workouttracker.data.supabase.SupabaseClient

class WorkoutTemplateRepository {

    private val client = SupabaseClient.client

    suspend fun getTemplates(userId: String): List<WorkoutTemplate> {
        return client.from("workout_templates")
            .select(columns = Columns.list("*")) {
                filter { eq("user_id", userId) }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<WorkoutTemplate>()
    }

    suspend fun getTemplateById(id: String): WorkoutTemplate? {
        return client.from("workout_templates")
            .select(columns = Columns.list("*")) {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<WorkoutTemplate>()
    }

    suspend fun createTemplate(template: WorkoutTemplate): WorkoutTemplate {
        return client.from("workout_templates")
            .insert(template) {}
            .decodeSingle<WorkoutTemplate>()
    }

    suspend fun updateTemplate(template: WorkoutTemplate): WorkoutTemplate {
        return client.from("workout_templates")
            .update(template) {
                filter { eq("id", template.id ?: "") }
            }
            .decodeSingle<WorkoutTemplate>()
    }

    suspend fun deleteTemplate(id: String) {
        client.from("workout_templates")
            .delete {
                filter { eq("id", id) }
            }
    }
}