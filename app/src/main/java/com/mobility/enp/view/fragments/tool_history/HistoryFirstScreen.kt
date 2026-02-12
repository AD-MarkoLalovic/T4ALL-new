package com.mobility.enp.view.fragments.tool_history

import android.os.Bundle
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
import com.mobility.enp.view.adapters.tool_history.MyTollCountriesFirstScreenAdapter
import com.mobility.enp.view.adapters.tool_history.first_screen.HistoryPassageAdapter
import com.mobility.enp.view.adapters.tool_history.first_screen.HistoryPassageAdapterCroatia
import com.mobility.enp.view.adapters.tool_history.first_screen.HistorySerialAdapter
import com.mobility.enp.view.dialogs.GeneralMessageDialog
import com.mobility.enp.view.dialogs.GeneralMessageDialogInfoButton
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HistoryFirstScreen : Fragment(), HistoryPassageAdapter.SendToFragment,
    HistorySerialAdapter.PaginationUpdate,
    HistoryPassageAdapterCroatia.SendToFragment {

    private var _binding: FragmentPassageHistoryBinding? = null
    private val binding: FragmentPassageHistoryBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val vModel: UserPassViewModel by activityViewModels { UserPassViewModel.Factory }

    private lateinit var statusFilterAdapter: MyTollCountriesFirstScreenAdapter
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

        historySerialAdapter = HistorySerialAdapter(vModel, this, this, this, this)

        binding.cycler.adapter = historySerialAdapter
        binding.cycler.layoutManager = LinearLayoutManager(requireContext())

        setObservers()

        vModel.getBaseDataAlternativeApi()

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

        binding.infoIcon.setOnClickListener {
            val dialog = GeneralMessageDialogInfoButton.newInstance(
                getString(R.string.tool_history),
                getString(R.string.prolasci_info)
            )
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, "infoDialog")
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
                vModel.tagFlow.collect { indexData ->
                    if (!indexData.isEmpty()) {

                        binding.progBar.visibility = View.GONE

                        vModel.indexData = indexData[0] // for filter fragment req data

                        indexData[0].availableCountries?.let { availableCountries ->
                            vModel.setAvailableCountriesMain(availableCountries)
                        }

                        historySerialAdapter.setAdapterData(indexData[0])
                    }

                    //todo modify this
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
                    vModel.saveRoomTagDataFirstScreen(tagIndex.data)
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
                    val countryList = ArrayList<String>()

                    tagIndex.data.second?.let { cardData ->

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
                    }

                    tagIndex.data.first.availableCountries = countryList
                    
                    vModel.saveRoomTagDataFirstScreen(tagIndex.data.first)
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

            statusFilterAdapter = MyTollCountriesFirstScreenAdapter { selectedStatus ->

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

                vModel.selectedCountry = selectedCountry

                if (::historySerialAdapter.isInitialized) {
                    historySerialAdapter.clearData()
                }

                binding.progBar.visibility = View.VISIBLE

                if (vModel.isNetAvailable()) {
                    vModel.getBaseDataAlternativeApiForCountriesOnMain()
                }

            }

            binding.cyclerTagTypes.adapter = statusFilterAdapter

            statusFilterAdapter.submitList(countryList.reversed()) {
                statusFilterAdapter.setTabPosition(0)
            }
        }
    }

    private fun runSavedDataCheck() {
        if (savedDataCheckJob?.isActive == true) return

        binding.loopIcon.isEnabled = false

        savedDataCheckJob = viewLifecycleOwner.lifecycleScope.launch {

            val bindingMain = (activity as MainActivity).binding

            MainActivity.showSnackMessage(
                getString(R.string.checking_for_connection), bindingMain
            )

            val navController = findNavController()

            if (navController.currentDestination?.id == R.id.noInternetConnectionDialog) {
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
                R.id.action_global_noInternetConnectionDialog, bundle
            )

            binding.progBar.visibility = View.VISIBLE

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

    private fun showNoInternetDialog() {
        val navController = findNavController()

        if (navController.currentDestination?.id == R.id.noInternetConnectionDialog) {
            return
        }

        val bundle = Bundle().apply {
            putString(getString(R.string.title), getString(R.string.no_connection_title))
            putString(
                getString(R.string.subtitle), getString(R.string.please_connect_to_the_internet)
            )
        }

        navController.navigate(
            R.id.action_global_noInternetConnectionDialog, bundle
        )
    }

    override fun sendComplaintData(complaintBody: ComplaintBody) {
        vModel.postComplaint(complaintBody)
    }

    override fun sendObjectionData(objectionBody: ObjectionBody) {
        vModel.postObjection(objectionBody)
    }

    // uses the same interface for both croatia and normal adapter should seperate these
    override fun sendDataFill(
        nextPage: Int,
        flow: MutableStateFlow<SubmitResult<V2HistoryTagResponse?>>,
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