package com.mobility.enp.view.fragments.intro

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentIntroScreenRegionsBinding
import com.mobility.enp.util.SharedPreferencesHelper

class IntroScreenRegions : Fragment() {

    private var _binding: FragmentIntroScreenRegionsBinding? = null
    private val binding: FragmentIntroScreenRegionsBinding get() = _binding!!

    private var isExpanded = false


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

        val savedLanguage = SharedPreferencesHelper.getSaveIntroSelectedLanguage(requireContext())

        when (savedLanguage) {
            "cyr" -> {
                binding.tvSelectedLanguage?.text = getString(R.string.srp_cyr)
                binding.langTwo?.text = getString(R.string.srp)
                binding.langThree?.text = getString(R.string.eng)
            }

            "sr" -> {
                binding.tvSelectedLanguage?.text = getString(R.string.srp)
                binding.langTwo?.text = getString(R.string.srp_cyr)
                binding.langThree?.text = getString(R.string.eng)
            }

            "en" -> {
                binding.tvSelectedLanguage?.text = getString(R.string.eng)
                binding.langTwo?.text = getString(R.string.srp)
                binding.langThree?.text = getString(R.string.srp_cyr)
            }
        }


        val languageOptions: LinearLayout? = binding.languageOptions

        binding.tvSelectedLanguage?.setOnClickListener {
            toggleDropdown(languageOptions!!)
        }

        binding.langTwo?.setOnClickListener {
            toggleDropdown(languageOptions!!)
            when (binding.langTwo?.text) {
                "SRP" -> setLanguage("sr")
                "СРП" -> setLanguage("cyr")
                "ENG" -> setLanguage("en")
            }
        }

        binding.langThree?.setOnClickListener {
            toggleDropdown(languageOptions!!)
            when (binding.langThree?.text) {
                "SRP" -> setLanguage("sr")
                "СРП" -> setLanguage("cyr")
                "ENG" -> setLanguage("en")
            }
        }

        binding.buttonNextRegions?.setOnClickListener {
            findNavController().navigate(R.id.action_introScreenRegions_to_introScreenAdvantages)
        }
    }

    private fun setLanguage(languageCode: String) {
        SharedPreferencesHelper.setSaveIntroSelectedLanguage(requireContext(), languageCode)
        SharedPreferencesHelper.setUserLanguage(requireContext(), languageCode)

        activity?.recreate()
    }

    private fun toggleDropdown(view: LinearLayout) {
        if (isExpanded) {
            animateHeight(view, view.height, 0) {
                view.visibility = View.GONE
            }
        } else {
            view.visibility = View.VISIBLE

            view.measure(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val targetHeight = view.measuredHeight

            animateHeight(view, 0, targetHeight)
        }
        isExpanded = !isExpanded
    }

    private fun animateHeight(
        view: View,
        startHeight: Int,
        endHeight: Int,
        onEnd: (() -> Unit)? = null
    ) {
        ValueAnimator.ofInt(startHeight, endHeight).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                view.layoutParams.height = it.animatedValue as Int
                view.requestLayout()
            }
            doOnEnd { onEnd?.invoke() }
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}