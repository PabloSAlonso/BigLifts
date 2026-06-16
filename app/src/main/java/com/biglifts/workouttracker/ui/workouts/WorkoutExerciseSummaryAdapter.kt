package com.biglifts.workouttracker.ui.workouts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.biglifts.workouttracker.R
import com.biglifts.workouttracker.data.api.SessionExerciseWithExercise
import com.biglifts.workouttracker.databinding.ItemWorkoutExerciseSummaryBinding

class WorkoutExerciseSummaryAdapter :
    ListAdapter<SessionExerciseWithExercise, WorkoutExerciseSummaryAdapter.ExerciseViewHolder>(
        ExerciseDiffCallback()
    ) {

    private val setsMap = mutableMapOf<String, List<SeriesSummary>>()

    fun submitSets(exerciseId: String, sets: List<SeriesSummary>) {
        setsMap[exerciseId] = sets
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemWorkoutExerciseSummaryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExerciseViewHolder(
        private val binding: ItemWorkoutExerciseSummaryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: SessionExerciseWithExercise) {
            val exerciseData = exercise.exercises
            binding.tvExerciseName.text = exerciseData?.name ?: itemView.context.getString(R.string.unknown_exercise)
            binding.tvCategory.text = exerciseData?.category ?: ""
            binding.tvEquipment.text = exerciseData?.equipment ?: ""

            // Target info
            val targetReps = when {
                exercise.targetRepsMin != null && exercise.targetRepsMax != null ->
                    "${exercise.targetRepsMin}-${exercise.targetRepsMax}"
                exercise.targetRepsMin != null -> "${exercise.targetRepsMin}"
                else -> ""
            }
            binding.tvTarget.text = buildString {
                exercise.targetSets?.let { append("${it}x") }
                if (targetReps.isNotEmpty()) append(targetReps)
                exercise.targetRpe?.let { append(" @RPE ${String.format("%.0f", it)}") }
            }
            binding.tvTarget.isVisible = binding.tvTarget.text.isNotEmpty()
        }
    }

    data class SeriesSummary(
        val setNumber: Int,
        val weight: Double?,
        val reps: Int?,
        val rir: Int?,
        val rpe: Double?,
        val completed: Boolean
    )

    class ExerciseDiffCallback : DiffUtil.ItemCallback<SessionExerciseWithExercise>() {
        override fun areItemsTheSame(
            oldItem: SessionExerciseWithExercise,
            newItem: SessionExerciseWithExercise
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: SessionExerciseWithExercise,
            newItem: SessionExerciseWithExercise
        ): Boolean = oldItem == newItem
    }
}