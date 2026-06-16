package com.biglifts.workouttracker.ui.workouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.biglifts.workouttracker.R
import com.biglifts.workouttracker.databinding.FragmentWorkoutDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkoutDetailFragment : Fragment() {

    private var _binding: FragmentWorkoutDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WorkoutDetailViewModel by viewModels()
    private lateinit var adapter: WorkoutExerciseSummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val workoutId = arguments?.getString("workout_id") ?: ""
        val workoutName = arguments?.getString("workout_name") ?: "Workout"

        setupRecyclerView()
        setupUI(workoutName)
        observeData()
        viewModel.loadWorkoutDetail(workoutId)
    }

    private fun setupRecyclerView() {
        adapter = WorkoutExerciseSummaryAdapter()
        binding.rvExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExercises.adapter = adapter
    }

    private fun setupUI(workoutName: String) {
        binding.toolbar.title = workoutName
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.btnStartWorkout.setOnClickListener {
            val workoutId = arguments?.getString("workout_id") ?: ""
            val bundle = bundleOf(
                "workout_id" to workoutId,
                "workout_name" to binding.toolbar.title.toString()
            )
            findNavController().navigate(R.id.action_detail_to_active, bundle)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.workout.collect { workout ->
                    workout?.let {
                        binding.tvDate.text = it.startedAt ?: ""
                        binding.tvDuration.text = it.durationMinutes?.let { min -> "${min} min" } ?: "--"
                        binding.tvTotalSets.text = it.totalSets?.let { sets -> "$sets sets" } ?: "-- sets"
                        binding.tvVolume.text = it.totalVolume?.let { vol ->
                            if (vol >= 1000) String.format("%.1fK kg", vol / 1000)
                            else String.format("%.0f kg", vol)
                        } ?: "--"
                        binding.tvRpe.text = it.avgRpe?.let { rpe -> "Avg RPE: ${String.format("%.1f", rpe)}" } ?: ""
                        binding.tvNotes.text = it.notes ?: ""
                        binding.tvNotes.isVisible = !it.notes.isNullOrBlank()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.exercises.collect { exercises ->
                    adapter.submitList(exercises)
                    binding.tvNoExercises.isVisible = exercises.isEmpty()
                    binding.rvExercises.isVisible = exercises.isNotEmpty()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { loading ->
                    binding.progressBar.isVisible = loading
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}