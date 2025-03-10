package com.mobility.enp.view.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
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
import com.mobility.enp.util.Util
import com.mobility.enp.util.collectLatestLifecycleFlow
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

        setupBinding()
        setupAdapters()
        setupObservers()
        setupClickListeners()

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

        collectLatestLifecycleFlow(viewModel.homeTollHistory) { tollHistory ->
            homePassageAdapter.submitList(tollHistory)
        }

        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.let { data ->
                binding.cardViewAccountHomeScreen.backgroundTintList =
                    ColorStateList.valueOf(data.franchisePrimaryColor)
                binding.constraintLayoutInCard.background = data.franchiseHomeBackgroundLocation
                if (::homePromotionsAdapter.isInitialized) {
                    homePromotionsAdapter.updateColor(franchiseModel)
                }
                if (::adapterProgress.isInitialized) {
                    adapterProgress.updateFranchiserDotColor(franchiseModel.promotionsDot)
                }
                binding.switchToPageBill.setBackgroundResource(data.rightArrowResource)
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
            is SubmitResult.Loading -> binding.progBar.visibility = View.VISIBLE
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
        viewModel.homeCards.value?.let {
            setHomeCardsAdapter(it)
        }

        franchiseViewModel.getFranchiseModel(result.data.home.portalKey, requireContext())

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
                    .error(R.drawable.ic_account_home_screen) // Ako slika ne postoji, postavi default
            )
            .into(imageView)
    }

    private fun setHomeCardsAdapter(cardsList: List<HomeCardsEntity>) {
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

        homePromotionsAdapter = HomePromotionsAdapter(filteredList, onItemClicked = {
            findNavController().navigate(R.id.action_homeFragment_to_paymentAndPassageFragment)
        }, { delete ->
            binding.progBar.visibility = View.VISIBLE
            viewModel.updateDeleteHomeCard(delete)
            binding.progBar.visibility = View.GONE
        })

        if (filteredList.isNotEmpty()) {
            adapterProgress = HomeProgressAdapter(filteredList.size)

            binding.cyclerPromotions.visibility = View.VISIBLE
            binding.cyclerPromotions.adapter = homePromotionsAdapter

            binding.cyclerProgress.visibility = View.VISIBLE
            binding.cyclerProgress.adapter = adapterProgress
            binding.cyclerProgress.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            binding.cyclerPromotions.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val currentCompletelyVisiblePosition =
                        (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                    // Pozivam setCurrentDot samo kada se pozicija stvarno promeni
                    if (currentCompletelyVisiblePosition != adapterProgress.checkedPosition) {
                        adapterProgress.setCurrentDot(currentCompletelyVisiblePosition)
                    }
                }
            })
        } else {
            Log.d("HomeFragment", "Filtered list is empty, no items to display")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}