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
import com.biglifts.workouttracker.ui.workout.timer.RestTimerViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ActiveWorkoutFragment : Fragment() {

    private var _binding: FragmentActiveWorkoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ActiveWorkoutViewModel by viewModels()
    private val restTimerViewModel: RestTimerViewModel by viewModels()
    private lateinit var activeExerciseAdapter: ActiveExerciseAdapter

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
        observeRestTimer()
        startTimer()

        if (workoutId != null) {
            viewModel.loadWorkout(workoutId)
        } else {
            viewModel.startNewWorkout(workoutName)
        }
    }

    private fun setupUI(workoutName: String) {
        binding.tvWorkoutName.text = workoutName

        // Setup exercise adapter
        activeExerciseAdapter = ActiveExerciseAdapter(
            onAddSet = { exerciseId -> viewModel.addSet(exerciseId) },
            onSetUpdated = { exerciseId, set -> viewModel.updateSet(exerciseId, set) },
            onRemoveExercise = { exerciseId -> viewModel.removeExercise(exerciseId) },
            onSetCompleted = { onSetCompleted() },
            onAddTechniqueSet = { exerciseId, technique -> viewModel.addSetWithTechnique(exerciseId, technique) }
        )
        binding.rvActiveExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActiveExercises.adapter = activeExerciseAdapter

        binding.btnAddExercise.setOnClickListener {
            showExercisePicker()
        }

        binding.btnFinishWorkout.setOnClickListener {
            showFinishDialog()
        }

        binding.toolbar.setNavigationOnClickListener {
            showExitDialog()
        }

        // Rest timer controls
        binding.btnPauseTimer.setOnClickListener {
            if (restTimerViewModel.isRunning.value) {
                restTimerViewModel.pauseTimer()
                binding.btnPauseTimer.text = getString(R.string.resume)
            } else {
                restTimerViewModel.resumeTimer(requireContext())
                binding.btnPauseTimer.text = getString(R.string.pause)
            }
        }

        binding.btnAddTime.setOnClickListener {
            restTimerViewModel.addTime(15)
        }

        binding.btnStopTimer.setOnClickListener {
            restTimerViewModel.stopTimer()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activeExercises.collect { exercises ->
                    val adapterExercises = exercises.map { data ->
                        ActiveExerciseAdapter.ActiveExercise(
                            exerciseId = data.exerciseId,
                            exercise = data.exercise,
                            previousSets = data.previousSets,
                            previousBestWeight = data.previousBestWeight,
                            previousBestReps = data.previousBestReps,
                            orderIndex = data.orderIndex
                        )
                    }
                    activeExerciseAdapter.submitList(adapterExercises)
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

    private fun observeRestTimer() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                restTimerViewModel.timeRemaining.collect { seconds ->
                    if (seconds > 0) {
                        binding.restTimerCard.isVisible = true
                        val mins = seconds / 60
                        val secs = seconds % 60
                        binding.tvRestTime.text = String.format("%d:%02d", mins, secs)
                    } else {
                        binding.restTimerCard.isVisible = false
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                restTimerViewModel.isRunning.collect { running ->
                    binding.btnPauseTimer.text = if (running) getString(R.string.pause) else getString(R.string.resume)
                }
            }
        }
    }

    private fun onSetCompleted() {
        restTimerViewModel.startTimer(requireContext())
    }

    private fun startTimer() {
        handler.postDelayed(timerRunnable, 1000)
    }

    private fun showExercisePicker() {
        findNavController().navigate(R.id.active_to_exercise_picker)
    }

    private fun showFinishDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.finish_workout_question))
            .setMessage(getString(R.string.save_workout_session))
            .setPositiveButton(getString(R.string.finish)) { _, _ ->
                viewModel.finishWorkout()
                handler.removeCallbacks(timerRunnable)
                restTimerViewModel.stopTimer()
                findNavController().popBackStack()
            }
            .setNegativeButton(getString(R.string.continue_str), null)
            .show()
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.exit_workout_question))
            .setMessage(getString(R.string.workout_will_be_discarded))
            .setPositiveButton(getString(R.string.exit)) { _, _ ->
                viewModel.discardWorkout()
                handler.removeCallbacks(timerRunnable)
                restTimerViewModel.stopTimer()
                findNavController().popBackStack()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timerRunnable)
        _binding = null
    }
}