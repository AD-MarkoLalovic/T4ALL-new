package com.mobility.enp.view.fragments.card

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mobility.enp.BuildConfig
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentTosBinding
import com.mobility.enp.viewmodel.PaymentAndPassageViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CardFragment : Fragment() {

    private var _binding: FragmentTosBinding? = null
    private val binding: FragmentTosBinding get() = _binding!!
    private val viewModel: PaymentAndPassageViewModel by activityViewModels()

    private var countryCode: String? = null

    companion object {
        const val TAG = "Headers"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tos, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: CardFragmentArgs by navArgs()
        countryCode = args.countryCode ?: "RS"

        val baseUrl = when {
            BuildConfig.FLAVOR.contains("stage") -> {
                "https://admintest.toll4all.com/mweb/customers/add-card/"
            }

            BuildConfig.FLAVOR.contains("prod") -> {
                "https://openbalkan-etc.com/mweb/customers/add-card/"
            }

            else -> {
                Log.w("BuildType", "Unrecognized BUILD_TYPE: ${BuildConfig.BUILD_TYPE}")
                "about:blank"
            }
        }

        val countryUrls = mapOf(
            "MK" to "mk",
            "ME" to "me",
            "RS" to "rs"
        )

        val finalUrl = baseUrl + (countryUrls[countryCode] ?: "rs") // Ako nema u mapi, koristi "rs"

        initializeWebViewSettings(finalUrl)
        fetchAndLoadUrl(finalUrl)
    }

    private fun initializeWebViewSettings(url: String) {
        binding.webView.settings.apply {
            javaScriptEnabled = url.startsWith("https://admintest.toll4all.com") ||
                    url.startsWith("https://openbalkan-etc.com")

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
        binding.webView.isFocusable = true
        binding.webView.isFocusableInTouchMode = true
        binding.webView.webChromeClient = WebChromeClient()

        binding.webView.webViewClient = createWebViewClient()
    }

    private fun createWebViewClient() = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            _binding?.progBar?.visibility = View.GONE

            url?.let { link ->
                if (link.contains("/payment/success")) {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            R.string.credit_card_successful,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    countryCode?.let {
                        viewModel.addCard(it)
                    }

                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(2000L)

                        // Pre navigacije proveravamo da li je fragment još uvek aktivan
                        if (isAdded && isResumed) {
                            findNavController().popBackStack()
                        }
                    }
                }
            }

            Log.d(TAG, "url: $url")
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            _binding?.progBar?.visibility = View.VISIBLE
        }
    }

    // Ova metoda učitava URL u WebView sa dodanim Authorization header-om
    private fun fetchAndLoadUrl(url: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = fetchToken()
            val headers = mapOf("Authorization" to token)
            binding.webView.loadUrl(url, headers)
        }
    }

    // Ova funkcija vraća token koji je potreban za autentifikaciju
    private suspend fun fetchToken(): String {
        val tokenData = viewModel.getUserTokenCardWeb()
        return tokenData?.let { "${it.tokenType} ${it.accessToken}" } ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Zaustavljamo učitavanje u WebView pre nego što postavimo _binding na null
        binding.webView.stopLoading()
        _binding = null
    }
}
