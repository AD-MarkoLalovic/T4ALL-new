package com.mobility.enp.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.Config
import com.mobility.enp.R
import com.mobility.enp.data.model.registration.CountryModel
import com.mobility.enp.databinding.FragmentLocationSelectBinding
import com.mobility.enp.view.adapters.SelectCountryAdapter
import com.mobility.enp.viewmodel.LoginViewModel

class LocationSelectFragment : Fragment() {

    private var _binding: FragmentLocationSelectBinding? = null
    private val binding: FragmentLocationSelectBinding get() = _binding!!
    private lateinit var adapter: SelectCountryAdapter
    private lateinit var adapterData: ArrayList<CountryModel>
    private lateinit var selectedCountry: CountryModel
    private val viewModel: LoginViewModel by activityViewModels()

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
        adapterData = arrayListOf<CountryModel>()
        context?.let {
            viewModel.initDatabase()
            adapterData.add(
                CountryModel(
                    getString(R.string.serbia_country_code),
                    getString(R.string.serbia),
                    AppCompatResources.getDrawable(it, R.drawable.serbia_flag)
                )
            )
            adapterData.add(
                CountryModel(
                    getString(R.string.macedonia_country_code),
                    getString(R.string.macedonia),
                    AppCompatResources.getDrawable(it, R.drawable.macedonia_flag)
                )
            )
            adapterData.add(
                CountryModel(
                    getString(R.string.montenegro_country_code),
                    getString(R.string.montenegro),
                    AppCompatResources.getDrawable(it, R.drawable.montenegro_flag)
                )
            )

            adapter = SelectCountryAdapter(adapterData)
            adapter.setInter(object : SelectCountryAdapter.SelectedCountry {
                override fun pickedCountry(county: CountryModel) {
                    selectedCountry = county
                    Log.d(Config.TAGREG, "selected : $selectedCountry")
                }
            })
            binding.cycler.adapter = adapter
            binding.cycler.layoutManager = LinearLayoutManager(context)
            selectedCountry = adapterData[0] // initially set to 0
            Log.d(Config.TAGREG, "selected : $selectedCountry")
        }

        binding.button.setOnClickListener {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}