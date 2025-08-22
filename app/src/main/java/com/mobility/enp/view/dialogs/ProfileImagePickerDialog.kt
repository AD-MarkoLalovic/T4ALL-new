package com.mobility.enp.view.dialogs

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.mobility.enp.databinding.DialogChangeProfilePictureBinding
import com.mobility.enp.util.setDimensionsPercent
import com.mobility.enp.viewmodel.FranchiseViewModel
import androidx.core.graphics.drawable.toDrawable
import com.mobility.enp.R
import com.mobility.enp.util.FragmentResultKeys
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.toast

class ProfileImagePickerDialog(
    private val imageSelectionListener: ImagePickDialogListener,
    val imageExists: Boolean
) : DialogFragment() {

    private var _binding: DialogChangeProfilePictureBinding? = null
    private val binding: DialogChangeProfilePictureBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    // Kamera contract
    private val takePictureContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap?
                imageSelectionListener.onImageSelected(imageBitmap)
                dismiss()
            }
        }

    private val pickProfileImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            imageSelectionListener.onImageSelected(bitmap)
            dismiss()
        }
    }

    // Camera permisija launcher
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                SharedPreferencesHelper.resetPermissionDenyCount(
                    requireContext(),
                    "camera_deny_count"
                )
                dispatchTakePictureIntent()
            } else {
                SharedPreferencesHelper.incrementPermissionDenyCount(
                    requireContext(),
                    "camera_deny_count"
                )

                val denyCount = SharedPreferencesHelper.getPermissionDenyCount(
                    requireContext(),
                    "camera_deny_count"
                )
                if (denyCount > 2) {
                    cameraPermissionDeniedDialog()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        _binding = DialogChangeProfilePictureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionDeniedDialogResultListener()

        binding.bttDelete.visibility = if (imageExists) View.VISIBLE else View.GONE

        binding.bttFromCamera.setOnClickListener { dispatchTakePictureIntent() }
        binding.bttFromGallery.setOnClickListener { openGallery() }
        binding.bttDelete.setOnClickListener {
            imageSelectionListener.onDeleteImage()
            dismiss()
        }
        binding.changeProfilePictureDialogClose.setOnClickListener { dismiss() }

        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { model ->
            model?.let {
                binding.bttFromGallery.backgroundTintList =
                    ColorStateList.valueOf(it.franchisePrimaryColor)
                binding.bttFromCamera.setImageResource(it.cameraResource)
                binding.changeProfilePictureDialogClose.setImageResource(it.franchiseCloseButton)
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                takePictureContract.launch(takePictureIntent)
            } catch (e: ActivityNotFoundException) {
                toast(getString(R.string.camera_permission_required_message))
            }
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openGallery() {
        pickProfileImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }


    private fun cameraPermissionDeniedDialog() {
        PermissionDeniedDialog.newInstance(
            title = requireContext().getString(R.string.permission_denied_message),
            subtitle = requireContext().getString(R.string.camera_permission_required_message),
            resultKey = FragmentResultKeys.CAMERA_PERMISSION_RESULT,
            resultValueKey = FragmentResultKeys.CAMERA_PERMISSION_CONFIRMED
        ).show(childFragmentManager, "CameraPermissionDeniedDialog")
    }

    private fun permissionDeniedDialogResultListener() {
        childFragmentManager.setFragmentResultListener(
            FragmentResultKeys.CAMERA_PERMISSION_RESULT,
            viewLifecycleOwner
        ) { _, bundle ->
            val clickSettings =
                bundle.getBoolean(FragmentResultKeys.CAMERA_PERMISSION_CONFIRMED, false)
            if (clickSettings) {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", requireContext().packageName, null)
                startActivity(intent)
            }
        }
    }

    interface ImagePickDialogListener {
        fun onImageSelected(imageBitmap: Bitmap?)
        fun onDeleteImage()
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
