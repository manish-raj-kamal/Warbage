package com.warbage.recycling.network

import com.warbage.recycling.BuildConfig
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://warbage.vercel.app/"
    private const val APP_KEY = "warbage-android-v1"

    /**
     * Stored demo user identifier if user is in demo mode (no Firebase session).
     */
    @Volatile
    var demoUserId: String? = null

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("X-App-Key", APP_KEY)
            .header("Accept", "application/json")

        // 1. Check Firebase Auth current user
        val currentUser = try {
            FirebaseAuth.getInstance().currentUser
        } catch (_: Exception) {
            null
        }

        if (currentUser != null) {
            try {
                val tokenResult = Tasks.await(currentUser.getIdToken(false), 5, TimeUnit.SECONDS)
                val idToken = tokenResult?.token
                if (!idToken.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $idToken")
                }
            } catch (e: Exception) {
                // If token fetch times out, continue with App-Key
                if (BuildConfig.DEBUG) {
                    android.util.Log.w("RetrofitClient", "Failed to retrieve Firebase ID token", e)
                }
            }
        } else if (!demoUserId.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer demo:$demoUserId")
        }

        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: WarbageApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WarbageApiService::class.java)
    }
}
