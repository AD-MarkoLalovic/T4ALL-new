package com.mobility.enp.view.fragments.tool_history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
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
import com.mobility.enp.databinding.FragmentToolHistorySearchResultBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.tool_history.result.HistoryPassageAdapterCroatiaResultScreen
import com.mobility.enp.view.adapters.tool_history.result.HistoryPassageAdapterResultScreen
import com.mobility.enp.view.adapters.tool_history.result.HistorySerialAdapterResultScreen
import com.mobility.enp.view.dialogs.GeneralMessageDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class HistoryResultScreen : Fragment(), HistoryPassageAdapterResultScreen.SendToFragment,
    HistorySerialAdapterResultScreen.PaginationUpdate,
    HistoryPassageAdapterCroatiaResultScreen.SendToFragment {

    private lateinit var binding: FragmentToolHistorySearchResultBinding
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val vModel: UserPassViewModel by activityViewModels { UserPassViewModel.Factory }

    companion object {
        const val TAG = "HistoryResult"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_tool_history_search_result,
            container,
            false
        )
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            vModel.selectedTags.clear()
            findNavController().popBackStack()
        }

        setObservers()
        setFranchise()

        vModel.getBaseDataAlternativeApiResultScreen()
    }

    private fun setFranchise() {
        //btnReset
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.btnReset.setTextColor(color)
            }
        }
    }

    private fun setIndexData(tagIndex: IndexData) {
        binding.progBar.visibility = View.GONE
        when (vModel.allTagsSelected) {
            true -> {  // uses unmodified tag index for adapter to list all possible tags and passages
                val historySerialAdapter =
                    HistorySerialAdapterResultScreen(tagIndex, vModel, this, this, this, this)
                binding.cycler.adapter = historySerialAdapter
                binding.cycler.layoutManager = LinearLayoutManager(context)

                Log.d(TAG, "setIndexData: umodified")
            }

            false -> {  // modifies tag response so that only the selected tags from user are searched and prevents pagination

                tagIndex.data?.tags = vModel.selectedTags
                tagIndex.data?.currentPage = 1
                tagIndex.data?.total = 1
                tagIndex.data?.perPage = 10
                tagIndex.data?.lastPage = 1

                val historySerialAdapter =
                    HistorySerialAdapterResultScreen(tagIndex, vModel, this, this, this, this)
                binding.cycler.adapter = historySerialAdapter
                binding.cycler.layoutManager = LinearLayoutManager(context)

                Log.d(TAG, "setIndexData: modified")
            }
        }
    }

    private fun setObservers() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vModel.indexDataResultScreen.collect { indexData ->
                    indexData?.let {
                        setIndexData(indexData)
                    }
                }
            }
        }

        collectLatestLifecycleFlow(vModel.baseTagDataStateResultScreen) { tagIndex ->
            when (tagIndex) {
                is SubmitResult.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    tagIndex.data.first?.let {
                        vModel.setIndexDataResultScreen(it)
                    }
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

                else -> {
                    SubmitResult.Empty
                }
            }
        }

        collectLatestLifecycleFlow(vModel.complaintObjectionStateFiltered) { serverResponse ->
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

        binding.btnReset.setOnClickListener {
            findNavController().navigate(HistoryResultScreenDirections.actionToolHistorySearchResultFragmentToToolHistoryFragment())
        }
    }

    override fun sendComplaintData(complaintBody: ComplaintBody) {
        binding.progBar.visibility = View.VISIBLE
        vModel.postComplaintFiltered(complaintBody)
    }

    override fun sendObjectionData(objectionBody: ObjectionBody) {
        binding.progBar.visibility = View.VISIBLE
        vModel.postObjectionFiltered(objectionBody)
    }

    override fun sendDataFill(
        nextPage: Int,
        flow: MutableStateFlow<SubmitResult<V2HistoryTagResponse?>>,
        tagSerialNumber: String
    ) {
        binding.progBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            vModel.getToolHistoryTransitResultFragment(flow, tagSerialNumber, nextPage)
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

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}