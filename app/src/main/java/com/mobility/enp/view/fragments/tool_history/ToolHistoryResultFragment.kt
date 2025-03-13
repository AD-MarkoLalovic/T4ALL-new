package com.mobility.enp.view.fragments.tool_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.data.model.api_tool_history.listing.ToolHistoryListing
import com.mobility.enp.databinding.FragmentToolHistorySearchResultBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.tool_history.result.HistoryContentPagingAdapter
import com.mobility.enp.view.adapters.tool_history.result.HistoryResultAdapter
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class ToolHistoryResultFragment : Fragment(), HistoryContentPagingAdapter.SendToFragment {

    private lateinit var binding: FragmentToolHistorySearchResultBinding
    private var errorBody: MutableLiveData<ErrorBody> = MutableLiveData()
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

        binding.btnReset.setOnClickListener {
            findNavController().navigate(ToolHistoryResultFragmentDirections.actionToolHistorySearchResultFragmentToToolHistoryFragment())
        }
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

        binding.cycler.adapter =
            HistoryResultAdapter(listOfTags, vModel, this, this, vModel.getCountryCode())
        binding.cycler.layoutManager = LinearLayoutManager(context)
    }

    private fun setObservers() {
        errorBody = MutableLiveData()
        errorBody.observe(viewLifecycleOwner) { errorBody ->
            binding.progBar.visibility = View.GONE
            Toast.makeText(
                context,
                errorBody.errorBody,
                Toast.LENGTH_SHORT
            ).show()
            if (errorBody.errorCode == 405 || errorBody.errorCode == 401) {
                MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
            }
        }
        vModel.complaintResponseFiltered.observe(viewLifecycleOwner) { lostTag ->
            lostTag?.let {
                setAdapter()
                binding.progBar.visibility = View.GONE
            }
        }

    }

    override fun sendComplaintData(complaintBody: ComplaintBody) {
        binding.progBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            vModel.postComplaintFiltered(complaintBody, errorBody)
        }
    }

    override fun sendObjectionData(objectionBody: ObjectionBody) {
        binding.progBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            vModel.postObjectionFiltered(objectionBody, errorBody)
        }
    }

    override fun sendDataFill(
        nextPage: Int,
        flow: MutableStateFlow<SubmitResult<ToolHistoryListing>>,
        tagSerialNumber: String
    ) {
        binding.progBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            vModel.getToolHistoryTransitResultPagination(
                flow,
                tagSerialNumber,
                nextPage
            )
        }
    }

    override fun invalidToken(errorMessage: String, errorCode: Int) {
        showMessage(errorMessage ?: "")
        MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun stopSpinner() {
        binding.progBar.visibility = View.GONE
    }
}