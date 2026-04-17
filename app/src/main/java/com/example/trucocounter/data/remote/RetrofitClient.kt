package com.example.trucocounter.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // PASO 1: Ir a https://mockapi.io y crear un proyecto
    // PASO 2: Crear el recurso "equipos" con campos: nombre (String), puntos (Number)
    // PASO 3: Reemplazar YOUR_PROJECT_ID con el ID que aparece en la URL de tu proyecto
    // Ejemplo: si tu URL es https://6612abc123.mockapi.io → BASE_URL = "https://6612abc123.mockapi.io/api/v1/"
    private const val BASE_URL = "https://YOUR_PROJECT_ID.mockapi.io/api/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
