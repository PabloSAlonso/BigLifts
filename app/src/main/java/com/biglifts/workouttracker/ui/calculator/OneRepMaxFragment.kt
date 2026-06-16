package com.biglifts.workouttracker.ui.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.biglifts.workouttracker.databinding.FragmentOneRepMaxBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class OneRepMaxFragment : Fragment() {

    private var _binding: FragmentOneRepMaxBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOneRepMaxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCalculate.setOnClickListener {
            calculate1RM()
        }
    }

    private fun calculate1RM() {
        val weight = binding.etWeight.text.toString().toDoubleOrNull()
        val reps = binding.etReps.text.toString().toIntOrNull()

        if (weight == null || reps == null || reps < 1) {
            binding.tvResult.text = "Enter valid weight and reps"
            return
        }

        // Multiple 1RM formulas
        val brzycki = weight * (36.0 / (37.0 - reps))
        val epley = weight * (1 + reps / 30.0)
        val lander = (100 * weight) / (101.3 - 2.67123 * reps)
        val lombardi = weight * Math.pow(reps.toDouble(), 0.10)
        val oconner = weight * (1 + reps * 0.025)
        val mayhew = (100 * weight) / (52.2 + 41.9 * Math.exp(-0.055 * reps))

        binding.tvBrzycki.text = "${brzycki.roundToInt()} kg"
        binding.tvEpley.text = "${epley.roundToInt()} kg"
        binding.tvLander.text = "${lander.roundToInt()} kg"
        binding.tvLombardi.text = "${lombardi.roundToInt()} kg"
        binding.tvOconner.text = "${oconner.roundToInt()} kg"
        binding.tvMayhew.text = "${mayhew.roundToInt()} kg"

        // Average of all formulas
        val average = (brzycki + epley + lander + lombardi + oconner + mayhew) / 6
        binding.tvAverage.text = "Estimated 1RM: ${average.roundToInt()} kg"

        // Show percentage table
        val percentages = listOf(100, 95, 90, 85, 80, 75, 70, 65, 60, 55, 50)
        val sb = StringBuilder()
        percentages.forEach { pct ->
            val weightAtPct = (average * pct / 100).roundToInt()
            sb.appendLine("$pct% = $weightAtPct kg")
        }
        binding.tvPercentages.text = sb.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}