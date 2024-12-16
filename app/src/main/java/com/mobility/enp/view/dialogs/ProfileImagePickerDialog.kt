package com.mobility.enp.view.dialogs

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
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
import com.mobility.enp.databinding.DialogChangeProfilePictureBinding

class ProfileImagePickerDialog(private val imageSelectionListener: ImagePickDialogListener,val imageExists:Boolean) : DialogFragment() {

    private lateinit var binding: DialogChangeProfilePictureBinding

    // Contract za pokretanje kamere i dobivanje rezultata
    private val takePictureContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap?
                imageSelectionListener.onImageSelected(imageBitmap)

                dismiss()
            }
        }

    // Contract za odabir slike iz galerije i dobivanje rezultata
    private val pickGalleryImageContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data
                val imageBitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().contentResolver,
                    selectedImageUri
                )
                imageSelectionListener.onImageSelected(imageBitmap)

                dismiss()
            }
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Dozvola za kameru je odobrena, pokreni intent za snimanje slike
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                try {
                    takePictureContract.launch(takePictureIntent)
                } catch (e: ActivityNotFoundException) {
                    // Handle error if camera is not available
                }
            } else {
                // Korisnik nije odobrio dozvolu za kameru, prikaži poruku ili obavijestite korisnika
                Toast.makeText(
                    requireContext(),
                    "Potrebna je dozvola za pristup kameri.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = DialogChangeProfilePictureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (imageExists){
            binding.bttDelete.visibility = View.VISIBLE
        }else{
            binding.bttDelete.visibility = View.GONE
        }

        binding.bttFromCamera.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.bttFromGallery.setOnClickListener {
            openGallery()
        }

        binding.bttDelete.setOnClickListener {
            imageSelectionListener.onDeleteImage()
            dismiss()
        }

        binding.changeProfilePictureDialogClose.setOnClickListener {
            dismiss()
        }
    }


    private fun dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Dozvola za kameru je već odobrena, pokreni intent za snimanje slike
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                takePictureContract.launch(takePictureIntent)
            } catch (e: ActivityNotFoundException) {
                // Handle error if camera is not available
            }
        } else {
            // Dozvola za kameru nije odobrena, zatraži dozvolu od korisnika
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickGalleryImageContract.launch(galleryIntent)
    }

    interface ImagePickDialogListener {
        fun onImageSelected(imageBitmap: Bitmap?)
        fun onDeleteImage()
    }

    override fun onStart() {
        super.onStart()
        setWidthPercent(95)
        isCancelable = false
    }

    private fun DialogFragment.setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }


}