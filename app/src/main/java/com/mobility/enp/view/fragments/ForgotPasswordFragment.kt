package com.mobility.enp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.login.ForgotPasswordRequest
import com.mobility.enp.databinding.FragmentForgotPasswordBinding
import com.mobility.enp.view.MainActivity
import com.mobility.enp.viewmodel.ForgotPasswordViewModel

class ForgotPasswordFragment : Fragment() {

    private lateinit var binding: FragmentForgotPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()
    private var errorBody: MutableLiveData<ErrorBody> = MutableLiveData()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_forgot_password, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        setObservers()

        binding.backForgotPass.setOnClickListener {
            val action =
                ForgotPasswordFragmentDirections.actionForgotPasswordFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        binding.bttConfirmForgotPass.setOnClickListener {
            if (binding.inputEmailForgotPass.text?.isNotBlank() == true) {
                val email = ForgotPasswordRequest(binding.inputEmailForgotPass.text.toString())

                viewModel.forgotPass(email, errorBody)
            } else {
                Toast.makeText(
                    requireContext(), getString(R.string.enter_your_email), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setObservers() {
        errorBody = MutableLiveData()
        errorBody.observe(viewLifecycleOwner) { errorBody ->
            context?.let { context ->
                if (errorBody.errorCode == 405 || errorBody.errorCode == 401) {
                    MainActivity.logoutOnInvalidToken(context, findNavController())
                }
            }
        }

        viewModel.result.observe(viewLifecycleOwner) {
            val bundle = Bundle().apply {
                putBoolean(getString(R.string.bool), it)
            }
            findNavController().navigate(
                R.id.action_global_forgotPassDialog, bundle
            )
        }

        viewModel.checkNetForgotPass.observe(viewLifecycleOwner) { hasInternet ->
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

        }
    }

}