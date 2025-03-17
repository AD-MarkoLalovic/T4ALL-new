package com.mobility.enp.view.fragments.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentIntroScreenAboutBinding

class IntroScreenAbout : Fragment() {

    private var _binding: FragmentIntroScreenAboutBinding? = null
    private val binding: FragmentIntroScreenAboutBinding get() = _binding!!

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSpinner()

    }

    private fun setSpinner() {
        val spinner = binding.spinnerIntroAboutLang
        val language = resources.getStringArray(R.array.language_options_intro)

        val adapter = ArrayAdapter(requireContext(), R.layout.spiner_intro_lang_item, language)
        adapter.setDropDownViewResource(R.layout.spiner_intro_lang_dropdown_item)

        spinner?.adapter = adapter

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> Toast.makeText(requireContext(), "Izabran Srpski (Latinica)", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(requireContext(), "Izabran Srpski (Ćirilica)", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(requireContext(), "Izabran Engleski", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}