package com.mobility.enp.view.fragments.my_profile

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mobility.enp.databinding.FragmentSettingsBinding
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.dialogs.GeneralMessageDialogNotifications
import com.mobility.enp.view.dialogs.LanguageDialog
import com.mobility.enp.view.fragments.LoginFragment.Companion.TAG

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        context?.let {
            val fragmentManager = (it as AppCompatActivity).supportFragmentManager

            binding.notificationSwitch.setOnClickListener { _ ->
                if (isPermissionGranted(it)) {
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
                    generalMessageDialog.show(fragmentManager, "NotificationDiag")
                } else {
                    Dexter.withContext(it)
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
                                p1?.continuePermissionRequest()
                            }

                        }).check()
                }
            }
        }

        binding.languageIconInSettings.setOnClickListener {
            val languageDialog = LanguageDialog { languageSelected, canSwitchLanguage ->
                if (canSwitchLanguage) {
                    MainActivity.setLocale(requireContext(), languageSelected)
                    activity?.let { act ->
                        act.recreate()
                    }
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

}