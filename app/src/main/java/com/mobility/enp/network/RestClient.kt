package com.mobility.enp.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mobility.enp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class RestClient<T>(apiServiceClass: Class<T>, token: String?) {

    private val client = getUnsafeOkHttpClient(token).build()

    private val retrofit: Retrofit by lazy {

        val baseUrl = BuildConfig.API_URL

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)  // <!-- client gets added to our retrofit instance here
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

    val apiService: T = retrofit.create(apiServiceClass)

    companion object {
        fun <T> create(apiServiceClass: Class<T>, token: String?): RestClient<T> {
            return RestClient(apiServiceClass, token)
        }

        private fun getUnsafeOkHttpClient(token: String?): OkHttpClient.Builder {
            try {
                val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        override fun checkClientTrusted(
                            chain: Array<java.security.cert.X509Certificate>,
                            authType: String
                        ) {
                        }

                        override fun checkServerTrusted(
                            chain: Array<java.security.cert.X509Certificate>,
                            authType: String
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                            return arrayOf()
                        }
                    }
                )

                val sslContext: SSLContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())

                val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

                val builder = OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                    .hostnameVerifier { _, _ -> true }

                builder.addInterceptor {
                    val request: Request = it.request()

                    val requestBuilder = request.newBuilder()

                    requestBuilder
                        .addHeader("Cache-Control", "no-cache")
                        .addHeader("Connection", "keep-alive")
                        .addHeader("X-Platform", "Android")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept", "application/json")
                        .addHeader("X-Platform", "Android" )

                    if (!token.isNullOrEmpty()) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }

                    it.proceed(requestBuilder.build())
                }

                builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                builder.connectTimeout(30, TimeUnit.SECONDS)
                builder.readTimeout(30, TimeUnit.SECONDS)
                builder.writeTimeout(30, TimeUnit.SECONDS)

                return builder
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

}