package com.biglifts.workouttracker.ui.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.biglifts.workouttracker.data.models.Exercise
import com.biglifts.workouttracker.data.models.WorkoutSet
import com.biglifts.workouttracker.databinding.ItemActiveExerciseBinding

class ActiveExerciseAdapter(
    private val onAddSet: (String) -> Unit,
    private val onSetUpdated: (String, WorkoutSet) -> Unit,
    private val onRemoveExercise: (String) -> Unit
) : ListAdapter<ActiveExercise, ActiveExerciseAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    private val setsAdapters = mutableMapOf<String, SetAdapter>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemActiveExerciseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateSets(exerciseId: String, sets: List<WorkoutSet>) {
        setsAdapters[exerciseId]?.submitList(sets)
    }

    fun addPreviousSets(exerciseId: String, sets: List<WorkoutSet>) {
        setsAdapters[exerciseId]?.setPreviousSets(sets)
    }

    inner class ExerciseViewHolder(
        private val binding: ItemActiveExerciseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activeExercise: ActiveExercise) {
            binding.tvExerciseName.text = activeExercise.exercise.name
            binding.tvMuscleGroup.text = activeExercise.exercise.muscle_group
                ?: activeExercise.exercise.category

            // Previous best indicator
            if (activeExercise.previousBestWeight != null) {
                binding.tvPreviousBest.visibility = View.VISIBLE
                binding.tvPreviousBest.text = "PR: ${activeExercise.previousBestWeight}kg x ${activeExercise.previousBestReps}"
            } else {
                binding.tvPreviousBest.visibility = View.GONE
            }

            // Sets recycler view
            val setAdapter = SetAdapter(
                exerciseId = activeExercise.exerciseId,
                previousSets = activeExercise.previousSets,
                onSetUpdated = { set -> onSetUpdated(activeExercise.exerciseId, set) }
            )
            setsAdapters[activeExercise.exerciseId] = setAdapter

            binding.rvSets.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rvSets.adapter = setAdapter
            binding.rvSets.isNestedScrollingEnabled = false

            binding.btnAddSet.setOnClickListener {
                onAddSet(activeExercise.exerciseId)
            }

            binding.ivRemove.setOnClickListener {
                onRemoveExercise(activeExercise.exerciseId)
            }
        }
    }

    data class ActiveExercise(
        val exerciseId: String,
        val exercise: Exercise,
        val previousSets: List<WorkoutSet> = emptyList(),
        val previousBestWeight: Double? = null,
        val previousBestReps: Int? = null,
        val orderIndex: Int = 0
    )

    class ExerciseDiffCallback : DiffUtil.ItemCallback<ActiveExercise>() {
        override fun areItemsTheSame(oldItem: ActiveExercise, newItem: ActiveExercise): Boolean {
            return oldItem.exerciseId == newItem.exerciseId
        }
        override fun areContentsTheSame(oldItem: ActiveExercise, newItem: ActiveExercise): Boolean {
            return oldItem == newItem
        }
    }
}