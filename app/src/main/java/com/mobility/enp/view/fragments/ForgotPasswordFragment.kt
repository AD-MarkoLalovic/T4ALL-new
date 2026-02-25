package com.mobility.enp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mobility.enp.NavigationDirections
import com.mobility.enp.R
import com.mobility.enp.data.model.login.ForgotPasswordRequest
import com.mobility.enp.databinding.FragmentForgotPasswordBinding
import com.mobility.enp.util.ForgotPasswordUiState
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.util.toast
import com.mobility.enp.viewmodel.ForgotPasswordViewModel

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding: FragmentForgotPasswordBinding get() = _binding!!
    private val viewModel: ForgotPasswordViewModel by viewModels { ForgotPasswordViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        clickListener()
    }

    private fun clickListener() {
        binding.backForgotPass.setOnClickListener {
            val action =
                ForgotPasswordFragmentDirections.actionForgotPasswordFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        binding.bttConfirmForgotPass.setOnClickListener {
            if (binding.inputEmailForgotPass.text?.isNotBlank() == true) {
                val email = ForgotPasswordRequest(binding.inputEmailForgotPass.text.toString())

                viewModel.forgotPass(email)
            } else {
                Toast.makeText(
                    requireContext(), getString(R.string.enter_your_email), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setObservers() {
        collectLatestLifecycleFlow(viewModel.submitSuccessful) { result ->
            when (result) {
                is ForgotPasswordUiState.Loading -> binding.progressBarForgotPass.visibility =
                    View.VISIBLE

                is ForgotPasswordUiState.Success -> {
                    binding.progressBarForgotPass.visibility =
                        View.GONE

                    viewModel.clearState()

                    val action = NavigationDirections.actionGlobalForgotPassDialog(
                        sentSuccessfully = true,
                        message = null
                    )
                    findNavController().navigate(action)
                }

                is ForgotPasswordUiState.Failure -> {
                    binding.progressBarForgotPass.visibility =
                        View.GONE

                    when (result.error) {
                        is NetworkError.NoConnection -> {
                            viewModel.clearState()
                            noInternet()
                        }

                        is NetworkError.ServerError -> toast(getString(R.string.server_error_msg))

                        is NetworkError.ApiError -> {
                            viewModel.clearState()
                            val action = NavigationDirections.actionGlobalForgotPassDialog(
                                sentSuccessfully = false,
                                message = result.error.errorResponse.message
                                    ?: getString(R.string.subtitle_dialog_forgot_pass)
                            )
                            findNavController().navigate(action)
                        }
                    }
                }
                ForgotPasswordUiState.Idle -> {}
            }
        }
    }

    private fun noInternet() {
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
            R.id.action_global_noInternetConnectionDialog,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearState()
        _binding = null
    }

}