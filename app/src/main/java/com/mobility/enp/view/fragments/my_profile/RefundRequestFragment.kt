package com.mobility.enp.view.fragments.my_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentRefundRequestBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.refund_request_adapters.RefundRequestsCreatedAdapter
import com.mobility.enp.view.ui_models.refund_request.RefundRequestUIModel
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.RefundRequestViewModel

class RefundRequestFragment : Fragment() {

    private var _binding: FragmentRefundRequestBinding? = null
    private val binding: FragmentRefundRequestBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: RefundRequestViewModel by viewModels { RefundRequestViewModel.Factory }
    private lateinit var adapter: RefundRequestsCreatedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRefundRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.buttonRequest.setOnClickListener {
            findNavController().navigate(R.id.action_refundRequestFragment2_to_tagPickerRequestFragment)
        }

        if (!::adapter.isInitialized) {
            adapter = RefundRequestsCreatedAdapter(emptyList())
            binding.rvRefundRequest.adapter = adapter
        }
    }

    private fun observeViewModel() {
        collectLatestLifecycleFlow(viewModel.refundRequestUI) { result ->
            when (result) {
                is SubmitResult.Loading -> showLoadingState()
                is SubmitResult.Success -> showSuccessState(result.data)
                is SubmitResult.Empty -> showEmptyState()
                is SubmitResult.FailureNoConnection -> showNoConnectionState()
                is SubmitResult.FailureServerError -> showServerErrorState()
                is SubmitResult.FailureApiError -> showApiErrorState(result.errorMessage)
                else -> {} // todo add case for invalid server token
            }
        }
    }

    private fun showLoadingState() {
        binding.apply {
            refundLoadingView.visibility = View.VISIBLE
            txNoRequirements.visibility = View.GONE
            buttonRequest.visibility = View.GONE
        }
    }

    private fun showSuccessState(data: List<RefundRequestUIModel>) {
        adapter = RefundRequestsCreatedAdapter(data)
        binding.rvRefundRequest.adapter = adapter

        binding.apply {
            refundLoadingView.visibility = View.GONE
            txNoRequirements.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
            buttonRequest.visibility = View.VISIBLE
        }
    }

    private fun showEmptyState() {
        binding.apply {
            txNoRequirements.visibility = View.VISIBLE
            refundLoadingView.visibility = View.GONE
            buttonRequest.visibility = View.VISIBLE
        }
    }

    private fun showNoConnectionState() {
        binding.apply {
            refundLoadingView.visibility = View.GONE
            buttonRequest.visibility = View.VISIBLE
        }
        noInternetMessage()
    }

    private fun showServerErrorState() {
        binding.apply {
            txNoRequirements.visibility = View.VISIBLE
            refundLoadingView.visibility = View.GONE
            buttonRequest.visibility = View.VISIBLE
        }
        Toast.makeText(requireContext(), getString(R.string.server_error_msg), Toast.LENGTH_SHORT).show()
    }

    private fun showApiErrorState(message: String) {
        binding.apply {
            txNoRequirements.visibility = View.VISIBLE
            refundLoadingView.visibility = View.GONE
            buttonRequest.visibility = View.VISIBLE
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun noInternetMessage() {
        val mainBinding = (activity as MainActivity).binding
        MainActivity.showSnackMessage(getString(R.string.no_internet), mainBinding)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
