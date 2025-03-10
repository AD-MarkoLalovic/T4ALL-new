package com.mobility.enp.view.fragments.my_profile

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.databinding.FragmentChangePasswordBinding
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.dialogs.ChangePasswordDialog
import com.mobility.enp.viewmodel.ChangePasswordViewModel
import com.mobility.enp.viewmodel.FranchiseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ChangePasswordFragment : Fragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    private val viewModel: ChangePasswordViewModel by viewModels()
    private var errorBody: MutableLiveData<ErrorBody> = MutableLiveData()
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        setObservers()
        setFranchiser()

        binding.btChangePassword.setOnClickListener {
            val oldPassword = binding.enterOldPassword.text.toString()
            val newPassword = binding.enterNewPassword.text.toString()
            val repeatPassword = binding.enterRepeatPassword.text.toString()

            validatePassword(oldPassword, newPassword, repeatPassword)
        }

        val color = franchiseViewModel.franchiseModel.value?.franchisePrimaryColor
        if (color == null) {
            requireContext().resources.getColor(R.color.figmaSplashScreenColor, null)
        }

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
                        color!!
                    )

                } else {
                    enterOldPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    enterOldPasswordLayout.endIconDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_eye_visible
                    )
                    enterOldPassword.setTextAppearance(R.style.Paragraph)
                    enterOldPassword.setTextColor(
                        color!!
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
                        color!!
                    )

                } else {
                    enterNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    enterNewPasswordLayout.endIconDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_eye_visible
                    )
                    enterNewPassword.setTextAppearance(R.style.Paragraph)
                    enterNewPassword.setTextColor(
                        color!!
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
                        color!!
                    )

                } else {
                    enterRepeatPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    enterRepeatPasswordLayout.endIconDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_eye_visible
                    )
                    enterRepeatPassword.setTextAppearance(R.style.Paragraph)
                    enterRepeatPassword.setTextColor(
                        color!!
                    )
                }

                enterRepeatPassword.setSelection(enterRepeatPassword.text?.length ?: 0)
            }
        }

        observeChangePasswordStatus()

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
                        editText?.setTextColor(color)
                    }
                }
            }
        }
    }

    private fun setObservers() {
        errorBody = MutableLiveData()
        errorBody.observe(viewLifecycleOwner, Observer { errorBody ->
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
        })

        viewModel.checkNetChangePass.observe(viewLifecycleOwner, Observer { hasInternet ->
            if (hasInternet != null && !hasInternet) {

                val bundle = Bundle().apply {
                    putString(getString(R.string.title), getString(R.string.no_connection_title))
                    putString(
                        getString(R.string.subtitle),
                        getString(R.string.please_connect_to_the_internet)
                    )
                }

                findNavController().navigate(R.id.action_global_noInternetConnectionDialog, bundle)

                val binding = (activity as MainActivity).binding
                MainActivity.showSnackMessage(getString(R.string.checking_for_connection), binding)

            }
        })
    }

    private fun validatePassword(oldPassword: String, newPassword: String, repeatPassword: String) {
        lifecycleScope.launch {
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
                        viewModel.changePassword(oldPassword, newPassword, errorBody)
                    }
                }
            }
        }
    }

    private suspend fun isOldPasswordCorrect(oldPassword: String): Boolean {
        return lifecycleScope.async {
            val currentPassword = viewModel.getUserPassword()
            currentPassword == oldPassword
        }.await()
    }

    private fun observeChangePasswordStatus() {
        viewModel.changePasswordStatusLiveData.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                showDialogChangePassword()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.password_change_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showDialogChangePassword() {
        val dialog = ChangePasswordDialog(
            getString(R.string.password_changed),
            getString(R.string.you_will_be_asked_to_login_again)
        ) {
            MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
        }
        dialog.isCancelable = false
        dialog.show(childFragmentManager, "ChangePasswordFragment")
    }


}