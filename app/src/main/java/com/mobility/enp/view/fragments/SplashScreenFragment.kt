package com.mobility.enp.view.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mobility.enp.Config
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentSplashScreenBinding
import com.mobility.enp.viewmodel.SplashAndIntroScreensViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment() {

    private lateinit var binding: FragmentSplashScreenBinding
    private val viewModel: SplashAndIntroScreensViewModel by viewModels { SplashAndIntroScreensViewModel.factory  }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            val userToken = viewModel.fetchUserToken()
            delay(Config.SLASH_SCREEN_TIME)

            setNavigation(userToken)
        }
    }

    private fun setNavigation(token: String?) {
        val sharedPreferences =
            requireContext().getSharedPreferences("FirstLaunch", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch) {
            with(sharedPreferences.edit()) {
                putBoolean("isFirstLaunch", false)
                apply()
            }
            findNavController().navigate(R.id.action_splashScreenFragment_to_introScreenAbout)
        } else {
            if (token != null) {
                findNavController().navigate(R.id.action_splashScreenFragment_to_homeFragment)
            } else {
                findNavController().navigate(R.id.action_splashScreenFragment_to_loginFragment)
            }
        }
    }

}