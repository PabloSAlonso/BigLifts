package com.biglifts.workouttracker.ui.exercises

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.biglifts.workouttracker.databinding.FragmentExercisesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExercisesFragment : Fragment() {

    private var _binding: FragmentExercisesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExercisesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExercisesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeData()
    }

    private fun setupUI() {
        binding.rvExercises.layoutManager = LinearLayoutManager(requireContext())

        binding.etSearch.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                viewModel.searchExercises(textView.text.toString())
                true
            } else false
        }

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            val category = when (checkedIds.firstOrNull()) {
                com.biglifts.workouttracker.R.id.chipChest -> "chest"
                com.biglifts.workouttracker.R.id.chipBack -> "back"
                com.biglifts.workouttracker.R.id.chipLegs -> "legs"
                com.biglifts.workouttracker.R.id.chipShoulders -> "shoulders"
                com.biglifts.workouttracker.R.id.chipArms -> "biceps"
                com.biglifts.workouttracker.R.id.chipCore -> "core"
                else -> null
            }
            viewModel.filterByCategory(category)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.exercises.collect { exercises ->
                    // Update adapter
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}