package com.mobility.enp.view.fragments.new_toll_history

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentTollHistoryMainBinding
import com.mobility.enp.network.error.ApiMessageException
import com.mobility.enp.util.FragmentResultKeys
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.new_toll_history.AllowedCountryFilterAdapter
import com.mobility.enp.view.adapters.new_toll_history.TollHistoryPagingAdapter
import com.mobility.enp.view.dialogs.ChangePasswordDialog
import com.mobility.enp.viewmodel.toll_history.NewTollHistoryViewModel
import kotlinx.coroutines.launch

class TollHistoryMain : Fragment() {

    private var _binding: FragmentTollHistoryMainBinding? = null
    private val binding: FragmentTollHistoryMainBinding get() = _binding!!

    private val viewModel: NewTollHistoryViewModel by viewModels { NewTollHistoryViewModel.factory }

    private lateinit var pagingAdapter: TollHistoryPagingAdapter
    private lateinit var countryAdapter: AllowedCountryFilterAdapter

    private var lastShownRefreshError: Throwable? = null
    private var appendErrorSnackbar: Snackbar? = null

    private var shouldRetryOnNetworkBack = false
    private var hasSeenLoadingForCurrentFilter = false

    private lateinit var connectivityManager: ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTollHistoryMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        setupAdapters()
        observePagingData()
        observeCountries()
        observeLoadStates()
        observeLogoutEvent()

        //viewModel.initialize("RS")
    }

    override fun onStart() {
        super.onStart()
        registerNetworkCallback()
    }

    override fun onStop() {
        appendErrorSnackbar?.dismiss()
        unregisterNetworkCallback()
        super.onStop()
    }

    private fun setupAdapters() {
        pagingAdapter = TollHistoryPagingAdapter(
            onComplaintClick = { itemId ->
                //navigateToComplaint(itemId)
            },
            onObjectionClick = { complaintId, maxReached ->
                if (maxReached) {
                    //showMaxObjectionsDialog()
                } else {
                    //navigateToObjection(complaintId)
                }
            }
        )


        binding.recyclerPassage.adapter = pagingAdapter
        binding.recyclerPassage.layoutManager = LinearLayoutManager(requireContext())

        countryAdapter = AllowedCountryFilterAdapter { countryCode ->
            viewModel.onCountrySelected(countryCode)
        }
        binding.recyclerAllowedCountries.adapter = countryAdapter
        binding.recyclerAllowedCountries.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL, false
        )
    }

    private fun observePagingData() {
        collectLatestLifecycleFlow(viewModel.pagingFlow) { pagingData ->
            Log.d(
                "MARKO",
                "submitData: filter=${viewModel.currentFilter.value} pagingData=${pagingData.hashCode()}"
            )
            hasSeenLoadingForCurrentFilter = false
            pagingAdapter.submitData(pagingData)
        }
    }

    private fun observeCountries() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allowedCountriesUi.collect { countries ->
                    countryAdapter.submitList(countries)
                }
            }
        }
    }


    private fun observeLoadStates() {
        collectLatestLifecycleFlow(pagingAdapter.loadStateFlow) { loadStates ->
            val refreshState = loadStates.refresh
            val appendState = loadStates.append
            val itemCount = pagingAdapter.itemCount

            val isAnyRefreshLoading =
                refreshState is LoadState.Loading ||
                        loadStates.mediator?.refresh is LoadState.Loading

            if (isAnyRefreshLoading) {
                hasSeenLoadingForCurrentFilter = true
            }

            binding.progressTollHistory.isVisible =
                isAnyRefreshLoading && itemCount == 0

            val showTextNoPassage = hasSeenLoadingForCurrentFilter &&
                    !isAnyRefreshLoading &&
                    refreshState is LoadState.NotLoading &&
                    itemCount == 0

            binding.txNoPassage.isVisible = showTextNoPassage

            if (refreshState is LoadState.Error) {
                if (refreshState.error !== lastShownRefreshError) {
                    lastShownRefreshError = refreshState.error
                    handleRefreshError(refreshState.error, itemCount)
                }
            } else {
                lastShownRefreshError = null
            }

            if (appendState is LoadState.Error) {
                showAppendErrorSnackbar(appendState.error)
            } else {
                appendErrorSnackbar?.dismiss()
                appendErrorSnackbar = null
            }
        }
    }

    private fun showAppendErrorSnackbar(throwable: Throwable) {
        val msg = resolveUserMessage(throwable)
        if (appendErrorSnackbar?.isShown == true) return

        appendErrorSnackbar?.dismiss()
        appendErrorSnackbar = Snackbar.make(binding.root, msg, Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.action_retry)) {
                pagingAdapter.retry()
            }

        appendErrorSnackbar?.show()
    }

    private fun handleRefreshError(throwable: Throwable, data: Int) {
        when (throwable) {
            is java.net.UnknownHostException,
            is java.net.ConnectException,
            is java.net.SocketTimeoutException -> {
                shouldRetryOnNetworkBack = true
                binding.progressTollHistory.isVisible = data == 0
                showNoInternetDialog()
            }

            is ApiMessageException -> {
                Snackbar.make(binding.root, throwable.userMessage, Snackbar.LENGTH_LONG).show()
            }

            else -> {
                val msg = throwable.message ?: getString(R.string.server_error_msg)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun resolveUserMessage(throwable: Throwable): String {
        return when (throwable) {
            is java.net.UnknownHostException,
            is java.net.ConnectException,
            is java.net.SocketTimeoutException -> getString(R.string.no_internet)

            is ApiMessageException -> throwable.userMessage
            else -> throwable.message ?: getString(R.string.server_error_msg)
        }
    }

    private fun observeLogoutEvent() {
        collectLatestLifecycleFlow(viewModel.logoutEvent) { _ ->
            MainActivity.logoutOnInvalidToken(
                requireContext(),
                findNavController()
            )
        }
    }

    private fun showNoInternetDialog() {
        ChangePasswordDialog.newInstance(
            title = getString(R.string.no_connection_title),
            subtitle = getString(R.string.please_connect_to_the_internet),
            resultKey = FragmentResultKeys.NO_INTERNET_RESULT,
            resultValueKey = FragmentResultKeys.NO_INTERNET_CONFIRMED
        ).show(childFragmentManager, "TollHistoryMainNoInternet")
    }

    private fun registerNetworkCallback() {
        if (networkCallback != null) return

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                activity?.runOnUiThread {
                    onInternetAvailable()
                }
            }

        }

        connectivityManager.registerNetworkCallback(request, networkCallback!!)
    }

    private fun onInternetAvailable() {
        if (!shouldRetryOnNetworkBack) return
        if (!isAdded || _binding == null) return

        shouldRetryOnNetworkBack = false
        //dismissNoInternetDialogIfShown()
        pagingAdapter.retry()
    }

    private fun unregisterNetworkCallback() {
        networkCallback?.let {
            runCatching {
                connectivityManager.unregisterNetworkCallback(it)
            }
        }
        networkCallback = null
    }


    override fun onDestroyView() {
        super.onDestroyView()
        appendErrorSnackbar?.dismiss()
        _binding = null
        appendErrorSnackbar = null
    }
}