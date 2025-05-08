package com.mobility.enp.view.dialogs

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.mobility.enp.databinding.DialogChangeProfilePictureBinding
import com.mobility.enp.databinding.LoginNotificationDialogBinding
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.FranchiseViewModel

class LoginNotificationDialog() : DialogFragment() {

    private var _binding: LoginNotificationDialogBinding? = null
    private val binding: LoginNotificationDialogBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = LoginNotificationDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.let { model ->
                model.franchisePrimaryColor.let { color ->
                    binding.txChangeProfilePicture.setTextColor(color)
                    binding.txContentDialogChangeProfilePicture.setTextColor(color)
                }
                binding.changeProfilePictureDialogClose.setImageResource(model.franchiseCloseButton)
            }

        }
    }

    override fun onStart() {
        super.onStart()
        setDimensionsPercent(95)
        isCancelable = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}