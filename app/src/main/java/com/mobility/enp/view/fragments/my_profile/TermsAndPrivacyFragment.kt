package com.mobility.enp.view.fragments.my_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.mobility.enp.databinding.FragmentTermsAndPrivacyBinding

class TermsAndPrivacyFragment : Fragment() {

    private var _binding: FragmentTermsAndPrivacyBinding? = null
    private val binding: FragmentTermsAndPrivacyBinding get() = _binding!!
    private val args: TermsAndPrivacyFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTermsAndPrivacyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userInfo = args.userInfo

        initializeWebViewSettings()

        binding.webViewTermsAndPrivacy.loadUrl("https://toll4all.com/terms-and-privacy/$userInfo")
    }

    private fun initializeWebViewSettings() = with(binding) {
        webViewTermsAndPrivacy.settings.apply {
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setGeolocationEnabled(false)
            javaScriptCanOpenWindowsAutomatically = false
        }

        // WebChromeClient za upravljanje napretkom učitavanja
        webViewTermsAndPrivacy.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                // prikazujem progress bar dok se stranica učitava
                if (newProgress < 100) {
                    progressBarTerms.visibility = View.VISIBLE
                } else {
                    progressBarTerms.visibility = View.GONE
                }
            }
        }

        // WebViewClient za upravljanje učitavanjem stranica
        webViewTermsAndPrivacy.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                // krijem progress bar kada se stranica potpuno učita
                progressBarTerms.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webViewTermsAndPrivacy.apply {
            clearHistory()
            clearCache(true)
            loadUrl("about:blank")
            removeAllViews()
            destroy()
        }
        _binding = null
    }


}