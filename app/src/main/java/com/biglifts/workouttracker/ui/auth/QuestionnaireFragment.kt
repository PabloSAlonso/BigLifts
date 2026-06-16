package com.biglifts.workouttracker.ui.auth

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
import com.biglifts.workouttracker.R
import com.biglifts.workouttracker.databinding.FragmentQuestionnaireBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuestionnaireFragment : Fragment() {

    private var _binding: FragmentQuestionnaireBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuestionnaireViewModel by viewModels()
    private var currentStep = 1
    private val totalSteps = 4

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionnaireBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeState()
        updateStepUI()
    }

    private fun setupUI() {
        binding.btnNext.setOnClickListener {
            if (validateCurrentStep()) {
                if (currentStep < totalSteps) {
                    currentStep++
                    updateStepUI()
                } else {
                    saveProfile()
                }
            }
        }

        binding.btnBack.setOnClickListener {
            if (currentStep > 1) {
                currentStep--
                updateStepUI()
            }
        }

        binding.sliderDaysPerWeek.addOnChangeListener { _, value, _ ->
            binding.tvDaysPerWeek.text = "${value.toInt()} days per week"
        }
    }

    private fun updateStepUI() {
        binding.stepBasicInfo.isVisible = false
        binding.stepActivityLevel.isVisible = false
        binding.stepTrainingGoal.isVisible = false
        binding.stepSchedule.isVisible = false

        when (currentStep) {
            1 -> binding.stepBasicInfo.isVisible = true
            2 -> binding.stepActivityLevel.isVisible = true
            3 -> binding.stepTrainingGoal.isVisible = true
            4 -> binding.stepSchedule.isVisible = true
        }

        binding.progressBar.progress = (currentStep * 100) / totalSteps
        binding.btnBack.isVisible = currentStep > 1
        binding.btnNext.text = if (currentStep == totalSteps) "Finish" else "Next"
    }

    private fun validateCurrentStep(): Boolean {
        when (currentStep) {
            1 -> {
                val age = binding.etAge.text.toString().toIntOrNull()
                if (age == null || age < 14 || age > 120) {
                    binding.tilAge.error = "Enter valid age (14-120)"
                    return false
                }
                binding.tilAge.error = null

                if (binding.chipGroupGender.checkedChipId == View.NO_ID) {
                    Snackbar.make(binding.root, "Select gender", Snackbar.LENGTH_SHORT).show()
                    return false
                }

                val height = binding.etHeight.text.toString().toDoubleOrNull()
                if (height == null || height < 100 || height > 250) {
                    binding.tilHeight.error = "Enter valid height"
                    return false
                }
                binding.tilHeight.error = null

                val weight = binding.etWeight.text.toString().toDoubleOrNull()
                if (weight == null || weight < 30 || weight > 300) {
                    binding.tilWeight.error = "Enter valid weight"
                    return false
                }
                binding.tilWeight.error = null
            }
            2 -> {
                if (binding.rgActivityLevel.checkedRadioButtonId == View.NO_ID) {
                    Snackbar.make(binding.root, "Select activity level", Snackbar.LENGTH_SHORT).show()
                    return false
                }
            }
            3 -> {
                if (binding.chipGroupExperience.checkedChipId == View.NO_ID) {
                    Snackbar.make(binding.root, "Select experience level", Snackbar.LENGTH_SHORT).show()
                    return false
                }
                if (binding.rgGoal.checkedRadioButtonId == View.NO_ID) {
                    Snackbar.make(binding.root, "Select your goal", Snackbar.LENGTH_SHORT).show()
                    return false
                }
            }
        }
        return true
    }

    private fun saveProfile() {
        val gender = when (binding.chipGroupGender.checkedChipId) {
            R.id.chipMale -> "male"
            R.id.chipFemale -> "female"
            else -> "other"
        }

        val experience = when (binding.chipGroupExperience.checkedChipId) {
            R.id.chipBeginner -> "beginner"
            R.id.chipIntermediate -> "intermediate"
            R.id.chipAdvanced -> "advanced"
            else -> "beginner"
        }

        val goal = when (binding.rgGoal.checkedRadioButtonId) {
            R.id.rbLoseFat -> "lose_fat"
            R.id.rbGainMuscle -> "gain_muscle"
            R.id.rbStrength -> "strength"
            R.id.rbMaintain -> "maintain"
            R.id.rbGeneral -> "general_fitness"
            else -> "general_fitness"
        }

        val activityLevel = when (binding.rgActivityLevel.checkedRadioButtonId) {
            R.id.rbSedentary -> "sedentary"
            R.id.rbLight -> "light"
            R.id.rbModerate -> "moderate"
            R.id.rbActive -> "active"
            R.id.rbVeryActive -> "very_active"
            else -> "moderate"
        }

        viewModel.completeOnboarding(
            age = binding.etAge.text.toString().toInt(),
            gender = gender,
            heightCm = binding.etHeight.text.toString().toDouble(),
            weightKg = binding.etWeight.text.toString().toDouble(),
            targetWeightKg = binding.etTargetWeight.text.toString().toDoubleOrNull(),
            activityLevel = activityLevel,
            experience = experience,
            goal = goal,
            daysPerWeek = binding.sliderDaysPerWeek.value.toInt(),
            injuries = binding.etInjuries.text.toString().ifBlank { null },
            equipment = binding.etEquipment.text.toString().ifBlank { null }
        )
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AuthUiState.Loading -> {
                            binding.btnNext.isEnabled = false
                            binding.btnBack.isEnabled = false
                        }
                        is AuthUiState.OnboardingComplete -> {
                            binding.btnNext.isEnabled = true
                            binding.btnBack.isEnabled = true
                            navigateToMain()
                        }
                        is AuthUiState.Error -> {
                            binding.btnNext.isEnabled = true
                            binding.btnBack.isEnabled = true
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        findNavController().navigate(R.id.action_questionnaire_to_main)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}