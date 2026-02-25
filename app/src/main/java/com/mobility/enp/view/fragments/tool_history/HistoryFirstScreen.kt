package com.mobility.enp.view.fragments.tool_history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
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
import com.mobility.enp.data.model.api_tool_history.v2base_model.DataValidation
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HistoryFirstScreen : Fragment(), HistoryPassageAdapter.SendToFragment,
    HistoryPassageAdapterCroatia.SendToFragment {

    private var _binding: FragmentPassageHistoryBinding? = null
    private val binding: FragmentPassageHistoryBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: UserPassViewModel by activityViewModels { UserPassViewModel.Factory }
    private var listIndexData: List<IndexData> = emptyList()

    private lateinit var statusFilterAdapter: MyTollCountriesFirstScreenAdapter
    private lateinit var historySerialAdapter: HistorySerialAdapter

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

        viewModel.nullData()
        viewModel.setCsvState()

        runInternetConnectionCheck()

        binding.progBar.visibility = View.VISIBLE
        binding.loopIcon.isEnabled = false

        historySerialAdapter = HistorySerialAdapter(viewModel, this, this, this)

        binding.cycler.adapter = historySerialAdapter
        binding.cycler.layoutManager = LinearLayoutManager(requireContext())

        setObservers()

        viewModel.getBaseDataAlternativeApi()

        binding.loopIcon.setOnClickListener {
            Log.d(TAG, "onViewCreated: ")
            if (Util.isNetworkAvailable(requireContext())) {
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

    private fun setObservers() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allowedCountriesFlow.collect { allowedCountries ->
                    val listOfCountries: ArrayList<String> = arrayListOf()
                    for (data in allowedCountries) {
                        listOfCountries.add(data.country)
                    }
                    setAvailableFilters(listOfCountries.toList())
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tagFlow.collect { indexData ->
                    if (!indexData.isEmpty()) {

                        binding.progBar.visibility = View.GONE

                        indexData[0].availableCountries?.let { availableCountries ->
                            viewModel.setAvailableCountriesMain(availableCountries)
                        }

                        listIndexData = indexData

                        historySerialAdapter.setAdapterData(indexData)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.listOfCountriesMainScreen.collect { countriesList ->
                    if (countriesList.isNotEmpty()) {
                        setAvailableFilters(countriesList)
                        statusFilterAdapter.performClick(viewModel.availableCountryAdapterPosition.value)
                    }
                }
            }
        }


        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.loopIcon.setBackgroundResource(franchiseModel.loopIcon)

                val drawable = AppCompatResources
                    .getDrawable(requireContext(), R.drawable.ic_info)
                    ?.mutate()

                drawable?.setTint(color)

                binding.infoIcon.setImageDrawable(drawable)
            }
        }

        collectLatestLifecycleFlow(viewModel.baseTagDataStateByCountry) { tagIndex ->
            when (tagIndex) {
                is SubmitResult.Loading -> {
                    if (listIndexData.isEmpty()) {
                        binding.progBar.visibility = View.VISIBLE
                    }
                }

                is SubmitResult.Success -> {
                    binding.progBar.visibility = View.GONE
                    viewModel.saveRoomTagDataFirstScreen(tagIndex.data)
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
                    showError(tagIndex.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(tagIndex.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                else -> {}
            }
        }


        collectLatestLifecycleFlow(viewModel.baseTagDataStateFirstScreen) { tagIndex ->
            when (tagIndex) {
                is SubmitResult.Loading -> {
                    if (listIndexData.isEmpty()) {
                        binding.progBar.visibility = View.VISIBLE
                    }
                }

                is SubmitResult.Success -> {

                    binding.loopIcon.isEnabled = true

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

                    if (countryList.isNotEmpty()) {
                        viewModel.saveAllowedCountries(countryList)
                    }
                    viewModel.saveRoomTagDataFirstScreen(tagIndex.data.first)
                }

                is SubmitResult.FailureNoConnection -> {
                    showNoInternetDialog()
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


        collectLatestLifecycleFlow(viewModel.complaintObjectionState) { serverResponse ->
            when (serverResponse) {

                is SubmitResult.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    binding.progBar.visibility = View.GONE
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
            viewModel.selectedCountry = when (countryList.reversed()[0]) {
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

            statusFilterAdapter = MyTollCountriesFirstScreenAdapter(
                onSelected = { selectedStatus ->
                    viewModel.setCountryAdapterPosition(statusFilterAdapter.getTabPosition())

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

                    viewModel.selectedCountry = selectedCountry

                    if (::historySerialAdapter.isInitialized) {
                        historySerialAdapter.clearData()
                        historySerialAdapter.setAdapterData(listIndexData)
                    }

                    if (viewModel.isNetAvailable()) {
                        if (listIndexData.isEmpty()) {
                            binding.progBar.visibility = View.VISIBLE
                        }
                        viewModel.getBaseDataAlternativeApiForCountriesOnMain()
                    }
                },
                onShowSpinner = { showSpinner ->
                    when (showSpinner) {
                        false -> {
                            binding.buttonProgBar.visibility = View.INVISIBLE
                        }

                        true -> {
                            binding.buttonProgBar.visibility = View.VISIBLE
                        }
                    }
                }
            )

            binding.cyclerTagTypes.adapter = statusFilterAdapter

            statusFilterAdapter.submitList(countryList.reversed()) {
                statusFilterAdapter.setTabPosition(0)
            }
        }
    }

    private fun runInternetConnectionCheck() {
        binding.loopIcon.isEnabled = false

        if (!viewModel.isNetAvailable()) {
            viewLifecycleOwner.lifecycleScope.launch {

                val bindingMain = (activity as MainActivity).binding

                MainActivity.showSnackMessage(
                    getString(R.string.checking_for_connection), bindingMain
                )

                showNoInternetDialog()

                binding.progBar.visibility = View.VISIBLE

                while (isActive) {
                    if (viewModel.internetAvailable()) {
                        binding.loopIcon.isEnabled = true
                        MainActivity.showSnackMessage(
                            getString(R.string.connection_restored),
                            bindingMain
                        )
                        viewModel.getBaseDataAlternativeApi()
                        break
                    } else {
                        delay(1000L)
                    }
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

    override fun sendComplaintData(complaintBody: ComplaintBody, dataValidation: DataValidation) {
        viewModel.postComplaint(complaintBody, dataValidation)
    }

    override fun sendObjectionData(objectionBody: ObjectionBody, dataValidation: DataValidation) {
        viewModel.postObjection(objectionBody, dataValidation)
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
        listIndexData = emptyList()
        viewModel.setAvailableCountriesMain(emptyList())
    }


}