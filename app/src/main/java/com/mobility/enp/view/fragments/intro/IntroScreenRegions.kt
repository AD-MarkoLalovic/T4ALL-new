package com.mobility.enp.view.fragments.intro

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.mobility.enp.Config
import com.mobility.enp.databinding.FragmentIntroScreenRegionsBinding
import com.mobility.enp.interf.VpInterface

class IntroScreenRegions : Fragment() {

    private var _binding: FragmentIntroScreenRegionsBinding? = null
    private val binding: FragmentIntroScreenRegionsBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroScreenRegionsBinding.inflate(
            inflater,
            container,
            false
        )

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