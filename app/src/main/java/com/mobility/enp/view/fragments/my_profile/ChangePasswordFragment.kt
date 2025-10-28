package com.mobility.enp.view.fragments.my_profile

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.mobility.enp.R
import com.mobility.enp.data.model.api_my_profile.ChangePasswordRequest
import com.mobility.enp.databinding.FragmentChangePasswordBinding
import com.mobility.enp.util.FragmentResultKeys
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResultFold
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.util.toast
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.dialogs.ChangePasswordDialog
import com.mobility.enp.viewmodel.ChangePasswordViewModel
import com.mobility.enp.viewmodel.FranchiseViewModel
import kotlinx.coroutines.launch

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding: FragmentChangePasswordBinding get() = _binding!!
    private val viewModel: ChangePasswordViewModel by viewModels { ChangePasswordViewModel.factory }
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    private var currentPassword: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        setupLostTagDialogResultListener()
        setFranchiser()

        binding.btChangePassword.setOnClickListener {
            val oldPassword = binding.enterOldPassword.text.toString()
            val newPassword = binding.enterNewPassword.text.toString()
            val repeatPassword = binding.enterRepeatPassword.text.toString()

            validatePassword(oldPassword, newPassword, repeatPassword)
        }

        val color = franchiseViewModel.franchiseModel.value?.franchisePrimaryColor
            ?: requireContext().resources.getColor(R.color.figmaSplashScreenColor, null)

        with(binding) {
            enterOldPassword.setOnClickListener {
                if (enterOldPassword.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    enterOldPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    enterOldPasswordLayout.endIconDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_eye_invisible
                    )
                    enterOldPassword.setTextAppearance(R.style.Paragraph)
                    enterOldPassword.setTextColor(
                        color
                    )

                } else {
                    enterOldPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    enterOldPasswordLayout.endIconDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_eye_visible
                    )
                    enterOldPassword.setTextAppearance(R.style.Paragraph)
                    enterOldPassword.setTextColor(
                        color
                    )
                }

                enterOldPassword.setSelection(enterOldPassword.text?.length ?: 0)
            }

            enterNewPassword.setOnClickListener {
                if (enterNewPassword.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    enterNewPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    enterNewPasswordLayout.endIconDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_eye_invisible
                    )
                    enterNewPassword.setTextAppearance(R.style.Paragraph)
                    enterNewPassword.setTextColor(
                        color
                    )

                } else {
                    enterNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    enterNewPasswordLayout.endIconDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_eye_visible
                    )
                    enterNewPassword.setTextAppearance(R.style.Paragraph)
                    enterNewPassword.setTextColor(
                        color
                    )
                }

                enterNewPassword.setSelection(enterNewPassword.text?.length ?: 0)
            }

            enterRepeatPassword.setOnClickListener {
                if (enterRepeatPassword.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    enterRepeatPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    enterRepeatPasswordLayout.endIconDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_eye_invisible
                    )
                    enterRepeatPassword.setTextAppearance(R.style.Paragraph)
                    enterRepeatPassword.setTextColor(
                        color
                    )

                } else {
                    enterRepeatPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    enterRepeatPasswordLayout.endIconDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_eye_visible
                    )
                    enterRepeatPassword.setTextAppearance(R.style.Paragraph)
                    enterRepeatPassword.setTextColor(
                        color
                    )
                }

                enterRepeatPassword.setSelection(enterRepeatPassword.text?.length ?: 0)
            }
        }


    }

    private fun setFranchiser() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.btChangePassword.backgroundTintList = ColorStateList.valueOf(color)


                val parent = binding.constaintLayout

                for (i in 0 until parent.childCount) {
                    val view = parent.getChildAt(i)
                    if (view is TextInputLayout) {
                        view.boxStrokeColor = color
                        val editText = view.editText
                        editText?.textSelectHandle?.setTint(color)
                        editText?.setTextColor(color)

                        val states = arrayOf(
                            intArrayOf(android.R.attr.state_pressed),  // pressed
                            intArrayOf(android.R.attr.state_focused),  // focused
                            intArrayOf()                               // default
                        )

                        val colors = intArrayOf(
                            color,        // pressed
                            color,        // focused
                            color         // default
                        )

                        view.cursorColor = ColorStateList(states, colors)
                    }
                }
            }
        }
    }

    private fun setObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            val pass = viewModel.getPass()
            currentPassword = pass
        }

        collectLatestLifecycleFlow(viewModel.changePassword) { resultFold ->
            when (resultFold) {
                is SubmitResultFold.Failure -> handleError(resultFold.error)
                SubmitResultFold.Idle -> {}
                SubmitResultFold.Loading -> binding.progressBarChangePass.visibility = View.VISIBLE
                is SubmitResultFold.Success<*> -> showDialogChangePassword()
            }
        }
    }

    private fun validatePassword(oldPassword: String, newPassword: String, repeatPassword: String) {
        // Provera da li je polje za staru lozinku prazno ili neispravno
        if (oldPassword.isEmpty() || !isOldPasswordCorrect(oldPassword)) {
            binding.enterOldPasswordLayout.apply {
                error = if (oldPassword.isEmpty()) {
                    getString(R.string.enter_old_password_message)
                } else {
                    getString(R.string.incorrect_old_password_message)
                }
                isErrorEnabled = true // Podesavanje errorEnabled na true
                errorIconDrawable = null // Uklanjanje ikone greške
            }
        } else {
            binding.enterOldPasswordLayout.error = null
            binding.enterOldPasswordLayout.isErrorEnabled = false // Ponistenje errora

            // Provera dužine nove lozinke
            if (newPassword.length < 8) {
                binding.enterNewPasswordLayout.apply {
                    error = getString(R.string.password_error_message)
                    isErrorEnabled = true
                    errorIconDrawable = null
                }
            } else {
                binding.enterNewPasswordLayout.error = null
                binding.enterNewPasswordLayout.isErrorEnabled = false

                // Provera da li se ponovljena lozinka podudara sa novom lozinkom ili da li je prazno polje
                if (newPassword != repeatPassword || repeatPassword.isEmpty()) {
                    binding.enterRepeatPasswordLayout.apply {
                        error = if (repeatPassword.isEmpty()) {
                            getString(R.string.repeat_the_new_password)
                        } else {
                            getString(R.string.password_repeat_error_message)
                        }
                        isErrorEnabled = true
                        errorIconDrawable = null
                    }
                } else {
                    binding.enterRepeatPasswordLayout.error = null
                    binding.enterRepeatPasswordLayout.isErrorEnabled =
                        false // Ponistenje errora

                    // Ako je sve validno, promeni lozinku
                    viewModel.passwordChange(
                        ChangePasswordRequest(
                            oldPassword = oldPassword,
                            newPassword = newPassword,
                            newPasswordConfirmation = repeatPassword
                        )
                    )
                }
            }
        }

    }

    private fun isOldPasswordCorrect(oldPassword: String): Boolean {
        return currentPassword == oldPassword
    }

    private fun handleError(error: Throwable) {
        when (error) {
            is NetworkError.ServerError -> {
                binding.progressBarChangePass.visibility = View.GONE
                toast(getString(R.string.server_error_msg))
            }

            is NetworkError.ApiError -> {
                binding.progressBarChangePass.visibility = View.GONE
                toast(
                    error.errorResponse.message ?: getString(R.string.server_error_msg)
                )
            }

            is NetworkError.NoConnection -> {
                noInternetMessage()
            }
        }
        viewModel.resetChangePasswordState()
    }

    private fun noInternetMessage() {
        val bundle = Bundle().apply {
            putString(getString(R.string.title), getString(R.string.no_connection_title))
            putString(
                getString(R.string.subtitle),
                getString(R.string.please_connect_to_the_internet)
            )
        }

        findNavController().navigate(R.id.action_global_noInternetConnectionDialog, bundle)
    }

    private fun setupLostTagDialogResultListener() {
        childFragmentManager.setFragmentResultListener(
            FragmentResultKeys.CHANGE_PASS_RESULT,
            viewLifecycleOwner
        ) { _, bundle ->
            val confirmed = bundle.getBoolean(FragmentResultKeys.CHANGE_PASS_CONFIRMED, false)
            if (confirmed) {
                MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
            }
        }
    }

    private fun showDialogChangePassword() {
        ChangePasswordDialog.newInstance(
            title = requireContext().getString(R.string.password_changed),
            subtitle = requireContext().getString(R.string.you_will_be_asked_to_login_again),
            resultKey = FragmentResultKeys.CHANGE_PASS_RESULT,
            resultValueKey = FragmentResultKeys.CHANGE_PASS_CONFIRMED
        ).show(childFragmentManager, "ChangePasswordFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}