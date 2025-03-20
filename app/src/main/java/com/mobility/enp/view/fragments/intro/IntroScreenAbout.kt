package com.mobility.enp.view.fragments.intro

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentIntroScreenAboutBinding
import com.mobility.enp.view.dialogs.NotificationsRequestDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

class IntroScreenAbout : Fragment() {

    private var _binding: FragmentIntroScreenAboutBinding? = null
    private val binding: FragmentIntroScreenAboutBinding get() = _binding!!

    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission is granted. You can proceed with sending notifications.
                sendNotification()
            }
        }
    private var isExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentIntroScreenAboutBinding.inflate(
                inflater,
                container,
                false
            )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences =
            requireContext().getSharedPreferences("IntroLanguage", Context.MODE_PRIVATE)
        val savedLanguage =
            sharedPreferences.getString("selected_Language", "sr")

        when (savedLanguage) {
            "cyr" -> {
                binding.tvSelectedLanguage?.text = getString(R.string.srp_cyr)
                binding.langTwo?.text = getString(R.string.srp)
                binding.langThree?.text = getString(R.string.eng)
            }

            "sr" -> {
                binding.tvSelectedLanguage?.text = getString(R.string.srp)
                binding.langTwo?.text = getString(R.string.srp_cyr)
                binding.langThree?.text = getString(R.string.eng)
            }

            "en" -> {
                binding.tvSelectedLanguage?.text = getString(R.string.eng)
                binding.langTwo?.text = getString(R.string.srp)
                binding.langThree?.text = getString(R.string.srp_cyr)
            }
        }


        val languageOptions: LinearLayout? = binding.languageOptions

        binding.tvSelectedLanguage?.setOnClickListener {
            toggleDropdown(languageOptions!!)
        }

        binding.langTwo?.setOnClickListener {
            toggleDropdown(languageOptions!!)
            when (binding.langTwo?.text) {
                "SRP" -> setLanguage("sr")
                "СРП" -> setLanguage("cyr")
                "ENG" -> setLanguage("en")
            }
        }

        binding.langThree?.setOnClickListener {
            toggleDropdown(languageOptions!!)
            when (binding.langThree?.text) {
                "SRP" -> setLanguage("sr")
                "СРП" -> setLanguage("cyr")
                "ENG" -> setLanguage("en")
            }
        }

        binding.buttonNext?.setOnClickListener {
            findNavController().navigate(R.id.action_introScreenAbout_to_introScreenRegions)
        }
        if (!franchiseViewModel.getDialogStatus()){
            franchiseViewModel.setDialogStatus(true)
            permissionCheck()
        }
    }

    private fun permissionCheck() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            showNotificationPermissionRationale()
        }
    }

    private fun setLanguage(languageCode: String) {
        val sharedPreferences =
            requireContext().getSharedPreferences("IntroLanguage", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("selected_Language", languageCode)
            apply()
        }

        val shared = requireContext().getSharedPreferences("AppLanguage", Context.MODE_PRIVATE)
        with(shared.edit()) {
            putString("user_language", languageCode)
            apply()
        }
        activity?.recreate()
    }

    private fun toggleDropdown(view: LinearLayout) {
        if (isExpanded) {
            animateHeight(view, view.height, 0) {
                view.visibility = View.GONE
            }
        } else {
            view.visibility = View.VISIBLE

            view.measure(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val targetHeight = view.measuredHeight

            animateHeight(view, 0, targetHeight)
        }
        isExpanded = !isExpanded
    }

    private fun sendNotification() {
        Toast.makeText(requireContext(), getString(R.string.permission_granted), Toast.LENGTH_SHORT)
            .show()
    }

    private fun showNotificationPermissionRationale() {
        lifecycleScope.launch {
            val fragmentManager = (requireContext() as AppCompatActivity).supportFragmentManager
            val generalMessageDialog = NotificationsRequestDialog(
                getString(R.string.notification_title),
                getString(R.string.notification_subtitle),
                object : NotificationsRequestDialog.OnButtonClick {
                    override fun onClickConfirmed() {
                        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )
            generalMessageDialog.show(fragmentManager, "permDialog")
        }
    }

    private fun animateHeight(
        view: View,
        startHeight: Int,
        endHeight: Int,
        onEnd: (() -> Unit)? = null
    ) {
        ValueAnimator.ofInt(startHeight, endHeight).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                view.layoutParams.height = it.animatedValue as Int
                view.requestLayout()
            }
            doOnEnd { onEnd?.invoke() }
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}