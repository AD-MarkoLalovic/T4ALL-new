package com.mobility.enp.view.fragments.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.mobility.enp.Config
import com.mobility.enp.databinding.FragmentIntroScreenBinding
import com.mobility.enp.interf.VpInterface
import com.mobility.enp.util.IntroScreensRepository
import com.mobility.enp.view.adapters.ViewPager2Adapter
import kotlinx.coroutines.launch

class IntroScreenFragment : Fragment() {

    private lateinit var binding: FragmentIntroScreenBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ViewPager2Adapter

    companion object {
        const val Tag = "IntroPages"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentIntroScreenBinding.inflate(inflater, container, false)
        viewPager = binding.introPager

        context?.let {
            adapter = ViewPager2Adapter(it as FragmentActivity, object : VpInterface {
                override fun switchToPage(int: Int) {
                    binding.introPager.currentItem = int
                }

                override fun switchToLogin() {
                    findNavController().navigate(IntroScreenFragmentDirections.actionIntroScreenFragmentToLoginFragment())
                }

            })

            binding.introPager.adapter = adapter

            TabLayoutMediator(binding.intoTabLayout, viewPager) { _, _ ->
            }.attach()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        viewLifecycleOwner.lifecycleScope.launch {
            IntroScreensRepository(requireContext()).saveIntroPageShown(Config.introKey, true)
        }
    }

}