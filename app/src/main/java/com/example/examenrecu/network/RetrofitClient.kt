package com.example.examenrecu.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // ⚠️ IMPORTANTE: Cambia esta URL según tu caso

    // OPCIÓN 1: Si usas EMULADOR de Android Studio
    private const val BASE_URL = "http://10.0.16.38:8000/api/"

    // OPCIÓN 2: Si usas DISPOSITIVO FÍSICO (celular/tablet)
    // Primero obtén tu IP local con: ipconfig (Windows) o ifconfig (Mac/Linux)
    // Luego reemplaza con tu IP, ejemplo:
    // private const val BASE_URL = "http://192.168.1.100:8000/api/"

    // OPCIÓN 3: Si Laravel está en un servidor externo
    // private const val BASE_URL = "https://tudominio.com/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}