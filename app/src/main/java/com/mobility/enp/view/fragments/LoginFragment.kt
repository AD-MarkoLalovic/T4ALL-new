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
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.model.login.LoginBody
import com.mobility.enp.databinding.FragmentLoginBinding
import com.mobility.enp.network.Repository
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.dialogs.LanguageDialog
import com.mobility.enp.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

            if (userName.isNotEmpty() || userPassword.isNotEmpty()) {

                val body = LoginBody(userName, userPassword)

                if (Repository.isNetworkAvailable(requireContext())) {
                    binding.progbar.visibility = View.VISIBLE
                    loginViewModel.loginUser(body)
                } else {
                    val bundle = Bundle().apply {
                        putString(
                            getString(R.string.title),
                            getString(R.string.no_connection_title)
                        )
                        putString(
                            getString(R.string.subtitle),
                            getString(R.string.please_connect_to_the_internet)
                        )
                    }

                    findNavController().navigate(
                        R.id.action_global_loginNoInternetConnectionDialog,
                        bundle
                    )
                }

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
                    MainActivity.setLocale(requireContext(), languageSelected)
                    activity?.let { act ->
                        act.recreate()
                    }
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
            if (binding.editPassword.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                // Sakrij lozinku
                binding.editPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.passwordContainer.endIconDrawable = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_eye_invisible
                ) // Precrtano oko
                binding.editPassword.setTextAppearance(R.style.Paragraph)
                binding.editPassword.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.figmaSplashScreenColor
                    )
                )

            } else {
                // Prikaži lozinku
                binding.editPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.passwordContainer.endIconDrawable = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_eye_visible
                ) // Normalno oko
                binding.editPassword.setTextAppearance(R.style.Paragraph)
                binding.editPassword.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.figmaSplashScreenColor
                    )
                )
            }
            // Postavi kursor na kraj teksta
            binding.editPassword.setSelection(binding.editPassword.text?.length ?: 0)
        }
    }

    private fun setObservers() {
        collectLatestLifecycleFlow(loginViewModel.userLogin) { result ->
            when (result) {
                is SubmitResult.Loading -> {
                    binding.progbar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    binding.progbar.visibility = View.GONE

                    lifecycleScope.launch(Dispatchers.IO) {
                        loginViewModel.insertLoginToken(
                            UserLoginResponseRoomTable(
                                null,
                                result.data.data?.accessToken,
                                result.data.data?.tokenType,
                                userName, userPassword
                            )
                        )

                        loginViewModel.storeLastUserEmail(userName)

                        loginViewModel.sendLanguage(requireContext())

                        withContext(Dispatchers.Main) {
                            findNavController().navigate(LoginFragmentDirections.actionGlobalHomeFragment())
                        }
                    }
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
        val bundle = Bundle().apply {
            putString(getString(R.string.title), getString(R.string.no_connection_title))
            putString(
                getString(R.string.subtitle),
                getString(R.string.please_connect_to_the_internet)
            )
        }
        findNavController().navigate(R.id.action_global_noInternetConnectionDialog, bundle)
    }

    companion object {
        const val TAG = "loginFragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}