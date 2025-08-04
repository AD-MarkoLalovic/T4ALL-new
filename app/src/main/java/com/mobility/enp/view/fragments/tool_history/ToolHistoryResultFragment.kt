package com.mobility.enp.view.fragments.tool_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponse
import com.mobility.enp.databinding.FragmentToolHistorySearchResultBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.tool_history.main_and_filter_screen.ToolHistoryListingAdapter
import com.mobility.enp.view.adapters.tool_history.main_and_filter_screen.ToolHistoryListingPassageAdapter
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ToolHistoryResultFragment : Fragment(), ToolHistoryListingPassageAdapter.SendToFragment,
    ToolHistoryListingAdapter.SavePassageData, ToolHistoryListingAdapter.PaginationUpdate {

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

        setObservers()
        setAdapter()
        setFranchise()
    }

    private fun setFranchise() {
        //btnReset
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.btnReset.setTextColor(color)
            }
        }
    }

    private fun setAdapter() {
        val listOfTags: List<Tag> = if (vModel.allTagsSelected) {
            vModel.tagSerials
        } else {
            vModel.selectedTags
        }

        val indexData = vModel.indexData
        indexData?.data?.tags = listOfTags  // sets selected tags to object for reuse

        indexData?.let { data ->
            val toolHistoryListingAdapter =
                ToolHistoryListingAdapter(data, vModel, this, this, this,this)
            binding.cycler.adapter = toolHistoryListingAdapter
            binding.cycler.layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setObservers() {
        collectLatestLifecycleFlow(vModel.complaintObjectionStateFiltered) { serverResponse ->
            when (serverResponse) {
                is SubmitResult.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    binding.progBar.visibility = View.GONE
                    setAdapter()
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
            findNavController().navigate(ToolHistoryResultFragmentDirections.actionToolHistorySearchResultFragmentToToolHistoryFragment())
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


    override fun psgData(toolHistoryListing: V2HistoryTagResponse) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                vModel.insertPassageData(toolHistoryListing)
            }
        }
    }

    override fun sendDataFill(
        nextPage: Int,
        flow: MutableStateFlow<SubmitResult<V2HistoryTagResponse>>,
        tagSerialNumber: String
    ) {
        binding.progBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            vModel.getToolHistoryTransitResultFragment(flow, tagSerialNumber, nextPage)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun stopSpinner() {
        binding.progBar.visibility = View.GONE
    }

    private fun showNoConnectionState() {
        binding.progBar.visibility = View.GONE
        noInternetMessage()
    }

    private fun noInternetMessage() {
        val mainBinding = (activity as MainActivity).binding
        MainActivity.showSnackMessage(getString(R.string.no_internet), mainBinding)
    }

    override fun sendDataFillMainAdapter(   // updates tags on main adapter
        nextPage: Int,
        perPage: Int,
        flow: MutableStateFlow<SubmitResult<IndexData>>,
    ) {
        binding.progBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            vModel.getBaseTagDataPagination(nextPage,perPage,flow)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}