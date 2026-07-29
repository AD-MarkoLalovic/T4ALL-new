package com.mobility.enp.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.mobility.enp.Config
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentSplashScreenBinding
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.viewmodel.SplashAndIntroScreensViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment() {

    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SplashAndIntroScreensViewModel by viewModels {
        SplashAndIntroScreensViewModel.factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeNavigation()
    }

    private fun observeNavigation() {

        viewLifecycleOwner.lifecycleScope.launch {

            val userToken = runCatching {
                viewModel.fetchUserToken()
            }.getOrNull()

            delay(Config.SLASH_SCREEN_TIME)

            if (!isFragmentSafe()) return@launch

            navigateNext(userToken)
        }
    }

    private fun navigateNext(token: String?) {

        val navController = findNavController()

        // Prevent stale navigation calls
        if (navController.currentDestination?.id != R.id.splashScreenFragment) {
            return
        }

        when {

            SharedPreferencesHelper.isFirstLaunch(requireContext()) -> {

                navController.navigateSafe(
                    R.id.action_splashScreenFragment_to_introScreenAbout
                )
            }

            !token.isNullOrBlank() -> {

                navController.navigate(
                    R.id.homeFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.splashScreenFragment, true)
                        .build()
                )
            }

            else -> {

                navController.navigateSafe(
                    R.id.action_splashScreenFragment_to_loginFragment
                )
            }
        }
    }

    /**
     * Prevent navigation after fragment/view destruction
     */
    private fun isFragmentSafe(): Boolean {
        return isAdded &&
                view != null &&
                _binding != null
    }

    /**
     * Safe navigation extension
     */
    private fun NavController.navigateSafe(actionId: Int) {

        val action = currentDestination?.getAction(actionId)

        if (action != null) {
            navigate(actionId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}