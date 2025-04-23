package com.mobility.enp.view.fragments

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.mobility.enp.BuildConfig
import com.mobility.enp.R
import com.mobility.enp.data.model.login.LoginBody
import com.mobility.enp.databinding.FragmentLoginBinding
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.dialogs.GeneralMessageDialog
import com.mobility.enp.view.dialogs.LanguageDialog
import com.mobility.enp.viewmodel.LoginState
import com.mobility.enp.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding: FragmentLoginBinding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModels { LoginViewModel.Factory }

    private lateinit var userName: String
    private lateinit var userPassword: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFcmToken()

        binding.lifecycleOwner = viewLifecycleOwner

        setObservers()

        passwordVisibility()

        if (BuildConfig.DEBUG) {
            binding.editEmail.setText(BuildConfig.TEST_USERNAME)
            binding.editPassword.setText(BuildConfig.TEST_PASSWORD)
        } else {
            loginViewModel.getLastUser()
        }

        binding.buttonLogin.setOnClickListener {

            userName = binding.editEmail.text.toString()
            userPassword = binding.editPassword.text.toString()

            if (userName.isNotEmpty() && userPassword.isNotEmpty()) {

                val body = LoginBody(userName, userPassword)
                loginViewModel.loginUser(body, requireContext())

            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.enter_email_password),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.loginForgetPassword.setOnClickListener {
            it.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)

                    val action =
                        LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment()
                    findNavController().navigate(action)
                }
        }

        binding.createAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_newRegFlow)
        }

        binding.privacyTerms.setOnClickListener {

            it.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(100)

                    viewLifecycleOwner.lifecycleScope.launch {
                        val arg = loginViewModel.getLanguageKey()
                        val action =
                            LoginFragmentDirections.actionLoginFragmentToTermsAndPrivacyFragment(arg)
                        findNavController().navigate(action)
                    }
                }
        }

        binding.languagePicker.setOnClickListener {
            val languageDialog = LanguageDialog { languageSelected, canSwitchLanguage ->
                if (canSwitchLanguage) {
                    SharedPreferencesHelper.setLanguageChanged(requireContext(), true)
                    SharedPreferencesHelper.setUserLanguage(requireContext(), languageSelected)

                    activity?.recreate()
                } else {
                    Log.d(
                        TAG,
                        "data registered : $languageSelected $canSwitchLanguage"
                    )  // to be implemented
                    Toast.makeText(requireContext(), "Language not available", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            languageDialog.show(parentFragmentManager, "languageDialog")
        }

        binding.customerSupport.setOnClickListener {
            it.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f).scaleY(1f).setDuration(100)

                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToLoginContactFormDialog())

                }
        }
    }

    private fun setFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(MainActivity.TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            loginViewModel.writeFcmToken(token)

            Log.w(TAG, token)
        }
    }

    private fun passwordVisibility() {
        binding.passwordContainer.setEndIconOnClickListener {
            _binding?.let { safeBinding ->
                if (safeBinding.editPassword.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Sakrij lozinku
                    safeBinding.editPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    safeBinding.passwordContainer.endIconDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_eye_invisible
                    )
                    safeBinding.editPassword.setTextAppearance(R.style.Paragraph)
                    safeBinding.editPassword.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.figmaSplashScreenColor
                        )
                    )

                } else {
                    // Prikaži lozinku
                    safeBinding.editPassword.inputType =
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    safeBinding.passwordContainer.endIconDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_eye_visible
                    )
                    safeBinding.editPassword.setTextAppearance(R.style.Paragraph)
                    safeBinding.editPassword.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.figmaSplashScreenColor
                        )
                    )
                }
                safeBinding.editPassword.setSelection(safeBinding.editPassword.text?.length ?: 0)
            }
        }

    }


    private fun setObservers() {

        collectLatestLifecycleFlow(loginViewModel.loginState) { state ->
            when (state) {
                is LoginState.Loading -> {
                    binding.progbar.visibility = View.VISIBLE
                }

                is LoginState.Success -> {
                    binding.progbar.visibility = View.GONE
                    findNavController().navigate(LoginFragmentDirections.actionGlobalHomeFragment())
                }

                is LoginState.Failure -> {
                    binding.progbar.visibility = View.GONE
                    when (state.error) {

                        is NetworkError.NoConnection -> showNoInternetDialog()
                        is NetworkError.ServerError -> showErrorMessage(getString(R.string.server_error_msg))
                        is NetworkError.ApiError -> {
                            val statusCode = state.error.errorResponse.code
                            if (statusCode == 403) {
                                showPermissionDeniedDialog()
                            } else {
                                val errorMessage = state.error.errorResponse.message
                                    ?: getString(R.string.server_error_msg)
                                showErrorMessage(errorMessage)
                            }
                        }
                    }
                    loginViewModel.setIdleState()
                }

                is LoginState.Idle -> {}
            }
        }

        collectLatestLifecycleFlow(loginViewModel.fcmToken) { result ->
            when (result) {
                is SubmitResult.Loading -> {
                    logMessage("posting fcm token")
                }

                is SubmitResult.Success -> {
                    logMessage("fcm token posted")
                }

                is SubmitResult.Empty -> {}
                is SubmitResult.FailureNoConnection -> showNoInternetDialog()
                is SubmitResult.FailureServerError -> showErrorMessage(getString(R.string.server_error_msg))
                is SubmitResult.FailureApiError -> showErrorMessage(result.errorMessage)
                is SubmitResult.InvalidApiToken -> {
                    showErrorMessage(result.errorMessage)
                }
            }
        }

        if (!BuildConfig.DEBUG) {
            loginViewModel.lastUserEmail.observe(viewLifecycleOwner) { lastUser ->
                binding.editEmail.setText(lastUser.email)
            }
        }
    }

    private fun showErrorMessage(message: String) {
        binding.progbar.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun logMessage(msg: String) {
        Log.d(TAG, "fcmToken: $msg")
    }

    private fun showNoInternetDialog() {
        val binding = (activity as MainActivity).binding
        MainActivity.showSnackMessage(
            getString(R.string.no_internet), binding
        )
    }

    companion object {
        const val TAG = "loginFragment"
    }

    private fun showPermissionDeniedDialog() {
        val dialog =
            GeneralMessageDialog(
                requireContext().getString(R.string.access_denied),
                requireContext().getString(R.string.only_t4all_users_access_denied)
            )
        dialog.show(parentFragmentManager, "ShowPermissionDeniedDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}