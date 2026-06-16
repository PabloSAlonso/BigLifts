package com.biglifts.workouttracker.ui.measurements

import android.os.Bundle
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
import com.biglifts.workouttracker.databinding.FragmentMeasurementsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MeasurementsFragment : Fragment() {

    private var _binding: FragmentMeasurementsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MeasurementsViewModel by viewModels()
    private lateinit var adapter: MeasurementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeasurementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeData()
        viewModel.loadMeasurements()
    }

    private fun setupUI() {
        adapter = MeasurementAdapter { measurement ->
            // Show detail or edit
        }
        binding.rvMeasurements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMeasurements.adapter = adapter

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.measurements_to_add)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.measurements.collect { measurements ->
                    adapter.submitList(measurements)
                    binding.tvEmpty.isVisible = measurements.isEmpty()

                    // Show latest weight
                    measurements.firstOrNull()?.let { latest ->
                        binding.tvLatestWeight.text = latest.weight?.let { "${it} kg" } ?: "--"
                        binding.tvLatestBodyFat.text = latest.bodyFatPercentage?.let { "${it}%" } ?: "--"
                        binding.tvLatestDate.text = latest.measuredAt ?: ""
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