package com.biglifts.workouttracker.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.biglifts.workouttracker.R
import com.biglifts.workouttracker.data.api.ApiClient
import com.biglifts.workouttracker.databinding.FragmentProfileBinding
import com.biglifts.workouttracker.util.CsvExport
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeData()
    }

    private fun setupUI() {
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_profile_to_login)
        }

        binding.layoutExportWorkouts.setOnClickListener {
            exportWorkouts()
        }

        binding.layoutExportMeasurements.setOnClickListener {
            exportMeasurements()
        }
    }

    private fun exportWorkouts() {
        val apiClient = ApiClient(requireContext().applicationContext)
        viewLifecycleOwner.lifecycleScope.launch {
            val uri = CsvExport.exportWorkouts(requireContext(), apiClient)
            if (uri != null) {
                CsvExport.shareFile(requireContext(), uri, "workouts_export.csv")
                Snackbar.make(binding.root, "Workouts exported successfully", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, "Failed to export workouts", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportMeasurements() {
        val apiClient = ApiClient(requireContext().applicationContext)
        viewLifecycleOwner.lifecycleScope.launch {
            val uri = CsvExport.exportMeasurements(requireContext(), apiClient)
            if (uri != null) {
                CsvExport.shareFile(requireContext(), uri, "measurements_export.csv")
                Snackbar.make(binding.root, "Measurements exported successfully", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, "Failed to export measurements", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeData() {
        // Observe user profile data
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}