package com.mobility.enp.view.fragments.registration_v2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentTosBinding
import com.mobility.enp.databinding.UserRegistrationLoginBinding

class RegistrationFragment : Fragment() {

    private var _binding: UserRegistrationLoginBinding? = null
    private val binding: UserRegistrationLoginBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = UserRegistrationLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedCountry = arguments?.getString("countryData") ?: "RS"

        val url = when (selectedCountry) {
            "RS" -> "https://toll4all.com/tag-registracija"
            "MK" -> "https://toll4all.com/mk/tag-registracija"
            "ME" -> "https://toll4all.com/mne/tag-registracija"
            else -> "https://toll4all.com/"
        }

        binding.progBar.visibility = View.VISIBLE
        binding.webView.settings.javaScriptEnabled = url.startsWith("https://toll4all.com/")

        binding.webView.settings.setGeolocationEnabled(true)

        // Allow content to use the viewport width
        binding.webView.settings.useWideViewPort = true
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.builtInZoomControls = true

        // Enable text input
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true
        binding.webView.settings.domStorageEnabled = true

        // Allow mixed content (https and http) if needed
        binding.webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        binding.webView.isFocusable = true
        binding.webView.isFocusableInTouchMode = true

        binding.webView.requestFocus(View.FOCUS_DOWN)

        val userAgent =
            "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) " + "AppleWebKit/537.36 (KHTML, like Gecko) " + "Chrome/90.0.4430.85 Mobile Safari/537.36"

        binding.webView.settings.userAgentString = userAgent
        binding.webView.addJavascriptInterface(
            WebAppInterface(),
            "AndroidInterface"   // android javascript interface gets added here
        )

        // Set up WebViewClient to handle page navigation within the WebView
        binding.webView.webViewClient = object :
            WebViewClient() {  // access the object override methods i see a lot of interesting methods here
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progBar.visibility = View.GONE


                //its used in AndroidInterface.method
                view?.loadUrl(
                    "javascript:(function() { " + "var elements = document.getElementsByClassName('back-icon-link');" + "if (elements.length > 0) {" + "   elements[0].addEventListener('click', function() { " + "       AndroidInterface.onBackIconClick(); " + "   });" + "}" + "})()"
                )
            }
        }

        // Set up WebChromeClient to handle JavaScript alerts, progress, etc.
        binding.webView.webChromeClient = WebChromeClient()

        binding.webView.loadUrl(url)
    }

    inner class WebAppInterface {

        // Function called from JavaScript
        @JavascriptInterface
        fun onBackIconClick() {  // it gets detected here
            // Notify fragment or perform any action
            binding.webView.post {  // fixes a navigation issue.
                findNavController().navigate(R.id.action_tosFragment_to_loginFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.apply {
            clearHistory()
            clearCache(true)
            loadUrl("about:blank")
            removeAllViews()
            destroy()
        }
        _binding = null
    }
}