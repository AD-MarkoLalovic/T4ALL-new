package com.mobility.enp.view.fragments.my_profile

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.databinding.FragmentProfileBinding
import com.mobility.enp.util.ImageRepository
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.dialogs.ProfileImagePickerDialog
import com.mobility.enp.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(), ProfileImagePickerDialog.ImagePickDialogListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding get() = _binding!!
    private val viewModelProfile: ProfileViewModel by viewModels()
    private var errorBody: MutableLiveData<ErrorBody> = MutableLiveData()

    private val imageRepository: ImageRepository by lazy {
        ImageRepository(requireContext())
    }

    companion object {
        const val TAG = "PROFILE"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        errorBody = MutableLiveData()

        setObserver()

        val displayName = viewModelProfile.getDisplayName(requireContext())
        Log.d("MARKO", "onViewCreated: $displayName")
        binding.userName.text = displayName

        lifecycleScope.launch {
            try {

                displayName.let {
                    imageRepository.getAndSetProfileImage(binding.imageProfile, it)
                }

                viewModelProfile.checkStoredPicture()
            } catch (e: Exception) {
                Log.d(TAG, "error: null data in room")
            }
        }


        binding.bttChangeProfilePicture.setOnClickListener {
            // Otvaranje dijaloga za izbor slike
            val image = ProfileImagePickerDialog(this@ProfileFragment)
            image.show(parentFragmentManager, "Profile Image")

        }

        binding.buttonMyTags.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToMyTagsFragment2())
        }

        binding.buttonRefundRequest.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToRefundRequestFragment2())
        }

        binding.buttonBasicInformation.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToBasicInformationFragment())
        }

        binding.buttonChangePassword.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToChangePasswordFragment())
        }

        binding.bttDeleteProfilePicIcon.setOnClickListener {
            viewModelProfile.deleteProfilePicture()
        }

        binding.buttonSignOut.setOnClickListener {
            context?.let {
                lifecycleScope.launch {
                    // added internet check if no internet just logout without token delete

                    viewModelProfile.deleteFirebaseToken(errorBody)  // this deletes from server
                    viewModelProfile.postLogoutUser(errorBody)

                    viewModelProfile.logout() // this deletes room local

                    findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToLoginFragment())
                }
            }
        }

        binding.buttonSettings.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToSettingsFragment())
        }

        binding.buttonTermsAndConditions.setOnClickListener {
            viewModelProfile.fetchLocalData()
        }

        binding.buttonSupport.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToSupportDialog())
        }

        binding.buttonMyInvoices.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionGlobalInvoicesFragment())
        }

        binding.buttonDeactivateAccount.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToDeactivateAccountDialog())
        }
    }

    private fun setObserver() {
        errorBody.observe(viewLifecycleOwner) { errorBody ->
            context?.let { context ->
                Toast.makeText(
                    context,
                    errorBody.errorBody,
                    Toast.LENGTH_SHORT
                ).show()
                if (errorBody.errorCode == 405 || errorBody.errorCode == 401) {
                    MainActivity.logoutOnInvalidToken(context, findNavController())
                }
            }
        }

        viewModelProfile.userInfo.observe(viewLifecycleOwner) { userInfo ->
            userInfo?.let {
                val action =
                    ProfileFragmentDirections.actionProfileFragmentToTermsAndPrivacyFragment(
                        userInfo
                    )
                findNavController().navigate(action)
                // Resetujte userInfo nakon navigacije
                viewModelProfile.resetUserInfo()
            }
        }

        viewModelProfile.checkNet.observe(viewLifecycleOwner) { net ->
            net?.let {
                if (!it) {
                    val binding = (activity as MainActivity).binding
                    MainActivity.showSnackMessage(
                        getString(R.string.no_internet),
                        binding
                    )

                    viewModelProfile.resetCheckNet()
                }
            }
        }

        viewModelProfile.deletePic.observe(viewLifecycleOwner) { deleted ->
            if (deleted) {
                binding.imageProfile.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.ic_account_home_screen
                    )
                )
                binding.bttDeleteProfilePicIcon.visibility = View.GONE
            }else{
                binding.bttDeleteProfilePicIcon.visibility = View.VISIBLE
            }
        }
    }

    override fun onImageSelected(imageBitmap: Bitmap?) {

        imageBitmap?.let { bitmap ->
            // Konfiguracija Glide opcija
            val glideOption = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Omogućava keširanje učitanih slika
                .override(binding.imageProfile.width, binding.imageProfile.height) // Velicina slike
                .centerCrop() //Nacin skaliranja
                .transform(CircleCrop())
                .format(DecodeFormat.PREFER_RGB_565) // Preferirani format slike
            // ARGB_8888 za bolji kvalitet slike

            Glide.with(requireContext())
                .load(bitmap)
                .apply(glideOption)
                .into(binding.imageProfile)

            binding.bttDeleteProfilePicIcon.visibility = View.VISIBLE

            viewLifecycleOwner.lifecycleScope.launch {
                imageRepository.saveImageToStorage(bitmap, binding.userName.text.toString())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}