package com.mobility.enp.view.fragments.my_profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentSettingsBinding
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.view.dialogs.GeneralMessageDialogNotifications
import com.mobility.enp.view.dialogs.LanguageDialog
import com.mobility.enp.view.dialogs.NotificationsRequestDialog
import com.mobility.enp.view.fragments.LoginFragment.Companion.TAG
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission is granted. You can proceed with sending notifications.
                sendNotification()
            } else {
                binding.notificationSwitch.isChecked = isPermissionGranted()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        setFranchise()

        val fragmentManager = (requireContext() as AppCompatActivity).supportFragmentManager

        binding.notificationSwitch.setOnClickListener { _ ->
            if (isPermissionGranted()) {
                // Permission is granted, allow the switch to change its state
                binding.notificationSwitch.isChecked = true
                val generalMessageDialog = GeneralMessageDialogNotifications(
                    getString(com.mobility.enp.R.string.permissions_title),
                    getString(com.mobility.enp.R.string.permission_subtitle),
                    object : GeneralMessageDialogNotifications.OnButtonClick {
                        override fun onClickConfirmed() {
                            openAppSettings()
                        }
                    })
                generalMessageDialog.show(fragmentManager, "NotificationDialog")
            } else {
                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        sendNotification()
                    }

                    else -> {
                        showNotificationPermissionRationale()
                    }
                }
            }
        }

        binding.languageIconInSettings.setOnClickListener {
            val languageDialog = LanguageDialog { languageSelected, canSwitchLanguage ->
                if (canSwitchLanguage) {
                    SharedPreferencesHelper.setLanguageChanged(requireContext(), true)
                    SharedPreferencesHelper.setUserLanguage(requireContext(), languageSelected)

                    activity?.recreate()
                    viewModel.sendingLangToServer()
                } else {
                    Log.d(
                        TAG,
                        "data registered : $languageSelected $canSwitchLanguage"
                    )  // to be implemented
                    Toast.makeText(
                        requireContext(),
                        "Language not available",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            languageDialog.show(parentFragmentManager, "languageDialog")
        }
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

                    override fun onClickRejected() {
                        binding.notificationSwitch.isChecked = isPermissionGranted()
                    }
                }
            )
            generalMessageDialog.show(fragmentManager, "permDialog")
        }
    }

    private fun setFranchise() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.let {
                binding.notificationSwitch.trackTintList = franchiseModel.navHomeDrawable
                binding.languageIconInSettings.setImageResource(franchiseModel.languageIcon)

            } ?: run {
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),  // When switch is ON
                    intArrayOf(-android.R.attr.state_checked) // When switch is OFF
                )

                val colors = intArrayOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.figmaSplashScreenColor
                    ),  // ON color
                    ContextCompat.getColor(requireContext(), R.color.white) // OFF color
                )

                val colorStateList = ColorStateList(states, colors)
                binding.notificationSwitch.trackTintList = colorStateList
            }
        }
    }

    private fun sendNotification() {
        binding.notificationSwitch.isChecked = isPermissionGranted()
        Toast.makeText(requireContext(), getString(R.string.permission_granted), Toast.LENGTH_SHORT)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context?.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()
        binding.notificationSwitch.isChecked = isPermissionGranted()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}