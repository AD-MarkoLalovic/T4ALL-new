package com.mobility.enp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentLocationSelectBinding
import com.mobility.enp.view.adapters.SelectCountryAdapter
import com.mobility.enp.viewmodel.LocationSelectViewModel

class LocationSelectFragment : Fragment() {

    private var _binding: FragmentLocationSelectBinding? = null
    private val binding: FragmentLocationSelectBinding get() = _binding!!
    private lateinit var adapter: SelectCountryAdapter

    private val viewModel: LocationSelectViewModel by viewModels { LocationSelectViewModel.Factory }
    private var selectedCountry: String = "RS"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_location_select, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val countries = viewModel.fetchCountries(requireContext())
        adapter = SelectCountryAdapter(countries) { country ->
            selectedCountry = country
        }
        binding.rvListCountries.adapter = adapter

        binding.button.setOnClickListener {
            Log.d("MARKO", "onViewCreated: $selectedCountry")
            findNavController().navigate(
                LocationSelectFragmentDirections.actionLocationSelectFragmentToTosFragment(
                    selectedCountry
                )
            )
        }


        binding.backArrow.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    override fun onResume() {
        super.onResume()
        selectedCountry = "RS"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}