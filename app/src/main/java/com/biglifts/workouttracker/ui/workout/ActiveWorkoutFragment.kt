package com.biglifts.workouttracker.ui.workout

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.biglifts.workouttracker.R
import com.biglifts.workouttracker.databinding.FragmentActiveWorkoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ActiveWorkoutFragment : Fragment() {

    private var _binding: FragmentActiveWorkoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ActiveWorkoutViewModel by viewModels()

    private val handler = Handler(Looper.getMainLooper())
    private var startTime = System.currentTimeMillis()

    private val timerRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - startTime
            val hours = elapsed / 3600000
            val minutes = (elapsed % 3600000) / 60000
            val seconds = (elapsed % 60000) / 1000
            binding.tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val workoutId = arguments?.getString("workout_id")
        val workoutName = arguments?.getString("workout_name") ?: "Workout"

        setupUI(workoutName)
        observeData()
        startTimer()

        if (workoutId != null) {
            viewModel.loadWorkout(workoutId)
        } else {
            viewModel.startNewWorkout(workoutName)
        }
    }

    private fun setupUI(workoutName: String) {
        binding.tvWorkoutName.text = workoutName

        binding.btnAddExercise.setOnClickListener {
            showExercisePicker()
        }

        binding.btnFinishWorkout.setOnClickListener {
            showFinishDialog()
        }

        binding.toolbar.setNavigationOnClickListener {
            showExitDialog()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activeExercises.collect { exercises ->
                    binding.rvActiveExercises.isVisible = exercises.isNotEmpty()
                    binding.tvNoExercises.isVisible = exercises.isEmpty()
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

    private fun startTimer() {
        handler.postDelayed(timerRunnable, 1000)
    }

    private fun showExercisePicker() {
        // Navigate to exercise picker dialog
        findNavController().navigate(R.id.active_to_exercise_picker)
    }

    private fun showFinishDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Finish Workout?")
            .setMessage("Save this workout session?")
            .setPositiveButton("Finish") { _, _ ->
                viewModel.finishWorkout()
                handler.removeCallbacks(timerRunnable)
                findNavController().popBackStack()
            }
            .setNegativeButton("Continue", null)
            .show()
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Exit Workout?")
            .setMessage("Current workout will be discarded.")
            .setPositiveButton("Exit") { _, _ ->
                viewModel.discardWorkout()
                handler.removeCallbacks(timerRunnable)
                findNavController().popBackStack()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timerRunnable)
        _binding = null
    }
}