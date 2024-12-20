package com.mobility.enp.view.fragments.tool_history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_tool_history.ToolHistoryListing
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.databinding.FragmentToolHistorySearchResultBinding
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.tool_history.result.HistoryContentPagingAdapter
import com.mobility.enp.view.adapters.tool_history.result.HistoryResultAdapter
import com.mobility.enp.viewmodel.PassageHistoryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ToolHistoryResultFragment : Fragment(), HistoryContentPagingAdapter.SendToFragment {

    private lateinit var binding: FragmentToolHistorySearchResultBinding
    private val viewModel: PassageHistoryViewModel by activityViewModels()
    private var errorBody: MutableLiveData<ErrorBody> = MutableLiveData()

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
        Log.d(TAG, "onViewCreated: ${viewModel.startDate.value} ${viewModel.endDate.value} ")

        setObservers()
        setAdapter()

        binding.btnReset.setOnClickListener {
            findNavController().navigate(ToolHistoryResultFragmentDirections.actionToolHistorySearchResultFragmentToToolHistoryFragment())
        }
    }

    private fun setAdapter() {

        val listOfTags: List<Tag> = if (viewModel.allTagsSelected) {
            viewModel.tagSerials
        } else {
            viewModel.selectedTags
        }

        val adapter =
            HistoryResultAdapter(listOfTags, viewModel, this, this, viewModel.getCountryCode())
        binding.cycler.adapter = adapter
        binding.cycler.layoutManager = LinearLayoutManager(context)
    }

    private fun setObservers() {
        errorBody = MutableLiveData()
        errorBody.observe(viewLifecycleOwner) { errorBody ->
            binding.progBar.visibility = View.GONE
            context?.let { context ->
                Toast.makeText(
                    context,
                    errorBody.errorBody,
                    Toast.LENGTH_SHORT
                ).show()
                if (errorBody.errorCode == 405 || errorBody.errorCode == 401) {
                    MainActivity.logoutOnInvalidToken(context, findNavController())
                }
            }
        }
        viewModel.complaintResponseFiltered.observe(viewLifecycleOwner) {
            it?.let {
                setAdapter()
                binding.progBar.visibility = View.GONE
            }
        }

    }

    override fun sendComplaintData(complaintBody: ComplaintBody) {
        binding.progBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.postComplaintFiltered(complaintBody, errorBody)
        }
    }

    override fun sendObjectionData(objectionBody: ObjectionBody) {
        binding.progBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.postObjectionFiltered(objectionBody, errorBody)
        }
    }

    override fun sendDataFill(
        nextPage: Int,
        dataFill: MutableLiveData<ToolHistoryListing>,
        tagSerialNumber: String
    ) {
        binding.progBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.getToolHistoryListingMutableTimeFiltered(
                dataFill,
                errorBody,
                tagSerialNumber,
                nextPage
            )
        }
    }

    override fun stopSpinner() {
        binding.progBar.visibility = View.GONE
    }
}