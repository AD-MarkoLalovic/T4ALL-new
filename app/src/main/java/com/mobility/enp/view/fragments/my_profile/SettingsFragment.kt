package com.mobility.enp.view.fragments.my_profile

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentSettingsBinding
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.dialogs.GeneralMessageDialogNotifications
import com.mobility.enp.view.dialogs.LanguageDialog
import com.mobility.enp.view.fragments.LoginFragment.Companion.TAG
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.SettingsViewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

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
            if (isPermissionGranted(requireContext())) {
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
                Dexter.withContext(requireContext())
                    .withPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                            Log.d(
                                MainActivity.TAG,
                                "onPermissionGranted: can get notifications"
                            )
                        }

                        override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                            Log.d(MainActivity.TAG, "onPermissionDenied: denied")
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            p0: PermissionRequest?,
                            p1: PermissionToken?
                        ) {
                            showCustomPermissionDialog(p1)
                        }

                    })
                    .withErrorListener { error -> Log.d("Dexter", "Error: ${error.toString()}") }
                    .check()
            }
        }

        binding.languageIconInSettings.setOnClickListener {
            val languageDialog = LanguageDialog { languageSelected, canSwitchLanguage ->
                if (canSwitchLanguage) {
                    MainActivity.setLocale(requireContext(), languageSelected)
                    activity?.let { act ->
                        act.recreate()
                    }
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

    private fun showCustomPermissionDialog(token: PermissionToken?) {
        AlertDialog.Builder(requireContext())
            .setTitle("Dozvolite T4A da vam šalje notifikacije")
            .setMessage("Notifikacije služe za preuzimanje računa i export tabela u istoriji prolazaka.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                Handler(Looper.getMainLooper()).postDelayed({
                    token?.continuePermissionRequest()
                }, 200) // Short delay to prevent overlapping dialogs
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                token?.cancelPermissionRequest()
            }
            .show()
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

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context?.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun isPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            binding.notificationSwitch.isChecked = isPermissionGranted(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}