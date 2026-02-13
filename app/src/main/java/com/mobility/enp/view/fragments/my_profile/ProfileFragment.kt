package com.mobility.enp.view.fragments.my_profile

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentProfileBinding
import com.mobility.enp.util.ImageRepository
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.dialogs.GeneralMessageDialog
import com.mobility.enp.view.dialogs.ProfileImagePickerDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment(), ProfileImagePickerDialog.ImagePickDialogListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModelProfile: ProfileViewModel by viewModels { ProfileViewModel.Factory }

    private val imageRepository: ImageRepository by lazy {
        ImageRepository(requireContext())
    }

    companion object {
        const val TAG = "PROFILE_FRAGMENT"
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

        setObserver()
        setCurrentVersion()

        viewModelProfile.setRefundRequestVisibility()
        SharedPreferencesHelper.setCurrentTab(requireContext(), 0)

        binding.bttChangeProfilePicture.setOnClickListener {
            // check for existing image in room
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val imageExists = viewModelProfile.checkStoredImageData()

                withContext(Dispatchers.Main) {
                    val image = ProfileImagePickerDialog(this@ProfileFragment, imageExists)
                    image.show(parentFragmentManager, "Profile Image")
                }
            }
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

        binding.buttonSignOut.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {

                franchiseViewModel.runOnce = true

                // added internet check if no internet just logout without token delete
                viewModelProfile.deleteFirebaseToken()  // this deletes from server
                viewModelProfile.postLogoutUser()

                viewModelProfile.logout() // this deletes room local
                franchiseViewModel.deleteData() // this deletes stored object as it will persist on logout otherwise

                (requireContext() as MainActivity).resetToDefault()

                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.navigation, true)
                    .setEnterAnim(R.anim.slide_in_left)
                    .setExitAnim(R.anim.slide_out_right)
                    .build()

                findNavController().navigate(R.id.loginFragment, null, options)
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

    private fun setCurrentVersion() {
        try {
            val packageInfo =
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            packageInfo.versionName.let { gradleVersionName ->  // use version code for other one

                binding.versionCode.text = buildString {
                    append(ContextCompat.getString(requireContext(), R.string.version))
                    append(" ")
                    append(gradleVersionName)
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun setObserver() {

        collectLatestLifecycleFlow(viewModelProfile.postDeleteFcmToken) { flow ->

            when (flow) {
                is SubmitResult.Loading -> {
                    logMessage("Deleting fcm token")
                }

                is SubmitResult.Success -> {
                    logMessage("Fcm Token Deleted")
                }

                is SubmitResult.FailureServerError -> {
                    logMessage(getString(R.string.server_error_msg))
                }

                is SubmitResult.FailureApiError -> {
                    logMessage(flow.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    logMessage(flow.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                else -> {
                    SubmitResult.Empty
                }
            }

        }

        collectLatestLifecycleFlow(viewModelProfile.postLogoutUser) { flow ->

            when (flow) {
                is SubmitResult.Success -> {
                    logMessage("Server logged out user")
                }

                is SubmitResult.FailureServerError -> {
                    logMessage(getString(R.string.server_error_msg))
                }

                is SubmitResult.FailureApiError -> {
                    logMessage(flow.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    logMessage(flow.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                else -> {
                    SubmitResult.Empty
                }
            }
        }

        franchiseViewModel.openSuccessDialog.observe(viewLifecycleOwner) { showDialog ->
            if (showDialog != null && showDialog) {
                franchiseViewModel.postOpenDialog(null)
                GeneralMessageDialog.newInstance(
                    requireContext().getString(R.string.support_successful_mail),
                    requireContext().getString(R.string.support_successful_massage)
                ).show(childFragmentManager, "GeneralDialogSupport")
            }
        }

        viewModelProfile.displayName.observe(viewLifecycleOwner) { displayName ->
            binding.userName.text = displayName
            viewLifecycleOwner.lifecycleScope.launch {
                imageRepository.getAndSetProfileImage(binding.imageProfile, displayName)
            }
        }

        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { data ->
            data?.let { model ->
                model.franchiseProfileResource?.let {
                    binding.rectangleProfilePicture.setImageResource(it)
                }
                binding.bttChangeProfilePicture.setImageResource(model.franchisePlusButton)
                binding.imageProfile.setBackgroundResource(data.franchiseProfilePictureResource)
                binding.userName.setTextColor(data.homePageWelcomeTextColor)
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

                    showNoInternetDialog()

                    viewModelProfile.resetCheckNet()
                }
            }
        }

        viewModelProfile.deletePic.observe(viewLifecycleOwner) { deleted ->
            if (deleted) { // deleted profile picture should return svg of franchise
                franchiseViewModel.franchiseModel.value?.let { data ->
                    binding.imageProfile.setImageDrawable(
                        AppCompatResources.getDrawable(
                            requireContext(),
                            data.franchiseProfilePictureResource
                        )
                    )
                } ?: run {
                    binding.imageProfile.setImageDrawable(
                        AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.default_user_picture
                        )
                    )
                }
            }
        }

        viewModelProfile.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.profileProgressBar.visibility = View.VISIBLE
                binding.profileContainer.visibility = View.GONE
            } else {
                binding.profileProgressBar.visibility = View.GONE
                binding.profileContainer.visibility = View.VISIBLE
            }
        }

        viewModelProfile.showRefundCard.observe(viewLifecycleOwner) { shouldShow ->
            if (shouldShow || franchiseViewModel.franchiseModel.value != null) {
                binding.buttonRefundRequest.visibility = View.VISIBLE
            } else {
                binding.buttonRefundRequest.visibility = View.GONE
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

            viewLifecycleOwner.lifecycleScope.launch {
                imageRepository.saveImageToStorage(bitmap, binding.userName.text.toString())
            }
        }
    }

    private fun showNoInternetDialog() {
        val bundle = Bundle().apply {
            putString(getString(R.string.title), getString(R.string.no_connection_title))
            putString(
                getString(R.string.subtitle),
                getString(R.string.please_connect_to_the_internet)
            )
        }
        findNavController().navigate(R.id.action_global_noInternetConnectionDialog, bundle)
    }

    private fun logMessage(message: String) {
        Log.d(TAG, "logFcmMessage: $message")
    }

    override fun onDeleteImage() {
        viewModelProfile.deleteProfilePicture()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}