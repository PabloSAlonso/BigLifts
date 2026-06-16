package com.biglifts.workouttracker.ui.workouts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.biglifts.workouttracker.data.models.WorkoutSession
import com.biglifts.workouttracker.databinding.ItemWorkoutHistoryBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class WorkoutHistoryAdapter(
    private val onClick: (WorkoutSession) -> Unit,
    private val onLongClick: (WorkoutSession) -> Unit
) : ListAdapter<WorkoutSession, WorkoutHistoryAdapter.WorkoutViewHolder>(WorkoutDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WorkoutViewHolder(
        private val binding: ItemWorkoutHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(session: WorkoutSession) {
            binding.tvWorkoutName.text = session.name

            // Date
            val startedAt = session.startedAt?.let {
                try { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) } catch (e: Exception) { null }
            }
            binding.tvDate.text = startedAt?.let { date ->
                val now = LocalDateTime.now()
                val daysAgo = ChronoUnit.DAYS.between(date.toLocalDate(), now.toLocalDate())
                when {
                    daysAgo == 0L -> "Today, ${date.format(DateTimeFormatter.ofPattern("h:mm a"))}"
                    daysAgo == 1L -> "Yesterday, ${date.format(DateTimeFormatter.ofPattern("h:mm a"))}"
                    daysAgo < 7L -> "${daysAgo}d ago, ${date.format(DateTimeFormatter.ofPattern("h:mm a"))}"
                    else -> date.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
                }
            } ?: "Unknown date"

            // Duration
            binding.tvDuration.text = session.durationMinutes?.let { "${it}min" } ?: "--"

            // Sets
            binding.tvSets.text = session.totalSets?.let { "${it} sets" } ?: "-- sets"

            // Volume
            binding.tvVolume.text = session.totalVolume?.let {
                if (it >= 1000) String.format("%.1fK kg", it / 1000)
                else String.format("%.0f kg", it)
            } ?: "--"

            // RPE
            binding.tvRpe.text = session.avgRpe?.let { "RPE ${String.format("%.1f", it)}" } ?: ""

            // Completed indicator
            val isCompleted = session.completedAt != null
            binding.ivCompleted.visibility = if (isCompleted) View.VISIBLE else View.INVISIBLE

            ViewCompat.setTransitionName(binding.root, "workout_card_${session.id}")

            binding.root.setOnClickListener { onClick(session) }
            binding.root.setOnLongClickListener {
                onLongClick(session)
                true
            }
        }
    }

    class WorkoutDiffCallback : DiffUtil.ItemCallback<WorkoutSession>() {
        override fun areItemsTheSame(oldItem: WorkoutSession, newItem: WorkoutSession): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: WorkoutSession, newItem: WorkoutSession): Boolean {
            return oldItem == newItem
        }
    }
}