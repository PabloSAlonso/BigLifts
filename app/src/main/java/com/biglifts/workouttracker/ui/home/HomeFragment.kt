package com.biglifts.workouttracker.ui.home

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
import com.biglifts.workouttracker.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: RecentWorkoutAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupUI()
        observeData()
        viewModel.loadData()
    }

    private fun setupRecyclerView() {
        adapter = RecentWorkoutAdapter { workout ->
            val bundle = bundleOf(
                "workout_id" to workout.id,
                "workout_name" to workout.name
            )
            findNavController().navigate(R.id.home_to_detail, bundle)
        }
        binding.rvRecentWorkouts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentWorkouts.adapter = adapter
    }

    private fun setupUI() {
        binding.btnStartWorkout.setOnClickListener {
            findNavController().navigate(R.id.home_to_new_workout)
        }

        binding.cardMeasurements.setOnClickListener {
            findNavController().navigate(R.id.home_to_measurements)
        }

        binding.cardCalculator.setOnClickListener {
            findNavController().navigate(R.id.home_to_calculator)
        }

        binding.cardVolume.setOnClickListener {
            findNavController().navigate(R.id.home_to_volume)
        }

        binding.cardTemplates.setOnClickListener {
            findNavController().navigate(R.id.home_to_templates)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recentWorkouts.collect { workouts ->
                    adapter.submitList(workouts)
                    binding.tvNoWorkouts.isVisible = workouts.isEmpty()
                    binding.rvRecentWorkouts.isVisible = workouts.isNotEmpty()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stats.collect { stats ->
                    binding.tvTotalWorkouts.text = "${stats.totalWorkouts}"
                    binding.tvWeekWorkouts.text = "${stats.weekWorkouts}"
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}