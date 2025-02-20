package com.mobility.enp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_home_page.homedata.HomeScreenData
import com.mobility.enp.data.model.api_home_page.homedata.Promotion
import com.mobility.enp.databinding.FragmentHomeWelcomeBinding
import com.mobility.enp.network.Repository
import com.mobility.enp.util.ImageRepository
import com.mobility.enp.util.Util
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.home.HomeBillsAdapter
import com.mobility.enp.view.adapters.home.HomePassageAdapter
import com.mobility.enp.view.adapters.home.HomeProgressAdapter
import com.mobility.enp.view.adapters.home.HomePromotionsAdapter
import com.mobility.enp.view.dialogs.GeneralMessageDialog
import com.mobility.enp.viewmodel.HomeViewModel
import com.mobility.enp.viewmodel.HomeViewModelTEST
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeTESTFragment : Fragment() {

    private var _binding: FragmentHomeWelcomeBinding? = null
    private val binding: FragmentHomeWelcomeBinding get() = _binding!!
    private val viewModelHome: HomeViewModelTEST by viewModels()
    private var errorBody: MutableLiveData<ErrorBody> = MutableLiveData()
    var homeUserData = MutableLiveData<HomeScreenData>()
    private lateinit var isInternetAvailable: MutableLiveData<Boolean>
    private var returnedListPromotion: List<Promotion> = emptyList()

    private val imageRepository: ImageRepository by lazy {
        ImageRepository(requireContext())
    }

    private val TAG = "HOME"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home_welcome, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {
            binding.progBar.visibility = View.VISIBLE

            homeUserData = MutableLiveData()
            errorBody = MutableLiveData()
            isInternetAvailable = MutableLiveData()

            setObserver()

            viewModelHome.getUserHomeData(it, errorBody, homeUserData, isInternetAvailable)
            viewModelHome.checkStoredPromotions()

            binding.lifecycleOwner = viewLifecycleOwner
        }


    }

    private fun setPromotionAdapter(promotionsList: ArrayList<Promotion>) {
        val filteredList: ArrayList<Promotion> = arrayListOf()
        var hasModification = false

        val isSerbiaAdded = { promotionsList.any { it.countryCode == "RS" } }

        for (promotion in promotionsList) {  // removes user deleted promotions
            promotion.deletedByUser?.let { deletedByUser ->
                if (!deletedByUser) {
                    filteredList.add(promotion)
                } else {
                    if (Util.hasTimePassed(promotion.time)) {
                        promotion.deletedByUser = false
                        promotion.time = System.currentTimeMillis()
                        viewModelHome.upsertPromotion(promotion)
                        hasModification = true
                        Log.d(TAG, "10 days have passed $promotion")
                    } else {
                        Log.d(TAG, "10 days have not passed $promotion")
                    }
                }
            }
        }

        val adapter = HomePromotionsAdapter(filteredList, { promotion ->
            if (isSerbiaAdded() && promotion.countryCode != "RS") {
                showSerbiaRequiredDialog()
            } else {
                val action = HomeFragmentDirections.actionHomeFragmentToCardFragment(promotion)
                findNavController().navigate(action)
            }

        }, { promotion ->
            binding.progBar.visibility = View.VISIBLE
            viewModelHome.userDeletedPromotion(promotion)
        })
        val adapterProgress = HomeProgressAdapter(filteredList.size)

        binding.cyclerPromotions.visibility = View.VISIBLE

        binding.cyclerPromotions.adapter = adapter
        binding.cyclerPromotions.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.cyclerProgress.visibility = View.VISIBLE
        binding.cyclerProgress.adapter = adapterProgress
        binding.cyclerProgress.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.cyclerPromotions.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val currentCompletelyVisibleLab =
                    (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                Log.d(TAG, "onScrollStateChanged: $currentCompletelyVisibleLab")
                adapterProgress.setCurrentDot(currentCompletelyVisibleLab)
            }
        })

        if (hasModification) {
            hasModification = false
            viewModelHome.reloadPromotionList()
        }

    }

    private fun triggerUpdate() {
        lifecycleScope.launch(Dispatchers.Main) {
            val bindingMain = (activity as MainActivity).binding
            MainActivity.showSnackMessage(getString(R.string.connection_restored), bindingMain)
            binding.progBar.visibility = View.GONE

            viewModelHome.getUserHomeData(
                requireContext(), errorBody, homeUserData, isInternetAvailable
            )
        }
    }

    private fun setObserver() {
        isInternetAvailable.observe(viewLifecycleOwner) { hasInternet ->
            if (hasInternet != null && !hasInternet) {
                var homeScreenData: HomeScreenData? = null

                lifecycleScope.launch {
                    val diff = async(Dispatchers.IO) {
                        viewModelHome.fetchHomeData()
                    }
                    homeScreenData = diff.await()
                }


                if (homeScreenData != null) {
                    val bindingMain = (activity as MainActivity).binding
                    MainActivity.showSnackMessage(
                        getString(R.string.offline_using_stored_data), bindingMain
                    )
                    homeScreenData?.let {
                        homeUserData.value = it
                    }
                } else {
                    val bundle = Bundle().apply {
                        putString(
                            getString(R.string.title),
                            getString(R.string.no_connection_title)
                        )
                        putString(
                            getString(R.string.subtitle),
                            getString(R.string.please_connect_to_the_internet)
                        )
                    }

                    findNavController().navigate(
                        R.id.action_global_noInternetConnectionDialog,
                        bundle
                    )

                    val binding = (activity as MainActivity).binding
                    MainActivity.showSnackMessage(
                        getString(R.string.checking_for_connection), binding
                    )

                    lifecycleScope.launch(Dispatchers.IO) {
                        while (true) {
                            if (Repository.isNetworkAvailable(requireContext())) {
                                triggerUpdate()
                                break
                            } else {
                                delay(1000L)
                            }
                        }
                    }
                }
            }
        }

        errorBody.observe(viewLifecycleOwner) { errorBody ->
            if (errorBody.errorCode != 500) {
                Toast.makeText(
                    context, errorBody.errorBody, Toast.LENGTH_SHORT
                ).show()
            } else {
                binding.progBar.visibility = View.GONE
                binding.noInvoices.visibility = View.VISIBLE
                binding.noToolHistory.visibility = View.VISIBLE
            }
            if (errorBody.errorCode == 405 || errorBody.errorCode == 401) {
                MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
            }
        }

        viewModelHome.promotionList.observe(viewLifecycleOwner) { promotionList ->
            Log.d(TAG, "list: $promotionList")
            if (promotionList == null || promotionList.isEmpty()) {  // no initial list from api set up so call will be made
                viewModelHome.getUserAllowedCountries(errorBody)
            } else { // set before no new api call will be made for countries
                viewModelHome.getCreditCards(errorBody) // fetch existing user credit cards
                returnedListPromotion = promotionList
            }
        }

        viewModelHome.userCreditCards.observe(viewLifecycleOwner) { creditCardData ->
            binding.progBar.visibility = View.GONE
            Log.d(TAG, "credit cards: $creditCardData")

            val existingCountryCodes = creditCardData?.data?.map { data ->
                data.country?.code
            } ?: emptyList()

            Log.d(TAG, "existing codes: $existingCountryCodes")

            val finalList: ArrayList<Promotion> = ArrayList()

            for (promotion in returnedListPromotion) {  // second filter removes added cards from list
                promotion.countryCode?.let { countryCode ->
                    if (!existingCountryCodes.contains(countryCode)) {
                        finalList.add(promotion)
                    }
                }
            }

            setPromotionAdapter(finalList)

        }

        homeUserData.observe(viewLifecycleOwner) { homeData ->

            // Ažuriraj bazu sa novim podacima
            viewModelHome.insertHomeData(homeData)

            // Sakrij progress bar i postavi display name
            binding.progBar.visibility = View.GONE
            homeData.data?.customer?.displayName?.let { displayName ->
                binding.homeUserName.text = displayName
                //Repository.saveDisplayName(requireContext(), displayName)
            }

            // Postavi sliku korisnika
            viewLifecycleOwner.lifecycleScope.launch {
                val displayName = homeData.data?.customer?.displayName.orEmpty()
                imageRepository.getAndSetProfileImage(binding.imageAccountHomeScreen, displayName)
            }

            // Postavi adapter i podatke za listu prolaza
            homeData.data?.tollHistory?.let { list ->

                if (list.isEmpty()) {
                    binding.noToolHistory.visibility = View.VISIBLE
                }

               /* val adapter = HomePassageAdapter(list.toCollection(ArrayList()))
                binding.recyclerLastPassages.adapter = adapter
                binding.recyclerLastPassages.layoutManager = LinearLayoutManager(requireContext())*/
            }

            // Postavi adapter i podatke za listu mesečnih računa
            /*homeData.data?.invoices?.let { invoices ->

                if (invoices.isEmpty()) {
                    binding.noInvoices.visibility = View.VISIBLE
                }

                val adapter = HomeBillsAdapter(
                    invoices,
                    object : HomeBillsAdapter.AdapterSwitchToPage {
                        override fun switchToBills() {
                            findNavController().navigate(R.id.action_global_invoicesFragment)
                        }

                        override fun switchToInvoices() {
                            findNavController().navigate(R.id.action_global_toolHistoryFragment)
                        }
                    },
                    requireContext()
                )
                binding.recyclerViewMonthlyBills.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                binding.recyclerViewMonthlyBills.adapter = adapter

            }*/


        }


        context?.let {
            viewModelHome.countryData.observe(viewLifecycleOwner) { model ->
                //generates objects has iterator
                Log.d(TAG, "setObserver: $model")
                val promotionsList = model.data?.results?.mapNotNull { data ->
                    viewModelHome.createPromotion(requireContext(), data!!.code)
                } ?: emptyList()

                //save to room promotion here
                Log.d(TAG, "promotion list: $promotionsList")

                viewModelHome.savePromotion(promotionsList)
            }
        }
    }

    private fun showSerbiaRequiredDialog() {
        val dialog = GeneralMessageDialog(
            getString(R.string.notification),
            getString(R.string.first_add_card_serbia)
        )
        dialog.show(parentFragmentManager, "HomeNoAddCardDialog")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}