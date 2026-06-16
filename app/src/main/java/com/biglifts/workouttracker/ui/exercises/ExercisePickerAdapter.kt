package com.biglifts.workouttracker.ui.exercises

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.biglifts.workouttracker.data.models.Exercise
import com.biglifts.workouttracker.databinding.ItemExercisePickerBinding

class ExercisePickerAdapter(
    private val onClick: (Exercise) -> Unit
) : ListAdapter<Exercise, ExercisePickerAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExercisePickerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExerciseViewHolder(
        private val binding: ItemExercisePickerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: Exercise) {
            binding.tvName.text = exercise.name
            binding.tvCategory.text = exercise.category.replace("_", " ").uppercase()
            binding.tvMuscleGroup.text = exercise.muscle_group ?: ""
            binding.tvEquipment.text = exercise.equipment?.replace("_", " ")?.uppercase() ?: ""

            // Difficulty indicator
            val difficultyColor = when (exercise.difficulty) {
                "beginner" -> com.biglifts.workouttracker.R.color.success
                "intermediate" -> com.biglifts.workouttracker.R.color.warning
                "advanced" -> com.biglifts.workouttracker.R.color.error
                else -> com.biglifts.workouttracker.R.color.text_secondary
            }

            binding.root.setOnClickListener { onClick(exercise) }
        }
    }

    class ExerciseDiffCallback : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem == newItem
        }
    }
}