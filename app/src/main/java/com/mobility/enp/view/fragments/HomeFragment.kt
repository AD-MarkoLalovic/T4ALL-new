package com.mobility.enp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentHomeWelcomeBinding
import com.mobility.enp.util.ImageRepository
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.view.adapters.TotalCurrencyAdapter
import com.mobility.enp.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeWelcomeBinding? = null
    private val binding: FragmentHomeWelcomeBinding get() = _binding!!
    private lateinit var totalCurrencyAdapter: TotalCurrencyAdapter

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    private val imageRepository: ImageRepository by lazy {
        ImageRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home_welcome, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.fetchHomeData()

        binding.rvTotalCurrency?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        totalCurrencyAdapter = TotalCurrencyAdapter(emptyList())
        binding.rvTotalCurrency?.adapter = totalCurrencyAdapter


        // Posmatramo LiveData i reagujemo na promene
        viewModel.homeData.observe(viewLifecycleOwner) { result ->
            when (result) {
                is SubmitResult.Loading -> binding.progBar.visibility = View.VISIBLE
                is SubmitResult.Success -> {
                    binding.progBar.visibility = View.GONE
                    val invoiceDetails = result.data.invoice.flatMap { it.invoiceDetails }
                    totalCurrencyAdapter.submitList(invoiceDetails)


                }

                is SubmitResult.Empty -> {
                    // Trenutno ne preduzimamo nikakve akcije za praznu vrednost
                }

                is SubmitResult.FailureNoConnection -> {}
                is SubmitResult.FailureServerError -> {
                    binding.progBar.visibility = View.GONE
                }

                is SubmitResult.FailureApiError -> {
                    binding.progBar.visibility = View.GONE
                    //showMessage(result.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {}
            }
        }

        binding.switchToPageBill?.setOnClickListener {
            findNavController().navigate(R.id.action_global_invoicesFragment)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}