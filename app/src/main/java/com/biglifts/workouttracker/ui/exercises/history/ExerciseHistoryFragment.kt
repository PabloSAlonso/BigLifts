package com.biglifts.workouttracker.ui.exercises.history

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.biglifts.workouttracker.R
import com.biglifts.workouttracker.data.api.ExerciseHistoryEntry
import com.biglifts.workouttracker.databinding.FragmentExerciseHistoryBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExerciseHistoryFragment : Fragment() {

    private var _binding: FragmentExerciseHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExerciseHistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exerciseId = arguments?.getString("exercise_id") ?: ""
        val exerciseName = arguments?.getString("exercise_name") ?: "Exercise"

        binding.toolbar.title = exerciseName
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        setupCharts()
        observeData()
        viewModel.loadExerciseHistory(exerciseId)
    }

    private fun setupCharts() {
        binding.chart1rm.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            legend.isEnabled = true
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.textColor = Color.GRAY
            axisLeft.textColor = Color.BLACK
            axisLeft.granularity = 1f
            axisRight.isEnabled = false
            animateX(500)
        }

        binding.chartVolume.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            legend.isEnabled = true
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.textColor = Color.GRAY
            axisLeft.textColor = Color.BLACK
            axisLeft.granularity = 1f
            axisRight.isEnabled = false
            animateX(500)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.history.collect { history ->
                    if (history.isNotEmpty()) {
                        updateCharts(history)
                        updateStats(history)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pr.collect { pr ->
                    binding.tvCurrentPr.text = pr?.let {
                        "PR: ${it.weight?.toInt() ?: "?"}kg x ${it.reps ?: "?"}"
                    } ?: "No PR yet"
                }
            }
        }
    }

    private fun updateCharts(history: List<ExerciseHistoryEntry>) {
        // 1RM progression
        val estimated1rmEntries = history
            .filter { it.estimated1rm != null }
            .mapIndexed { index, entry ->
                Entry(index.toFloat(), entry.estimated1rm!!.toFloat())
            }

        if (estimated1rmEntries.isNotEmpty()) {
            val dataSet = LineDataSet(estimated1rmEntries, "Estimated 1RM").apply {
                color = requireContext().getColor(R.color.primary)
                setCircleColor(requireContext().getColor(R.color.primary))
                lineWidth = 2f
                circleRadius = 4f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            binding.chart1rm.data = LineData(dataSet)
            binding.chart1rm.invalidate()
        }

        // Volume per entry
        val volumeEntries = history
            .filter { it.volume != null }
            .mapIndexed { index, entry ->
                Entry(index.toFloat(), entry.volume!!.toFloat())
            }

        if (volumeEntries.isNotEmpty()) {
            val dataSet = LineDataSet(volumeEntries, "Volume (kg)").apply {
                color = requireContext().getColor(R.color.secondary)
                setCircleColor(requireContext().getColor(R.color.secondary))
                lineWidth = 2f
                circleRadius = 4f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setFillColor(requireContext().getColor(R.color.secondary))
                fillAlpha = 30
                setDrawFilled(true)
            }
            binding.chartVolume.data = LineData(dataSet)
            binding.chartVolume.invalidate()
        }
    }

    private fun updateStats(history: List<ExerciseHistoryEntry>) {
        val completed = history.filter { it.weight != null }

        if (completed.isNotEmpty()) {
            val maxWeight = completed.maxOf { it.weight ?: 0.0 }
            val totalVolume = completed.sumOf { it.volume ?: 0.0 }
            val avgRpe = completed.filter { it.rpe != null }.map { it.rpe!! }.average()

            binding.tvMaxWeight.text = "${maxWeight.toInt()} kg"
            binding.tvTotalVolume.text = if (totalVolume >= 1000) {
                String.format("%.1fK kg", totalVolume / 1000)
            } else {
                "${totalVolume.toInt()} kg"
            }
            binding.tvAvgRpe.text = if (avgRpe.isNaN()) "--" else String.format("%.1f", avgRpe)
            binding.tvTotalSets.text = "${completed.size}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}