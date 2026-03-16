package com.mobility.enp.view.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.mobility.enp.R
import com.mobility.enp.data.model.ProfileImage
import com.mobility.enp.data.model.home.cards.entity.HomeCardsEntity
import com.mobility.enp.data.model.home.relation.HomeWithDetails
import com.mobility.enp.databinding.FragmentHomeWelcomeBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.SubmitResultFold
import com.mobility.enp.util.Util
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.util.safeNavigate
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.TotalCurrencyAdapter
import com.mobility.enp.view.adapters.home.HomePassageAdapter
import com.mobility.enp.view.adapters.home.HomeProgressAdapter
import com.mobility.enp.view.adapters.home.HomePromotionsAdapter
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.HomeViewModel
import java.io.File

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeWelcomeBinding? = null
    private val binding: FragmentHomeWelcomeBinding get() = _binding!!
    private lateinit var totalCurrencyAdapter: TotalCurrencyAdapter
    private lateinit var homePassageAdapter: HomePassageAdapter
    private lateinit var homePromotionsAdapter: HomePromotionsAdapter
    private lateinit var adapterProgress: HomeProgressAdapter

    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentHomeWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        franchiseViewModel.getFranchiseModel(requireContext())  // removed check enabled for stage and prod now

        setupBinding()
        setupAdapters()
        setupObservers()
        setupClickListeners()

        (activity as MainActivity).showNavBar()

        viewModel.fetchHomeData()
    }

    private fun setupBinding() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun setupAdapters() {
        binding.rvTotalCurrency.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            totalCurrencyAdapter = TotalCurrencyAdapter(emptyList())
            adapter = totalCurrencyAdapter
        }

        homePassageAdapter = HomePassageAdapter()
        binding.recyclerLastPassages.adapter = homePassageAdapter

        homePromotionsAdapter = HomePromotionsAdapter(
            onItemClicked = { card ->
                if (card.additionEnabled == true) {
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToPaymentAndPassageFragment(
                            card.code
                        )
                    safeNavigate(action, R.id.homeFragment)
                } else if (card.isSocialNetworks) {

                    when (card.code) {
                        "facebook" -> {
                            openFacebookPage()
                        }

                        "instagram" -> {
                            openInstagramProfile()
                        }

                        "tag" -> {
                            if (Util.isNetworkAvailable(requireContext())) {
                                viewModel.loadTagOrderUrl()
                            } else {
                                showNoInternetDialog()
                            }
                        }
                    }
                } else {
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToPaymentAndPassageFragment("RS")
                    safeNavigate(action, R.id.homeFragment)
                }

            },
            onDeleteClicked = { card ->
                binding.progBar.visibility = View.VISIBLE
                viewModel.updateDeleteHomeCard(card)
                val newList =
                    homePromotionsAdapter.currentList.toMutableList().apply { remove(card) }
                homePromotionsAdapter.submitList(newList)

                adapterProgress.submitList(newList.indices.toList()) {
                    if (newList.isNotEmpty()) {
                        val newCheckedPosition =
                            if (adapterProgress.checkedPosition >= newList.size) {
                                newList.lastIndex
                            } else {
                                adapterProgress.checkedPosition
                            }
                        adapterProgress.setCurrentDot(newCheckedPosition)
                    }
                }

                binding.progBar.visibility = View.GONE
            }, franchiseViewModel.franchiseModel.value
        )

        adapterProgress = HomeProgressAdapter(franchiseViewModel.franchiseModel.value)
        binding.cyclerPromotions.adapter = homePromotionsAdapter
        binding.cyclerProgress.adapter = adapterProgress
        binding.cyclerProgress.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.cyclerPromotions.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val currentCompletelyVisiblePosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                // Pozivam setCurrentDot samo kada se pozicija stvarno promeni
                if (currentCompletelyVisiblePosition != RecyclerView.NO_POSITION &&
                    currentCompletelyVisiblePosition != adapterProgress.checkedPosition
                ) {
                    adapterProgress.setCurrentDot(currentCompletelyVisiblePosition)
                }
            }
        })

    }

    private fun setupObservers() {
        collectLatestLifecycleFlow(viewModel.homeData) { result ->
            handleHomeDataResult(result)
        }

        collectLatestLifecycleFlow(viewModel.profileImage) { profileImage ->
            profileImage?.let {
                displayProfileImage(binding.imageAccountHomeScreen, it)
            }
        }
        collectLatestLifecycleFlow(viewModel.homeCards) { cards ->
            cards?.let {
                setHomeCardsAdapter(it.card, it.countryCode)
            }
        }
        collectLatestLifecycleFlow(viewModel.homeTollHistory) { tollHistory ->
            homePassageAdapter.submitList(tollHistory)
        }

        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.let { data ->
                binding.cardViewAccountHomeScreen.backgroundTintList =
                    ColorStateList.valueOf(data.franchisePrimaryColor)
                binding.constraintLayoutInCard.background = data.franchiseHomeBackgroundLocation
                binding.switchToPageBill.setBackgroundResource(data.rightArrowResource)
                binding.imageAccountHomeScreen.setBackgroundResource(data.franchiseProfilePictureResource)
                binding.homeUserName.setTextColor(data.homePageWelcomeTextColor)
                binding.tvWelcomeBack.setTextColor(data.homePageWelcomeTextColor)
            }
        }

        collectLatestLifecycleFlow(viewModel.tagOrderUrl) { result ->
            when (result) {
                is SubmitResultFold.Failure -> {}
                SubmitResultFold.Idle -> {}
                SubmitResultFold.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResultFold.Success -> {
                    binding.progBar.visibility = View.GONE
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToTagOrderWebFragment(result.data)
                    findNavController().navigate(action)
                    viewModel.clearTagOrderUrl()  // resetujem da se ne navigira ponovo pri rotaciji
                }
            }

        }
    }

    private fun setupClickListeners() {
        binding.switchToPageBill.setOnClickListener {
            findNavController().navigate(R.id.action_global_invoicesFragment)
        }
    }

    private fun handleHomeDataResult(result: SubmitResult<HomeWithDetails>) {  // data save in room directly on api response / model returned combined
        when (result) {
            is SubmitResult.Loading -> {
                binding.progBar.visibility = View.VISIBLE
            }

            is SubmitResult.Success -> {
                handleSuccess(result)
                binding.progBar.visibility = View.GONE
                binding.linearHomeContainer.visibility = View.VISIBLE
                binding.cardViewAccountHomeScreen.visibility = View.VISIBLE
                binding.imageAccountHomeScreen.visibility = View.VISIBLE
            }

            is SubmitResult.Empty -> {}
            is SubmitResult.FailureNoConnection -> showNoInternetDialog()
            is SubmitResult.FailureServerError -> showErrorMessage(getString(R.string.server_error_msg))
            is SubmitResult.FailureApiError -> showErrorMessage(result.errorMessage)
            is SubmitResult.InvalidApiToken -> {
                showErrorMessage(result.errorMessage)
                MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
            }
        }
    }

    private fun handleSuccess(result: SubmitResult.Success<HomeWithDetails>) {

        result.data.home.displayName.let { viewModel.loadProfileImage(it) }
        val invoiceDetails = result.data.invoice
        if (invoiceDetails.isNotEmpty()) {
            val invoice = invoiceDetails.flatMap { it.invoiceDetails }
            totalCurrencyAdapter.submitList(invoice)
            binding.noInvoices.visibility = View.GONE
            binding.invoicesContainerHome.visibility = View.VISIBLE
        } else {
            binding.noInvoices.visibility = View.VISIBLE
            binding.invoicesContainerHome.visibility = View.GONE
            binding.noInvoices.visibility = View.VISIBLE
        }
        val tollHistory = result.data.tollHistory
        if (tollHistory.isNotEmpty()) {
            binding.noToolHistory.visibility = View.GONE
        } else {
            binding.noToolHistory.visibility = View.VISIBLE
        }

    }

    private fun showErrorMessage(message: String) {
        binding.progBar.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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

    private fun displayProfileImage(imageView: ImageView, profileImage: ProfileImage) {
        Glide.with(imageView.context)
            .load(File(profileImage.imagePath))
            .apply(
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Omogućava keširanje učitanih slika
                    .override(imageView.width, imageView.height) // Velicina slike
                    .centerCrop() //Nacin skaliranja
                    .transform(CircleCrop())
                    .format(DecodeFormat.PREFER_ARGB_8888) // Kvalitetnije, ali troši više RAM-a
                    // Preferirani format slike
                    .error(R.drawable.default_user_picture) // Ako slika ne postoji, postavi default
            )
            .into(imageView)
    }

    private fun setHomeCardsAdapter(cardsList: List<HomeCardsEntity>, countryCode: String?) {
        val filteredList = mutableListOf<HomeCardsEntity>()

        cardsList.forEach { card ->
            if (!card.deletedByUser) {
                filteredList.add(card)
            } else if (Util.hasTimePassed(card.time)) {
                card.deletedByUser = false
                card.time = System.currentTimeMillis()
                viewModel.updateDeleteHomeCard(card)
                filteredList.add(card)
                Log.d("HomeFragment hasTimePassed", "10 days have passed $card")
            } else {
                Log.d("HomeFragment hasTimePassed", "10 days have not passed $card")
            }
        }


        val priority = mapOf(
            "RS" to 0,
            "ME" to 1,
            "MK" to 2,
            "tag" to 3,
            "facebook" to 4,
            "instagram" to 5
        )
        var sortedList = filteredList.sortedBy { priority[it.code] ?: 100 }
        sortedList = if (franchiseViewModel.franchiseModel.value != null || countryCode != "RS") {
            sortedList.filter { it.code != "tag" }
        } else {
            sortedList
        }

        // fixes promotion card description not translating because its hardcoded in room and not correct when language is changed
        for (entity: HomeCardsEntity in sortedList) {
            when (entity.code) {
                "RS" -> {
                    entity.title = requireContext().getString(R.string.serbian_passage)
                    entity.description =
                        requireContext().getString(R.string.tag_device_payment_method_serbia)
                }

                "ME" -> {
                    entity.title = requireContext().getString(R.string.montenegro_passage)
                    entity.description =
                        requireContext().getString(R.string.tag_device_payment_method_montenegro)
                }

                "MK" -> {
                    entity.title = requireContext().getString(R.string.north_macedonian_passage)
                    entity.description =
                        requireContext().getString(R.string.tag_device_payment_method_north_macedonia)
                }

                "facebook" -> {
                    entity.description = requireContext().getString(R.string.facebook_text)
                }

                "instagram" -> {
                    entity.description = requireContext().getString(R.string.instagram_text)
                }

                "tag" -> {
                    entity.title = requireContext().getString(R.string.buy_tag_online_title)
                    entity.description =
                        requireContext().getString(R.string.tag_purchase_online_description)
                }
            }
        }

        homePromotionsAdapter.submitList(sortedList)
        adapterProgress.submitList(sortedList.indices.toList())

        binding.cyclerPromotions.scrollToPosition(0)
        binding.cyclerProgress.scrollToPosition(0)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun openFacebookPage() {
        val facebookUrl = "https://www.facebook.com/toll4all/"

        val uri = "fb://facewebmodal/f?href=$facebookUrl".toUri()

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.facebook.katana")
        }

        try {
            requireContext().startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            requireContext().startActivity(
                Intent(Intent.ACTION_VIEW, facebookUrl.toUri())
            )
        }
    }

    fun openInstagramProfile() {
        val username = "tollforall"  // instagram username
        val uri = "http://instagram.com/_u/$username".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.instagram.android")
        }

        try {
            requireContext().startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Instagram app not installed  open browser
            requireContext().startActivity(
                Intent(Intent.ACTION_VIEW, "https://instagram.com/$username".toUri())
            )
        }
    }

}