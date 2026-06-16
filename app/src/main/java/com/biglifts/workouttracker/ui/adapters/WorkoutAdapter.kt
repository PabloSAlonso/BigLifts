package com.biglifts.workouttracker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.biglifts.workouttracker.data.models.WorkoutSession
import com.biglifts.workouttracker.databinding.ItemWorkoutBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class WorkoutAdapter(
    private val onItemClick: (WorkoutSession) -> Unit,
    private val onDeleteClick: (WorkoutSession) -> Unit
) : ListAdapter<WorkoutSession, WorkoutAdapter.WorkoutViewHolder>(WorkoutDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WorkoutViewHolder(
        private val binding: ItemWorkoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(session: WorkoutSession) {
            binding.tvWorkoutName.text = session.name

            val startedAt = session.startedAt?.let {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
            }
            binding.tvWorkoutDate.text = startedAt?.let { date ->
                val now = LocalDateTime.now()
                val daysAgo = ChronoUnit.DAYS.between(date, now)
                when {
                    daysAgo == 0L -> "Today, ${date.format(DateTimeFormatter.ofPattern("h:mm a"))}"
                    daysAgo == 1L -> "Yesterday, ${date.format(DateTimeFormatter.ofPattern("h:mm a"))}"
                    else -> date.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
                }
            } ?: "Unknown date"

            binding.root.setOnClickListener { onItemClick(session) }
            binding.ivDelete.setOnClickListener { onDeleteClick(session) }
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