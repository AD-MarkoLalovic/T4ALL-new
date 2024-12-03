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
    private var navController: NavController? = null

    private lateinit var countDownTimerRegions: CountDownTimer  // for progBar
    private var vpInterface: VpInterface? = null

    fun setInterface(vpInterface: VpInterface) {
        this.vpInterface = vpInterface
    }

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

        binding.lifecycleOwner = viewLifecycleOwner
        navController = findNavController()

        countDownTimerRegions = object :
            CountDownTimer(Config.TOTAL_TIME_MILLIS.toLong(), Config.INTERVAL_MILLIS.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                val timeRemaining = millisUntilFinished / 1000
                Log.d(Config.TAG_INTRO, "onTick: $timeRemaining")
                val progress =
                    ((Config.TOTAL_TIME_MILLIS - millisUntilFinished) * 100 / Config.TOTAL_TIME_MILLIS).toInt()
                binding.progressBar.progress = progress
            }

            override fun onFinish() {
                try {
                    vpInterface?.switchToPage(2)
                } catch (e: UninitializedPropertyAccessException) {
                    Log.d(IntroScreenFragment.Tag, "exception: \n\n ${e.cause} \n ${e.message}")
                }
            }
        }

        binding.btnSkipPage.setOnClickListener {
            countDownTimerRegions.cancel()
            vpInterface?.switchToLogin()
        }
    }


    override fun onResume() {
        super.onResume()
        countDownTimerRegions.start()
        binding.progressBar.progress = 0
    }

    override fun onDestroyView() {
        countDownTimerRegions.cancel()
        super.onDestroyView()
        _binding = null
    }

}