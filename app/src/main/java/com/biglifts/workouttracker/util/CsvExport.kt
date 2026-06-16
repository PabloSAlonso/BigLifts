package com.biglifts.workouttracker.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.biglifts.workouttracker.data.api.ApiClient
import com.biglifts.workouttracker.data.models.BodyMeasurement
import com.biglifts.workouttracker.data.models.WorkoutSession
import java.io.File
import java.io.FileWriter

object CsvExport {

    suspend fun exportWorkouts(context: Context, apiClient: ApiClient): Uri? {
        return try {
            val response = apiClient.getWorkouts(limit = 1000)
            val workouts = response.data

            val file = File(context.cacheDir, "workouts_export.csv")
            FileWriter(file).use { writer ->
                writer.appendLine("Date,Name,Duration(min),Total Volume,Total Sets,Avg RPE,Notes")

                workouts.forEach { workout ->
                    writer.appendLine(
                        listOf(
                            workout.completedAt ?: workout.startedAt ?: "",
                            escapeCsv(workout.name),
                            workout.durationMinutes?.toString() ?: "",
                            workout.totalVolume?.let { String.format("%.1f", it) } ?: "",
                            workout.totalSets?.toString() ?: "",
                            workout.avgRpe?.let { String.format("%.1f", it) } ?: "",
                            escapeCsv(workout.notes ?: "")
                        ).joinToString(",")
                    )
                }
            }

            getFileUri(context, file, "workouts_export.csv")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun exportMeasurements(context: Context, apiClient: ApiClient): Uri? {
        return try {
            val measurements = apiClient.getBodyMeasurements()

            val file = File(context.cacheDir, "measurements_export.csv")
            FileWriter(file).use { writer ->
                writer.appendLine("Date,Weight(kg),Body Fat(%),Chest,WAIST,Hips,Arms,Thighs,Calves,Neck,Shoulders,Notes")

                measurements.forEach { m ->
                    writer.appendLine(
                        listOf(
                            m.measuredAt ?: "",
                            m.weight?.toString() ?: "",
                            m.bodyFatPercentage?.let { String.format("%.1f", it) } ?: "",
                            m.chest?.toString() ?: "",
                            m.waist?.toString() ?: "",
                            m.hips?.toString() ?: "",
                            m.arms?.toString() ?: "",
                            m.thighs?.toString() ?: "",
                            m.calves?.toString() ?: "",
                            m.neck?.toString() ?: "",
                            m.shoulders?.toString() ?: "",
                            escapeCsv(m.notes ?: "")
                        ).joinToString(",")
                    )
                }
            }

            getFileUri(context, file, "measurements_export.csv")
        } catch (e: Exception) {
            null
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun getFileUri(context: Context, file: File, fileName: String): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun shareFile(context: Context, uri: Uri, fileName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "BigLifts - $fileName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share CSV"))
    }
}
