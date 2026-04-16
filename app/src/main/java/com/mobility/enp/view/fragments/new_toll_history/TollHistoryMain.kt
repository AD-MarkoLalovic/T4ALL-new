package com.mobility.enp.view.fragments.new_toll_history

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
import com.mobility.enp.databinding.FragmentTollHistoryMainBinding
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.new_toll_history.AllowedCountryFilterAdapter
import com.mobility.enp.view.adapters.new_toll_history.TollHistoryPagingAdapter
import com.mobility.enp.viewmodel.toll_history.NewTollHistoryViewModel
import kotlinx.coroutines.launch

class TollHistoryMain : Fragment() {

    private var _binding: FragmentTollHistoryMainBinding? = null
    private val binding: FragmentTollHistoryMainBinding get() = _binding!!

    private val viewModel: NewTollHistoryViewModel by viewModels { NewTollHistoryViewModel.factory }

    private lateinit var pagingAdapter: TollHistoryPagingAdapter
    private lateinit var countryAdapter: AllowedCountryFilterAdapter

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
        setupAdapters()
        observePagingData()
        observeCountries()
        observeLoadStates()
        observeLogoutEvent()

        //viewModel.initialize("RS")
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
            val itemCount = pagingAdapter.itemCount
            val isInitialRefreshLoading =
                refreshState is LoadState.Loading && itemCount == 0

            binding.progressTollHistory.isVisible = isInitialRefreshLoading

            if (refreshState is LoadState.Error) {
                // TODO: prikaži error poruku korisniku
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}