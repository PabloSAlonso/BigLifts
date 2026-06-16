package com.biglifts.workouttracker.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.biglifts.workouttracker.data.models.WorkoutSession
import com.biglifts.workouttracker.databinding.ItemRecentWorkoutBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class RecentWorkoutAdapter(
    private val onClick: (WorkoutSession) -> Unit
) : ListAdapter<WorkoutSession, RecentWorkoutAdapter.WorkoutViewHolder>(WorkoutDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemRecentWorkoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WorkoutViewHolder(
        private val binding: ItemRecentWorkoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(session: WorkoutSession) {
            binding.tvName.text = session.name

            val startedAt = session.startedAt?.let {
                try { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) } catch (e: Exception) { null }
            }
            binding.tvDate.text = startedAt?.let { date ->
                val now = LocalDateTime.now()
                val daysAgo = ChronoUnit.DAYS.between(date.toLocalDate(), now.toLocalDate())
                when {
                    daysAgo == 0L -> "Today"
                    daysAgo == 1L -> "Yesterday"
                    daysAgo < 7L -> "${daysAgo}d ago"
                    else -> date.format(DateTimeFormatter.ofPattern("MMM d"))
                }
            } ?: ""

            binding.tvSets.text = session.totalSets?.let { "$it sets" } ?: ""

            binding.root.setOnClickListener { onClick(session) }
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