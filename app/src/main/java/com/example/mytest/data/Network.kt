/**
 * Назначение: Network.kt
 * Дата создания: 10.02.2026
 * Автор: Валерьева Татьяна
 * Описание: Модуль для работы с API через Retrofit. Настроен под REST API Supabase.
 */
package com.example.mytest.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Body

// 1. Добавляем DTO (объекты данных), которые приходят из интернета
data class ExerciseDto(
    val id: Int,
    val title: String,
    val description: String,
    val image_url: String?
)

data class AuthResponse(
    val access_token: String?,
    val token_type: String?,
    val user: Any?
)

interface ApiService {
    // Получение списка упражнений (Задание Модуль В, п. 3.c)
    @GET("rest/v1/exercises")
    suspend fun getExercises(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String
    ): List<ExerciseDto>

    // Регистрация/Вход (Задание Модуль В, п. 1.a)
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Header("apikey") apiKey: String,
        @Body body: Map<String, String>
    ): AuthResponse
}

object RetrofitInstance {
    // Этот URL тебе выдадут на конкурсе, пока стоит заглушка
    private const val BASE_URL = "https://your-project-id.supabase.co/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}