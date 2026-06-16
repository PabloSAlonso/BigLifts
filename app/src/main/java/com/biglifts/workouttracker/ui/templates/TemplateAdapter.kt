package com.biglifts.workouttracker.ui.templates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.biglifts.workouttracker.databinding.ItemTemplateBinding
import com.biglifts.workouttracker.data.models.WorkoutTemplate

class TemplateAdapter(
    private val onStartTemplate: (WorkoutTemplate) -> Unit,
    private val onEditTemplate: (WorkoutTemplate) -> Unit,
    private val onDeleteTemplate: (WorkoutTemplate) -> Unit
) : ListAdapter<WorkoutTemplate, TemplateAdapter.TemplateViewHolder>(TemplateDiffCallback()) {

    inner class TemplateViewHolder(private val binding: ItemTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(template: WorkoutTemplate) {
            binding.tvTemplateName.text = template.name
            binding.tvExerciseCount.text = "${template.exerciseCount} exercises"
            binding.tvCategory.text = template.category.uppercase()
            binding.tvSplitDay.text = template.splitDay?.uppercase() ?: "CUSTOM"
            binding.tvGoal.text = template.goal.replaceFirstChar { it.uppercase() }

            binding.btnStart.setOnClickListener { onStartTemplate(template) }
            binding.btnEdit.setOnClickListener { onEditTemplate(template) }
            binding.btnDelete.setOnClickListener { onDeleteTemplate(template) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val binding = ItemTemplateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TemplateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TemplateDiffCallback : DiffUtil.ItemCallback<WorkoutTemplate>() {
        override fun areItemsTheSame(oldItem: WorkoutTemplate, newItem: WorkoutTemplate) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: WorkoutTemplate, newItem: WorkoutTemplate) =
            oldItem == newItem
    }
}