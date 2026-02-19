package com.mobility.enp.view.fragments.tool_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.databinding.FragmentToolHistorySearchResultBinding
import com.mobility.enp.view.adapters.tool_history.result.HistorySerialAdapterResult
import com.mobility.enp.view.dialogs.GeneralMessageDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.launch


class HistoryResultScreen : Fragment(), HistoryPassageAdapterResultScreen.SendToFragment,
    HistoryPassageAdapterCroatiaResultScreen.SendToFragment {

    private lateinit var binding: FragmentToolHistorySearchResultBinding
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: UserPassViewModel by activityViewModels { UserPassViewModel.Factory }
    private lateinit var historySerialAdapter: HistorySerialAdapterResult
    private var listIndexData: List<IndexData> = emptyList()

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
            viewModel.selectedTags.clear()
            findNavController().popBackStack()
        }

        setFranchise()
        setObserver()

    }

    private fun setObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tagFlow.collect { indexData ->
                    if (!indexData.isEmpty()) {
                        binding.progBar.visibility = View.GONE

                        val userSelectedTags = viewModel.getSelectedTagList()

                        if (userSelectedTags.isNotEmpty()) {
                            val list = indexData[0]
                            list.data?.tags = userSelectedTags
                            listIndexData = listOf(list)
                            historySerialAdapter.setAdapterData(listOf(list))
                        } else {
                            listIndexData = indexData
                            historySerialAdapter.setAdapterData(indexData)
                        }
                    }
                }
            }
        }
    }

    private fun setFranchise() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.btnReset.setTextColor(color)
            }
        }
    }


    override fun sendComplaintData(complaintBody: ComplaintBody) {
        viewModel.postComplaint(complaintBody)
    }

    override fun sendObjectionData(objectionBody: ObjectionBody) {
        viewModel.postObjection(objectionBody)
    }

    override fun croatiaReclamationDialog() {

        val fm = activity?.supportFragmentManager

        fm?.let { manager ->
            GeneralMessageDialog.newInstance(
                getString(R.string.complaint), getString(R.string.croatian_reclamation)
            ).show(manager, "croatiaDialog")
        }
    }

}