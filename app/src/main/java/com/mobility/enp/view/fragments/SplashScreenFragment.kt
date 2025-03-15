package com.mobility.enp.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.mobility.enp.Config
import com.mobility.enp.R
import com.mobility.enp.data.model.api_room_models.UserLoginResponseRoomTable
import com.mobility.enp.databinding.FragmentSplashScreenBinding
import com.mobility.enp.util.IntroScreensRepository
import com.mobility.enp.viewmodel.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment() {

    private lateinit var binding: FragmentSplashScreenBinding
    private lateinit var action: NavDirections
    private val loginViewModel: LoginViewModel by viewModels { LoginViewModel.Factory }
    private var userData: UserLoginResponseRoomTable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_splash_screen, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            userData = loginViewModel.getUserToken()

            proceedAfterDatabaseInitialization()
        }

    }

    private suspend fun proceedAfterDatabaseInitialization() {

        delay(Config.SLASH_SCREEN_TIME)

        val introPageShown =
            IntroScreensRepository(requireContext()).getIntroPageShow(Config.introKey)

        if (!userData?.accessToken.isNullOrEmpty()) {  // token exists user loged in has to use logout to return to login
            action = SplashScreenFragmentDirections.actionSplashScreenFragmentToHomeFragment()
        } else if (introPageShown == true) { // intro pages have been shown before
            action = SplashScreenFragmentDirections.actionSplashScreenFragmentToLoginFragment()
        } else { // show intro pages
            action =
                SplashScreenFragmentDirections.actionSplashScreenFragmentToIntroScreenFragment()
        }

        findNavController().navigate(action)

    }

}