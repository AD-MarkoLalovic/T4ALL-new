package com.mobility.enp.view.fragments.my_profile

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentAddTagBinding
import com.mobility.enp.util.FragmentResultKeys
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResultFold
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.dialogs.ChangePasswordDialog
import com.mobility.enp.viewmodel.AddTagViewModel
import com.mobility.enp.viewmodel.FranchiseViewModel

class AddTagFragment : Fragment() {

    private var _binding: FragmentAddTagBinding? = null
    private val binding: FragmentAddTagBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: AddTagViewModel by viewModels { AddTagViewModel.factory }

    private lateinit var countryCode: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        countryCode = viewModel.getCountryCode().orEmpty()

        setObservers()
        setFranchiser()
        setVerificationTextByCountryCode(countryCode)

        binding.bttConfirmAddTag.setOnClickListener {
            val serial = binding.serialNumber.text.toString().trim()
            val verification = binding.verificationCode.text.toString().trim()

            if (serial.isNotEmpty() && verification.isNotEmpty()
            ) {
                if (countryCode == "ME") {
                    viewModel.addNewTag(serial, verification, true)
                } else {
                    viewModel.addNewTag(serial, verification, false)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.please_enter_all_required_data,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun setFranchiser() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.bttConfirmAddTag.backgroundTintList = ColorStateList.valueOf(color)

                val parent = binding.constraintLayout

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
        collectLatestLifecycleFlow(viewModel.addTag) { result ->
            when (result) {
                is SubmitResultFold.Failure -> {
                    handleError(result.error)
                }

                SubmitResultFold.Idle -> {}
                SubmitResultFold.Loading -> {
                    binding.progBarAddTag.visibility = View.VISIBLE
                }

                is SubmitResultFold.Success<*> -> {
                    setupChangePasswordResultListener()
                    showSuccessAddedTagDialog()
                    clearInputFields()
                    viewModel.resetAddTagState()
                }
            }
        }
    }

    private fun setupChangePasswordResultListener() {
        childFragmentManager.setFragmentResultListener(
            FragmentResultKeys.SUCCESS_ADDED_TAG,
            viewLifecycleOwner
        ) { _, bundle ->
            val confirmed = bundle.getBoolean(FragmentResultKeys.ADDED_TAG_CONFIRMED, false)
            if (confirmed) {
                val action = AddTagFragmentDirections.actionAddTagFragmentToMyTagsFragment2()
                findNavController().navigate(action)
            }
        }
    }

    private fun showSuccessAddedTagDialog() {
        ChangePasswordDialog.newInstance(
            title = requireContext().getString(R.string.add_tag),
            subtitle = requireContext().getString(R.string.tag_added_successfully),
            resultKey = FragmentResultKeys.SUCCESS_ADDED_TAG,
            resultValueKey = FragmentResultKeys.ADDED_TAG_CONFIRMED
        ).show(childFragmentManager, "SuccessAddedTagDialog")
    }

    private fun handleError(error: Throwable) {
        when (error) {
            is NetworkError.ServerError -> {
                binding.progBarAddTag.visibility = View.GONE
                showToastMessage(getString(R.string.server_error_msg))
            }

            is NetworkError.ApiError -> {
                binding.progBarAddTag.visibility = View.GONE
                showToastMessage(
                    error.errorResponse.message ?: getString(R.string.server_error_msg)
                )
            }

            is NetworkError.NoConnection -> {
                noInternetMessage()
            }
        }
    }

    private fun setVerificationTextByCountryCode(code: String) {
        if (code == "ME") {
            binding.txVerificationCodeAddTag.text = getString(R.string.confirm_serial_number)
            binding.verificationCode.hint = getString(R.string.confirm_serial_number)
        } else {
            binding.txVerificationCodeAddTag.text = getString(R.string.verification_code)
            binding.verificationCode.hint = getString(R.string.hint_enter_verification_code)
        }
    }


    private fun showToastMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun noInternetMessage() {
        val mainBinding = (activity as MainActivity).binding
        MainActivity.showSnackMessage(getString(R.string.no_internet), mainBinding)
    }

    private fun clearInputFields() {
        binding.serialNumber.setText("")
        binding.verificationCode.setText("")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}