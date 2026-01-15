package com.mobility.enp.view.fragments.tool_history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponse
import com.mobility.enp.databinding.FragmentPassageHistoryBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.Util
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.tool_history.MyTollCountriesFilterAdapter
import com.mobility.enp.view.adapters.tool_history.first_screen.HistoryPassageAdapter
import com.mobility.enp.view.adapters.tool_history.first_screen.HistoryPassageAdapterCroatia
import com.mobility.enp.view.adapters.tool_history.first_screen.HistorySerialAdapter
import com.mobility.enp.view.dialogs.GeneralMessageDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFirstScreen : Fragment(), HistoryPassageAdapter.SendToFragment,
    HistorySerialAdapter.SavePassageData, HistorySerialAdapter.PaginationUpdate,
    HistoryPassageAdapterCroatia.SendToFragment {

    private var _binding: FragmentPassageHistoryBinding? = null
    private val binding: FragmentPassageHistoryBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val vModel: UserPassViewModel by activityViewModels { UserPassViewModel.Factory }

    private lateinit var statusFilterAdapter: MyTollCountriesFilterAdapter
    private lateinit var historySerialAdapter: HistorySerialAdapter
    private var savedDataCheckJob: Job? = null

    companion object {
        const val TAG = "ToolHist"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPassageHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vModel.nullData()
        vModel.setCsvState()


        binding.progBar.visibility = View.VISIBLE
        binding.loopIcon.isEnabled = false

        setObservers()

        vModel.getBaseDataAlternativeApi()

        runExistingFilterCheck()

        binding.loopIcon.setOnClickListener {
            if (Util.isNetworkAvailable(requireContext())) {
                vModel.selectedCountry = ""  // should clear for filter fragment
                findNavController().navigate(HistoryFirstScreenDirections.actionToolHistoryFragmentToToolHistorySearchFragment())
            } else {
                Toast.makeText(
                    context, context?.getString(R.string.no_internet), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun runExistingFilterCheck() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {

            val indexData = vModel.fetchIndexData()   // room

            indexData?.availableCountries?.let { countryList ->
                if (countryList.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        vModel.setAvailableCountriesMain(countryList)
                    }
                }
            }
        }
    }

    private fun triggerUpdate() {
        val bindingMain = (activity as MainActivity).binding

        MainActivity.showSnackMessage(getString(R.string.connection_restored), bindingMain)

        binding.progBar.visibility = View.GONE
        binding.loopIcon.isEnabled = true

        vModel.getBaseDataAlternativeApi()
    }

    private fun setObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vModel.indexDataMainScreen.collect { indexData ->
                    indexData?.let {
                        setIndexData(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vModel.listOfCountriesMainScreen.collect { countriesList ->
                    if (countriesList.isNotEmpty()) {
                        setAvailableFilters(countriesList)
                        statusFilterAdapter.performClick(vModel.availableCountryAdapterPosition.value)
                    }
                }
            }
        }


        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let {
                binding.loopIcon.setBackgroundResource(franchiseModel.loopIcon)
            }
        }

        collectLatestLifecycleFlow(vModel.baseTagDataStateByCountry) { tagIndex ->
            when (tagIndex) {
                is SubmitResult.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    vModel.setIndexDataMainScreen(tagIndex.data)
                }

                is SubmitResult.FailureNoConnection -> {
                    showNoConnectionState()
                    runSavedDataCheck()
                }

                is SubmitResult.FailureServerError -> {
                    binding.progBar.visibility = View.GONE
                    showError(getString(R.string.server_error_msg))
                }

                is SubmitResult.FailureApiError -> {
                    binding.progBar.visibility = View.GONE
                    showError(tagIndex.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(tagIndex.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                else -> {}
            }
        }


        collectLatestLifecycleFlow(vModel.baseTagDataStateFirstScreen) { tagIndex ->
            when (tagIndex) {
                is SubmitResult.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    // sets available countries filter and primary adapter
                    tagIndex.data.second?.let { cardData ->
                        val countryList = ArrayList<String>()

                        if (cardData.data?.showTabHR == true) {
                            countryList.add(getString(R.string.croatia))
                        }
                        if (cardData.data?.showTabME == true) {
                            countryList.add(getString(R.string.montenegro))
                        }
                        if (cardData.data?.showTabMK == true) {
                            countryList.add(getString(R.string.macedonia))
                        }
                        if (cardData.data?.showTabRS == true) {
                            countryList.add(getString(R.string.serbia))
                        }

                        vModel.setAvailableCountriesMain(countryList)
                    }

                    vModel.setIndexDataMainScreen(tagIndex.data.first)
                }

                is SubmitResult.FailureNoConnection -> {
                    showNoInternetDialog()
                    runSavedDataCheck()
                }

                is SubmitResult.FailureServerError -> {
                    binding.progBar.visibility = View.GONE
                    showError(getString(R.string.server_error_msg))
                }

                is SubmitResult.FailureApiError -> {
                    binding.progBar.visibility = View.GONE
                    showError(tagIndex.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(tagIndex.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                else -> {}
            }
        }


        collectLatestLifecycleFlow(vModel.complaintObjectionState) { serverResponse ->
            when (serverResponse) {
                is SubmitResult.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    binding.progBar.visibility = View.GONE
                    vModel.getBaseDataAlternativeApi()
                }

                is SubmitResult.FailureNoConnection -> {
                    showNoConnectionState()
                }

                is SubmitResult.FailureServerError -> {
                    binding.progBar.visibility = View.GONE
                    showError(getString(R.string.server_error_msg))
                }

                is SubmitResult.FailureApiError -> {
                    binding.progBar.visibility = View.GONE
                    showError(serverResponse.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(serverResponse.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                else -> {}
            }
        }
    }

    private fun setAvailableFilters(countryList: List<String>) {
        if (countryList.isNotEmpty()) {
            vModel.selectedCountry = when (countryList.reversed()[0]) {
                getString(R.string.croatia) -> {
                    getString(R.string.croatia_hr)
                }

                getString(R.string.montenegro) -> {
                    getString(R.string.montenegro_me)
                }

                getString(R.string.macedonia) -> {
                    getString(R.string.northmacedonia_mk)
                }

                getString(R.string.serbia) -> {
                    getString(R.string.serbia_rs)
                }

                else -> ""
            }

            statusFilterAdapter = MyTollCountriesFilterAdapter { selectedStatus ->

                vModel.setCountryAdapterPosition(statusFilterAdapter.getTabPosition())

                val selectedCountry = when (selectedStatus) {
                    getString(R.string.croatia) -> {
                        getString(R.string.croatia_hr)
                    }

                    getString(R.string.montenegro) -> {
                        getString(R.string.montenegro_me)
                    }

                    getString(R.string.macedonia) -> {
                        getString(R.string.northmacedonia_mk)
                    }

                    getString(R.string.serbia) -> {
                        getString(R.string.serbia_rs)
                    }

                    else -> ""
                }

                Log.d("Test123", "setAvailableFilters: ${statusFilterAdapter.getTabPosition()}")

                vModel.selectedCountry = selectedCountry

                historySerialAdapter.clearData()

                binding.progBar.visibility = View.VISIBLE

                if (vModel.isNetAvailable()) {
                    vModel.getBaseDataAlternativeApiForCountriesOnMain()
                } else {
                    fetchStoredData()
                }

            }

            binding.cyclerTagTypes.adapter = statusFilterAdapter

            statusFilterAdapter.submitList(countryList.reversed()) {
                statusFilterAdapter.setTabPosition(0)
            }
        }
    }

    private fun fetchStoredData() {

        viewLifecycleOwner.lifecycleScope.launch {
            val indexData = vModel.fetchIndexData()   // room

            indexData?.let { data ->

                val bindingMain = (activity as MainActivity).binding

                MainActivity.showSnackMessage(
                    getString(R.string.offline_using_stored_data), bindingMain
                )

                vModel.setStateIndex(data)
            }
        }
    }

    private fun runSavedDataCheck() {
        if (savedDataCheckJob?.isActive == true) return

        binding.loopIcon.isEnabled = false

        savedDataCheckJob = viewLifecycleOwner.lifecycleScope.launch {

            val indexData = vModel.fetchIndexData()   // room

            indexData?.let { data ->

                val bindingMain = (activity as MainActivity).binding

                MainActivity.showSnackMessage(
                    getString(R.string.offline_using_stored_data), bindingMain
                )

                vModel.setStateIndex(data)

            } ?: run {
                val navController = findNavController()

                if (navController.currentDestination?.id ==
                    R.id.noInternetConnectionDialog
                ) {
                    return@launch
                }

                val bundle = Bundle().apply {
                    putString(getString(R.string.title), getString(R.string.no_connection_title))
                    putString(
                        getString(R.string.subtitle),
                        getString(R.string.please_connect_to_the_internet)
                    )
                }

                navController.navigate(
                    R.id.action_global_noInternetConnectionDialog,
                    bundle
                )

                val bindingMain = (activity as MainActivity).binding
                MainActivity.showSnackMessage(
                    getString(R.string.checking_for_connection), bindingMain
                )
                binding.progBar.visibility = View.VISIBLE
            }

            while (isActive) {
                if (vModel.internetAvailable()) {
                    triggerUpdate()
                    break
                } else {
                    delay(1000L)
                }
            }
        }
    }

    private fun setIndexData(indexData: IndexData) {
        Log.d(TAG, "setIndexData: $indexData")

        binding.loopIcon.isEnabled = true

        binding.progBar.visibility = View.GONE

        if (vModel.listOfCountriesMainScreen.value.isNotEmpty()) {
            indexData.availableCountries = (vModel.listOfCountriesMainScreen.value)
        }

        CoroutineScope(Dispatchers.IO).launch {
            vModel.insertRoomToolHistoryIndexData(indexData)
        }

        vModel.indexData =
            indexData  // filter fragment need some data from here saving here to reduce api calls

        historySerialAdapter =
            HistorySerialAdapter(indexData, vModel, this, this, this, this, this)

        binding.cycler.adapter = historySerialAdapter
        binding.cycler.layoutManager = LinearLayoutManager(requireContext())

    }

    private fun showNoInternetDialog() {
        val navController = findNavController()

        if (navController.currentDestination?.id ==
            R.id.noInternetConnectionDialog
        ) {
            return
        }

        val bundle = Bundle().apply {
            putString(getString(R.string.title), getString(R.string.no_connection_title))
            putString(
                getString(R.string.subtitle),
                getString(R.string.please_connect_to_the_internet)
            )
        }

        navController.navigate(
            R.id.action_global_noInternetConnectionDialog,
            bundle
        )
    }

    override fun sendComplaintData(complaintBody: ComplaintBody) {
        vModel.postComplaint(complaintBody)
    }

    override fun sendObjectionData(objectionBody: ObjectionBody) {
        vModel.postObjection(objectionBody)
    }

    override fun sendDataFill(
        nextPage: Int,
        flow: MutableStateFlow<SubmitResult<V2HistoryTagResponse>>,
        tagSerialNumber: String
    ) {
        binding.progBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            vModel.getToolHistoryTransit(flow, tagSerialNumber, nextPage)
        }
    }

    override fun sendDataFillMainAdapter(
        // updates tags on main adapter
        nextPage: Int,
        perPage: Int,
        flow: MutableStateFlow<SubmitResult<IndexData>>,
    ) {
        binding.progBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            vModel.getBaseTagDataPagination(nextPage, perPage, flow)
        }
    }

    override fun stopSpinner() {
        binding.progBar.visibility = View.GONE
    }

    override fun croatiaReclamationDialog() {
        val fm = activity?.supportFragmentManager

        fm?.let { manager ->
            GeneralMessageDialog.newInstance(
                getString(R.string.complaint), getString(R.string.croatian_reclamation)
            ).show(manager, "croatiaDialog")
        }
    }

    override fun psgData(toolHistoryListing: V2HistoryTagResponse) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                vModel.insertPassageData(toolHistoryListing)
            }
        }
    }

    private fun showNoConnectionState() {
        binding.progBar.visibility = View.GONE
        noInternetMessage()
    }

    private fun noInternetMessage() {
        val mainBinding = (activity as MainActivity).binding
        MainActivity.showSnackMessage(getString(R.string.no_internet), mainBinding)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}