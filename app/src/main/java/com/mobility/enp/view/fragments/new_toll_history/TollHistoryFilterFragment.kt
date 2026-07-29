package com.mobility.enp.view.fragments.new_toll_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mobility.enp.databinding.FragmentTollHistoryFilterBinding
import com.mobility.enp.viewmodel.toll_history.TollHistoryFilterViewModel

class TollHistoryFilterFragment : Fragment() {

    private var _binding: FragmentTollHistoryFilterBinding? = null
    private val binding: FragmentTollHistoryFilterBinding get() = _binding!!

    private val viewModel: TollHistoryFilterViewModel by viewModels { TollHistoryFilterViewModel.factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTollHistoryFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}