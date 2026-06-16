package com.biglifts.workouttracker.ui.measurements

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.biglifts.workouttracker.databinding.ItemMeasurementBinding
import com.biglifts.workouttracker.data.models.BodyMeasurement

class MeasurementAdapter(
    private val onClick: (BodyMeasurement) -> Unit
) : ListAdapter<BodyMeasurement, MeasurementAdapter.MeasurementViewHolder>(MeasurementDiffCallback()) {

    inner class MeasurementViewHolder(private val binding: ItemMeasurementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(measurement: BodyMeasurement) {
            binding.tvWeight.text = measurement.weight?.let { "${it} kg" } ?: "--"
            binding.tvBodyFat.text = measurement.bodyFatPercentage?.let { "${it}%" } ?: "--"
            binding.tvDate.text = measurement.measuredAt ?: ""
            binding.tvChest.text = measurement.chest?.let { "${it} cm" } ?: "--"
            binding.tvWaist.text = measurement.waist?.let { "${it} cm" } ?: "--"
            binding.tvHips.text = measurement.hips?.let { "${it} cm" } ?: "--"
            binding.root.setOnClickListener { onClick(measurement) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementViewHolder {
        val binding = ItemMeasurementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MeasurementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MeasurementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MeasurementDiffCallback : DiffUtil.ItemCallback<BodyMeasurement>() {
        override fun areItemsTheSame(oldItem: BodyMeasurement, newItem: BodyMeasurement) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BodyMeasurement, newItem: BodyMeasurement) =
            oldItem == newItem
    }
}