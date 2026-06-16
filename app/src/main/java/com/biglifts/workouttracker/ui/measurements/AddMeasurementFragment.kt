package com.biglifts.workouttracker.ui.measurements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.biglifts.workouttracker.databinding.FragmentAddMeasurementBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddMeasurementFragment : Fragment() {

    private var _binding: FragmentAddMeasurementBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MeasurementsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMeasurementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.btnSave.setOnClickListener {
            val weight = binding.etWeight.text.toString().toDoubleOrNull()
            val bodyFat = binding.etBodyFat.text.toString().toDoubleOrNull()
            val neck = binding.etNeck.text.toString().toDoubleOrNull()
            val shoulders = binding.etShoulders.text.toString().toDoubleOrNull()
            val chest = binding.etChest.text.toString().toDoubleOrNull()
            val leftBicep = binding.etLeftBicep.text.toString().toDoubleOrNull()
            val rightBicep = binding.etRightBicep.text.toString().toDoubleOrNull()
            val waist = binding.etWaist.text.toString().toDoubleOrNull()
            val hips = binding.etHips.text.toString().toDoubleOrNull()
            val leftThigh = binding.etLeftThigh.text.toString().toDoubleOrNull()
            val rightThigh = binding.etRightThigh.text.toString().toDoubleOrNull()
            val leftCalf = binding.etLeftCalf.text.toString().toDoubleOrNull()
            val rightCalf = binding.etRightCalf.text.toString().toDoubleOrNull()
            val notes = binding.etNotes.text.toString().ifBlank { null }

            // Average left/right for arms, thighs, calves
            val arms = listOfNotNull(leftBicep, rightBicep).average().takeIf { leftBicep != null || rightBicep != null }
            val thighs = listOfNotNull(leftThigh, rightThigh).average().takeIf { leftThigh != null || rightThigh != null }
            val calves = listOfNotNull(leftCalf, rightCalf).average().takeIf { leftCalf != null || rightCalf != null }

            viewModel.addMeasurement(
                weight = weight,
                bodyFatPercentage = bodyFat,
                neck = neck,
                shoulders = shoulders,
                chest = chest,
                arms = arms,
                waist = waist,
                hips = hips,
                thighs = thighs,
                calves = calves,
                notes = notes
            )
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}