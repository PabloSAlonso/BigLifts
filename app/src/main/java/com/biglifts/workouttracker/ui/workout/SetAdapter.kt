package com.biglifts.workouttracker.ui.workout

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.biglifts.workouttracker.data.models.WorkoutSet
import com.biglifts.workouttracker.databinding.ItemSetInputBinding

class SetAdapter(
    private val exerciseId: String,
    private val previousSets: List<WorkoutSet>,
    private val onSetUpdated: (WorkoutSet) -> Unit
) : ListAdapter<WorkoutSet, SetAdapter.SetViewHolder>(SetDiffCallback()) {

    private val previousBestMap = mutableMapOf<Int, WorkoutSet>()

    init {
        previousSets.forEach { set ->
            val existing = previousBestMap[set.setNumber]
            if (existing == null || (set.weight ?: 0.0) > (existing.weight ?: 0.0)) {
                previousBestMap[set.setNumber] = set
            }
        }
    }

    fun setPreviousSets(sets: List<WorkoutSet>) {
        previousBestMap.clear()
        sets.forEach { set ->
            val existing = previousBestMap[set.setNumber]
            if (existing == null || (set.weight ?: 0.0) > (existing.weight ?: 0.0)) {
                previousBestMap[set.setNumber] = set
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val binding = ItemSetInputBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class SetViewHolder(
        private val binding: ItemSetInputBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var weightWatcher: TextWatcher? = null
        private var repsWatcher: TextWatcher? = null
        private var rirWatcher: TextWatcher? = null
        private val handler = Handler(Looper.getMainLooper())
        private var weightRunnable: Runnable? = null
        private var repsRunnable: Runnable? = null
        private var rirRunnable: Runnable? = null

        fun bind(set: WorkoutSet, setNumber: Int) {
            // Remove old watchers and pending callbacks
            weightWatcher?.let { binding.etWeight.removeTextChangedListener(it) }
            repsWatcher?.let { binding.etReps.removeTextChangedListener(it) }
            rirWatcher?.let { binding.etRir.removeTextChangedListener(it) }
            weightRunnable?.let { handler.removeCallbacks(it) }
            repsRunnable?.let { handler.removeCallbacks(it) }
            rirRunnable?.let { handler.removeCallbacks(it) }

            binding.tvSetNumber.text = "$setNumber"

            // Show previous best as hint
            val previousBest = previousBestMap[setNumber]
            if (previousBest != null) {
                binding.tvPrevious.visibility = View.VISIBLE
                binding.tvPrevious.text = "Prev: ${previousBest.weight?.toInt() ?: "?"}kg x ${previousBest.reps ?: "?"}"
            } else if (previousSets.isNotEmpty()) {
                val bestSet = previousSets.maxByOrNull { it.weight ?: 0.0 }
                if (bestSet != null) {
                    binding.tvPrevious.visibility = View.VISIBLE
                    binding.tvPrevious.text = "Best: ${bestSet.weight?.toInt() ?: "?"}kg x ${bestSet.reps ?: "?"}"
                } else {
                    binding.tvPrevious.visibility = View.GONE
                }
            } else {
                binding.tvPrevious.visibility = View.GONE
            }

            // Pre-fill with current values
            binding.etWeight.setText(set.weight?.let { if (it > 0) it.toInt().toString() else "" } ?: "")
            binding.etReps.setText(set.reps?.toString() ?: "")
            binding.etRir.setText(set.rir?.toString() ?: "")

            // Set watchers with debounce
            weightWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    weightRunnable?.let { handler.removeCallbacks(it) }
                    weightRunnable = Runnable {
                        val weight = s.toString().toDoubleOrNull()
                        onSetUpdated(set.copy(weight = weight))
                    }
                    handler.postDelayed(weightRunnable!!, 500)
                }
            }

            repsWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    repsRunnable?.let { handler.removeCallbacks(it) }
                    repsRunnable = Runnable {
                        val reps = s.toString().toIntOrNull()
                        onSetUpdated(set.copy(reps = reps))
                    }
                    handler.postDelayed(repsRunnable!!, 500)
                }
            }

            rirWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    rirRunnable?.let { handler.removeCallbacks(it) }
                    rirRunnable = Runnable {
                        val rir = s.toString().toIntOrNull()
                        onSetUpdated(set.copy(rir = rir))
                    }
                    handler.postDelayed(rirRunnable!!, 500)
                }
            }

            binding.etWeight.addTextChangedListener(weightWatcher)
            binding.etReps.addTextChangedListener(repsWatcher)
            binding.etRir.addTextChangedListener(rirWatcher)

            // Intensity technique indicator
            if (set.isIntensityTechnique && set.intensityTechnique != null) {
                binding.tvTechnique.visibility = View.VISIBLE
                binding.tvTechnique.text = when (set.intensityTechnique) {
                    "dropset" -> "DS"
                    "restpause" -> "RP"
                    "myorep" -> "MR"
                    "cluster" -> "CL"
                    else -> set.intensityTechnique.take(2).uppercase()
                }
            } else {
                binding.tvTechnique.visibility = View.GONE
            }

            // Completed checkbox
            binding.cbCompleted.isChecked = set.completed
            binding.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onSetUpdated(set.copy(completed = isChecked))
            }
        }
    }

    class SetDiffCallback : DiffUtil.ItemCallback<WorkoutSet>() {
        override fun areItemsTheSame(oldItem: WorkoutSet, newItem: WorkoutSet): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: WorkoutSet, newItem: WorkoutSet): Boolean {
            return oldItem == newItem
        }
    }
}