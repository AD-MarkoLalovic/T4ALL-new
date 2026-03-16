package com.mobility.enp.view.fragments

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.mobility.enp.BuildConfig
import com.mobility.enp.R
import com.mobility.enp.data.model.login.LoginBody
import com.mobility.enp.databinding.FragmentLoginBinding
import com.mobility.enp.util.FragmentResultKeys
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.Util
import com.mobility.enp.util.Util.animateClickLoginScreen
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.util.safeNavigate
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.dialogs.GeneralMessageDialog
import com.mobility.enp.view.dialogs.LanguageDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.LoginState
import com.mobility.enp.viewmodel.LoginViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding: FragmentLoginBinding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModels { LoginViewModel.Factory }
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        franchiseViewModel.runOnce = true

        setObservers()
        fragmentResultListener()

        passwordVisibility()
        setupDebugCredentials()

        setupLogin()
        setupNavigationClicks()
        setupLanguagePicker()
        setupImeActions()

        setFcmToken()
    }

    private fun setupImeActions() {
        binding.editPassword.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                binding.buttonLogin.performClick()
                true
            } else {
                false
            }
        }
    }

    private fun fragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(
            FragmentResultKeys.LANGUAGE_DIALOG_RESULT,
            viewLifecycleOwner
        ) { _, bundle ->
            val languageSelected = bundle.getString(FragmentResultKeys.LANGUAGE_DIALOG_KEY)
                ?: return@setFragmentResultListener
            val canSwitchLanguage = bundle.getBoolean(FragmentResultKeys.LANGUAGE_CAN_SWITCH, false)

            if (canSwitchLanguage) {
                SharedPreferencesHelper.setLanguageChanged(requireContext(), true)
                SharedPreferencesHelper.setUserLanguage(requireContext(), languageSelected)
                activity?.recreate()
            } else {
                Toast.makeText(requireContext(), "Language not available", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setupLanguagePicker() {
        binding.languagePicker.setOnClickListener {
            LanguageDialog().show(parentFragmentManager, "LanguageDialog")
        }
    }

    private fun setupLogin() {
        binding.buttonLogin.setOnClickListener {
            val userName = binding.editEmail.text.toString()
            val userPassword = binding.editPassword.text.toString()

            if (userName.isNotEmpty() && userPassword.isNotEmpty()) {
                loginViewModel.loginUser(LoginBody(userName, userPassword))
            } else {
                showEmptyCredentialsError()
            }
        }
    }

    private fun showEmptyCredentialsError() {
        Toast.makeText(
            requireContext(),
            getString(R.string.enter_email_password),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setupDebugCredentials() {
        if (BuildConfig.DEBUG) {
            binding.editEmail.setText(BuildConfig.TEST_USERNAME)
            binding.editPassword.setText(BuildConfig.TEST_PASSWORD)
        } else {
            loginViewModel.loadLastUser()
        }
    }

    private fun openCustomerSupport(view: View) {
        view.isEnabled = false

        val action =
            LoginFragmentDirections.actionLoginFragmentToLoginContactFormDialog()

        view.animateClickLoginScreen {
            safeNavigate(action, R.id.loginFragment)
            post { isEnabled = true }
        }
    }

    private fun openTermsAndPrivacy(view: View) {
        view.isEnabled = false

        val arg = loginViewModel.getLanguageKey()
        val action =
            LoginFragmentDirections.actionLoginFragmentToTermsAndPrivacyFragment(arg)

        view.animateClickLoginScreen {
            safeNavigate(action, R.id.loginFragment)
            post { isEnabled = true }
        }
    }

    private fun openForgotPassword(view: View) {
        view.isEnabled = false
        val action = LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment()

        view.animateClickLoginScreen {
            safeNavigate(action, R.id.loginFragment)
            post { isEnabled = true }
        }
    }

    private fun setupNavigationClicks() {
        binding.loginForgetPassword.setOnClickListener { view ->
            openForgotPassword(view)
        }

        binding.createAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_newRegFlow)
        }

        binding.privacyTerms.setOnClickListener { view ->
            openTermsAndPrivacy(view)
        }

        binding.customerSupport.setOnClickListener { view ->
            openCustomerSupport(view)
        }

        binding.buyTagLogin?.setOnClickListener {
            if (Util.isNetworkAvailable(requireContext())) {
                val url = BuildConfig.TAG_ORDER_BASE_URL
                val action = LoginFragmentDirections.actionLoginFragmentToTagOrderWebFragment(url)
                findNavController().navigate(action)
            } else {
                showNoInternetDialog()
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

                    val portalKey = state.portalKey
                    if (portalKey != null && portalKey.isNotEmpty()) {
                        (activity as MainActivity).logoFix(portalKey)
                        franchiseViewModel.getFranchiseModelFromLogin(portalKey, requireContext())
                    }

                    binding.progbar.visibility = View.GONE

                    val options = NavOptions.Builder()
                        .setPopUpTo(R.id.loginFragment, true)
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                        .build()

                    findNavController().navigate(R.id.homeFragment, null, options)
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
            collectLatestLifecycleFlow(loginViewModel.lastUserEmail) { email ->
                if (!email.isNullOrEmpty()) binding.editEmail.setText(email)
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
        GeneralMessageDialog.newInstance(
            requireContext().getString(R.string.access_denied),
            requireContext().getString(R.string.only_t4all_users_access_denied)
        ).show(parentFragmentManager, "ShowPermissionDeniedDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}