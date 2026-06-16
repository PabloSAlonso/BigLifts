package com.biglifts.workouttracker.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.biglifts.workouttracker.R
import com.biglifts.workouttracker.databinding.FragmentNewWorkoutBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class NewWorkoutFragment : Fragment() {

    private var _binding: FragmentNewWorkoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val defaultName = String.format(getString(R.string.workout_name_default), SimpleDateFormat("MMM d", Locale.getDefault()).format(Date()))
        binding.etWorkoutName.setText(defaultName)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.btnStart.setOnClickListener {
            val name = binding.etWorkoutName.text.toString().trim()
            if (name.isBlank()) {
                binding.tilWorkoutName.error = getString(R.string.enter_workout_name)
                return@setOnClickListener
            }

            val bundle = bundleOf("workout_name" to name)
            findNavController().navigate(R.id.action_new_workout_to_active, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}