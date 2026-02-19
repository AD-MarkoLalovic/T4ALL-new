package com.mobility.enp.view.fragments.tool_history

import android.os.Bundle
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
import com.mobility.enp.data.model.api_tool_history.v2base_model.DataValidation
import com.mobility.enp.databinding.FragmentToolHistorySearchResultBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.tool_history.result.HistoryPassageAdapterCroatiaResult
import com.mobility.enp.view.adapters.tool_history.result.HistoryPassageAdapterResult
import com.mobility.enp.view.adapters.tool_history.result.HistorySerialAdapterResult
import com.mobility.enp.view.dialogs.GeneralMessageDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.launch


class HistoryResultScreen : Fragment(), HistoryPassageAdapterResult.SendToFragment,
    HistoryPassageAdapterCroatiaResult.SendToFragment {

    private lateinit var binding: FragmentToolHistorySearchResultBinding
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: UserPassViewModel by activityViewModels { UserPassViewModel.Factory }
    private lateinit var historySerialAdapter: HistorySerialAdapterResult

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

        historySerialAdapter = HistorySerialAdapterResult(viewModel, this, this, this)

        binding.cycler.adapter = historySerialAdapter
        binding.cycler.layoutManager = LinearLayoutManager(requireContext())

        setFranchise()
        setObserver()
    }

    private fun setObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tagFlowResult.collect { indexData ->
                    if (!indexData.isEmpty()) {
                        binding.progBar.visibility = View.GONE

                        val userSelectedTags = viewModel.getSelectedTagList()

                        val uiList = indexData.map { item ->
                            if (userSelectedTags.isNotEmpty()) {
                                item.copy(
                                    data = item.data?.copy(
                                        tags = userSelectedTags.toList()
                                    )
                                )
                            } else item
                        }

                        if (userSelectedTags.isNotEmpty()) {
                            val list = indexData[0].copy()
                            list.data?.tags = userSelectedTags
                            historySerialAdapter.setAdapterData(uiList)
                        } else {
                            historySerialAdapter.setAdapterData(indexData)
                        }
                    }
                }
            }
        }

        collectLatestLifecycleFlow(viewModel.baseApiErrors) { data ->
            when (data) {
                is SubmitResult.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    binding.progBar.visibility = View.GONE
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
                    showError(data.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(data.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                else -> {}
            }
        }

        collectLatestLifecycleFlow(viewModel.complaintObjectionStateResult) { serverResponse ->
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

    private fun setFranchise() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.btnReset.setTextColor(color)
            }
        }
    }


    override fun sendComplaintData(complaintBody: ComplaintBody, dataValidation: DataValidation) {
        viewModel.postComplaintResult(complaintBody, dataValidation)
    }

    override fun sendObjectionData(objectionBody: ObjectionBody, dataValidation: DataValidation) {
        viewModel.postObjectionResult(objectionBody, dataValidation)
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

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showNoConnectionState() {
        binding.progBar.visibility = View.GONE
        noInternetMessage()
    }

    private fun noInternetMessage() {
        val mainBinding = (activity as MainActivity).binding
        MainActivity.showSnackMessage(getString(R.string.no_internet), mainBinding)
    }

}