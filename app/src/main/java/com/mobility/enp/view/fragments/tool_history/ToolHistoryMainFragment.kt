package com.mobility.enp.view.fragments.tool_history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
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
import com.mobility.enp.view.adapters.tool_history.main_and_filter_screen.ToolHistoryListingAdapter
import com.mobility.enp.view.adapters.tool_history.main_and_filter_screen.ToolHistoryListingPassageAdapter
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ToolHistoryMainFragment : Fragment(), ToolHistoryListingPassageAdapter.SendToFragment,
    ToolHistoryListingAdapter.SavePassageData, ToolHistoryListingAdapter.PaginationUpdate {

    private var _binding: FragmentPassageHistoryBinding? = null
    private val binding: FragmentPassageHistoryBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val vModel: UserPassViewModel by activityViewModels { UserPassViewModel.Factory }

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

        binding.loopIcon.setOnClickListener {
            if (Util.isNetworkAvailable(requireContext())) {
                findNavController().navigate(ToolHistoryMainFragmentDirections.actionToolHistoryFragmentToToolHistorySearchFragment())
            } else {
                Toast.makeText(
                    context, context?.getString(R.string.no_internet), Toast.LENGTH_SHORT
                ).show()
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

        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.loopIcon.setBackgroundResource(franchiseModel.loopIcon)
            }
        }

        collectLatestLifecycleFlow(vModel.baseTagDataState) { tagIndex ->
            when (tagIndex) {
                is SubmitResult.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    setIndexData(tagIndex.data.first)
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

                else -> {
                    SubmitResult.Empty
                }
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

                else -> {
                    SubmitResult.Empty
                }
            }
        }
    }

    private fun runSavedDataCheck() {
        binding.loopIcon.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            val indexData = vModel.fetchIndexData()   // room

            indexData?.let { data ->

                val bindingMain = (activity as MainActivity).binding

                MainActivity.showSnackMessage(
                    getString(R.string.offline_using_stored_data), bindingMain
                )

                vModel.setStateIndex(data)

            } ?: run {
                val bundle = Bundle().apply {
                    putString(
                        getString(R.string.title), getString(R.string.no_connection_title)
                    )
                    putString(
                        getString(R.string.subtitle),
                        getString(R.string.please_connect_to_the_internet)
                    )
                }

                findNavController().navigate(
                    R.id.action_global_noInternetConnectionDialog, bundle
                )

                val bindingMain = (activity as MainActivity).binding
                MainActivity.showSnackMessage(
                    getString(R.string.checking_for_connection), bindingMain
                )
                binding.progBar.visibility = View.VISIBLE
            }

            while (true) {
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

        CoroutineScope(Dispatchers.IO).launch {
            vModel.insertRoomToolHistoryIndexData(indexData)
        }

        vModel.indexData =
            indexData  // filter fragment need some data from here saving here to reduce api calls

        val toolHistoryListingAdapter =
            ToolHistoryListingAdapter(indexData, vModel, this, this, this, this)

        binding.cycler.adapter = toolHistoryListingAdapter
        binding.cycler.layoutManager = LinearLayoutManager(requireContext())

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