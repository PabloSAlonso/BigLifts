package com.biglifts.workouttracker.ui.templates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.biglifts.workouttracker.R
import com.biglifts.workouttracker.databinding.FragmentWorkoutTemplatesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkoutTemplatesFragment : Fragment() {

    private var _binding: FragmentWorkoutTemplatesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WorkoutTemplatesViewModel by viewModels()
    private lateinit var adapter: TemplateAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutTemplatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        adapter = TemplateAdapter(
            onStartTemplate = { template ->
                viewModel.startFromTemplate(template)
            },
            onEditTemplate = { template ->
                viewModel.editTemplate(template)
            },
            onDeleteTemplate = { template ->
                viewModel.deleteTemplate(template)
            }
        )

        binding.rvTemplates.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTemplates.adapter = adapter

        viewModel.templates.observe(viewLifecycleOwner) { templates ->
            adapter.submitList(templates)
            binding.tvEmpty.visibility = if (templates.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.navigateToActiveWorkout.observe(viewLifecycleOwner) { sessionId ->
            sessionId?.let {
                val bundle = Bundle().apply {
                    putString("workout_id", it)
                }
                findNavController().navigate(R.id.activeWorkoutFragment, bundle)
                viewModel.onNavigated()
            }
        }

        binding.btnNewTemplate.setOnClickListener {
            viewModel.createTemplate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}