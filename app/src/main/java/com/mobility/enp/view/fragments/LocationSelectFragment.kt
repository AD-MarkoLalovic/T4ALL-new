package com.mobility.enp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mobility.enp.databinding.FragmentLocationSelectBinding
import com.mobility.enp.view.adapters.SelectCountryAdapter
import com.mobility.enp.viewmodel.LocationSelectViewModel

class LocationSelectFragment : Fragment() {

    private var _binding: FragmentLocationSelectBinding? = null
    private val binding: FragmentLocationSelectBinding get() = _binding!!
    private val viewModel: LocationSelectViewModel by viewModels { LocationSelectViewModel.Factory }
    private lateinit var adapter: SelectCountryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentLocationSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        setupClicks()

    }

    private fun setupRecycler() {
        adapter = SelectCountryAdapter(
            items = viewModel.countries,
            selectedCode = viewModel.selectCode.value,
            onClick = ::onCountryClicked
        )
        binding.rvListCountries.adapter = adapter
    }

    private fun setupClicks() {
        binding.button.setOnClickListener { navigateNext() }
        binding.backArrow.setOnClickListener { navigateBack() }
    }

    private fun onCountryClicked(code: String) {
        viewModel.onCountrySelected(code)
        adapter.setSelectedCode(code)
    }

    private fun navigateNext() {
        val code = viewModel.selectCode.value
        findNavController().navigate(
            LocationSelectFragmentDirections
                .actionLocationSelectFragmentToTosFragment(code)
        )
    }

    private fun navigateBack() {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}