package com.mobility.enp.view.fragments.my_profile

import android.graphics.Bitmap
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
import androidx.navigation.fragment.navArgs
import com.mobility.enp.databinding.FragmentTagOrderWebBinding

class TagOrderWebFragment : Fragment() {

    private var _binding: FragmentTagOrderWebBinding? = null
    private val binding: FragmentTagOrderWebBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTagOrderWebBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: TagOrderWebFragmentArgs by navArgs()

        binding.webView.addJavascriptInterface(
            WebAppInterface(),
            "AndroidInterface"
        )

        initializeWebViewSettings(args.url)


    }

    private fun initializeWebViewSettings(url: String) {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            setGeolocationEnabled(false)
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            javaScriptCanOpenWindowsAutomatically = false
            domStorageEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        binding.webView.isFocusable = true
        binding.webView.isFocusableInTouchMode = true
        binding.webView.webChromeClient = WebChromeClient()
        binding.webView.webViewClient = createWebViewClient()
        binding.webView.loadUrl(url)
    }

    private fun createWebViewClient() = object : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            _binding?.progBar?.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            _binding?.progBar?.visibility = View.GONE

            view?.evaluateJavascript(
                """
            (function() {
                var backBtn = document.querySelector('.back-icon-link');
                if (backBtn) {
                    backBtn.addEventListener('click', function(e) {
                        e.preventDefault();
                        e.stopPropagation();
                        AndroidInterface.onBackClicked();
                    });
                }
                
                var links = document.querySelectorAll('a.btn.btn-primary');
        links.forEach(function(link) {
            if (link.href.indexOf('toll4all.com') !== -1) {
                link.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    AndroidInterface.onBackClicked();
                });
            }
        });                
            })()
        """, null
            )
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun onBackClicked() {
            _binding?.webView?.post {
                if (isAdded) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.apply {
            stopLoading()
            clearHistory()
            loadUrl("about:blank")
            (parent as? ViewGroup)?.removeView(this)
            destroy()
        }
        _binding = null
    }
}