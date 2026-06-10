package com.mobility.enp.view.fragments.card

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mobility.enp.BuildConfig
import com.mobility.enp.databinding.FragmentAddCardRsWvBinding

class AddCardRsWebViewFragment : Fragment() {

    private var _binding: FragmentAddCardRsWvBinding? = null
    private val binding: FragmentAddCardRsWvBinding get() = _binding!!

    private val backUrl = "https://toll4all.com/customer/cards?tab_country=BA_RS"
    private val args: AddCardRsWebViewFragmentArgs by navArgs()

    /** Sprečava dupli navigateUp / fragment result za isti redirect. */
    private var backNavigationHandled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCardRsWvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
        submitArsForm()
    }

    private fun setupWebView() {
        binding.addCardRsWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        binding.addCardRsWebView.webViewClient = object : WebViewClient() {

            override fun onReceivedSslError(
                view: WebView,
                handler: android.webkit.SslErrorHandler,
                error: android.net.http.SslError
            ) {
                logE("SSL ERROR: ${error.primaryError} url=${error.url}")
                if (BuildConfig.DEBUG) {
                    handler.proceed()
                } else {
                    handler.cancel()
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                if (!request.isForMainFrame) return false
                return handleBackRedirectIfNeeded(request.url.toString())
            }

            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: Bitmap?
            ) {
                super.onPageStarted(view, url, favicon)
                _binding?.addCardRsProgress?.visibility = View.VISIBLE
            }


            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (!isBackUrl(url)) {
                    _binding?.addCardRsProgress?.visibility = View.GONE
                }
                // Backup: neki redirecti ne uđu u shouldOverrideUrlLoading
                handleBackRedirectIfNeeded(url)
            }
        }
    }

    /**
     * @return true ako smo preseli navigaciju (ne učitavaj toll4all web u WebView)
     */
    private fun handleBackRedirectIfNeeded(url: String): Boolean {
        if (backNavigationHandled || !isBackUrl(url)) {
            return false
        }

        backNavigationHandled = true

        val status = Uri.parse(url).getQueryParameter("status")
        val success = status.equals("SUCCESS", ignoreCase = true)

        logD("Back redirect: status=$status success=$success url=$url")

        parentFragmentManager.setFragmentResult(
            REQUEST_KEY_ARS_ADD_CARD,
            bundleOf(
                RESULT_SUCCESS to success,
                RESULT_STATUS to (status ?: "")
            )
        )

        binding.addCardRsWebView.stopLoading()
        findNavController().navigateUp()
        return true
    }

    private fun isBackUrl(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            uri.host.equals("toll4all.com", ignoreCase = true) &&
                    uri.path?.startsWith("/customer/cards") == true &&
                    uri.getQueryParameter("tab_country") == "BA_RS"
        } catch (_: Exception) {
            false
        }
    }

    private fun submitArsForm() {
        val postData = buildString {
            append("card_token=${Uri.encode(args.token)}")
            append("&etcw_customer_id=${args.etcwCustomerId}")
            append("&back_url=${Uri.encode(backUrl)}")
        }

        logD("postUrl -> ${args.redirectUrl} | data=$postData")

        binding.addCardRsWebView.postUrl(
            args.redirectUrl,
            postData.toByteArray(Charsets.UTF_8)
        )
    }

    override fun onDestroyView() {
        binding.addCardRsWebView.apply {
            stopLoading()
            webViewClient = WebViewClient()
            destroy()
        }
        _binding = null
        super.onDestroyView()
    }

    private fun logD(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    private fun logE(message: String) {
        if (BuildConfig.DEBUG) Log.e(TAG, message)
    }

    companion object {
        private const val TAG = "ARS_WEBVIEW"
        const val REQUEST_KEY_ARS_ADD_CARD = "ars_add_card_result"
        const val RESULT_SUCCESS = "success"
        const val RESULT_STATUS = "status"
    }
}