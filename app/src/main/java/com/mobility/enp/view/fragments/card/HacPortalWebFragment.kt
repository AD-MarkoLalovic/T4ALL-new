package com.mobility.enp.view.fragments.card

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mobility.enp.databinding.FragmentHacPortalWebBinding

class HacPortalWebFragment : Fragment() {

    private var _binding: FragmentHacPortalWebBinding? = null
    private val binding: FragmentHacPortalWebBinding get() = _binding!!

    private var url: String = "about:blank"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHacPortalWebBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: HacPortalWebFragmentArgs by navArgs()
        url = args.url

        initializeWebViewSettings(url)

        binding.webViewBack.setOnClickListener {
            if (binding.webViewHac.canGoBack()) {
                binding.webViewHac.goBack()
            } else {
                findNavController().popBackStack()
            }
        }

    }

    private fun initializeWebViewSettings(url: String) {
        binding.webViewHac.settings.apply {
            javaScriptEnabled = true

            setGeolocationEnabled(false)
            useWideViewPort = true
            loadWithOverviewMode = false
            builtInZoomControls = true
            javaScriptCanOpenWindowsAutomatically = false
            domStorageEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_NO_CACHE
            userAgentString =
                "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Mobile Safari/537.36"
        }
        binding.webViewHac.isFocusable = true
        binding.webViewHac.isFocusableInTouchMode = true
        binding.webViewHac.webChromeClient = WebChromeClient()

        binding.webViewHac.webViewClient = createWebViewClient()

        binding.webViewHac.loadUrl(url)
    }

    private fun createWebViewClient() = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            _binding?.progBarHac?.visibility = View.GONE
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            _binding?.progBarHac?.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webViewHac.stopLoading()
        _binding = null
    }
}