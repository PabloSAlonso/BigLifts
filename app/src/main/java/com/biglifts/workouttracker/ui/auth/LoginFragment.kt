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
import com.biglifts.workouttracker.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()
    private var isLoginMode = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.isLoggedIn) {
            navigateToMain()
            return
        }

        setupUI()
        observeState()
    }

    private fun setupUI() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (!validateInputs(email, password)) return@setOnClickListener

            if (isLoginMode) {
                viewModel.login(email, password)
            } else {
                val username = binding.etUsername.text.toString().trim()
                if (username.isBlank()) {
                    binding.tilUsername.error = getString(R.string.username_required)
                    return@setOnClickListener
                }
                if (username.length < 3) {
                    binding.tilUsername.error = getString(R.string.min_3_characters)
                    return@setOnClickListener
                }
                viewModel.register(email, password, username)
            }
        }

        binding.btnRegister.setOnClickListener {
            isLoginMode = !isLoginMode
            updateModeUI()
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isBlank()) {
                binding.tilEmail.error = getString(R.string.enter_email_first)
                return@setOnClickListener
            }
            // TODO: Implement password reset
            Snackbar.make(binding.root, getString(R.string.password_reset_coming_soon), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun updateModeUI() {
        binding.tilUsername.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        if (isLoginMode) {
            binding.btnLogin.text = getString(R.string.login)
            binding.btnRegister.text = getString(R.string.create_account)
            binding.tilUsername.isVisible = false
        } else {
            binding.btnLogin.text = getString(R.string.register)
            binding.btnRegister.text = getString(R.string.already_have_account_login)
            binding.tilUsername.isVisible = true
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var valid = true

        if (email.isBlank()) {
            binding.tilEmail.error = getString(R.string.email_required)
            valid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.invalid_email_format)
            valid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isBlank()) {
            binding.tilPassword.error = getString(R.string.password_required)
            valid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.min_6_characters)
            valid = false
        } else {
            binding.tilPassword.error = null
        }

        return valid
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AuthUiState.Loading -> {
                            binding.progressBar.isVisible = true
                            binding.btnLogin.isEnabled = false
                            binding.btnRegister.isEnabled = false
                        }
                        is AuthUiState.Success -> {
                            binding.progressBar.isVisible = false
                            binding.btnLogin.isEnabled = true
                            binding.btnRegister.isEnabled = true
                            navigateToMain()
                        }
                        is AuthUiState.Registered -> {
                            binding.progressBar.isVisible = false
                            binding.btnLogin.isEnabled = true
                            binding.btnRegister.isEnabled = true
                            // After registration, go to login then onboarding
                            isLoginMode = true
                            updateModeUI()
                            Snackbar.make(binding.root, getString(R.string.account_created_login), Snackbar.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                        is AuthUiState.Error -> {
                            binding.progressBar.isVisible = false
                            binding.btnLogin.isEnabled = true
                            binding.btnRegister.isEnabled = true
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                        is AuthUiState.LoggedOut -> {
                            binding.progressBar.isVisible = false
                            binding.btnLogin.isEnabled = true
                            binding.btnRegister.isEnabled = true
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        findNavController().navigate(R.id.action_login_to_main)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}