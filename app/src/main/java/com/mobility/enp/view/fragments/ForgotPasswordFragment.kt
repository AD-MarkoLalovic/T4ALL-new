package com.mobility.enp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.data.model.login.ForgotPasswordRequest
import com.mobility.enp.databinding.FragmentForgotPasswordBinding
import com.mobility.enp.util.SubmitResultCustomerSupport
import com.mobility.enp.view.MainActivity
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
        viewModel.submitSuccessful.observe(viewLifecycleOwner) { result ->

            when (result) {
                is SubmitResultCustomerSupport.Success -> {
                    val bundle = Bundle().apply {
                        putBoolean(getString(R.string.bool), true)
                    }
                    findNavController().navigate(
                        R.id.action_global_forgotPassDialog, bundle
                    )
                }

                is SubmitResultCustomerSupport.Failure -> {
                    val bundle = Bundle().apply {
                        putBoolean(getString(R.string.bool), false)
                    }
                    findNavController().navigate(
                        R.id.action_global_forgotPassDialog, bundle
                    )
                }

                is SubmitResultCustomerSupport.NoNetwork -> {
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

                    val binding = (activity as MainActivity).binding
                    MainActivity.showSnackMessage(
                        getString(R.string.checking_for_connection),
                        binding
                    )
                }

                else -> {}
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}