package com.mobility.enp.view.fragments.intro

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import com.mobility.enp.databinding.FragmentIntroScreenAboutBinding

class IntroScreenAbout : Fragment() {

    private var _binding: FragmentIntroScreenAboutBinding? = null
    private val binding: FragmentIntroScreenAboutBinding get() = _binding!!

    private var isExpanded = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentIntroScreenAboutBinding.inflate(
                inflater,
                container,
                false
            )
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("IntroLanguage", Context.MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("selected_Language", "sr") // Podrazumevano "sr"

        when (savedLanguage) {
            "cyr" -> {
                binding.tvSelectedLanguage?.text = "СРП"
                binding.langTwo?.text = "SRP"
                binding.langThree?.text = "ENG"
            }
            "sr" -> {
                binding.tvSelectedLanguage?.text = "SRP"
                binding.langTwo?.text = "СРП"
                binding.langThree?.text = "ENG"
            }
            "en" -> {
                binding.tvSelectedLanguage?.text = "ENG"
                binding.langTwo?.text = "SRP"
                binding.langThree?.text = "СРП"
            }
        }


        val languageOptions: LinearLayout? = binding.languageOptions

        binding.tvSelectedLanguage?.setOnClickListener {
            toggleDropdown(languageOptions!!)
        }

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
    }

    private fun setLanguage(languageCode: String) {
        val sharedPreferences = requireContext().getSharedPreferences("IntroLanguage", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("selected_Language", languageCode) // Spremanje izabranog jezika
            apply()
        }

        val shared = requireContext().getSharedPreferences("AppLanguage", Context.MODE_PRIVATE)
        with(shared.edit()) {
            putString("user_language", languageCode) // Spremanje izabranog jezika
            apply()
        }
        activity?.recreate()
    }

    private fun toggleDropdown(view: LinearLayout) {
        if (isExpanded) {
            // Ako je trenutno otvoren, animiramo smanjenje visine na 0 i zatim ga sakrivamo
            animateHeight(view, view.height, 0) {
                view.visibility = View.GONE
            }
        } else {
            // Prvo omogućavamo vidljivost da bismo mogli da izmerimo visinu
            view.visibility = View.VISIBLE

            // Merimo njegovu visinu tako da odgovara sadržaju (wrap_content)
            view.measure(
                ViewGroup.LayoutParams.MATCH_PARENT, // Može biti i WRAP_CONTENT ako želiš širu kontrolu
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val targetHeight = view.measuredHeight // Dobijamo pravu visinu sadržaja

            // Sada animiramo otvaranje do stvarne visine
            animateHeight(view, 0, targetHeight)
        }
        isExpanded = !isExpanded
    }


    private fun animateHeight(view: View, startHeight: Int, endHeight: Int, onEnd: (() -> Unit)? = null) {
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