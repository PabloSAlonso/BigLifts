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
import com.biglifts.workouttracker.data.models.WorkoutSession
import com.biglifts.workouttracker.databinding.FragmentWorkoutHistoryBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkoutHistoryFragment : Fragment() {

    private var _binding: FragmentWorkoutHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WorkoutHistoryViewModel by viewModels()
    private lateinit var adapter: WorkoutHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupUI()
        observeData()
        viewModel.loadWorkouts()

        // Container transform setup
        enterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 400L
            scrimColor = android.graphics.Color.TRANSPARENT
        }
        exitTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 400L
            scrimColor = android.graphics.Color.TRANSPARENT
        }
    }

    private fun setupRecyclerView() {
        adapter = WorkoutHistoryAdapter(
            onClick = { workout ->
                val bundle = bundleOf("workout_id" to workout.id, "workout_name" to workout.name)
                val extras = androidx.navigation.fragment.FragmentNavigatorExtras(
                    binding.root to "workout_detail_container"
                )
                findNavController().navigate(R.id.workouts_to_detail, bundle, null, extras)
            },
            onLongClick = { workout ->
                showDeleteDialog(workout)
            }
        )
        binding.rvWorkouts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWorkouts.adapter = adapter
    }

    private fun setupUI() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadWorkouts()
        }

        binding.fabNewWorkout.setOnClickListener {
            findNavController().navigate(R.id.workouts_to_new_workout)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.workouts.collect { workouts ->
                    adapter.submitList(workouts)
                    binding.tvEmpty.isVisible = workouts.isEmpty()
                    binding.rvWorkouts.isVisible = workouts.isNotEmpty()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { loading ->
                    binding.swipeRefresh.isRefreshing = loading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { error ->
                    error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun showDeleteDialog(workout: WorkoutSession) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Workout")
            .setMessage("Delete \"${workout.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteWorkout(workout.id ?: "")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}