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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.mobility.enp.BuildConfig
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.data.model.login.LoginBody
import com.mobility.enp.data.model.login.UserResponse
import com.mobility.enp.databinding.FragmentLoginBinding
import com.mobility.enp.network.Repository
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.dialogs.LanguageDialog
import com.mobility.enp.viewmodel.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding: FragmentLoginBinding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels()
    private lateinit var userName: String
    private lateinit var userPassword: String
    private var errorBody: MutableLiveData<ErrorBody> = MutableLiveData()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFcmToken()

        context?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                loginViewModel.initDatabase()
            }
        }

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

                context?.let {
                    if (Repository.isNetworkAvailable(it)) {
                        binding.progbar.visibility = View.VISIBLE
                        CoroutineScope(Dispatchers.IO).launch {
                            loginViewModel.loginUser(it, body, errorBody)
                        }
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

                    val action = LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment()
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

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                loginViewModel.writeFcmToken(token)
            }

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
        loginViewModel.loginLiveData = MutableLiveData<UserResponse>()
        errorBody = MutableLiveData()

        errorBody.observe(viewLifecycleOwner) { errorBody ->
            binding.progbar.visibility = View.GONE

            context?.let { context ->
                Toast.makeText(
                    context,
                    errorBody.errorBody,
                    Toast.LENGTH_SHORT
                ).show()
                if (errorBody.errorCode == 405 || errorBody.errorCode == 401) {
                    MainActivity.logoutOnInvalidToken(context, findNavController())
                }
            }
        }

        if (!BuildConfig.DEBUG) {
            loginViewModel.lastUserEmail.observe(viewLifecycleOwner) { lastUser ->
                binding.editEmail.setText(lastUser.email)
            }
        }

        loginViewModel.loginLiveData.observe(viewLifecycleOwner) { userServerResponse ->
            binding.progbar.visibility = View.GONE

            lifecycleScope.launch(Dispatchers.IO) {
                loginViewModel.insertLoginToken(
                    UserLoginResponseRoomTable(
                        null,
                        userServerResponse.data?.accessToken,
                        userServerResponse.data?.tokenType,
                        userServerResponse.message,
                        userName, userPassword
                    ), errorBody
                )

                loginViewModel.storeLastUserEmail(userName)

                loginViewModel.sendLanguage()

                withContext(Dispatchers.Main) {
                    findNavController().navigate(LoginFragmentDirections.actionGlobalHomeFragment())
                }
            }
        }
    }

    companion object {
        const val TAG = "loginFragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}